
package bixie.boogie.ast;

import java.util.List;

import bixie.boogie.ast.location.ILocation;

/**
 * Represents a attribute.
 */
public abstract class Attribute extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 */
	public Attribute(ILocation loc) {
		super(loc);
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		return "Attribute";
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		return children;
	}
}
