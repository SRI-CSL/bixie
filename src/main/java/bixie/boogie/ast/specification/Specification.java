
package bixie.boogie.ast.specification;

import java.util.List;

import bixie.boogie.ast.ASTNode;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a specification.
 */
public abstract class Specification extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * True iff this specification is free.
	 */
	protected boolean isFree;
	protected Attribute[] attributes;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param isFree
	 *            true iff this specification is free.
	 */
	public Specification(ILocation loc, boolean isFree) {
		super(loc);
		this.isFree = isFree;
	}

	public Specification(ILocation loc, Attribute[] attributes, boolean isFree) {
		super(loc);
		this.isFree = isFree;
		this.attributes = attributes;
	}
	
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Specification").append('[');
		sb.append(isFree);
		return sb.append(']').toString();
	}

	/**
	 * Checks iff this specification is free.
	 * 
	 * @return true iff this specification is free.
	 */
	public boolean isFree() {
		return isFree;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(isFree);
		return children;
	}
}
