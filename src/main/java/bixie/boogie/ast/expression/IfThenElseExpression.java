

package bixie.boogie.ast.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a if then else expression which is a special form of a expression.
 */
public class IfThenElseExpression extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The condition of this if then else expression.
	 */
	Expression condition;

	/**
	 * The then part of this if then else expression.
	 */
	Expression thenPart;

	/**
	 * The else part of this if then else expression.
	 */
	Expression elsePart;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param condition
	 *            the condition of this if then else expression.
	 * @param thenPart
	 *            the then part of this if then else expression.
	 * @param elsePart
	 *            the else part of this if then else expression.
	 */
	public IfThenElseExpression(ILocation loc, Expression condition,
			Expression thenPart, Expression elsePart) {
		super(loc);
		this.condition = condition;
		this.thenPart = thenPart;
		this.elsePart = elsePart;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param condition
	 *            the condition of this if then else expression.
	 * @param thenPart
	 *            the then part of this if then else expression.
	 * @param elsePart
	 *            the else part of this if then else expression.
	 */
	public IfThenElseExpression(ILocation loc, BoogieType type,
			Expression condition, Expression thenPart, Expression elsePart) {
		super(loc, type);
		this.condition = condition;
		this.thenPart = thenPart;
		this.elsePart = elsePart;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("IfThenElseExpression").append('[');
		sb.append(condition);
		sb.append(',').append(thenPart);
		sb.append(',').append(elsePart);
		return sb.append(']').toString();
	}

	/**
	 * Gets the condition of this if then else expression.
	 * 
	 * @return the condition of this if then else expression.
	 */
	public Expression getCondition() {
		return condition;
	}

	/**
	 * Gets the then part of this if then else expression.
	 * 
	 * @return the then part of this if then else expression.
	 */
	public Expression getThenPart() {
		return thenPart;
	}

	/**
	 * Gets the else part of this if then else expression.
	 * 
	 * @return the else part of this if then else expression.
	 */
	public Expression getElsePart() {
		return elsePart;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(condition);
		children.add(thenPart);
		children.add(elsePart);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		return new IfThenElseExpression(this.getLocation(), 
				this.getType(), 
				this.condition.substitute(s), 
				this.thenPart.substitute(s), 
				this.elsePart.substitute(s));
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		ret.addAll(this.condition.getFreeVariables());
		ret.addAll(this.thenPart.getFreeVariables());
		ret.addAll(this.elsePart.getFreeVariables());
		return ret;
	}
	
}
