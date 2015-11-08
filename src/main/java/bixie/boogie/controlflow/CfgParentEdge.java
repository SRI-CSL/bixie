/**
 * 
 */
package bixie.boogie.controlflow;

/**
 * @author schaef
 * represents the partial order information for CfgVariables
 */
public class CfgParentEdge {
	
	private CfgVariable varaible;
	private boolean isUnique; 
	
	public CfgParentEdge(CfgVariable var, boolean unique) {
		this.varaible = var;
		this.isUnique = unique;
	}

	/**
	 * @return the varaible
	 */
	public CfgVariable getVaraible() {
		return varaible;
	}

	/**
	 * @param varaible the varaible to set
	 */
	public void setVaraible(CfgVariable varaible) {
		this.varaible = varaible;
	}

	/**
	 * @return the isUnique
	 */
	public boolean isUnique() {
		return isUnique;
	}

	/**
	 * @param isUnique the isUnique to set
	 */
	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

}
