
package bixie.boogie.ast;

import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a left hand side.
 */
public abstract class LeftHandSide extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The type of this left hand side.
	 */
	BoogieType type;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 */
	public LeftHandSide(ILocation loc) {
		super(loc);
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this left hand side.
	 */
	public LeftHandSide(ILocation loc, BoogieType type) {
		super(loc);
		this.type = type;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("LeftHandSide").append('[');
		sb.append(type);
		return sb.append(']').toString();
	}

	/**
	 * Gets the type of this left hand side.
	 * 
	 * @return the type of this left hand side.
	 */
	public BoogieType getType() {
		return type;
	}

	/**
	 * Sets the type of this left hand side.
	 * 
	 * @param type
	 *            the type of this left hand side.
	 */
	public void setType(BoogieType type) {
		this.type = type;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(type);
		return children;
	}
}
