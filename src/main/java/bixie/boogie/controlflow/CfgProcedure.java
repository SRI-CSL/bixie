
package bixie.boogie.controlflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.expression.CfgExpression;
import bixie.util.Log;

/**
 * @author schaef TODO: this should have a different name because it actually
 *         represents the procedure/function and its cfg
 */
public class CfgProcedure {

	public static boolean printSSA = false; //TODO: printing hack
	private ILocation location;
	private BasicBlock rootNode, exitNode;
	private HashSet<CfgExpression> enusres = new HashSet<CfgExpression>(),
			requires = new HashSet<CfgExpression>(),
			invariant = new HashSet<CfgExpression>();
	private HashSet<CfgVariable> modifies = new HashSet<CfgVariable>();
	private String procedureName;

	private CfgVariable[] inParams = null;
	private CfgVariable[] outParams = null;
	private CfgVariable[] localVars = null;

	public CfgProcedure(String procname) {
		this.procedureName = procname;
	}

	/**
	 * @return the exitNode
	 */
	public BasicBlock getExitNode() {
		return exitNode;
	}

	/**
	 * @param exitNode
	 *            the exitNode to set
	 */
	public void setExitNode(BasicBlock exitNode) {
		this.exitNode = exitNode;
	}

	/**
	 * @return the rootNode
	 */
	public BasicBlock getRootNode() {
		return rootNode;
	}

	/**
	 * @param rootNode
	 *            the rootNode to set
	 */
	public void setRootNode(BasicBlock rootNode) {
		this.rootNode = rootNode;
	}

	/**
	 * @return the location
	 */
	public ILocation getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(ILocation location) {
		this.location = location;
	}

	/**
	 * @return the enusres
	 */
	public HashSet<CfgExpression> getEnsures() {
		return enusres;
	}

	/**
	 * @param enusres
	 *            the enusres to set
	 */
	public void setEnusres(HashSet<CfgExpression> enusres) {
		this.enusres = enusres;
	}

	/**
	 * @return the requires
	 */
	public HashSet<CfgExpression> getRequires() {
		return requires;
	}

	/**
	 * @param requires
	 *            the requires to set
	 */
	public void setRequires(HashSet<CfgExpression> requires) {
		this.requires = requires;
	}

	/**
	 * @return the invariant
	 */
	public HashSet<CfgExpression> getInvariant() {
		return invariant;
	}

	/**
	 * @param invariant
	 *            the invariant to set
	 */
	public void setInvariant(HashSet<CfgExpression> invariant) {
		this.invariant = invariant;
	}

	/**
	 * @return the modifies
	 */
	public HashSet<CfgVariable> getModifies() {
		return modifies;
	}

	/**
	 * @param modifies
	 *            the modifies to set
	 */
	public void setModifies(HashSet<CfgVariable> modifies) {
		this.modifies = modifies;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("procedure "+ this.procedureName + "(");
		String prefix = "";
		for (CfgVariable v : this.inParams) {
			sb.append(prefix);
			prefix = ", ";
			sb.append(v.getVarname());
			sb.append(" : ");
			sb.append(v.getType());
		}
		sb.append(") ");

		if (this.outParams.length>0) {
			sb.append(" returns (");
			prefix = "";
			for (CfgVariable v : this.outParams) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(v.getVarname());
				if (CfgProcedure.printSSA) {
					sb.append("__0");
				}
				sb.append(" : ");
				sb.append(v.getType());
			}
			sb.append(")");
		}
		sb.append("\n");
		
		if (this.modifies.size()>0) {
			sb.append("  modifies ");
			prefix = "";
			if (!CfgProcedure.printSSA) {
			for (CfgVariable v : this.modifies) {
				sb.append(prefix);
				prefix = ", ";
				sb.append(v.getVarname());
			}
			} else {
				//TODO: when printing in ssa, we have to add all incarnations of globals that may be modified to the modifies clause				
				for (CfgVariable v : this.modifies) {
					sb.append(prefix);
					prefix = ", ";
					sb.append(v.getVarname());
				}				
			}
			sb.append(";\n");
		}
		
		for (CfgExpression formula : this.enusres) {
			sb.append("ensures ");
			sb.append(formula.toString());
			sb.append(";\n");
		}

		for (CfgExpression formula : this.requires) {
			sb.append("requires ");
			sb.append(formula.toString());
			sb.append(";\n");
		}
		
		
		if (this.rootNode == null) {			
			return sb.toString();
		}


		if (this.localVars!=null) {
			for (CfgVariable local : this.localVars) {
				sb.append("  var ");
				sb.append(local.getVarname());
				sb.append(" : ");
				sb.append(local.getType().toString());
				//TODO: do we need to do something about the other properties of local here?
				sb.append(";\n");
			}
		}
		
