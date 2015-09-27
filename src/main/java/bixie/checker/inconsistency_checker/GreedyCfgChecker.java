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

import bixie.checker.report.Report;
import bixie.checker.transition_relation.TransitionRelation;
import bixie.prover.Prover;
import bixie.prover.ProverExpr;
import bixie.prover.ProverResult;
import bixie.prover.princess.PrincessProver;
import boogie.controlflow.AbstractControlFlowFactory;
import boogie.controlflow.BasicBlock;
import boogie.controlflow.CfgProcedure;

/**
 * @author schaef
 * 
 *         Inconsistent code detection algorithm based on greedy Cfg covering.
 *         It uses the Cfg-theory plugin of Princess.
 * 
 *         The algorithm is described in the papers: - Infeasible code detection
 *         (VSTTE'12) - A theory for control-flow graph exploration (ATVA'13)
 *
 */
public class GreedyCfgChecker extends AbstractChecker {

	/**
	 * @param cff
	 * @param p
	 */
	public GreedyCfgChecker(AbstractControlFlowFactory cff, CfgProcedure p) {
		super(cff, p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bixie.checker.checker.AbstractChecker#checkSat(bixie.prover.Prover,
	 * boogie.controlflow.AbstractControlFlowFactory,
	 * boogie.controlflow.CfgProcedure)
	 */
	@Override
	public Report runAnalysis(Prover prover) {

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

		/*
		 * ===== main algorithm ==== Two steps: In the first step, push the
		 * assertion flag and check which blocks have feasible executions. Then
		 * pop the flag to disable all assertions and check what blocks can now
		 * be reached.
		 * 
		 * Step 1:
		 */
		prover.push();
		prover.addAssertion(prover.mkNot(tr.assertionFlag));
		HashSet<BasicBlock> coveredBlocks = new HashSet<BasicBlock>();
		coveredBlocks.addAll(coverBlocks(blocksToCover, tr, ineffFlags));

		// coverBlocks returns the set of all feasible blocks.
		this.feasibleBlocks = new HashSet<BasicBlock>(coveredBlocks);

		/*
		 * Step 2: Pop the tr.assertionFlag. An re-run coverBlocks to cover
		 * everything that has a feasible execution if assertions are ignored.
		 */
		prover.pop();
		coveredBlocks.addAll(coverBlocks(blocksToCover, tr, ineffFlags));

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

	/**
	 * Tries to cover elements in blocks by covering all blocks in the effectual
	 * set of the CFG using Princess' cfg-theory. The map ineffFlags contains
	 * one helper variable for each block in the effectual set.
	 * 
	 * @param blocks
	 *            Map from SMT variables to BasicBlocks.
	 * @param tr
	 *            Transition relation of the analyzed procedure.
	 * @param ineffFlags
	 *            Map from SMT variables in blocks to helper variables for
	 *            Princess.
	 * @return The set of blocks that could be covered.
	 */
	protected Set<BasicBlock> coverBlocks(Map<ProverExpr, BasicBlock> blocks,
			TransitionRelation tr,
			LinkedHashMap<ProverExpr, ProverExpr> ineffFlags) {

		Set<BasicBlock> coveredBlocks = new HashSet<BasicBlock>();

		int threshold = ineffFlags.size();
		// hint for the greedy cover algorithm about
		// how many blocks could be covered in one query.
		if (threshold > 1)
			threshold = threshold / 2;

		while (threshold >= 1 && !ineffFlags.isEmpty()) {
			prover.push();

			coveredBlocks.addAll(coverBlocksWithThreshold(blocks, tr,
					ineffFlags, threshold));

			prover.pop();

			if (threshold == 1 || ineffFlags.isEmpty())
				break;

			do {
				threshold = (int) Math.ceil((double) threshold / 2.0);
			} while (threshold > ineffFlags.size());

		}

		return coveredBlocks;
	}

	/**
	 * Sub-step of coverBlocks. Finds all paths that contain at least
	 * 'threshold' previously uncovered blocks. Setting the threshold is more
	 * efficient looking for arbitrary new paths.
	 * 
	 * @param blocks
	 *            Map from SMT variables to BasicBlocks.
	 * @param tr
	 *            Transition relation of the analyzed procedure.
	 * @param ineffFlags
	 *            Map from SMT variables in blocks to helper variables for
	 *            Princess.
	 * @param threshold
	 *            lower bound for the number of new blocks that have to be
	 *            covered per path.
	 * @param timeLimit
	 *            the time limit for the prover. If the limit is reached, the
	 *            analysis stops and returns the current set of covered blocks.
	 *            If timeLimit is 0, the solver is not timed out.
	 * @return The set of blocks that could be covered for the given threshold.
	 */
	protected Set<BasicBlock> coverBlocksWithThreshold(
			Map<ProverExpr, BasicBlock> blocks, TransitionRelation tr,
			LinkedHashMap<ProverExpr, ProverExpr> ineffFlags, int threshold) {

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

			res = prover.checkSat(true);

		}
		return coveredBlocks;
	}

}
