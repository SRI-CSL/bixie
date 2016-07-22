
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.location.ILocation;

/**
 * Represents a return statement which is a special form of a statement.
 */
public class YieldStatement extends Statement {
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
	public YieldStatement(ILocation loc) {
		super(loc);
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		return "YieldStatement";
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		return children;
	}
}
