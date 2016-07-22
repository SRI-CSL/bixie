
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.ASTNode;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a statement.
 */
public abstract class Statement extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	
	protected Attribute[] attributes;
	
	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 */
	public Statement(ILocation loc) {
		super(loc);
		this.attributes = new Attribute[0];
	}

	public Statement(ILocation loc, Attribute[] attributes) {
		super(loc);
		this.attributes = attributes;
	}
	
	
	public Attribute[] getAttributes() {
		return this.attributes;
	}
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		return "Statement";
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		return children;
	}
}
