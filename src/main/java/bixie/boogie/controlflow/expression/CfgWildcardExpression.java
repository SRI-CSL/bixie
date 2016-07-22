
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgWildcardExpression extends CfgExpression {

	public CfgWildcardExpression(ILocation loc, BoogieType type) {
		super(loc, type);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {		
		//StringBuilder sb = new StringBuilder();
		throw new RuntimeException("wildcard not implemented");
		//return sb.toString();
	}

	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		throw new RuntimeException("clone/substitute not implemented");
	}
	
}
