
package bixie.boogie.ast;

import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a named attribute which is a special form of a attribute.
 */
public class NamedAttribute extends Attribute {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The name of this named attribute.
	 */
	String name;

	/**
	 * The values of this named attribute.
	 */
	Expression[] values;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param name
	 *            the name of this named attribute.
	 * @param values
	 *            the values of this named attribute.
	 */
	public NamedAttribute(ILocation loc, String name, Expression[] values) {
		super(loc);
		this.name = name;
		this.values = values;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("NamedAttribute").append('[');
		sb.append(name);
		sb.append(',');
		if (values == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < values.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(values[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the name of this named attribute.
	 * 
	 * @return the name of this named attribute.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the values of this named attribute.
	 * 
	 * @return the values of this named attribute.
	 */
	public Expression[] getValues() {
		return values;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(name);
		children.add(values);
		return children;
	}
}
