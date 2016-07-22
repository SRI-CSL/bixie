
package bixie.boogie.ast.specification;

import java.util.List;

import bixie.boogie.ast.location.ILocation;

/**
 * Represents a modifies specification which is a special form of a
 * specification.
 */
public class ModifiesSpecification extends Specification {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifiers of this modifies specification.
	 */
	String[] identifiers;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param isFree
	 *            true iff this specification is free.
	 * @param identifiers
	 *            the identifiers of this modifies specification.
	 */
	public ModifiesSpecification(ILocation loc, boolean isFree,
			String[] identifiers) {
		super(loc, isFree);
		this.identifiers = identifiers;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ModifiesSpecification").append('[');
		if (identifiers == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < identifiers.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(identifiers[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifiers of this modifies specification.
	 * 
	 * @return the identifiers of this modifies specification.
	 */
	public String[] getIdentifiers() {
		return identifiers;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifiers);
		return children;
	}
}
