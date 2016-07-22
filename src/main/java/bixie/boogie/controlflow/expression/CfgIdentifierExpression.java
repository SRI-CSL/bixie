
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.boogie.controlflow.CfgVariable;

/**
 * @author schaef
 * 
 */
public class CfgIdentifierExpression extends CfgExpression {

	private int currentIncarnation = 0;
	private CfgVariable variable = null;

	public CfgIdentifierExpression(ILocation loc, CfgVariable v) {
		super(loc, v.getType());
		this.variable = v;
	}

	public CfgIdentifierExpression(ILocation loc, CfgVariable v, int incarnation) {
		super(loc, v.getType());
		this.variable = v;
		this.currentIncarnation = incarnation;
	}

	public int getCurrentIncarnation() {
		return currentIncarnation;
	}

	public void setCurrentIncarnation(int currentIncarnation) {
		this.currentIncarnation = currentIncarnation;
	}

	public CfgVariable getVariable() {
		return variable;
	}

	public void setVariable(CfgVariable variable) {
		this.variable = variable;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.variable.getVarname());
		if (CfgProcedure.printSSA) {
			sb.append("__"+ this.getCurrentIncarnation());
		}
		 
		return sb.toString();
	}


	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		if (substitutes!=null && substitutes.containsKey(this.variable)) {
			return substitutes.get(this.variable).clone();
		}
		return new CfgIdentifierExpression(this.getLocation(), this.variable);
	}
		
}
