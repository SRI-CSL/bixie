
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a havoc statement which is a special form of a statement.
 */
public class HavocStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifiers of this havoc statement.
	 */
	String[] identifiers;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param identifiers
	 *            the identifiers of this havoc statement.
	 */
	public HavocStatement(ILocation loc, String[] identifiers) {
		super(loc);
		this.identifiers = identifiers;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            attributes
	 * @param formula
	 *            the formula of this assert statement.
	 */
	public HavocStatement(ILocation loc, Attribute[] attributes, String[] identifiers) {
		super(loc, attributes);
		this.identifiers = identifiers;
	}
	
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("HavocStatement").append('[');
		if (identifiers == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < identifiers.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(identifiers[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifiers of this havoc statement.
	 * 
	 * @return the identifiers of this havoc statement.
	 */
	public String[] getIdentifiers() {
		return identifiers;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifiers);
		return children;
	}
}
