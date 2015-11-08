/**
 * 
 */
package bixie.checker.inconsistency_checker;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import bixie.Options;
import bixie.boogie.controlflow.AbstractControlFlowFactory;
import bixie.boogie.controlflow.BasicBlock;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.checker.report.Report;
import bixie.checker.transition_relation.TransitionRelation;
import bixie.prover.Prover;
import bixie.prover.ProverExpr;
import bixie.prover.ProverResult;
import bixie.prover.princess.PrincessProver;
import bixie.util.Log;

/**
 * @author schaef
 * An implementation that first uses the Greedy Checker until 
 * it gets stuck with a query and then uses the CdcChecker to cover
 * the rest.
 */
public class CombinedChecker extends AbstractChecker {

	private int checkTimeLimit;
	/**
	 * @param cff
	 * @param p
	 */
	public CombinedChecker(AbstractControlFlowFactory cff, CfgProcedure p) {
		super(cff, p);
	}

	@SuppressWarnings("serial")
	static class CheckerTimeout extends Exception {
		public final Set<BasicBlock> lastCover;
		public CheckerTimeout(Set<BasicBlock> cover) {
			this.lastCover = new HashSet<BasicBlock>(cover);
		}
	}
	
	
	
	@Override
	public Report runAnalysis(Prover prover) {

		this.checkTimeLimit = Options.v().getTimeout()/10;
		if (this.checkTimeLimit <1) this.checkTimeLimit =1;
		
		TransitionRelation tr = new TransitionRelation(this.procedure,
				this.cff, prover);

		// Statistics.HACK_effectualSetSize = tr.getEffectualSet().size();

		LinkedHashMap<ProverExpr, ProverExpr> ineffFlags = new LinkedHashMap<ProverExpr, ProverExpr>();

		for (BasicBlock block : tr.getEffectualSet()) {
			ProverExpr v = tr.getReachabilityVariables().get(block);
			ineffFlags.put(v, prover.mkVariable("" + v + "_flag",
					prover.getBooleanType()));
		}

		/*
		 * Assert the transition relation of the procedure.
		 */
		prover.push();
		this.pushTransitionRelation(prover, tr);
		prover.addAssertion(tr.getEnsures());

		// construct the inverted reachabilityVariables which is used later
		// to keep track of what has been covered so far.
		Map<ProverExpr, BasicBlock> blocksToCover = createdInvertedReachabilityVariableMap(
				tr, new HashSet<BasicBlock>(tr.getReachabilityVariables()
						.keySet()));

		prover.push();
		prover.addAssertion(prover.mkNot(tr.assertionFlag));
		HashSet<BasicBlock> coveredBlocks = new HashSet<BasicBlock>();
		try {
			coveredBlocks.addAll(coverBlocks(blocksToCover, tr, ineffFlags));
		} catch (CheckerTimeout cto) {
			coveredBlocks.addAll(cto.lastCover);
			Log.info("Greedy approach timeout in round 1 after covering "+coveredBlocks.size()+" blocks. Switching to backup solver.");
			prover.pop();
			prover.pop();
			CdcChecker alternativeChecker = new CdcChecker(cff, procedure);
			Report r = alternativeChecker.runAnalysisFromIntermediateResult(prover, tr, coveredBlocks, new HashSet<BasicBlock>());
			Log.info("backup solver finished successfully");
			return r;
		}


		this.feasibleBlocks = new HashSet<BasicBlock>(coveredBlocks);

		prover.pop();
		try {
			coveredBlocks.addAll(coverBlocks(blocksToCover, tr, ineffFlags));
		} catch (CheckerTimeout cto) {
			coveredBlocks.addAll(cto.lastCover);
			Log.info("Greedy approach timeout in round 2 after covering "+coveredBlocks.size()+" blocks. Switching to backup solver.");
			prover.pop();
			CdcChecker alternativeChecker = new CdcChecker(cff, procedure);
			Report r = alternativeChecker.runAnalysisFromIntermediateResult(prover, tr, coveredBlocks, this.feasibleBlocks);
			Log.info("backup solver finished successfully");
			return r;			
		}

		/* Pop the transition relation. */
		prover.pop();

		/*
		 * ===== End of the main algorithm ==== everything that was not covered
		 * in either of the iterations is clearly unreachable.
		 */
		HashSet<BasicBlock> unreachable = new HashSet<BasicBlock>(
				blocksToCover.values());

		/*
		 * All blocks that are covered in the second round - that is, the blocks
		 * that are in coveredBlocks but not in feasibleBlocks - are potentially
		 * dangerous, because their inconsistency contains an assertion.
		 */
		HashSet<BasicBlock> dangerous = new HashSet<BasicBlock>(coveredBlocks);
		dangerous.removeAll(this.feasibleBlocks);

		Report report = new Report(tr);
		report.reportInconsistentCode(0, dangerous);
		report.reportInconsistentCode(1, unreachable);

		return report;
	}
	
