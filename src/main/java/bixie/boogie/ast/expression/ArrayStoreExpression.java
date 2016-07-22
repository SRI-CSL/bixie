

package bixie.boogie.ast.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a array store expression which is a special form of a expression.
 */
public class ArrayStoreExpression extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The array of this array store expression.
	 */
	Expression array;

	/**
	 * The indices of this array store expression.
	 */
	Expression[] indices;

	/**
	 * The value of this array store expression.
	 */
	Expression value;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param array
	 *            the array of this array store expression.
	 * @param indices
	 *            the indices of this array store expression.
	 * @param value
	 *            the value of this array store expression.
	 */
	public ArrayStoreExpression(ILocation loc, Expression array,
			Expression[] indices, Expression value) {
		super(loc);
		this.array = array;
		this.indices = indices;
		this.value = value;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param array
	 *            the array of this array store expression.
	 * @param indices
	 *            the indices of this array store expression.
	 * @param value
	 *            the value of this array store expression.
	 */
	public ArrayStoreExpression(ILocation loc, BoogieType type,
			Expression array, Expression[] indices, Expression value) {
		super(loc, type);
		this.array = array;
		this.indices = indices;
		this.value = value;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ArrayStoreExpression").append('[');
		sb.append(array);
		sb.append(',');
		if (indices == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < indices.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(indices[i1]);
			}
			sb.append(']');
		}
		sb.append(',').append(value);
		return sb.append(']').toString();
	}

	/**
	 * Gets the array of this array store expression.
	 * 
	 * @return the array of this array store expression.
	 */
	public Expression getArray() {
		return array;
	}

	/**
	 * Gets the indices of this array store expression.
	 * 
	 * @return the indices of this array store expression.
	 */
	public Expression[] getIndices() {
		return indices;
	}

	/**
	 * Gets the value of this array store expression.
	 * 
	 * @return the value of this array store expression.
	 */
	public Expression getValue() {
		return value;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(array);
		children.add(indices);
		children.add(value);
		return children;
	}
	
	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		Expression[] cidx = new Expression[this.indices.length];
		for (int i=0; i<this.indices.length; i++) {
			cidx[i] = this.indices[i].substitute(s);
		}
		return new ArrayStoreExpression(this.getLocation(), 
				this.getType(), this.array.substitute(s), cidx, this.value.substitute(s));
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		for (int i=0; i<this.indices.length; i++) {
			ret.addAll(this.indices[i].getFreeVariables());
		}
		ret.addAll(this.array.getFreeVariables());
		ret.addAll(this.value.getFreeVariables());
		return ret;
	}
	
}
