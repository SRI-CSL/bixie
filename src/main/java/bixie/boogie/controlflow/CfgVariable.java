
package bixie.boogie.controlflow;

import java.util.LinkedList;

import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgVariable {

	private String varname;
	private BoogieType type;

	private boolean isConstant;
	private boolean isGlobal;
	private boolean isUnique;
	private boolean isComplete = false;
	private LinkedList<CfgParentEdge> parentEdges = new LinkedList<CfgParentEdge>(); 
	
	// TODO: add missing fields

	public CfgVariable(String name, BoogieType type, boolean constant,
			boolean global, boolean unique, boolean complete) {
		this.varname = name;
		this.type = type;
		this.isConstant = constant;
		this.isGlobal = global;
		this.isUnique = unique;
		this.setComplete(complete);
	}

	public String getVarname() {
		return varname;
	}

	public BoogieType getType() {
		return type;
	}

	/**
	 * @return the isConstant
	 */
	public boolean isConstant() {
		return isConstant;
	}

	/**
	 * @param isConstant
	 *            the isConstant to set
	 */
	public void setConstant(boolean isConstant) {
		this.isConstant = isConstant;
	}

	/**
	 * @return the isUnique
	 */
	public boolean isUnique() {
		return isUnique;
	}

	/**
	 * @param isUnique
	 *            the isUnique to set
	 */
	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	/**
	 * @return the isGlobal
	 */
	public boolean isGlobal() {
		return isGlobal;
	}

	/**
	 * @param isGlobal
	 *            the isGlobal to set
	 */
	public void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}

	/**
	 * @return the parentEdges
	 */
	public LinkedList<CfgParentEdge> getParentEdges() {
		return parentEdges;
	}

	/**
	 * @param parentEdges the parentEdges to set
	 */
	public void setParentEdges(LinkedList<CfgParentEdge> parentEdges) {
		this.parentEdges = parentEdges;
	}

	/**
	 * @return the isComplete
	 */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * @param isComplete the isComplete to set
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	@Override
	public String toString() {
		return this.getVarname();
	}
}
