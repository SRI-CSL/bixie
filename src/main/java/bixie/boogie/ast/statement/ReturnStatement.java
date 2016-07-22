
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a return statement which is a special form of a statement.
 */
public class ReturnStatement extends Statement {
	
	private Expression expression;
	
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
	public ReturnStatement(ILocation loc) {
		super(loc);
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param e
	 * 			  the return expression. NOTE: this can only be used in code-expression. 
	 *            Regular boogie code never returns an expression but uses out-variables instead!
	 */
	public ReturnStatement(ILocation loc, Expression e) {
		super(loc);
		this.expression = e;
	}
	
	public Expression getExpression() {
		return this.expression;
	}
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		return "ReturnStatement";
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		return children;
	}
}
