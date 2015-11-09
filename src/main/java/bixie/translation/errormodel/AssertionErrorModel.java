/**
 * 
 */
package bixie.translation.errormodel;

import bixie.util.Log;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.statement.Statement;
import bixie.translation.soot.SootProcedureInfo;
import bixie.translation.soot.SootStmtSwitch;
import bixie.translation.soot.TranslationHelpers;
import soot.SootClass;

/**
 * @author schaef
 *
 */
public class AssertionErrorModel extends AbstractErrorModel {

	/**
	 * @param pinfo
	 * @param stmtswitch
	 */
	public AssertionErrorModel(SootProcedureInfo pinfo,
			SootStmtSwitch stmtswitch) {
		super(pinfo, stmtswitch);
	}
	
	
	public void createdExpectedException(Expression guard, SootClass exception) {
		//TODO:
		createdUnExpectedException(guard, exception);
	}
	
	public void createdUnExpectedException(Expression guard, SootClass exception) {
		Attribute[] attributes = TranslationHelpers.javaLocation2Attribute(this.stmtSwitch.getCurrentStatement());
		Statement assertion;
		if (guard!=null) {
			//assertion = this.pf.mkAssertStatement(loc,this.pf.mkUnaryExpression(loc, guard.getType(), UnaryOperator.LOGICNEG, guard));
			assertion = this.pf.mkAssertStatement(attributes,guard);
		} else {
			//assertion = this.pf.mkAssertStatement(loc,this.pf.mkBooleanLiteral(loc, false));
			//TODO:
			Log.error("unguarded exception " + exception);
			assertion = this.pf.mkReturnStatement();
		}		
		this.stmtSwitch.addStatement(assertion);		
	}
	
	

}
