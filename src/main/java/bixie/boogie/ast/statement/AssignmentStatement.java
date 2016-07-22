
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.LeftHandSide;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a assignment statement which is a special form of a statement.
 */
public class AssignmentStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The lhs of this assignment statement.
	 */
	LeftHandSide[] lhs;

	/**
	 * The rhs of this assignment statement.
	 */
	Expression[] rhs;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param lhs
	 *            the lhs of this assignment statement.
	 * @param rhs
	 *            the rhs of this assignment statement.
	 */
	public AssignmentStatement(ILocation loc, LeftHandSide[] lhs,
			Expression[] rhs) {
		super(loc);
		this.lhs = lhs;
		this.rhs = rhs;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("AssignmentStatement").append('[');
		if (lhs == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < lhs.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(lhs[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (rhs == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < rhs.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(rhs[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the lhs of this assignment statement.
	 * 
	 * @return the lhs of this assignment statement.
	 */
	public LeftHandSide[] getLhs() {
		return lhs;
	}

	/**
	 * Gets the rhs of this assignment statement.
	 * 
	 * @return the rhs of this assignment statement.
	 */
	public Expression[] getRhs() {
		return rhs;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(lhs);
		children.add(rhs);
		return children;
	}
}