		HashSet<BasicBlock> done = new HashSet<BasicBlock>();
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		todo.push(rootNode);
		while (!todo.isEmpty()) {
			BasicBlock b = todo.pop();
			done.add(b);
			sb.append(b.toString());
			for (BasicBlock next : b.getSuccessors()) {
				if (!todo.contains(next) && !done.contains(next)) {
					todo.add(next);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * TODO: this is only for debugging and should be removed later
	 * @param filename
	 */	
	public void toFile(String filename) {
		File fpw = new File(filename);
		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(fpw), 
						StandardCharsets.UTF_8), true);) {
			pw.println(this.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the procedureName
	 */
	public String getProcedureName() {
		return procedureName;
	}

	/**
	 * @param procedureName
	 *            the procedureName to set
	 */
	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	/**
	 * @return the inParams
	 */
	public CfgVariable[] getInParams() {
		return inParams;
	}

	/**
	 * @param inParams
	 *            the inParams to set
	 */
	public void setInParams(CfgVariable[] inParams) {
		this.inParams = inParams;
	}

	/**
	 * @return the outParams
	 */
	public CfgVariable[] getOutParams() {
		return outParams;
	}

	/**
	 * @param outParams
	 *            the outParams to set
	 */
	public void setOutParams(CfgVariable[] outParams) {
		this.outParams = outParams;
	}

	/**
	 * @return the localVars
	 */
	public CfgVariable[] getLocalVars() {
		return localVars;
	}

	/**
	 * @param localVars
	 *            the localVars to set
	 */
	public void setLocalVars(CfgVariable[] localVars) {
		this.localVars = localVars;
	}

	public void toDot(String filename) {
		File fpw = new File(filename);		
		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(fpw), 
						StandardCharsets.UTF_8), true);) {
			pw.println("digraph dot {");
			LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
			HashSet<BasicBlock> done = new HashSet<BasicBlock>();
			todo.add(this.rootNode);
			StringBuffer sb = new StringBuffer();
			while (!todo.isEmpty()) {
				BasicBlock current = todo.pop();
				done.add(current);
//				 for (BasicBlock prev : current.getPredecessors()) {
//				 pw.println(" \""+ current.getLabel()
//				 +"\" -> \""+prev.getLabel()+"\" [style=dotted]");
//					if (!todo.contains(prev) && !done.contains(prev)) {
//						todo.add(prev);
//					}
//
//				 }
				for (BasicBlock next : current.getSuccessors()) {
					sb.append(" \"" + current.getLabel() + "\" -> \""
							+ next.getLabel() + "\" \n");
					if (!todo.contains(next) && !done.contains(next)) {
						todo.add(next);
					}
				}
			}
			
			for (BasicBlock b : done ) {
				pw.println("\""+b.getLabel()+"\" " + "[label=\""+ b.getLabel()+"\"];\n" );
			}
			pw.println(sb.toString());

			pw.println("}");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is a shallow clone, it does not clone the BasicBlocks
	 * that form the actual graph!
	 */
	
	public CfgProcedure duplicate() {
		CfgProcedure clone = new CfgProcedure(this.procedureName);
		clone.setLocation(location);
		clone.setEnusres(enusres);
		clone.setExitNode(exitNode);
		clone.setInParams(inParams);
		clone.setInvariant(invariant);
		clone.setLocalVars(localVars);
		clone.setModifies(modifies);
		clone.setOutParams(outParams);
		clone.setRequires(requires);
		clone.setRootNode(rootNode);
		return clone;
	}

	/**
	 * build a new subprogram that contains all paths in p from "from"
	 * to "to" including from and excluding "to"
	 * @param p
	 * @param from
	 * @param to
	 * @return
	 */
	public CfgProcedure computeSlice(BasicBlock from, BasicBlock to) {
		System.err.println("Slice from: "+ from.getLabel() + " to " + to.getLabel());
		LinkedList<BasicBlock> reachable = new LinkedList<BasicBlock>();
		//collect all blocks that are backward reachable from "to"
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		todo.add(to);
		while (!todo.isEmpty()) {
			BasicBlock current = todo.removeFirst();
			reachable.add(current);
			for (BasicBlock next : current.getPredecessors()) {
				if (!todo.contains(next) && !reachable.contains(next)) {
					todo.add(next);
				}
			}
		}
		//collect all blocks that are forward reachable from "from"
		todo = new LinkedList<BasicBlock>();
		todo.add(from);
		LinkedList<BasicBlock> done = new LinkedList<BasicBlock>();
		while (!todo.isEmpty()) {
			BasicBlock current = todo.removeFirst();
			done.add(current);
			for (BasicBlock next : current.getSuccessors()) {
				if (!todo.contains(next) && !done.contains(next)) {
					todo.add(next);
				}
			}
		}		
//		for (BasicBlock b : reachable) {
//			System.err.println("reachable "+ b.getLabel());
//		}
//		for (BasicBlock b : done) {
//			System.err.println("done "+ b.getLabel());
//		}

		
		//intersect the sets of blocks reachable from "from" and bwd
		//reachable from "to".
		reachable.retainAll(done);
		//now exclude "to" from the set of reachable nodes.
		reachable.remove(to);		
		
		if (!reachable.contains(from)) {
			throw new RuntimeException("Slice didn't work");
		}
		
		return computeSlice(reachable, from);
	}
	


	/**
	 * compute a sub-program that contains all blocks on
	 * paths through the elements in component
	 * @param component
	 * @return
	 */
	public CfgProcedure computeSubProg(Collection<BasicBlock> component) {
		Set<BasicBlock> reachable = getAllReachable(component, false);
		reachable.addAll(getAllReachable(component, true));
		return computeSlice(reachable, this.getRootNode());
	}	
	
	
	private Set<BasicBlock> getAllReachable(Collection<BasicBlock> startset, boolean forward) {
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		Set<BasicBlock> done = new HashSet<BasicBlock>();
		todo.addAll(startset);
		while(!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			done.add(current);
			
			for (BasicBlock next : getNext(current, forward)) {
				if (!todo.contains(next) && !done.contains(next)) {
					todo.contains(next);
				}
			}	
		}
		return done;
	}
	
	private Set<BasicBlock> getNext(BasicBlock b, boolean forward) {
		if (forward) return b.getSuccessors();
		return b.getPredecessors();
	}
	
	/**
	 * compute a new cfgprocedure containing the slice of all blocks in reachable
	 * starting from "from".
	 * @param p
	 * @param reachable
	 * @param from
	 * @return
	 */
	public CfgProcedure computeSlice(Collection<BasicBlock> reachable, BasicBlock from) {
		//Now clone the subgraph
		CfgProcedure slice = this.duplicate();
		//first clone all reachable blocks
		HashMap<BasicBlock, BasicBlock> cloneMap = new HashMap<BasicBlock, BasicBlock>();
		for (BasicBlock b : reachable) {
			cloneMap.put(b, b.duplicate());
		}
		slice.setRootNode(cloneMap.get(from));		
		
		//now connect them as in the original
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> done = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> exits = new LinkedList<BasicBlock>();
		todo.add(from);
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			done.add(current);			
			BasicBlock current_clone = cloneMap.get(current);
			boolean hasReachableSuccessor = false;
			if (current.getSuccessors().size()>0) {
				for (BasicBlock next : current.getSuccessors()) {
					if (reachable.contains(next)) {
						BasicBlock next_clone = cloneMap.get(next);
						current_clone.connectToSuccessor(next_clone);
						hasReachableSuccessor = true;
					}
					if (!todo.contains(next) && !done.contains(next) && reachable.contains(next)) {
						todo.add(next);
					}
				}
			}
			if (!hasReachableSuccessor) {
				exits.add(current_clone);
			}
		}
		
		if (exits.size() == 0) {
			throw new RuntimeException("Error during slicing");
		} else if (exits.size() == 1) {
			slice.setExitNode(exits.get(0));	
		} else {
			Log.error("Something strange happened during the slice... test more!");
			BasicBlock sliceexit = new BasicBlock(slice.getLocation(), "$SliceExit");
			for (BasicBlock b : exits) {
				b.connectToSuccessor(sliceexit);
			}
			slice.setExitNode(sliceexit);
		}
			
		return slice;	
	}
	
	/**
	 * removes all blocks and resp. edges that are not reachable
	 * from the root node.
	 */
	public void pruneUnreachableBlocks() {
		if (this.rootNode==null) {
			Log.error("You tried to prune on an empty body!");
			return;
		}
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> reachable = new LinkedList<BasicBlock>();		
		todo.add(this.rootNode);
		//collect all forward reachable blocks.
		while (!todo.isEmpty()) {
			BasicBlock current = todo.removeFirst();
			reachable.add(current);						
			for (BasicBlock next : current.getSuccessors()) {
				if (!reachable.contains(next) && !todo.contains(next)) {
					todo.add(next);
				}
			}
		}

		todo = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> done = new LinkedList<BasicBlock>();		
		todo.add(this.rootNode);
		//now remove all unreachable predecessors of reachable nodes
		while (!todo.isEmpty()) {
			BasicBlock current = todo.removeFirst();
			done.add(current);	
			for (BasicBlock prev : new HashSet<BasicBlock>(current.getPredecessors())) {
				if (!reachable.contains(prev)) {
					prev.disconnectFromSuccessor(current);
				}
			}
			
			for (BasicBlock next : current.getSuccessors()) {
				if (!done.contains(next) && !todo.contains(next)) {
					todo.add(next);
				}
			}
		}
		
		
	}
}
