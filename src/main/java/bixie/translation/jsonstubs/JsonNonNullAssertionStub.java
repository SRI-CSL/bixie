/**
 * 
 */
package bixie.translation.jsonstubs;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.enums.BinaryOperator;
import bixie.boogie.type.BoogieType;
import bixie.translation.GlobalsCache;
import bixie.translation.soot.SootPrelude;
import bixie.translation.soot.SootStmtSwitch;
import bixie.translation.soot.TranslationHelpers;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * @author schaef
 * Class that describes a custom non-null assertion method, such as
 * com.google.common.base.Verify.verifyNonNull(Object, String, Object...)
 * and which parameter represents the condition.
 */
public class JsonNonNullAssertionStub  implements IStub {

	public String methodSignature;
	public int guardPosition;
	public boolean guardNegated;
	JsonStubDescriptor descriptor;
	
	public JsonNonNullAssertionStub(JsonStubDescriptor descr) {
		this.descriptor = descr;
		this.methodSignature = descr.methodSignature;
		this.guardPosition = descr.param1;
		this.guardNegated = descr.guardNegated;
	}

	@Override
	public boolean tryApply(SootStmtSwitch ss, Value lhs, InvokeExpr ivk) {
		SootMethod sm = ivk.getMethod();
		if (this.methodSignature.equals(sm.getSignature()) && this.guardPosition<ivk.getArgCount()) {
			ivk.getArg(this.guardPosition).apply(ss.getValueSwitch());
			ProgramFactory pf = GlobalsCache.v().getPf();
			BinaryOperator be = (this.guardNegated) ? BinaryOperator.COMPEQ : BinaryOperator.COMPNEQ;
			Expression guard =  pf.mkBinaryExpression(BoogieType.boolType, be, ss.getValueSwitch().getExpression(), SootPrelude.v().getNullConstant());
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