	protected Set<BasicBlock> coverBlocks(Map<ProverExpr, BasicBlock> blocks,
			TransitionRelation tr,
			LinkedHashMap<ProverExpr, ProverExpr> ineffFlags) throws CheckerTimeout {

		Set<BasicBlock> coveredBlocks = new HashSet<BasicBlock>();

		int threshold = ineffFlags.size();
		// hint for the greedy cover algorithm about
		// how many blocks could be covered in one query.
		if (threshold > 1)
			threshold = threshold / 2;

		while (threshold >= 1 && !ineffFlags.isEmpty()) {
			prover.push();

			coveredBlocks.addAll(coverBlocksWithThreshold(blocks, tr,
					ineffFlags, threshold, checkTimeLimit));

			prover.pop();

			if (threshold == 1 || ineffFlags.isEmpty())
				break;

			do {
				threshold = (int) Math.ceil((double) threshold / 2.0);
			} while (threshold > ineffFlags.size());
		}

		
		
		return coveredBlocks;
	}

	
	protected Set<BasicBlock> coverBlocksWithThreshold(
			Map<ProverExpr, BasicBlock> blocks, TransitionRelation tr,
			LinkedHashMap<ProverExpr, ProverExpr> ineffFlags, int threshold, int timeLimit) throws CheckerTimeout {

		// setup the CFG module
		LinkedList<ProverExpr> remainingBlockVars = new LinkedList<ProverExpr>();
		LinkedList<ProverExpr> remainingIneffFlags = new LinkedList<ProverExpr>();
		for (Entry<ProverExpr, ProverExpr> entry : ineffFlags.entrySet()) {
			remainingBlockVars.add(entry.getKey());
			remainingIneffFlags.add(entry.getValue());
		}

		((PrincessProver) prover).setupCFGPlugin(tr.getProverDAG(),
				remainingBlockVars, remainingIneffFlags, threshold);

		Set<BasicBlock> coveredBlocks = new HashSet<BasicBlock>();

		ProverResult res = prover.checkSat(true);

		while (res == ProverResult.Sat) {

			LinkedList<ProverExpr> trueInModel = new LinkedList<ProverExpr>();
			LinkedList<ProverExpr> flagsToAssert = new LinkedList<ProverExpr>();

			for (Entry<ProverExpr, BasicBlock> entry : blocks.entrySet()) {
				final ProverExpr pe = entry.getKey();
				if (prover.evaluate(pe).getBooleanLiteralValue()) {
					trueInModel.add(pe);
					ProverExpr flag = ineffFlags.get(pe);
					if (flag != null) {
						flagsToAssert.add(flag);
					}
					ineffFlags.remove(pe);
				}
			}

			for (ProverExpr e : trueInModel) {
				coveredBlocks.add(blocks.get(e));
				blocks.remove(e);
			}

			prover.addAssertion(prover.mkAnd(flagsToAssert
					.toArray(new ProverExpr[flagsToAssert.size()])));

			if (timeLimit>0) {
				 prover.checkSat(false);
				 res = prover.getResult(timeLimit);

				if (res == ProverResult.Running) {
					// the coverage algorithm could not make progress within the
					// given time limit. Falling back to the new algorithms.
					 prover.stop();
					 prover.pop();
					 throw new CheckerTimeout(coveredBlocks);
				}
			} else {
				res = prover.checkSat(true);	
			}
		}
		return coveredBlocks;
	}
	
}
