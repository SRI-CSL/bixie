
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgIntegerLiteral extends CfgExpression {

	private Long value;

	public CfgIntegerLiteral(ILocation loc, BoogieType type, Long value) {
		super(loc, type);
		this.value = value;
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.value);
		return sb.toString();
	}

	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		return new CfgIntegerLiteral(this.getLocation(), this.getType(), this.value);
	}
	
}
