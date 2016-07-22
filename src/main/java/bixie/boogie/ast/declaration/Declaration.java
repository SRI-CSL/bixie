
package bixie.boogie.ast.declaration;

import java.util.List;

import bixie.boogie.ast.ASTNode;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a declaration.
 */
public abstract class Declaration extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The attributes of this declaration.
	 */
	Attribute[] attributes;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            the attributes of this declaration.
	 */
	public Declaration(ILocation loc, Attribute[] attributes) {
		super(loc);
		this.attributes = attributes;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Declaration").append('[');
		if (attributes == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < attributes.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(attributes[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the attributes of this declaration.
	 * 
	 * @return the attributes of this declaration.
	 */
	public Attribute[] getAttributes() {
		return attributes;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(attributes);
		return children;
	}
}
