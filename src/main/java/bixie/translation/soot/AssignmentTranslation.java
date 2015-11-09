package bixie.translation.soot;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.expression.ArrayAccessExpression;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.translation.GlobalsCache;
import bixie.util.Log;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StringConstant;

public class AssignmentTranslation {

	public static void translateAssignment(SootStmtSwitch ss, Value lhs,
			Value rhs, Unit statement) {

		ProgramFactory pf = GlobalsCache.v().getPf();
		SootValueSwitch valueswitch = ss.getValueSwitch();

		if (rhs instanceof InvokeExpr) {
			InvokeExpr ivk = (InvokeExpr) rhs;
			InvokeTranslation
					.translateInvokeAssignment(ss, lhs, ivk, statement);
			return;
		}
		valueswitch.isLeftHandSide = true;
		lhs.apply(valueswitch);
		valueswitch.isLeftHandSide = false;
		Expression left = valueswitch.getExpression();

		Expression right;
		if (rhs instanceof NewExpr) {
			right = ss.createAllocatedVariable(((NewExpr) rhs).getBaseType());
		} else if (rhs instanceof NewArrayExpr) {
			NewArrayExpr nae = (NewArrayExpr) rhs;
			right = ss.createAllocatedVariable(nae.getType());
			nae.getSize().apply(valueswitch);
			Expression sizeexp = valueswitch.getExpression();
			// add the size expression.
			ss.addStatement(GlobalsCache.v().setArraySizeStatement(right,
					sizeexp));
		} else if (rhs instanceof NewMultiArrayExpr) {
			NewMultiArrayExpr nmae = (NewMultiArrayExpr) rhs;
			for (int i = 0; i < nmae.getSizeCount(); i++) {
				nmae.getSize(i).apply(valueswitch);
				// Expression sizeexp = valueswitch.getExpression();
				// TODO
				Log.debug("Mulit-arrays are not implemented!");
			}
			right = GlobalsCache.v().makeFreshGlobal(
					SootPrelude.v().getReferenceType(), true, true);
		} else if (rhs instanceof StringConstant) {
			StringConstant str = (StringConstant) rhs;

			// right = stringConstantMap.get(str);
			right = ss.createAllocatedVariable(rhs.getType());

			Expression[] indices = { right };
			// assign the size of the string to the appropriate field in the
			// $stringSizeHeapVariable array.
			translateAssignment(
					ss,
					SootPrelude.v().getStringSizeHeapVariable(),
					pf.mkArrayStoreExpression(SootPrelude.v()
							.getStringSizeHeapVariable().getType(), SootPrelude
							.v().getStringSizeHeapVariable(), indices, pf
							.mkIntLiteral(Integer.toString(str.value.length()))));
		} else {
			rhs.apply(valueswitch);
			right = valueswitch.getExpression();
		}

		translateAssignment(ss, left, right);
	}

	/**
	 * This method creates an assignment. It is used by caseAssignStmt and
	 * caseIdentityStmt
	 * 
	 * @param loc
	 * @param left
	 * @param right
	 */
	public static void translateAssignment(SootStmtSwitch ss, Expression left,
			Expression right) {
		ProgramFactory pf = GlobalsCache.v().getPf();
		if (left instanceof IdentifierExpression) {
			ss.addStatement(pf.mkAssignmentStatement(
					(IdentifierExpression) left,
					TranslationHelpers.castBoogieTypes(right, left.getType())));
		} else if (left instanceof ArrayAccessExpression) {
			ArrayAccessExpression aae = (ArrayAccessExpression) left;
			Expression arraystore = pf.mkArrayStoreExpression(aae.getArray()
					.getType(), aae.getArray(), aae.getIndices(), right);
			translateAssignment(ss, aae.getArray(), arraystore);
		} else {
			throw new RuntimeException("Unknown LHS type: "
					+ ((left == null) ? "null" : left.getClass()));
		}
	}

}
