
package bixie.boogie.controlflow.statement;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * @author schaef
 * 
 */
public abstract class CfgStatement {

	private ILocation location;
	private Attribute[] attributes;

	public CfgStatement(ILocation loc) {
		this.location = loc;
	}

	public CfgStatement(ILocation loc, Attribute[] attributes) {
		this.location = loc;
		this.attributes = attributes;
	}
	
	
	public ILocation getLocation() {
		return this.location;
	}

	public abstract CfgStatement duplicate();

	/**
	 * @return the attributes
	 */
	public Attribute[] getAttributes() {
		return attributes;
	}	
	
}
