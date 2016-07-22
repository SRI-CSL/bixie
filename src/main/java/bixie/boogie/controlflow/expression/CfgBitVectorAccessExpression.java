
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgBitVectorAccessExpression extends CfgExpression {

	private CfgExpression bitvector;
	private int start, end;

	public CfgBitVectorAccessExpression(ILocation loc, BoogieType type,
			CfgExpression bvexpression, int start, int end) {
		super(loc, type);
		this.bitvector = bvexpression;
		this.start = start;
		this.end = end;
	}

	/**
	 * @return the bitvector
	 */
	public CfgExpression getBitvector() {
		return bitvector;
	}

	/**
	 * @param bitvector
	 *            the bitvector to set
	 */
	public void setBitvector(CfgExpression bitvector) {
		this.bitvector = bitvector;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//TODO
		return sb.toString();
	}

	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {		
		return new CfgBitVectorAccessExpression(this.getLocation(), this.getType(), this.bitvector.clone(), this.start, this.end);
	}
	
	
}
