/**
 * 
 */
package bixie.transformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import util.Log;
import boogie.controlflow.BasicBlock;
import boogie.controlflow.CfgProcedure;
import boogie.controlflow.CfgVariable;
import boogie.controlflow.expression.CfgArrayAccessExpression;
import boogie.controlflow.expression.CfgArrayStoreExpression;
import boogie.controlflow.expression.CfgBinaryExpression;
import boogie.controlflow.expression.CfgBitVectorAccessExpression;
import boogie.controlflow.expression.CfgExpression;
import boogie.controlflow.expression.CfgFunctionApplication;
import boogie.controlflow.expression.CfgIdentifierExpression;
import boogie.controlflow.expression.CfgIfThenElseExpression;
import boogie.controlflow.expression.CfgQuantifierExpression;
import boogie.controlflow.expression.CfgUnaryExpression;
import boogie.controlflow.statement.CfgAssertStatement;
import boogie.controlflow.statement.CfgAssignStatement;
import boogie.controlflow.statement.CfgAssumeStatement;
import boogie.controlflow.statement.CfgCallStatement;
import boogie.controlflow.statement.CfgHavocStatement;
import boogie.controlflow.statement.CfgStatement;

/**
 * @author schaef
 *
 */
public class SingleStaticAssignment {
	
	public SingleStaticAssignment() {
				
	}
	
	/**
	 * computes an SSA version of p. This step also introduces blocks
	 * for frame conditions and a unified exit.
	 * @param p
	 */
	public void computeSSA(CfgProcedure p) {		
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> done = new LinkedList<BasicBlock>();

		//generate unified exit
		{
			Log.debug("------ create unified exit");
			LinkedList<BasicBlock> sinks = new LinkedList<BasicBlock>();
			todo.add(p.getRootNode());
			while (!todo.isEmpty()) {
				BasicBlock current = todo.removeLast();
				done.add(current);
				if (current.getSuccessors().size()>0) {
					for (BasicBlock next : current.getSuccessors()) {
						if (!todo.contains(next) && !done.contains(next)) {
							todo.add(next);
						}
					}
				} else {
					sinks.add(current);
				}			
			}
			
			if (sinks.size()==0) {
				throw new RuntimeException("Every procedure must have at least one sink: "+p.getProcedureName());
			}
			
			if (sinks.size()>1) {
				BasicBlock unifiedExit = new BasicBlock(p.getLocation(), "$unifiedExit");
				unifiedExit.returns = true;
				for (BasicBlock b : sinks) {
					b.returns = false;
					b.connectToSuccessor(unifiedExit);
				}
				p.setExitNode(unifiedExit);
			} 		
		}
		//generate inbetween block if needed
		{
			Log.debug("------ create block between");
			todo = new LinkedList<BasicBlock>();
			done = new LinkedList<BasicBlock>();		
			todo.add(p.getExitNode());
			while (!todo.isEmpty()) {
				BasicBlock current = todo.removeLast();
				done.add(current);
				for (BasicBlock next : current.getPredecessors()) {
					if (!todo.contains(next) && !done.contains(next)) {
						todo.add(next);
					}
				}			
				if (current.getPredecessors().size()>1) {
					for (BasicBlock next : new HashSet<BasicBlock>(current.getPredecessors())) {
						BasicBlock between = new BasicBlock(next.getLocationTag(), next.getLabel()+"$between$"+current.getLabel());
						next.disconnectFromSuccessor(current);
						next.connectToSuccessor(between);
						between.connectToSuccessor(current);
					}							
				}			
			}		
		}

		updateBlockSSA(p);		
		
		//IMPORTANT: for some of the later analysis set we must be
		//able to assume that the Exit node does not contain any
		//statements. Thus, we create a fresh block here.
		if (!p.getExitNode().getLabel().equals("$UnifiedExit")) {
			BasicBlock finalUnifiedExit = new BasicBlock(p.getExitNode().getLocationTag(), "$UnifiedExit");
			//connect each block that does not have a successor to the unified exit
			for (BasicBlock b : done) {
				if (b.getSuccessors().size() == 0) {
					b.connectToSuccessor(finalUnifiedExit);					
				}

				{
					HashMap<CfgVariable, Integer> offset = new HashMap<CfgVariable, Integer>();
					for (BasicBlock pred : finalUnifiedExit.getPredecessors()) {
						mergeSSAOffsets(offset, pred.getLocalIncarnationMap());
					}
					for (BasicBlock pred : new HashSet<BasicBlock>(
							finalUnifiedExit.getPredecessors())) {
						addFrameCondition(pred, offset);
					}
					recomputLocalSSA(finalUnifiedExit, offset);					
				}
				
			}
			//Now do the SSA for the postcondition using the offset of
			//the unified exit.
			for (CfgExpression expr : p.getEnsures()) {
				recomputLocalSSA(expr, finalUnifiedExit.getLocalIncarnationMap());	
			}
			

			p.setExitNode(finalUnifiedExit);
		}
				
		return;
	}

