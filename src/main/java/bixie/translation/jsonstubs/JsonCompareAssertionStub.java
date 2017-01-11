/**
 * 
 */
package bixie.translation.jsonstubs;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.enums.BinaryOperator;
import bixie.boogie.enums.UnaryOperator;
import bixie.boogie.type.BoogieType;
import bixie.translation.GlobalsCache;
import bixie.translation.soot.SootStmtSwitch;
import bixie.translation.soot.TranslationHelpers;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * @author schaef
 * Class that describes a custom assertion method, such as
 * com.google.common.base.Verify.verify(boolean, String, Object...)
 * and which parameter represents the condition.
 */
public class JsonCompareAssertionStub  implements IStub {

	public String methodSignature;
	public int lpos, rpos;
	public boolean guardNegated;
	JsonStubDescriptor descriptor;
	BinaryOperator bop;
	
	/**
	 */
	public JsonCompareAssertionStub(JsonStubDescriptor descr) {
		this.descriptor = descr; 
		this.methodSignature = descr.methodSignature;
		this.lpos = descr.param1;
		this.rpos = descr.param2;
		this.guardNegated = descr.guardNegated;
		this.bop = descr.be;
	}

	@Override
	public boolean tryApply(SootStmtSwitch ss, Value lhs, InvokeExpr ivk) {
		SootMethod sm = ivk.getMethod();
		if (this.methodSignature.equals(sm.getSignature()) && Math.max(lpos, rpos)<ivk.getArgCount()) {
			ivk.getArg(this.lpos).apply(ss.getValueSwitch());
			Expression left = ss.getValueSwitch().getExpression();
			ivk.getArg(this.rpos).apply(ss.getValueSwitch());
			Expression right = ss.getValueSwitch().getExpression();
			
			ProgramFactory pf = GlobalsCache.v().getPf();
			Expression guard = pf.mkBinaryExpression(BoogieType.boolType, bop, left, right);
			if (this.guardNegated) {
				guard = pf.mkUnaryExpression(BoogieType.boolType, UnaryOperator.LOGICNEG, guard);
			}	
			Attribute[] attributes = TranslationHelpers.javaLocation2Attribute(ss.getCurrentStatement());
			ss.addStatement(pf.mkAssertStatement(attributes, guard));
			return true;
		}
 		return false;
	}

	@Override
	public JsonStubDescriptor getDescriptor() {
		return this.descriptor;
	}

}
