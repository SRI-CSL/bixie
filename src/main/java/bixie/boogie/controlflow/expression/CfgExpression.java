
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public abstract class CfgExpression {

	private ILocation location;
	private BoogieType type;

	public CfgExpression(ILocation loc, BoogieType type) {
		this.location = loc;
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public BoogieType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(BoogieType type) {
		this.type = type;
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

	public CfgExpression clone() {
		return substitute(null);
	}
	
	public abstract CfgExpression substitute(HashMap<CfgVariable, CfgExpression> substitutes);	
	
}
