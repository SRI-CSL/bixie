/**
 * 
 */
package bixie.checker.inconsistency_checker;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import org.joogie.cfgPlugin.Util.Dag;

import ap.parser.IFormula;
import bixie.boogie.controlflow.AbstractControlFlowFactory;
import bixie.boogie.controlflow.BasicBlock;
import bixie.boogie.controlflow.CfgAxiom;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.boogie.controlflow.util.PartialBlockOrderNode;
import bixie.checker.report.Report;
import bixie.checker.transition_relation.TransitionRelation;
import bixie.prover.Prover;
import bixie.prover.ProverExpr;
import bixie.prover.ProverResult;
import bixie.prover.princess.PrincessProver;
import bixie.util.Log;

/**
 * @author schaef Inconsistent code detection algorithm that uses an abstract
 *         graph to find paths with a sat solver, and then an smt solver to
 *         check if the transition relation of the path is feasible. If not, it
 *         learns a conflict that can prune paths in the abstract graph.
 * 
 *         While this approach is less efficient on small procedures compared to
 *         the GreedyCfgChecker, it is orders of magnitude more efficient on
 *         large procedures.
 * 
 *         This algorithm is described in the paper: - Conflict-Directed Graph
 *         Coverage (NFM'15)
 */
public class CdcChecker extends AbstractChecker {

	TransitionRelation transitionRelation;
	LinkedHashSet<PartialBlockOrderNode> knownInfeasibleNodes = new LinkedHashSet<PartialBlockOrderNode>();
	// TODO: keep track of everything that has been proved infeasible
	// to make sure that we don't do the same work twice.
	Set<Set<BasicBlock>> infeasibleSubprograms = new LinkedHashSet<Set<BasicBlock>>();
	Set<Set<BasicBlock>> learnedConflicts = new LinkedHashSet<Set<BasicBlock>>();

