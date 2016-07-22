
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgQuantifierExpression extends CfgExpression {
	
	private boolean isUniversal;
	private BoogieType[] typeParams;
	private CfgVariable[] parameters;
	private Attribute[] attributes; 
	private CfgExpression subformula;	

	/**
	 * @return the isUniversal
	 */
	public boolean isUniversal() {
		return isUniversal;
	}

	/**
	 * @return the typeParams
	 */
	public BoogieType[] getTypeParams() {
		return typeParams;
	}

	/**
	 * @return the parameters
	 */
	public CfgVariable[] getParameters() {
		return parameters;
	}

	/**
	 * @return the attributes
	 */
	public Attribute[] getAttributes() {
		return attributes;
	}

	/**
	 * @return the subformula
	 */
	public CfgExpression getSubformula() {
		return subformula;
	}

	public CfgQuantifierExpression(ILocation loc, BoogieType type, boolean isUniversal, 
			BoogieType[] typeParams, CfgVariable[] parameters,
			Attribute[] attributes, CfgExpression subformula) {
		super(loc, type);
		this.isUniversal=isUniversal;
		this.parameters=parameters;
		this.typeParams=typeParams;
		this.attributes=attributes;
		this.subformula=subformula;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.isUniversal) sb.append("forall");
		else sb.append("exists");
		//forall i:int :: $i2b(i) <==> i != 0);
		
		//TODO: attributes and type parameters are missing
		
		boolean first = true;
		for (CfgVariable v : this.parameters) {
			if (first) first=false;	else sb.append(", ");
			sb.append(v.getVarname());
			sb.append(":");
			sb.append(v.getType());			
		}
		first = true;
		
		sb.append(" :: ");
		
		sb.append(this.subformula);
		
		return sb.toString();
	}

	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		//TODO:
		HashMap<CfgVariable, CfgExpression> localcopy = new HashMap<CfgVariable, CfgExpression>(substitutes);
		//do not substitute variables that are quantified in the current scope.
		for (CfgVariable v : this.parameters) {
			localcopy.remove(v);
		}
		CfgExpression e = this.subformula.substitute(localcopy);
		return new CfgQuantifierExpression(this.getLocation(), this.getType(), this.isUniversal, this.typeParams, this.parameters, this.attributes, e);
	}
	
}
