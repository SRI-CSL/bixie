

package bixie.boogie.ast.expression.literal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a real literal which is a special form of a expression.
 */
public class RealLiteral extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The value given as String. This representation is used to support
	 * arbitrarily large numbers. We do not need to compute with them but give
	 * them 1-1 to the decision procedure.
	 */
	String value;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param value
	 *            the value given as String.
	 */
	public RealLiteral(ILocation loc, String value) {
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
	 *            the value given as String.
	 */
	public RealLiteral(ILocation loc, BoogieType type, String value) {
		super(loc, type);
		this.value = value;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("RealLiteral").append('[');
		sb.append(value);
		return sb.append(']').toString();
	}

	/**
	 * Gets the value given as String. This representation is used to support
	 * arbitrarily large numbers. We do not need to compute with them but give
	 * them 1-1 to the decision procedure.
	 * 
	 * @return the value given as String.
	 */
	public String getValue() {
		return value;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(value);
		return children;
	}
	
	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		return new RealLiteral(this.getLocation(), this.getType(), this.value);
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		return new HashSet<IdentifierExpression>();
	}
	
}
