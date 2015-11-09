/*
 * boogieamp - Parser, Factory, and Utilities to create Boogie Programs from Java
 * Copyright (C) 2013 Martin Schaef and Stephan Arlt
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package bixie.boogie.controlflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.statement.CfgStatement;

public class BasicBlock {

	private ILocation location;
	private LinkedList<CfgStatement> statments = new LinkedList<CfgStatement>();

	private HashSet<BasicBlock> predecessors = new HashSet<BasicBlock>();
	private HashSet<BasicBlock> successors = new HashSet<BasicBlock>();
	private String label;

	public HashMap<CfgVariable, Integer> localIncarnationMap = new HashMap<CfgVariable, Integer>();
	public boolean isLoopHead = false;
	public boolean returns = false;
	
	public BasicBlock(ILocation loc, String label) {
		this.location = loc;
		this.label = label;
		this.predecessors = new HashSet<BasicBlock>();
		this.successors = new HashSet<BasicBlock>();
	}

	public ILocation getLocationTag() {
		return this.location;
	}

	public void setLocationTag(ILocation loc) {
		this.location = loc;
	}
	
	public void connectToSuccessor(BasicBlock successor) {
		this.successors.add(successor);
		successor.getPredecessors().add(this);
	}

	public void disconnectFromSuccessor(BasicBlock succ) {
		if (this.successors.contains(succ)
				&& succ.getPredecessors().contains(this)) {
			succ.getPredecessors().remove(this);
			this.successors.remove(succ);			
		} else {
			throw new RuntimeException("Cannot disconnect unconnected nodes!");
		}
	}

	/**
	 * Returns the max incarnation for each variable used in this Block
	 * 
	 * @return
	 */
	public HashMap<CfgVariable, Integer> getLocalIncarnationMap() {
		return localIncarnationMap;
	}


	/**
	 * @return the statments
	 */
	public LinkedList<CfgStatement> getStatements() {
		return this.statments;
	}

	/**
	 * @param statments
	 *            the statments to set
	 */
	public void setStatements(LinkedList<CfgStatement> statments) {
		// TODO: recompute the local SSA
		localIncarnationMap = new HashMap<CfgVariable, Integer>();
		this.statments = new LinkedList<CfgStatement>(statments);
	}

	public void addStatement(CfgStatement s, boolean first) {
		if (!first) {
			this.statments.addLast(s);
		} else {
			this.statments.addFirst(s);
		}
	}

	public void addStatement(CfgStatement s) {
		this.addStatement(s, false);
	}

	/**
	 * @return the successors
	 */
	public HashSet<BasicBlock> getSuccessors() {
		return successors;
	}

	/**
	 * @return the predecessors
	 */
	public HashSet<BasicBlock> getPredecessors() {
		return predecessors;
	}


	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t"+ this.label + ":\n");
		for (CfgStatement stmt : this.statments) {
			sb.append("\t\t");
			sb.append(stmt.toString()); 
			sb.append(";\n");
		}
		if (this.successors.size()==0) {
			sb.append("\t\t");
			sb.append("return;\n");
		} else {
			sb.append("\t\t");
			sb.append("goto ");
			String prefix = "" ;
			for (BasicBlock b : this.successors) {
				sb.append(prefix);				
				sb.append(b.getLabel());
				prefix = ", ";
			}
			sb.append(";\n");
		}
		return sb.toString();
	}
	
	
	@Override
	public int hashCode() {
		return this.label.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	    if (((BasicBlock)obj).getLabel().equals(this.label)) 
	    	return true;
	    return false;
	}

	/**
	 * Clones a basic block but does NOT clone the edges!
	 */	
	public BasicBlock duplicate() {
		return duplicate("");				
	}

	
	public BasicBlock duplicate(String labelprefix) {
		BasicBlock clone = new BasicBlock(location, labelprefix+label);		
		clone.isLoopHead = this.isLoopHead;		
		LinkedList<CfgStatement> clonestmts = new LinkedList<CfgStatement>();
		for (CfgStatement s : this.getStatements()) {
			clonestmts.add(s.duplicate());
		}
		clone.setStatements(clonestmts);
		return clone;
	}
}
