
package bixie.boogie.ast.declaration;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.Body;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.ast.specification.Specification;

/**
 * Represents a procedure or an implementation.
 */
public class ProcedureDeclaration extends ProcedureOrImplementationDeclaration {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	
	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            the attributes of this declaration.
	 * @param identifier
	 *            the identifier of this procedure.
	 * @param typeParams
	 *            the type params of this procedure.
	 * @param inParams
	 *            the in params of this procedure.
	 * @param outParams
	 *            the out params of this procedure.
	 * @param specification
	 *            the specification.
	 * @param body
	 *            the body.
	 */
	public ProcedureDeclaration(ILocation loc, Attribute[] attributes, String identifier,
			String[] typeParams, VarList[] inParams, VarList[] outParams,
			Specification[] specification, Body body) {
		super(loc, attributes);
		this.identifier = identifier;
		this.typeParams = typeParams;
		this.inParams = inParams;
		this.outParams = outParams;
		this.specification = specification;
		this.body = body;
	}	
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Procedure").append('[');
		sb.append(identifier);
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
		if (inParams == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < inParams.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(inParams[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (outParams == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < outParams.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(outParams[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (specification == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < specification.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(specification[i1]);
			}
			sb.append(']');
		}
		sb.append(',').append(body);
		return sb.append(']').toString();
	}


}
