
package bixie.boogie.ast;

import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a trigger which is a special form of a attribute.
 */
public class Trigger extends Attribute {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The triggers of this trigger.
	 */
	Expression[] triggers;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param triggers
	 *            the triggers of this trigger.
	 */
	public Trigger(ILocation loc, Expression[] triggers) {
		super(loc);
		this.triggers = triggers;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Trigger").append('[');
		if (triggers == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < triggers.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(triggers[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the triggers of this trigger.
	 * 
	 * @return the triggers of this trigger.
	 */
	public Expression[] getTriggers() {
		return triggers;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(triggers);
		return children;
	}
}
