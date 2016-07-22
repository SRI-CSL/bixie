
package bixie.boogie.ast.specification;

import java.util.List;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a requires specification which is a special form of a
 * specification.
 */
public class RequiresSpecification extends Specification {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The formula of this requires specification.
	 */
	Expression formula;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param isFree
	 *            true iff this specification is free.
	 * @param formula
	 *            the formula of this requires specification.
	 */
	public RequiresSpecification(ILocation loc, boolean isFree,
			Expression formula) {
		super(loc, isFree);
		this.formula = formula;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 * 			  attributes           
	 * @param isFree
	 *            true iff this specification is free.
	 * @param formula
	 *            the formula of this ensures specification.
	 */
	public RequiresSpecification(ILocation loc, Attribute[] attributes, boolean isFree,
			Expression formula) {
		super(loc, attributes, isFree);
		this.formula = formula;
	}
		
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("RequiresSpecification").append('[');
		sb.append(formula);
		return sb.append(']').toString();
	}

	/**
	 * Gets the formula of this requires specification.
	 * 
	 * @return the formula of this requires specification.
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
