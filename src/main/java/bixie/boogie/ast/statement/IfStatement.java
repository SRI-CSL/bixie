
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a if statement which is a special form of a statement.
 */
public class IfStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The condition of this if statement.
	 */
	Expression condition;

	/**
	 * The then part of this if statement.
	 */
	Statement[] thenPart;

	/**
	 * The else part of this if statement.
	 */
	Statement[] elsePart;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param condition
	 *            the condition of this if statement.
	 * @param thenPart
	 *            the then part of this if statement.
	 * @param elsePart
	 *            the else part of this if statement.
	 */
	public IfStatement(ILocation loc, Expression condition,
			Statement[] thenPart, Statement[] elsePart) {
		super(loc);
		this.condition = condition;
		this.thenPart = thenPart;
		this.elsePart = elsePart;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("IfStatement").append('[');
		sb.append(condition);
		sb.append(',');
		if (thenPart == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < thenPart.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(thenPart[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (elsePart == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < elsePart.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(elsePart[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the condition of this if statement.
	 * 
	 * @return the condition of this if statement.
	 */
	public Expression getCondition() {
		return condition;
	}

	/**
	 * Gets the then part of this if statement.
	 * 
	 * @return the then part of this if statement.
	 */
	public Statement[] getThenPart() {
		return thenPart;
	}

	/**
	 * Gets the else part of this if statement.
	 * 
	 * @return the else part of this if statement.
	 */
	public Statement[] getElsePart() {
		return elsePart;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(condition);
		children.add(thenPart);
		children.add(elsePart);
		return children;
	}
}
