
package bixie.boogie.ast.asttypes;

import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a primitive type which is a special form of a a s t type.
 */
public class PrimitiveAstType extends ASTType {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The name of this primitive type.
	 */
	String name;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param name
	 *            the name of this primitive type.
	 */
	public PrimitiveAstType(ILocation loc, String name) {
		super(loc);
		this.name = name;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param boogieType
	 *            the boogie type of this a s t type.
	 * @param name
	 *            the name of this primitive type.
	 */
	public PrimitiveAstType(ILocation loc, BoogieType boogieType, String name) {
		super(loc, boogieType);
		this.name = name;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("PrimitiveType").append('[');
		sb.append(name);
		return sb.append(']').toString();
	}

	/**
	 * Gets the name of this primitive type.
	 * 
	 * @return the name of this primitive type.
	 */
	public String getName() {
		return name;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(name);
		return children;
	}
}
