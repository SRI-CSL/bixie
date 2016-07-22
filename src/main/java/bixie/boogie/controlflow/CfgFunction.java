

package bixie.boogie.controlflow;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.expression.CfgExpression;

/**
 * @author schaef
 * 
 */
public class CfgFunction {

	private ILocation location;
	private String identifier;
	private CfgExpression body;

	private CfgVariable[] inParams = null;
	private CfgVariable outParam = null;

	public CfgFunction(String identifier, CfgExpression body) {
		this.identifier = identifier;
		this.body = body;
	}

	public CfgFunction(String identifier) {
		this.identifier = identifier;
		this.body = null;
	}

	/**
	 * @return the location
	 */
	public ILocation getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(ILocation location) {
		this.location = location;
	}

	/**
	 * @return the name
	 */
	public String getIndentifier() {
		return identifier;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setIndentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the body
	 */
	public CfgExpression getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(CfgExpression body) {
		this.body = body;
	}

	/**
	 * @return the inParams
	 */
	public CfgVariable[] getInParams() {
		return inParams;
	}

	/**
	 * @param inParams
	 *            the inParams to set
	 */
	public void setInParams(CfgVariable[] inParams) {
		this.inParams = inParams;
	}

	/**
	 * @return the outParams
	 */
	public CfgVariable getOutParam() {
		return outParam;
	}

	/**
	 * @param outParams
	 *            the outParams to set
	 */
	public void setOutParam(CfgVariable outParam) {
		this.outParam = outParam;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("procedure "+ this.identifier + "(");
		String prefix = "";
		for (CfgVariable v : this.inParams) {
			sb.append(prefix);
			prefix = ", ";
			sb.append(v.getVarname());
			sb.append(" : ");
			sb.append(v.getType());
		}
		sb.append(") ");

		sb.append(" returns (");
		prefix = "";
		sb.append(this.outParam.getVarname());
		sb.append(" : ");
		sb.append(this.outParam.getType());		
		sb.append(")");
		
		if (this.body!=null) {
			sb.append("{ \n");
			sb.append("\t"+ this.body);
			sb.append("}\n");
		}
		
		sb.append("\n");
		
		

		return sb.toString();
	}
	
}
