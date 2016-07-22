

package bixie.boogie.ast.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * This can be used as call forall parameter, or as if or while condition. In
 * all other places it is forbidden.
 */
public class WildcardExpression extends Expression {
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
	public WildcardExpression(ILocation loc) {
		super(loc);
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 */
	public WildcardExpression(ILocation loc, BoogieType type) {
		super(loc, type);
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		return "WildcardExpression";
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {		
		return new WildcardExpression(this.getLocation(), this.type);
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		return new HashSet<IdentifierExpression>();
	}

}
