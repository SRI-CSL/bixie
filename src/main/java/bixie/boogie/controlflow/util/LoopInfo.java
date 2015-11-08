package bixie.boogie.controlflow.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import bixie.boogie.controlflow.BasicBlock;

/**
 * @author schaef
 */
public class LoopInfo {

	public BasicBlock loopHead = null;
	public HashSet<BasicBlock> loopingPred = null;
	public HashSet<BasicBlock> loopBody = null;
	public HashSet<BasicBlock> loopExit = null;
	public Stack<BasicBlock> nestedLoopHeads = new Stack<BasicBlock>();
	public LinkedList<LoopInfo> nestedLoops = new LinkedList<LoopInfo>();
	public HashSet<BasicBlock> loopEntries = new HashSet<BasicBlock>();

	//Hack! this should not be public!
	public HashMap<BasicBlock, BasicBlock> blockCloneMap = new HashMap<BasicBlock, BasicBlock>();

	public boolean isNestedLoop = false;

	public void updateLoopEntries() {
		loopEntries.clear();
		for (BasicBlock b : loopHead.getPredecessors()) {
			if (!loopingPred.contains(b))
				loopEntries.add(b);
		}		
	}
	
	public void refreshLoopBody() {
		HashSet<BasicBlock> newbody = new HashSet<BasicBlock>();
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>(); 
		newbody.add(loopHead);
		for (BasicBlock b : loopHead.getPredecessors()) {
			if (!this.loopEntries.contains(b)) {
				todo.add(b);
			}
		}
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			newbody.add(current);
			for (BasicBlock b : current.getPredecessors()) {
				if (!todo.contains(b) && !newbody.contains(b)) {
					todo.add(b);
				}
			}
		}
		this.loopBody = newbody;
	}
	
	public LoopInfo(BasicBlock loophead, HashSet<BasicBlock> loopingpred,
			HashSet<BasicBlock> loopbody, HashSet<BasicBlock> loopexit) {
		loopHead = loophead;
		loopingPred = loopingpred;
		loopBody = loopbody;
		loopExit = loopexit;
		for (BasicBlock b : loopHead.getPredecessors()) {
			if (!loopingPred.contains(b))
				loopEntries.add(b);
		}
	}

	@Override
	public String toString() {
		return toString("");
	}

	public String toString(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(">> Loop: " + loopHead.toString() + "\n");
		 sb.append("loop head pre:\n");
		 for (BasicBlock b : loopHead.getPredecessors()) {
		 sb.append(" " + b.getLabel());
		 }
		 sb.append("\n");
		 sb.append(">> Body:\n");
		 sb.append(">>\t");
		 for (BasicBlock b : loopBody) {
		 sb.append(" " + b.getLabel());
		 }
		 sb.append("\n");
		 sb.append(">>");
		 sb.append(">> Exit:\n");
		 sb.append(">>\t");
		 for (BasicBlock b : loopExit) {
		 sb.append(" " + b.getLabel());
		 }
		 sb.append("\n");
		int i = 0;
		for (LoopInfo n : this.nestedLoops) {
			sb.append(">> Nested Loop " + (i++) + "\n");
			// sb.append(n.toString(prefix +"\t"));
			sb.append(n);
		}
		sb.append("\n");

		return sb.toString();
	}

}
