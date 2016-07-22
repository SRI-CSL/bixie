
package bixie.boogie.ast;

import java.util.List;

import bixie.boogie.ast.declaration.Declaration;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a unit.
 */
public class Unit extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The declarations of this unit.
	 */
	Declaration[] declarations;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param declarations
	 *            the declarations of this unit.
	 */
	public Unit(ILocation loc, Declaration[] declarations) {
		super(loc);
		this.declarations = declarations;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Unit").append('[');
		if (declarations == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < declarations.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(declarations[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the declarations of this unit.
	 * 
	 * @return the declarations of this unit.
	 */
	public Declaration[] getDeclarations() {
		return declarations;
	}

	/**
	 * Sets the declarations of this unit.
	 * 
	 * @param declarations
	 *            the declarations of this unit.
	 */
	public void setDeclarations(Declaration[] declarations) {
		this.declarations = declarations;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(declarations);
		return children;
	}
}
