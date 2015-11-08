/**
 * 
 */
package bixie.boogie.ast.declaration;

import java.util.List;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.Body;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.ast.specification.Specification;

/**
 * @author schaef
 *
 */
public abstract class ProcedureOrImplementationDeclaration extends Declaration {

	/**
	 * The identifier of this procedure.
	 */
	protected String identifier;

	/**
	 * The type params of this procedure.
	 */
	protected String[] typeParams;

	/**
	 * The in params of this procedure.
	 */
	protected VarList[] inParams;

	/**
	 * The out params of this procedure.
	 */
	protected VarList[] outParams;

	/**
	 * The specification. It is null for an implementation and != null (but its
	 * length may be 0) for a procedure.
	 */
	protected Specification[] specification;

	/**
	 * The body. If this is an implementation (getSpecification() returns null)
	 * this must be present, otherwise it is optional.
	 */
	protected Body body;
	
	
	public ProcedureOrImplementationDeclaration(ILocation loc,
			Attribute[] attributes) {
		super(loc, attributes);

	}

	/**
	 * Gets the identifier of this procedure.
	 * 
	 * @return the identifier of this procedure.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the type params of this procedure.
	 * 
	 * @return the type params of this procedure.
	 */
	public String[] getTypeParams() {
		return typeParams;
	}

	/**
	 * Gets the in params of this procedure.
	 * 
	 * @return the in params of this procedure.
	 */
	public VarList[] getInParams() {
		return inParams;
	}

	/**
	 * Gets the out params of this procedure.
	 * 
	 * @return the out params of this procedure.
	 */
	public VarList[] getOutParams() {
		return outParams;
	}

	/**
	 * Gets the specification. It is null for an implementation and != null (but
	 * its length may be 0) for a procedure.
	 * 
	 * @return the specification.
	 */
	public Specification[] getSpecification() {
		return specification;
	}

	/**
	 * Gets the body. If this is an implementation (getSpecification() returns
	 * null) this must be present, otherwise it is optional.
	 * 
	 * @return the body.
	 */
	public Body getBody() {
		return body;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifier);
		children.add(typeParams);
		children.add(inParams);
		children.add(outParams);
		children.add(specification);
		children.add(body);
		return children;
	}	
	
}
