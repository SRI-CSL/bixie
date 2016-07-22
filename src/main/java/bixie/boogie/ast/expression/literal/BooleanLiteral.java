

package bixie.boogie.ast.expression.literal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a boolean literal which is a special form of a expression.
 */
public class BooleanLiteral extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The value of this boolean literal.
	 */
	boolean value;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param value
	 *            the value of this boolean literal.
	 */
	public BooleanLiteral(ILocation loc, boolean value) {
		super(loc);
		this.value = value;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param value
	 *            the value of this boolean literal.
	 */
	public BooleanLiteral(ILocation loc, BoogieType type, boolean value) {
		super(loc, type);
		this.value = value;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BooleanLiteral").append('[');
		sb.append(value);
		return sb.append(']').toString();
	}

	/**
	 * Checks the value of this boolean literal.
	 * 
	 * @return the value of this boolean literal.
	 */
	public boolean getValue() {
		return value;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(value);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		return new BooleanLiteral(this.getLocation(), this.getType(), this.value);
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		return new HashSet<IdentifierExpression>();
	}
	
}
