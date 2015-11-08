package bixie.checker.transition_relation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import bixie.boogie.controlflow.AbstractControlFlowFactory;
import bixie.boogie.controlflow.BasicBlock;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.boogie.controlflow.expression.CfgExpression;
import bixie.boogie.controlflow.statement.CfgStatement;
import bixie.boogie.controlflow.util.HasseDiagram;
import bixie.boogie.controlflow.util.PartialBlockOrderNode;
import bixie.prover.Prover;
import bixie.prover.ProverExpr;

/**
 * @author schaef TODO: if we plan to do interprocedural analysis, we have to
 *         change the way globals are handled here.
 */
public class FaultLocalizationTransitionRelation extends
		AbstractTransitionRelation {

	public LinkedList<ProverExpr> obligations = new LinkedList<ProverExpr>();
	
	public HashMap<CfgStatement, BasicBlock> stmtOriginMap = new HashMap<CfgStatement, BasicBlock>(); 
	
	HasseDiagram hd;
	
	public FaultLocalizationTransitionRelation(CfgProcedure cfg,
			AbstractControlFlowFactory cff, Prover p) {
		super(cfg, cff, p);
		makePrelude();
	
		// create the ProverExpr for the precondition
		ProverExpr[] prec = new ProverExpr[cfg.getRequires().size()];
		int i = 0;
		for (CfgExpression expr : cfg.getRequires()) {
			prec[i] = this.expression2proverExpression(expr);
			i++;
		}
		this.requires = this.prover.mkAnd(prec);

		// create the ProverExpr for the precondition
		ProverExpr[] post = new ProverExpr[cfg.getEnsures().size()];
		i = 0;
		for (CfgExpression expr : cfg.getEnsures()) {
			post[i] = this.expression2proverExpression(expr);
			i++;
		}
		this.ensures = this.prover.mkAnd(post);

		this.hd = new HasseDiagram(cfg);
		computeSliceVC(cfg);
		this.hd = null;
		
		finalizeAxioms();
	}

	private void computeSliceVC(CfgProcedure cfg) {		
		PartialBlockOrderNode pon = hd.findNode(cfg.getRootNode());
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		todo.add(cfg.getRootNode());
		
		HashSet<BasicBlock> mustreach = pon.getElements();
		
//		System.err.println("-------");
//		for (BasicBlock b : mustreach) System.err.println(b.getLabel());
//		System.err.println("traverse ");
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();			
			obligations.addAll(statements2proverExpression(current.getStatements()));
			for (CfgStatement stmt : current.getStatements()) {
				this.stmtOriginMap.put(stmt, current);
			}
			BasicBlock next = foo(current, mustreach);
			if (next!=null) {
				if (mustreach.contains(next)) {
					todo.add(next);
				} else {
					System.err.println("FIXME: don't know what to do with "+next.getLabel());
				}
			}
		}		
//		System.err.println("traverse done");
	}
	
	private BasicBlock foo(BasicBlock b, HashSet<BasicBlock> mustpass) {
		HashSet<BasicBlock> done = new HashSet<BasicBlock>();
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		HashMap<BasicBlock, LinkedList<ProverExpr>> map = new HashMap<BasicBlock, LinkedList<ProverExpr>>(); 
		
		todo.addAll(b.getSuccessors());
		done.add(b);
		map.put(b, new LinkedList<ProverExpr>());
		map.get(b).add(this.prover.mkLiteral(true));
		
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			boolean allDone = true;
			LinkedList<LinkedList<ProverExpr>> prefix = new LinkedList<LinkedList<ProverExpr>>();
			for (BasicBlock pre : current.getPredecessors()) {
				if (!done.contains(pre)) {
					allDone = false; break;
				}
				prefix.add(map.get(pre));
			}
			if (!allDone) {
				todo.add(current);
				continue;
			}
			done.add(current);
			
			LinkedList<ProverExpr> conj = new LinkedList<ProverExpr>();
			if (prefix.size()>1) {
				//TODO
				LinkedList<ProverExpr> shared = prefix.getFirst();
				for (LinkedList<ProverExpr> list : prefix) {
					shared = sharedPrefix(shared, list);
				}

				conj.add(this.prover.mkAnd(shared.toArray(new ProverExpr[shared.size()])));

				LinkedList<ProverExpr> disj = new LinkedList<ProverExpr>();
				for (LinkedList<ProverExpr> list : prefix) {
					LinkedList<ProverExpr> cutlist = new LinkedList<ProverExpr>();
					cutlist.addAll(list);
					cutlist.removeAll(shared);
					disj.add(this.prover.mkAnd(cutlist.toArray(new ProverExpr[cutlist.size()])));
				}
				conj.add(this.prover.mkOr(disj.toArray(new ProverExpr[disj.size()])));

			} else if (prefix.size()==1) {
				conj.addAll(prefix.getFirst());
			} else {
				throw new RuntimeException("unexpected");
			}
			
			if (mustpass.contains(current)) {
				if (conj.size()==1 && conj.getFirst().equals(this.prover.mkLiteral(true))) {
					// in that case, the predecessor was already in mustpass so nothing needs to be done.
				} else {
					ProverExpr formula = this.prover.mkAnd(conj.toArray(new ProverExpr[conj.size()]));
					this.obligations.add(formula);
				}
				return current;
			} else {
				for (CfgStatement stmt : current.getStatements()) {
					this.stmtOriginMap.put(stmt, current);
				}

				conj.addAll(statements2proverExpression(current.getStatements()));				
				map.put(current, conj);
				
				for (BasicBlock suc : current.getSuccessors()) {
					if (!todo.contains(suc) && !done.contains(suc)) {
						todo.add(suc);
					}
				}
				
			}
			
		}	
		return null;
	}

	private LinkedList<ProverExpr> sharedPrefix(LinkedList<ProverExpr> shared, LinkedList<ProverExpr> list) {
		Iterator<ProverExpr> iterA = shared.iterator();
		Iterator<ProverExpr> iterB = list.iterator();
		LinkedList<ProverExpr> ret = new LinkedList<ProverExpr>();
		while(iterA.hasNext() && iterB.hasNext()) {
			ProverExpr next = iterA.next();
			if (next == iterB.next()) {
				ret.add(next);
			}
		}
		return ret;
	}


}
