

package bixie.boogie.ast.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.enums.BinaryOperator;
import bixie.boogie.type.BoogieType;

/**
 * Represents a binary expression which is a special form of a expression.
 */
public class BinaryExpression extends Expression {
	/**
	 * The operator of this binary expression.
	 */
	BinaryOperator operator;

	/**
	 * The left of this binary expression.
	 */
	Expression left;

	/**
	 * The right of this binary expression.
	 */
	Expression right;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param operator
	 *            the operator of this binary expression.
	 * @param left
	 *            the left of this binary expression.
	 * @param right
	 *            the right of this binary expression.
	 */
	public BinaryExpression(ILocation loc, BinaryOperator operator,
			Expression left, Expression right) {
		super(loc);
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param operator
	 *            the operator of this binary expression.
	 * @param left
	 *            the left of this binary expression.
	 * @param right
	 *            the right of this binary expression.
	 */
	public BinaryExpression(ILocation loc, BoogieType type,
			BinaryOperator operator, Expression left, Expression right) {
		super(loc, type);
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BinaryExpression").append('[');
		sb.append(operator);
		sb.append(',').append(left);
		sb.append(',').append(right);
		return sb.append(']').toString();
	}

	/**
	 * Gets the operator of this binary expression.
	 * 
	 * @return the operator of this binary expression.
	 */
	public BinaryOperator getOperator() {
		return operator;
	}

	/**
	 * Gets the left of this binary expression.
	 * 
	 * @return the left of this binary expression.
	 */
	public Expression getLeft() {
		return left;
	}

	/**
	 * Gets the right of this binary expression.
	 * 
	 * @return the right of this binary expression.
	 */
	public Expression getRight() {
		return right;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(operator);
		children.add(left);
		children.add(right);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		return new BinaryExpression(this.getLocation(), 
				this.getType(), 
				this.operator, 
				this.left.substitute(s), 
				this.right.substitute(s));
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		ret.addAll(this.left.getFreeVariables());
		ret.addAll(this.right.getFreeVariables());
		return ret;
	}
	
}
