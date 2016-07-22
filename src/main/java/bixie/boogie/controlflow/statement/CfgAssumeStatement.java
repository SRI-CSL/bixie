
package bixie.boogie.controlflow.statement;


import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.expression.CfgExpression;

/**
 * @author schaef
 * 
 */
public class CfgAssumeStatement extends CfgStatement {

	private CfgExpression condition;

	public CfgAssumeStatement(ILocation loc, Attribute[] attributes, CfgExpression cond) {
		super(loc, attributes);
		this.condition = cond;
	}
	
	
	public CfgAssumeStatement(ILocation loc, CfgExpression cond) {
		super(loc);
		this.condition = cond;
	}

	/**
	 * @return the condition
	 */
	public CfgExpression getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(CfgExpression condition) {
		this.condition = condition;
	}
	@Override
	public String toString() {		
		StringBuilder sb = new StringBuilder();
		sb.append("assume (");
//		BoogiePrinter bp = new BoogiePrinter(null);
//		bp.appendAttributes(sb, getAttributes());
		sb.append("(");
		sb.append(this.condition.toString());
		sb.append(")");
		return sb.toString();
	}

	public CfgStatement duplicate() {		
		if (this.getAttributes()!=null) {			
			return new CfgAssumeStatement(this.getLocation(), this.getAttributes().clone(), this.condition.clone());
		}
		
		return new CfgAssumeStatement(this.getLocation(), this.condition.clone());
	}

}
