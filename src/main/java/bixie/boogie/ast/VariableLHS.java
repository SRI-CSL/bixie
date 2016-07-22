
package bixie.boogie.ast;

import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a variable l h s which is a special form of a left hand side.
 */
public class VariableLHS extends LeftHandSide {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifier of this variable l h s.
	 */
	String identifier;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param identifier
	 *            the identifier of this variable l h s.
	 */
	public VariableLHS(ILocation loc, String identifier) {
		super(loc);
		this.identifier = identifier;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this left hand side.
	 * @param identifier
	 *            the identifier of this variable l h s.
	 */
	public VariableLHS(ILocation loc, BoogieType type, String identifier) {
		super(loc, type);
		this.identifier = identifier;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("VariableLHS").append('[');
		sb.append(identifier);
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifier of this variable l h s.
	 * 
	 * @return the identifier of this variable l h s.
	 */
	public String getIdentifier() {
		return identifier;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifier);
		return children;
	}
}