	/**
	 * updates the SSA tables of each block.
	 * NOTE: use computeSSA instead if you are not sure if there is a unified exit
	 * of the frame conditions haven't been created yet.
	 * @param p
	 */
	public void updateBlockSSA(CfgProcedure p) {
		Log.debug("------ recomute the ssa");
		//Do the actual SSA
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> done = new LinkedList<BasicBlock>();	
		if (p.getRootNode()==null) return;
		todo.add(p.getRootNode());

		//do SSA on the precondition first.
		
		
		
		for (CfgExpression expr : p.getRequires()) {
			recomputLocalSSA(expr, new HashMap<CfgVariable, Integer>());	
		}
		
		while (!todo.isEmpty()) {
			BasicBlock current = todo.removeLast();
			
			/*
			 * Check if SSA has already been computed for all predecessors. If
			 * not, add current to the bottom of the todo stack and continue
			 * with the next Block.
			 */
			{
				boolean allPredsDone = true;
				if (current.getPredecessors()!=null) {
					for (BasicBlock pred : current.getPredecessors()) {
						if (!done.contains(pred)) {
							allPredsDone = false;
							break;
						}
					}
				}
				if (allPredsDone) {
					done.add(current);
				} else {
					todo.addFirst(current);
					continue;
				}
			}
			// now compute the frame conditions for all predecessors
			{
				
				HashMap<CfgVariable, Integer> offset = new HashMap<CfgVariable, Integer>();
				if (current.getPredecessors().size() > 0) {
					for (BasicBlock pred : current.getPredecessors()) {
						mergeSSAOffsets(offset, pred.getLocalIncarnationMap());
					}
					for (BasicBlock pred : new HashSet<BasicBlock>(
							current.getPredecessors())) {
						addFrameCondition(pred, offset);
					}
				} 
				recomputLocalSSA(current, offset);
			}
			for (BasicBlock next : current.getSuccessors()) {
				if (!todo.contains(next) && !done.contains(next)) {
					todo.addLast(next);
				}
			}
		}		
	}
	
