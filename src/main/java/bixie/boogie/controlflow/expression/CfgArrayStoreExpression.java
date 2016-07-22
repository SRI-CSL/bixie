
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgArrayStoreExpression extends CfgExpression {

	private CfgExpression baseExpression, valueExpression;
	private CfgExpression[] indices;

	public CfgArrayStoreExpression(ILocation loc, BoogieType type,
			CfgExpression base, CfgExpression[] indices, CfgExpression value) {
		super(loc, type);
		// TODO Auto-generated constructor stub
		this.baseExpression = base;
		this.valueExpression = value;
		this.indices = indices;
	}

	/**
	 * @return the baseExpression
	 */
	public CfgExpression getBaseExpression() {
		return baseExpression;
	}

	/**
	 * @param baseExpression
	 *            the baseExpression to set
	 */
	public void setBaseExpression(CfgExpression baseExpression) {
		this.baseExpression = baseExpression;
	}

	/**
	 * @return the valueExpression
	 */
	public CfgExpression getValueExpression() {
		return valueExpression;
	}

	/**
	 * @param valueExpression
	 *            the valueExpression to set
	 */
	public void setValueExpression(CfgExpression valueExpression) {
		this.valueExpression = valueExpression;
	}

	/**
	 * @return the indices
	 */
	public CfgExpression[] getIndices() {
		return indices;
	}

	/**
	 * @param indices
	 *            the indices to set
	 */
	public void setIndices(CfgExpression[] indices) {
		this.indices = indices;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("store(");
		sb.append(baseExpression.toString()+ ", ");
		for (int i=0; i<this.indices.length;i++) {
			sb.append(this.indices[i].toString() + ", ");
		}
		sb.append(this.valueExpression.toString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		CfgExpression[] cloneindices = new CfgExpression[this.indices.length];
		for (int i=0; i<this.indices.length;i++) {
			cloneindices[i] = this.indices[i].substitute(substitutes);
		}
		CfgExpression clonebase = this.baseExpression.substitute(substitutes);		
		CfgExpression clonevalue = this.valueExpression.substitute(substitutes);
		return new CfgArrayStoreExpression(this.getLocation(), this.getType(), clonebase, cloneindices, clonevalue);
	}
	
}
