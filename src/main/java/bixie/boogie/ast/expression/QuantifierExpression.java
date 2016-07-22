

package bixie.boogie.ast.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a quantifier expression which is a special form of a expression.
 */
public class QuantifierExpression extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * This is true for universal and false for existential quantifier.
	 */
	boolean isUniversal;

	/**
	 * The type params of this quantifier expression.
	 */
	String[] typeParams;

	/**
	 * The parameters of this quantifier expression.
	 */
	VarList[] parameters;

	/**
	 * The attributes of this quantifier expression.
	 */
	Attribute[] attributes;

	/**
	 * The subformula of this quantifier expression.
	 */
	Expression subformula;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param isUniversal
	 *            this is true for universal and false for existential
	 *            quantifier.
	 * @param typeParams
	 *            the type params of this quantifier expression.
	 * @param parameters
	 *            the parameters of this quantifier expression.
	 * @param attributes
	 *            the attributes of this quantifier expression.
	 * @param subformula
	 *            the subformula of this quantifier expression.
	 */
	public QuantifierExpression(ILocation loc, boolean isUniversal,
			String[] typeParams, VarList[] parameters, Attribute[] attributes,
			Expression subformula) {
		super(loc);
		this.isUniversal = isUniversal;
		this.typeParams = typeParams;
		this.parameters = parameters;
		this.attributes = attributes;
		this.subformula = subformula;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param isUniversal
	 *            this is true for universal and false for existential
	 *            quantifier.
	 * @param typeParams
	 *            the type params of this quantifier expression.
	 * @param parameters
	 *            the parameters of this quantifier expression.
	 * @param attributes
	 *            the attributes of this quantifier expression.
	 * @param subformula
	 *            the subformula of this quantifier expression.
	 */
	public QuantifierExpression(ILocation loc, BoogieType type,
			boolean isUniversal, String[] typeParams, VarList[] parameters,
			Attribute[] attributes, Expression subformula) {
		super(loc, type);
		this.isUniversal = isUniversal;
		this.typeParams = typeParams;
		this.parameters = parameters;
		this.attributes = attributes;
		this.subformula = subformula;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("QuantifierExpression").append('[');
		sb.append(isUniversal);
		sb.append(',');
		if (typeParams == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < typeParams.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(typeParams[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (parameters == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < parameters.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(parameters[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (attributes == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < attributes.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(attributes[i1]);
			}
			sb.append(']');
		}
		sb.append(',').append(subformula);
		return sb.append(']').toString();
	}

	/**
	 * Checks this is true for universal and false for existential quantifier.
	 * 
	 * @return this is true for universal and false for existential quantifier.
	 */
	public boolean isUniversal() {
		return isUniversal;
	}

	/**
	 * Gets the type params of this quantifier expression.
	 * 
	 * @return the type params of this quantifier expression.
	 */
	public String[] getTypeParams() {
		return typeParams;
	}

	/**
	 * Gets the parameters of this quantifier expression.
	 * 
	 * @return the parameters of this quantifier expression.
	 */
	public VarList[] getParameters() {
		return parameters;
	}

	/**
	 * Gets the attributes of this quantifier expression.
	 * 
	 * @return the attributes of this quantifier expression.
	 */
	public Attribute[] getAttributes() {
		return attributes;
	}

	/**
	 * Gets the subformula of this quantifier expression.
	 * 
	 * @return the subformula of this quantifier expression.
	 */
	public Expression getSubformula() {
		return subformula;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(isUniversal);
		children.add(typeParams);
		children.add(parameters);
		children.add(attributes);
		children.add(subformula);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		throw new RuntimeException("substitution/clone for quantifier not implemented");
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		ret.addAll(this.subformula.getFreeVariables());
		//now remove all variables that are bound by the quantifier.
		for (VarList vl : this.parameters) {
			for (String s : vl.getIdentifiers()) {
				IdentifierExpression found = null;
				for (IdentifierExpression id : ret) {
					if (id.getIdentifier().equals(s)) {
						found = id; break;
					}
				}
				if (found!=null) ret.remove(found);
			}
		}
		return ret;
	}
	
}
