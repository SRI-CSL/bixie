
package bixie.boogie.controlflow.statement;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;

/**
 * @author schaef
 * 
 */
public class CfgHavocStatement extends CfgStatement {

	private CfgVariable[] variables;
	private CfgStatement replacedStatement = null;
	
	
	public CfgHavocStatement(ILocation loc, CfgVariable[] vars) {
		super(loc);
		this.variables = vars;
	}

	/**
	 * @return the variables
	 */
	public CfgVariable[] getVariables() {
		return variables;
	}

	/**
	 * @param variables
	 *            the variables to set
	 */
	public void setVariables(CfgVariable[] variables) {
		this.variables = variables;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("havoc ");
		for (int i=0; i<this.variables.length; i++) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(this.variables[i].getVarname());
		}
		return sb.toString();
	}

	public CfgStatement duplicate() {
		CfgHavocStatement ret = new CfgHavocStatement(this.getLocation(), this.getVariables());
		ret.setReplacedStatement(this.getReplacedStatement());		
		return ret; 
	}

	/**
	 * @return the replacedStatement
	 */
	public CfgStatement getReplacedStatement() {
		return replacedStatement;
	}

	/**
	 * @param replacedStatement the replacedStatement to set
	 */
	public void setReplacedStatement(CfgStatement replacedStatement) {
		this.replacedStatement = replacedStatement;
	}

}
