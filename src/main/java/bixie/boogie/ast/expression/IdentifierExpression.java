

package bixie.boogie.ast.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a identifier expression which is a special form of a expression.
 */
public class IdentifierExpression extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifier of this identifier expression.
	 */
	String identifier;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param identifier
	 *            the identifier of this identifier expression.
	 */
	public IdentifierExpression(ILocation loc, String identifier) {
		super(loc);
		this.identifier = identifier;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param identifier
	 *            the identifier of this identifier expression.
	 */
	public IdentifierExpression(ILocation loc, BoogieType type,
			String identifier) {
		super(loc, type);
		this.identifier = identifier;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("IdentifierExpression").append('[');
		sb.append(identifier);
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifier of this identifier expression.
	 * 
	 * @return the identifier of this identifier expression.
	 */
	public String getIdentifier() {
		return identifier;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifier);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		if (s!=null && s.containsKey(this.identifier)) {
			return s.get(this.identifier).clone();
		}
		return new IdentifierExpression(this.getLocation(), this.type, this.identifier);
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		ret.add(this);
		return ret;
	}
	
}
