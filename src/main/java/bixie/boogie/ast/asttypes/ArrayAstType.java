
package bixie.boogie.ast.asttypes;

import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a array type which is a special form of a a s t type.
 */
public class ArrayAstType extends ASTType {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The type params of this array type.
	 */
	String[] typeParams;

	/**
	 * The index types of this array type.
	 */
	ASTType[] indexTypes;

	/**
	 * The value type of this array type.
	 */
	ASTType valueType;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param typeParams
	 *            the type params of this array type.
	 * @param indexTypes
	 *            the index types of this array type.
	 * @param valueType
	 *            the value type of this array type.
	 */
	public ArrayAstType(ILocation loc, String[] typeParams,
			ASTType[] indexTypes, ASTType valueType) {
		super(loc);
		this.typeParams = typeParams;
		this.indexTypes = indexTypes;
		this.valueType = valueType;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param boogieType
	 *            the boogie type of this a s t type.
	 * @param typeParams
	 *            the type params of this array type.
	 * @param indexTypes
	 *            the index types of this array type.
	 * @param valueType
	 *            the value type of this array type.
	 */
	public ArrayAstType(ILocation loc, BoogieType boogieType,
			String[] typeParams, ASTType[] indexTypes, ASTType valueType) {
		super(loc, boogieType);
		this.typeParams = typeParams;
		this.indexTypes = indexTypes;
		this.valueType = valueType;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ArrayType").append('[');
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
		if (indexTypes == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < indexTypes.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(indexTypes[i1]);
			}
			sb.append(']');
		}
		sb.append(',').append(valueType);
		return sb.append(']').toString();
	}

	/**
	 * Gets the type params of this array type.
	 * 
	 * @return the type params of this array type.
	 */
	public String[] getTypeParams() {
		return typeParams;
	}

	/**
	 * Gets the index types of this array type.
	 * 
	 * @return the index types of this array type.
	 */
	public ASTType[] getIndexTypes() {
		return indexTypes;
	}

	/**
	 * Gets the value type of this array type.
	 * 
	 * @return the value type of this array type.
	 */
	public ASTType getValueType() {
		return valueType;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(typeParams);
		children.add(indexTypes);
		children.add(valueType);
		return children;
	}
}