	/**
	 * @param cff
	 * @param p
	 */
	public CdcChecker(AbstractControlFlowFactory cff, CfgProcedure p) {
		super(cff, p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * bixie.checker.infeasiblecode.AbstractInfeasibleCodeDetection#checkSat(org
	 * .gravy.prover.Prover,
	 * bixie.checker.verificationcondition.CfgTransitionRelation)
	 */
	@Override
	public Report runAnalysis(Prover prover) {
		TransitionRelation tr = new TransitionRelation(this.procedure, cff,
				prover);
		return runAnalysisFromIntermediateResult(prover, tr,
				new LinkedHashSet<BasicBlock>(), new LinkedHashSet<BasicBlock>());
	}

	public Report runAnalysisFromIntermediateResult(Prover prover,
			TransitionRelation tr, Set<BasicBlock> alreadyCovered,
			Set<BasicBlock> firstRoundResult) {
		this.prover = prover;
		this.transitionRelation = tr;
		/*
		 * before adding anything, push a frame on the prover stack so that we
		 * can clean up easily.
		 */
		prover.push();
		/* add the verification condition to the prover stack */
		for (Entry<CfgAxiom, ProverExpr> entry : tr.getPreludeAxioms()
				.entrySet()) {
			prover.addAssertion(entry.getValue());
		}
		prover.addAssertion(tr.getRequires());
		prover.addAssertion(tr.getEnsures());

		Set<BasicBlock> coveredBlocks = new LinkedHashSet<BasicBlock>();

		if (firstRoundResult.isEmpty()) {
			Log.debug("Round1 " + this.transitionRelation.getProcedureName());
			/*
			 * Cover all feasible path in the procedure while the assertion flag
			 * is set.
			 */
			prover.push();
			prover.addAssertion(prover
					.mkNot(this.transitionRelation.assertionFlag));

			coveredBlocks.addAll(computeJodCover(prover, tr, alreadyCovered));
			this.feasibleBlocks = new LinkedHashSet<BasicBlock>(coveredBlocks);
			/* pop the assertion flag. */
			prover.pop();
		} else {
			//not that alreadyCovered may contain more blocks than 
			//firstRoundResult
			coveredBlocks = new LinkedHashSet<BasicBlock>(alreadyCovered);
			this.feasibleBlocks = new LinkedHashSet<BasicBlock>(firstRoundResult);
		}

		Log.debug("Round2 " + this.transitionRelation.getProcedureName());

		// TODO: reset the known infeasible node...?
		knownInfeasibleNodes.clear();
		learnedConflicts.clear();
		infeasibleSubprograms.clear();

		/*
		 * Now cover all paths that are feasible if the assertion flag is not
		 * set.
		 */
		coveredBlocks.addAll(computeJodCover(prover, tr, coveredBlocks));

		/*
		 * algorithm is done, pop the verification condition from the prover
		 * stack.
		 */
		prover.pop();

		/* 'unreachable' is the set of all blocks minus the set coveredBlocks. */
		LinkedHashSet<BasicBlock> unreachable = new LinkedHashSet<BasicBlock>(tr
				.getReachabilityVariables().keySet());
		unreachable.removeAll(coveredBlocks);

		/*
		 * All blocks that are covered in the second round - that is, the blocks
		 * that are in coveredBlocks but not in feasibleBlocks - are potentially
		 * dangerous, because their inconsistency contains an assertion.
		 */
		LinkedHashSet<BasicBlock> dangerous = new LinkedHashSet<BasicBlock>(coveredBlocks);
		dangerous.removeAll(this.feasibleBlocks);



		Report report = new Report(tr);
		report.reportInconsistentCode(0, dangerous);
		report.reportInconsistentCode(1, unreachable);

		return report;
	}

	public Collection<BasicBlock> computeJodCover(Prover prover,
			TransitionRelation tr, Set<BasicBlock> alreadyCovered) {
		Set<BasicBlock> coveredBlocks = new LinkedHashSet<BasicBlock>(
				alreadyCovered);

		PartialBlockOrderNode poRoot = tr.getHasseDiagram().getRoot();
		coveredBlocks.addAll(findFeasibleBlocks2(prover, tr, poRoot,
				new LinkedHashSet<BasicBlock>(alreadyCovered)));

		return coveredBlocks;
	}

	/**
	 * Check subprogram
	 * 
	 * @param prover
	 * @param tr
	 * @param node
	 * @return
	 */
	private Set<BasicBlock> findFeasibleBlocks2(Prover prover,
			TransitionRelation tr, PartialBlockOrderNode node,
			Set<BasicBlock> alreadyCovered) {
		if (node.getSuccessors().size() > 0) {
			boolean allChildrenInfeasible = true;
			Set<BasicBlock> result = new LinkedHashSet<BasicBlock>();

			for (PartialBlockOrderNode child : node.getSuccessors()) {
				Set<BasicBlock> res = findFeasibleBlocks2(prover, tr, child,
						alreadyCovered);
				result.addAll(res);
				if (!res.isEmpty())
					allChildrenInfeasible = false;
				// check if we have proved this node to be infeasible
				if (knownInfeasibleNodes.contains(node))
					return new LinkedHashSet<BasicBlock>();
			}
			if (allChildrenInfeasible)
				knownInfeasibleNodes.add(node);
			return result;
		} else {
//			Log.debug("Step 3 A");
			LinkedHashSet<BasicBlock> result = new LinkedHashSet<BasicBlock>(alreadyCovered);
			if (alreadyCovered.containsAll(node.getElements()))
				return result;
			result.addAll(tryToFindConflictInPO(prover, tr, node, 0));
			return result;
		}
	}

	/**
	 * returns all nodes that occur on paths through b
	 * 
	 * @param b
	 * @return
	 */
	private Set<BasicBlock> getSubprogContaining(BasicBlock b) {
		Set<BasicBlock> knownInfeasibleBlocks = getKnownInfeasibleBlocks();

		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		LinkedHashSet<BasicBlock> done = new LinkedHashSet<BasicBlock>();

		todo.add(b);
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			done.add(current);
			for (BasicBlock x : current.getPredecessors()) {
				if (!todo.contains(x) && !done.contains(x)
						&& !knownInfeasibleBlocks.contains(x)) {
					todo.add(x);
				}
			}
		}
		// now the other direction
		todo.add(b);
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			done.add(current);
			for (BasicBlock x : current.getSuccessors()) {
				if (!todo.contains(x) && !done.contains(x)
						&& !knownInfeasibleBlocks.contains(x)) {
					todo.add(x);
				}
			}
		}
		return done;
	}

	// private Set<BasicBlock> getSubgraphContainingAll(Set<BasicBlock> nodes,
	// Set<BasicBlock> blocks) {
	// LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>(blocks);
	// LinkedHashSet<BasicBlock> done = new LinkedHashSet<BasicBlock>();
	// while (!todo.isEmpty()) {
	// BasicBlock current = todo.pop();
	// Set<BasicBlock> subprog = getSubgraphContaining(nodes, current);
	// done.addAll(subprog);
	// }
	// return done;
	// }

	private Set<BasicBlock> getKnownInfeasibleBlocks() {
		Set<BasicBlock> infeasibleBlocks = new LinkedHashSet<BasicBlock>();
		// for (PartialBlockOrderNode po : this.knownInfeasibleNodes) {
		// infeasibleBlocks.addAll(po.getElements());
		// }
		return infeasibleBlocks;
	}

	private ProverExpr mkDisjunction(TransitionRelation tr,
			Collection<BasicBlock> blocks) {
		ProverExpr next;
		if (blocks.size() == 0) {
			next = prover.mkLiteral(true);
		} else if (blocks.size() == 1) {
			next = tr.getReachabilityVariables().get(blocks.iterator().next());
		} else {
			ProverExpr[] disj = new ProverExpr[blocks.size()];
			int i = 0;
			for (BasicBlock n : blocks) {
				disj[i++] = tr.getReachabilityVariables().get(n);
			}
			next = prover.mkOr(disj);
		}
		return next;
	}

	/**
	 * Get a complete and feasible path from the model produced by princes.
	 * 
	 * @param prover
	 * @param tr
	 * @param necessaryNodes
	 *            one of these nodes needs to be in the path
	 * @return
	 */
	private LinkedHashSet<BasicBlock> getPathFromModel(Prover prover,
			TransitionRelation tr, Set<BasicBlock> allBlocks,
			Set<BasicBlock> necessaryNodes) {
		// Blocks selected by the model
		LinkedHashSet<BasicBlock> enabledBlocks = new LinkedHashSet<BasicBlock>();
		for (BasicBlock b : allBlocks) {
			final ProverExpr pe = tr.getReachabilityVariables().get(b);
			if (prover.evaluate(pe).getBooleanLiteralValue()) {
				enabledBlocks.add(b);
			}
		}

		for (BasicBlock block : necessaryNodes) {
			if (enabledBlocks.contains(block)) {
				// Get the path from block to the exit
				LinkedList<BasicBlock> blockToExit = new LinkedList<BasicBlock>();
				BasicBlock current = block;
				while (current != null) {
					blockToExit.add(current);
					BasicBlock _current = null;
					for (BasicBlock next : current.getSuccessors()) {
						if (enabledBlocks.contains(next)) {
							_current = next;
							break;
						}
					}
					current = _current;
				}

				if (blockToExit != null) {
					// Get the path from root to the block
					LinkedList<BasicBlock> rootToBlock = new LinkedList<BasicBlock>();
					current = block;
					while (current != null) {
						blockToExit.add(current);
						BasicBlock _current = null;
						for (BasicBlock next : current.getPredecessors()) {
							if (enabledBlocks.contains(next)) {
								_current = next;
								break;
							}
						}
						current = _current;
					}

					if (rootToBlock != null) {
						// We got a full path
						LinkedHashSet<BasicBlock> result = new LinkedHashSet<BasicBlock>();
						result.addAll(rootToBlock);
						result.addAll(blockToExit);
						return result;
					}
				}
			}
		}

		// Screwed
		// toDot("path_error.dot", new LinkedHashSet<BasicBlock>(allBlocks),
		// new LinkedHashSet<BasicBlock>(enabledBlocks),
		// new LinkedHashSet<BasicBlock>(necessaryNodes));
		throw new RuntimeException("Could not find a path");
	}

	/*
	 * private void makeColors(PartialBlockOrderNode node, int startColor, int
	 * endColor, HashMap<PartialBlockOrderNode, Integer> node2color) {
	 * 
	 * int range = (endColor - startColor) / 2; int midcolor = startColor +
	 * range;
	 * 
	 * node2color.put(node, midcolor);
	 * 
	 * int previous_color = startColor; int colordelta = (int) ((1.0) /
	 * ((double) node.getSuccessors().size()) * range);
	 * 
	 * for (PartialBlockOrderNode child : node.getSuccessors()) {
	 * makeColors(child, previous_color, previous_color + colordelta,
	 * node2color); previous_color += colordelta; }
	 * 
	 * }
	 * 
	 * public void toDot(String filename, TransitionRelation tr) { HasseDiagram
	 * hd = tr.getHasseDiagram(); // LinkedHashSet<PartialBlockOrderNode> poNodes =
	 * getPoNodes(hd.getRoot()); HashMap<PartialBlockOrderNode, Integer>
	 * node2color = new HashMap<PartialBlockOrderNode, Integer>();
	 * 
	 * makeColors(hd.getRoot(), 0x101010, 0xffffff, node2color); // int i=1; //
	 * for (PartialBlockOrderNode node : poNodes) { // double color =
	 * ((double)(i++))/((double)poNodes.size()+1) * // ((double)0xffffff); //
	 * node2color.put(node, (int)color ); // }
	 * 
	 * try (PrintWriter pw = new PrintWriter(new OutputStreamWriter( new
	 * FileOutputStream(filename), "UTF-8"))) { pw.println("digraph dot {");
	 * LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
	 * LinkedHashSet<BasicBlock> done = new LinkedHashSet<BasicBlock>();
	 * todo.add(tr.getProcedure().getRootNode()); StringBuffer sb = new
	 * StringBuffer(); while (!todo.isEmpty()) { BasicBlock current =
	 * todo.pop(); done.add(current); // for (BasicBlock prev :
	 * current.getPredecessors()) { // pw.println(" \""+ current.getLabel() //
	 * +"\" -> \""+prev.getLabel()+"\" [style=dotted]"); // if
	 * (!todo.contains(prev) && !done.contains(prev)) { // todo.add(prev); // }
	 * // // } for (BasicBlock next : current.getSuccessors()) { sb.append(" \""
	 * + current.getLabel() + "\" -> \"" + next.getLabel() + "\" \n"); if
	 * (!todo.contains(next) && !done.contains(next)) { todo.add(next); } } }
	 * 
	 * for (BasicBlock b : done) { StringBuilder sb_ = new StringBuilder();
	 * sb_.append(Integer.toHexString(node2color.get(hd.findNode(b)))); while
	 * (sb_.length() < 6) { sb_.insert(0, '0'); // pad with leading zero if
	 * needed } String colorHex = sb_.toString(); pw.println("\"" + b.getLabel()
	 * + "\" " + "[label=\"" + b.getLabel() + "\",style=filled, fillcolor=\"#" +
	 * colorHex + "\"];\n"); } pw.println(sb.toString());
	 * 
	 * pw.println("}"); pw.close(); } catch (IOException e) {
	 * e.printStackTrace(); } }
	 * 
	 * public void hasseToDot(String filename, TransitionRelation tr) {
	 * HasseDiagram hd = tr.getHasseDiagram();
	 * 
	 * try (PrintWriter pw = new PrintWriter(new OutputStreamWriter( new
	 * FileOutputStream(filename), "UTF-8"))) { pw.println("digraph dot {");
	 * LinkedList<PartialBlockOrderNode> todo = new
	 * LinkedList<PartialBlockOrderNode>(); LinkedHashSet<PartialBlockOrderNode> done
	 * = new LinkedHashSet<PartialBlockOrderNode>(); todo.add(hd.getRoot());
	 * StringBuffer sb = new StringBuffer(); while (!todo.isEmpty()) {
	 * PartialBlockOrderNode current = todo.pop(); done.add(current); // for
	 * (BasicBlock prev : current.getPredecessors()) { // pw.println(" \""+
	 * current.getLabel() // +"\" -> \""+prev.getLabel()+"\" [style=dotted]");
	 * // if (!todo.contains(prev) && !done.contains(prev)) { // todo.add(prev);
	 * // } // // } for (PartialBlockOrderNode next : current.getSuccessors()) {
	 * sb.append(" \"" + current.hashCode() + "\" -> \"" + next.hashCode() +
	 * "\" \n"); if (!todo.contains(next) && !done.contains(next)) {
	 * todo.add(next); } } }
	 * 
	 * for (PartialBlockOrderNode node : done) { StringBuilder _sb = new
	 * StringBuilder(); for (BasicBlock b : node.getElements()) {
	 * _sb.append(b.getLabel() + "\n"); }
	 * 
	 * pw.println("\"" + node.hashCode() + "\" " + "[label=\"" + _sb.toString()
	 * + "\"];\n"); } pw.println(sb.toString());
	 * 
	 * pw.println("}"); pw.close(); } catch (IOException e) {
	 * e.printStackTrace(); } }
	 */
	/*
	 * ---------------------------- Plan B --------------------------------
	 */

	private Set<BasicBlock> tryToFindConflictInPO(Prover prover,
			TransitionRelation tr, PartialBlockOrderNode node, int timeout) {
		// pick any
		learnedConflicts.clear();
		BasicBlock current = node.getElements().iterator().next();
		try {
			// find the first one cheap.
			Set<BasicBlock> path = up(current, current,
					new LinkedHashSet<BasicBlock>());

			while (path != null) {
				Log.debug("Searching Path ... ");
				if (checkPath(current, path)) {
					return path;
				}
				path = findNextPath(current);
			}

			Log.debug("DONE Searching Path ... ");

		} catch (InfeasibleException e) {
			this.knownInfeasibleNodes.add(node);
			Log.debug("YEAH");
		}
		return new LinkedHashSet<BasicBlock>();
	}

	private Set<BasicBlock> up(BasicBlock b, BasicBlock source,
			Set<BasicBlock> path) throws InfeasibleException {
		Set<BasicBlock> path_ = new LinkedHashSet<BasicBlock>(path);
		path_.add(b);
		if (isInLearnedConflicts(path_))
			return null;
		if (b != this.procedure.getRootNode()) {
			for (BasicBlock x : b.getPredecessors()) {
				Set<BasicBlock> result = up(x, source, path_);
				if (result != null)
					return result;
			}
		} else {
			Set<BasicBlock> result = down(source, source, path_);
			if (result != null)
				return result;
		}
		return null;
	}

	private Set<BasicBlock> down(BasicBlock b, BasicBlock source,
			Set<BasicBlock> path) throws InfeasibleException {
		Set<BasicBlock> path_ = new LinkedHashSet<BasicBlock>(path);
		path_.add(b);
		if (isInLearnedConflicts(path_))
			return null;
		if (b != this.procedure.getExitNode()) {
			for (BasicBlock x : b.getSuccessors()) {
				Set<BasicBlock> result = down(x, source, path_);
				if (result != null)
					return result;
			}
		} else {
			// ignore paths that are already known conflicts.
			if (isInLearnedConflicts(path_))
				return null;


			return path_;

		}
		return null;
	}

	private boolean isInLearnedConflicts(Set<BasicBlock> path) {
		for (Set<BasicBlock> conflict : learnedConflicts) {
			if (conflict.size() > 0 && path.size() > conflict.size()
					&& path.containsAll(conflict)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("serial")
	public static class InfeasibleException extends Exception {

	}

	/**
	 * Checks the feasibility of Path. If feasible, returns True. 
	 * Otherwise Y
	 * @param source
	 * @param path
	 * @return
	 * @throws InfeasibleException
	 */
	private boolean checkPath(BasicBlock source, Set<BasicBlock> path)
			throws InfeasibleException {
		Log.debug("checking path");

		prover.push();
		for (BasicBlock b : path) {
			prover.addAssertion(this.transitionRelation.blockTransitionReleations
					.get(b));
		}
		ProverResult res = prover.checkSat(true);
		prover.pop();
		if (res == ProverResult.Sat) {
			return true;
		} else if (res == ProverResult.Unsat) {			
			Set<BasicBlock> core = computePseudoUnsatCore(path);
			learnedConflicts.add(new LinkedHashSet<BasicBlock>(core));
			if (path.size() == core.size()) {
				Log.debug("nothing could be removed");
				return false;
			}
			Set<BasicBlock> inevitableBlocks = findNodeThatMustBePassed(this.transitionRelation
					.getHasseDiagram().findNode(source));
			if (inevitableBlocks.containsAll(core)) {
				Log.debug("FOUND CONFLICT! DONE");
				markSmallestSubtreeInfeasible(core);
				throw new InfeasibleException();
			} else {
				Log.debug("nothing learned. Looking for next path.");
			}
		} else {
			throw new RuntimeException("PROVER FAILED");
		}
		return false;
	}

	/**
	 * TODO: @Philipp, this gets stuck for some examples :(
	 * 
	 * @param path
	 */
	boolean allowHackedTimeouts = true;

	private Set<BasicBlock> computePseudoUnsatCore(Set<BasicBlock> path) {

		Set<BasicBlock> core = new LinkedHashSet<BasicBlock>(path);
		
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>(path);
		while (!todo.isEmpty()) {			
			BasicBlock current = todo.pop();
			core.remove(current);
			prover.push();
			for (BasicBlock b : core) {
				prover.addAssertion(this.transitionRelation.blockTransitionReleations
						.get(b));
			}

			ProverResult res = ProverResult.Unknown;
			if (!allowHackedTimeouts) {
				res = prover.checkSat(true);
			} else {
				prover.checkSat(false);
				res = prover.getResult(200);
				if (res == ProverResult.Running) {
					// the coverage algorithm could not make progress within the
					// given time limit. Falling back to the new algorithms.
					Log.debug("\tComputing Unsat Core took too long and got killed.");
					prover.stop();
					res = ProverResult.Unknown;
				}
			}
			prover.pop();

			if (res != ProverResult.Unsat) {
				core.add(current); // then we needed this one
			}
		}
		return core;
	}

	private Set<BasicBlock> findNodeThatMustBePassed(PartialBlockOrderNode node) {
		if (node == null)
			return new LinkedHashSet<BasicBlock>();
		Set<BasicBlock> result = findNodeThatMustBePassed(node.getParent());
		result.addAll(node.getElements());
		return result;
	}

	private void markSmallestSubtreeInfeasible(Set<BasicBlock> unsatCore) {
		PartialBlockOrderNode lowest = this.transitionRelation
				.getHasseDiagram().getRoot();
		for (BasicBlock b : unsatCore) {
			PartialBlockOrderNode current = this.transitionRelation
					.getHasseDiagram().findNode(b);
			if (isAbove(current, lowest)) {
				lowest = current;
			}
		}
		LinkedList<PartialBlockOrderNode> todo = new LinkedList<PartialBlockOrderNode>();
		todo.add(lowest);
		while (!todo.isEmpty()) {
			PartialBlockOrderNode current = todo.pop();
			this.knownInfeasibleNodes.add(current);
			todo.addAll(current.getSuccessors());
		}

	}

	private boolean isAbove(PartialBlockOrderNode child,
			PartialBlockOrderNode parent) {
		PartialBlockOrderNode node = child;
		while (node != null) {
			if (node == parent)
				return true;
			node = node.getParent();
		}
		return false;
	}

	/*
	 * ================ stuff to find path with sat solver =====================
	 */

	/**
	 * Use the solver to find a path through 'current' in the abstract model
	 * that has not yet been covered.
	 * 
	 * @param current
	 *            The block that must be contained on the path.
	 * @return The set of all blocks on that path.
	 */
	private Set<BasicBlock> findNextPath(BasicBlock current) {
		Log.debug("Finding next path");
		Set<BasicBlock> blocks = this.getSubprogContaining(current);

		prover.push();
		// assert this subprogram.
		// assertAbstractaPath(blocks);

		assertAbstractaPathCfGTheory(blocks);

		prover.addAssertion(transitionRelation.getReachabilityVariables().get(
				current));
		// block all learned conflicts
		Log.debug("Asserting " + this.learnedConflicts.size() + " conflicts");
		for (Set<BasicBlock> conflict : this.learnedConflicts) {
			ProverExpr[] conj = new ProverExpr[conflict.size()];
			int i = 0;
			for (BasicBlock b : conflict) {
				conj[i++] = this.transitionRelation.getReachabilityVariables()
						.get(b);
			}
			prover.addAssertion(prover.mkNot(prover.mkAnd(conj)));
		}
		Log.debug("Checking for path.");
		ProverResult res = prover.checkSat(true);
		if (res == ProverResult.Sat) {
			LinkedHashSet<BasicBlock> necessaryNodes = new LinkedHashSet<BasicBlock>();
			necessaryNodes.add(current);
			Set<BasicBlock> path = this.getPathFromModel(prover,
					transitionRelation, blocks, necessaryNodes);
			prover.pop();
			Log.debug("Found one.");
			return path;
		} else if (res == ProverResult.Unsat) {
			prover.pop();
			// otherwise, we can remove it.
		} else {
			throw new RuntimeException("PROVER FAILED");
		}
		Log.debug("Found NONE.");
		return null;
	}

	/**
	 * 
	 * @param blocks
	 */
	private void assertAbstractaPathCfGTheory(Set<BasicBlock> blocks) {
		LinkedHashMap<ProverExpr, ProverExpr> ineffFlags = new LinkedHashMap<ProverExpr, ProverExpr>();
		for (BasicBlock block : blocks) {
			ProverExpr v = transitionRelation.getReachabilityVariables().get(
					block);
			ineffFlags.put(v, prover.mkVariable("" + v + "_flag",
					prover.getBooleanType()));
		}
		Dag<IFormula> vcdag = transitionRelation.getProverDAG();

		LinkedList<ProverExpr> remainingBlockVars = new LinkedList<ProverExpr>();
		LinkedList<ProverExpr> remainingIneffFlags = new LinkedList<ProverExpr>();
		for (Entry<ProverExpr, ProverExpr> entry : ineffFlags.entrySet()) {
			remainingBlockVars.add(entry.getKey());
			remainingIneffFlags.add(entry.getValue());
		}

		((PrincessProver) prover).setupCFGPlugin(vcdag, remainingBlockVars,
				remainingIneffFlags, 1);

		// Encode each block
		for (BasicBlock block : blocks) {

			// Get the successors of the block
			LinkedList<BasicBlock> successors = new LinkedList<BasicBlock>();
			for (BasicBlock succ : block.getSuccessors()) {
				if (blocks.contains(succ)) {
					successors.add(succ);
				}
			}

			// Construct the disjunction of the successors

			// Make the assertion
			ProverExpr assertion = prover.mkImplies(transitionRelation
					.getReachabilityVariables().get(block),
					mkDisjunction(transitionRelation, successors));

			// Assert it
			prover.addAssertion(assertion);

		}

		prover.addAssertion(transitionRelation.getReachabilityVariables().get(
				transitionRelation.getProcedure().getRootNode()));
		prover.addAssertion(transitionRelation.getReachabilityVariables().get(
				transitionRelation.getProcedure().getExitNode()));
		// Log.debug("Entries "+count);
	}

}