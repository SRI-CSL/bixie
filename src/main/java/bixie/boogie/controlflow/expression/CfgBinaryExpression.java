
package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.enums.BinaryOperator;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgBinaryExpression extends CfgExpression {

	private BinaryOperator operator;
	private CfgExpression leftOp, rightOp;

	public CfgBinaryExpression(ILocation loc, BoogieType type,
			BinaryOperator op, CfgExpression left, CfgExpression right) {
		super(loc, type);
		this.operator = op;
		this.leftOp = left;
		this.rightOp = right;
	}

	public BinaryOperator getOperator() {
		return operator;
	}

	public void setOperator(BinaryOperator operator) {
		this.operator = operator;
	}

	/**
	 * @return
	 */
	public CfgExpression getLeftOp() {
		return leftOp;
	}

	/**
	 * @param leftOp
	 */
	public void setLeftOp(CfgExpression leftOp) {
		this.leftOp = leftOp;
	}

	/**
	 * @return the rightOp
	 */
	public CfgExpression getRightOp() {
		return rightOp;
	}

	/**
	 * @param rightOp
	 *            the rightOp to set
	 */
	public void setRightOp(CfgExpression rightOp) {
		this.rightOp = rightOp;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(this.leftOp);
		if (this.operator == BinaryOperator.ARITHDIV) {
			sb.append(" / ");
		} else if (this.operator == BinaryOperator.ARITHMINUS) {
			sb.append(" - ");
		} else if (this.operator == BinaryOperator.ARITHMOD) {
			sb.append(" % ");
		} else if (this.operator == BinaryOperator.ARITHMUL) {
			sb.append(" * ");
		} else if (this.operator == BinaryOperator.ARITHPLUS) {
			sb.append(" + ");
		} else if (this.operator == BinaryOperator.BITVECCONCAT) {
			sb.append(" ? "); //TODO
		} else if (this.operator == BinaryOperator.COMPEQ) {
			sb.append(" == ");
		} else if (this.operator == BinaryOperator.COMPGEQ) {
			sb.append(" >= ");
		} else if (this.operator == BinaryOperator.COMPGT) {
			sb.append(" > ");
		} else if (this.operator == BinaryOperator.COMPLEQ) {
			sb.append(" <= ");
		} else if (this.operator == BinaryOperator.COMPLT) {
			sb.append(" < ");
		} else if (this.operator == BinaryOperator.COMPNEQ) {
			sb.append(" != ");
		} else if (this.operator == BinaryOperator.COMPPO) {
			sb.append(" <: ");
		} else if (this.operator == BinaryOperator.LOGICAND) {
			sb.append(" && ");
		} else if (this.operator == BinaryOperator.LOGICIFF) {
			sb.append(" <==> ");
		} else if (this.operator == BinaryOperator.LOGICIMPLIES) {
			sb.append(" ==> ");
		} else if (this.operator == BinaryOperator.LOGICOR) {
			sb.append(" || ");			
		} else {
			throw new RuntimeException("Unknown Operator");
		}
		sb.append(this.rightOp);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		CfgExpression cloneleft = this.leftOp.substitute(substitutes);
		CfgExpression cloneright = this.rightOp.substitute(substitutes);
		return new CfgBinaryExpression(this.getLocation(), this.getType(), this.operator, cloneleft, cloneright);
	}

}
