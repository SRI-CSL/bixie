
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a assume statement which is a special form of a statement.
 */
public class AssumeStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The formula of this assume statement.
	 */
	Expression formula;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param formula
	 *            the formula of this assume statement.
	 */
	public AssumeStatement(ILocation loc, Expression formula) {
		super(loc);
		this.formula = formula;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            attributes
	 * @param formula
	 *            the formula of this assert statement.
	 */
	public AssumeStatement(ILocation loc, Attribute[] attributes, Expression formula) {
		super(loc, attributes);
		this.formula = formula;
	}
	
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("AssumeStatement").append('[');
		sb.append(formula);
		return sb.append(']').toString();
	}

	/**
	 * Gets the formula of this assume statement.
	 * 
	 * @return the formula of this assume statement.
	 */
	public Expression getFormula() {
		return formula;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(formula);
		return children;
	}
}
