
package bixie.boogie.controlflow.util;

import java.util.Collection;
import java.util.HashSet;

import bixie.boogie.controlflow.BasicBlock;
import bixie.util.Log;

/**
 * @author schaef
 */
public class PartialBlockOrderNode {
	
	HashSet<BasicBlock> currentClass = new HashSet<BasicBlock>();		
	HashSet<BasicBlock> unavoidables = new HashSet<BasicBlock>();
	private int height = 0;
	
	public int getHeight() {
		return height;
	}

	PartialBlockOrderNode parent = null;
	public PartialBlockOrderNode getParent() {
		return parent;
	}

	HashSet<PartialBlockOrderNode> successors = new HashSet<PartialBlockOrderNode>();
	
	
	public HashSet<PartialBlockOrderNode> getSuccessors() {
		return successors;
	}

	public PartialBlockOrderNode() {
		
	}

	public PartialBlockOrderNode(BasicBlock b, HashSet<BasicBlock> unav, int height) {
		this(b,unav);
		this.height = height;
	}
	
	
	public PartialBlockOrderNode(BasicBlock b, HashSet<BasicBlock> unav) {
		this.unavoidables = unav;
		this.currentClass.add(b);
	}

	public PartialBlockOrderNode(Collection<BasicBlock> blocks, HashSet<BasicBlock> unav) {
		this.unavoidables = unav;
		this.currentClass.addAll(blocks);
	}
	
	
	public void printLeafInfo() {
		if (this.successors.size()==0) {
			Log.info("LEAF: ");
			StringBuilder sb = new StringBuilder();
			sb.append("Blocks: ");
			for (BasicBlock b : this.currentClass) {
				sb.append(b.getLabel() + ", ");
			}
			Log.info(sb.toString());
			sb = new StringBuilder();
			sb.append("Unavoidables: ");
			for (BasicBlock b : this.unavoidables) {
				sb.append(b.getLabel() + ", ");
			}
			Log.info(sb.toString());
			Log.info("============= ");
		}
		for (PartialBlockOrderNode next : this.successors) {
			next.printLeafInfo();
		}
	}
	
	public HashSet<BasicBlock> getElements() {
		return this.currentClass;
	}

	public HashSet<BasicBlock> getUnavoidables() {
		return this.unavoidables;
	}

	public void connectParent(PartialBlockOrderNode parent) {
		this.parent = parent;
		parent.getSuccessors().add(this);
	}
	
	public HashSet<BasicBlock> getLeafRepresentatives() {
		HashSet<BasicBlock> ret = new HashSet<BasicBlock>();
		if (this.successors.size()==0) {
			BasicBlock b = this.currentClass.iterator().next();
			ret.add(b);
		}
		for (PartialBlockOrderNode next : this.successors) {
			ret.addAll(next.getLeafRepresentatives());
		}
		return ret;
	}
	
	private boolean subset(HashSet<BasicBlock> a, HashSet<BasicBlock> b) {
	  return a.size() <= b.size() && b.containsAll(a);
	}
	
	public boolean insert(BasicBlock b, HashSet<BasicBlock> unav) {
	    if (subset(unavoidables, unav)) {
	      
	        if (subset(unav, unavoidables)) {
	            this.currentClass.add(b);
	            //return true, because we found a node that fits
	            return true;
	        } else {
	            //the unavoidables of the new node are a clear super-set of the current one
	            //so try it with all the children
	            for (PartialBlockOrderNode succ : this.successors) {
	                //if we can insert it in a child, we are done.
	                if (succ.insert(b, unav))  return true;
	            }
	            //if not, we have to check how many children
	            //will be children of the new node.
	            PartialBlockOrderNode newnode = new PartialBlockOrderNode(b,unav, this.height+1);
	            for (PartialBlockOrderNode succ : new HashSet<PartialBlockOrderNode>(this.successors)) {
	                if (subset(unav, succ.unavoidables)) {
	                    disconnectAfromB(this, succ);
	                    connectAtoB(newnode,succ);
	                    succ.increaseHeight();
	                }
	            }           
	            connectAtoB(this, newnode);
	            return true;
	        }
	        
	    }
	    
	    return false;
	}
	
	public void increaseHeight() {
		this.height++;
		for (PartialBlockOrderNode succ : this.successors) {
			succ.increaseHeight();
		}
	}
	
	private void connectAtoB(PartialBlockOrderNode a, PartialBlockOrderNode b) {
		b.parent = a;
		a.successors.add(b);
	}

	private void disconnectAfromB(PartialBlockOrderNode a, PartialBlockOrderNode b) {
		b.parent = null;
		a.successors.remove(b);
	}
		
}