	private void mergeSSAOffsets(HashMap<CfgVariable, Integer> successorOffset,
			HashMap<CfgVariable, Integer> predecessorOffset) {
		for (Entry<CfgVariable, Integer> entry : predecessorOffset.entrySet()) {
			if (!successorOffset.containsKey(entry.getKey())) {
				successorOffset.put(entry.getKey(), entry.getValue());
			} else {
				if (successorOffset.get(entry.getKey()) < entry.getValue()) {
					successorOffset.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	private void addFrameCondition(BasicBlock b,
			HashMap<CfgVariable, Integer> maxOffset) {
		HashMap<CfgVariable, Integer> offset = b.getLocalIncarnationMap();
		for (Entry<CfgVariable, Integer> entry : maxOffset.entrySet()) {
			if (!offset.containsKey(entry.getKey())) {
			} else {
				if (offset.get(entry.getKey()) < entry.getValue()) {
					CfgIdentifierExpression[] left = { new CfgIdentifierExpression(
							b.getLocationTag(), entry.getKey(),
							entry.getValue()) };
					CfgIdentifierExpression[] right = { new CfgIdentifierExpression(
							b.getLocationTag(), entry.getKey(),
							offset.get(entry.getKey())) };
					CfgAssignStatement assign = new CfgAssignStatement(
							b.getLocationTag(), left, right);
					b.addStatement(assign);
					b.getLocalIncarnationMap().put(entry.getKey(), entry.getValue());//TODO: experiment
				}
			}
		}

	}
	
	public void recomputLocalSSA(BasicBlock b, HashMap<CfgVariable, Integer> offset) {
		b.localIncarnationMap = new HashMap<CfgVariable, Integer>(offset);
		for (CfgStatement stmt : b.getStatements()) {
			if (stmt instanceof CfgAssertStatement) {
				CfgAssertStatement asrt = (CfgAssertStatement) stmt;
				recomputLocalSSA(asrt.getCondition(), b.localIncarnationMap);
			} else if (stmt instanceof CfgAssumeStatement) {
				CfgAssumeStatement asum = (CfgAssumeStatement) stmt;
				recomputLocalSSA(asum.getCondition(), b.localIncarnationMap);
			} else if (stmt instanceof CfgAssignStatement) {
				CfgAssignStatement asgn = (CfgAssignStatement) stmt;
				recomputLocalSSA(asgn.getRight(), b.localIncarnationMap);
				for (CfgIdentifierExpression id : asgn.getLeft()) {
					if (!b.localIncarnationMap.containsKey(id.getVariable())) {
						b.localIncarnationMap.put(id.getVariable(), 0);
					}
					b.localIncarnationMap.put(id.getVariable(),
							b.localIncarnationMap.get(id.getVariable()) + 1);
					id.setCurrentIncarnation(b.localIncarnationMap.get(id.getVariable()));
				}
			} else if (stmt instanceof CfgCallStatement) {
				CfgCallStatement call = (CfgCallStatement) stmt;
				recomputLocalSSA(call.getArguments(), b.localIncarnationMap);
				for (CfgVariable v : call.getCallee().getModifies()) {
					if (!b.localIncarnationMap.containsKey(v)) {
						b.localIncarnationMap.put(v, 0);
					}
					b.localIncarnationMap.put(v, b.localIncarnationMap.get(v) + 1);
				}
				for (CfgIdentifierExpression id : call.getLeftHandSide()) {
					if (!b.localIncarnationMap.containsKey(id.getVariable())) {
						b.localIncarnationMap.put(id.getVariable(), 0);
					}
					b.localIncarnationMap.put(id.getVariable(),
							b.localIncarnationMap.get(id.getVariable()) + 1);
					id.setCurrentIncarnation(b.localIncarnationMap.get(id.getVariable()));
				}
			} else if (stmt instanceof CfgHavocStatement) {
				for (CfgVariable v : ((CfgHavocStatement) stmt).getVariables()) {
					if (!b.localIncarnationMap.containsKey(v)) {
						b.localIncarnationMap.put(v, 0);
					}
					b.localIncarnationMap.put(v, b.localIncarnationMap.get(v) + 1);
				}
			}
		}
	}

	private void recomputLocalSSA(CfgExpression[] exp,
			HashMap<CfgVariable, Integer> offset) {
		if (exp == null) {
			return;
		}
		for (int i = 0; i < exp.length; i++) {
			recomputLocalSSA(exp[i], offset);
		}
	}

	private void recomputLocalSSA(CfgExpression exp,
			HashMap<CfgVariable, Integer> offset) {
		if (exp instanceof CfgArrayAccessExpression) {
			CfgArrayAccessExpression aae = (CfgArrayAccessExpression) exp;
			recomputLocalSSA(aae.getIndices(), offset);
			recomputLocalSSA(aae.getBaseExpression(), offset);
		} else if (exp instanceof CfgArrayStoreExpression) {
			CfgArrayStoreExpression ase = (CfgArrayStoreExpression) exp;
			recomputLocalSSA(ase.getValueExpression(), offset);
			recomputLocalSSA(ase.getIndices(), offset);
			recomputLocalSSA(ase.getBaseExpression(), offset);
		} else if (exp instanceof CfgBinaryExpression) {
			CfgBinaryExpression bexp = (CfgBinaryExpression) exp;
			recomputLocalSSA(bexp.getLeftOp(), offset);
			recomputLocalSSA(bexp.getRightOp(), offset);
		} else if (exp instanceof CfgBitVectorAccessExpression) {
			CfgBitVectorAccessExpression bva = (CfgBitVectorAccessExpression) exp;
			recomputLocalSSA(bva.getBitvector(), offset);
		} else if (exp instanceof CfgFunctionApplication) {
			CfgFunctionApplication fa = (CfgFunctionApplication) exp;
			recomputLocalSSA(fa.getArguments(), offset);
		} else if (exp instanceof CfgIdentifierExpression) {
			CfgIdentifierExpression id = (CfgIdentifierExpression) exp;
			if (!offset.containsKey(id.getVariable())) {
				offset.put(id.getVariable(), Integer.valueOf(0));
			}
			id.setCurrentIncarnation(offset.get(id.getVariable()));
		} else if (exp instanceof CfgIfThenElseExpression) {
			CfgIfThenElseExpression ite = (CfgIfThenElseExpression) exp;
			recomputLocalSSA(ite.getCondition(), offset);
			recomputLocalSSA(ite.getThenExpression(), offset);
			recomputLocalSSA(ite.getElseExpression(), offset);
		} else if (exp instanceof CfgQuantifierExpression) {
			CfgQuantifierExpression qe = (CfgQuantifierExpression) exp;
			//TODO: does that make sense?
			for (CfgVariable v : qe.getParameters()) {
				offset.put(v, 0);
			}
			recomputLocalSSA(((CfgQuantifierExpression) exp).getSubformula(), offset);
		} else if (exp instanceof CfgUnaryExpression) {
			CfgUnaryExpression uexp = (CfgUnaryExpression) exp;
			recomputLocalSSA(uexp.getExpression(), offset);
		}
	}
	

}
