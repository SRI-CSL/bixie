package bixie.translation.jsonstubs;

import bixie.translation.soot.SootStmtSwitch;
import soot.Value;
import soot.jimple.InvokeExpr;

public interface IStub {
	
	public boolean tryApply(SootStmtSwitch ss, Value lhs, InvokeExpr ivk);
	
	public JsonStubDescriptor getDescriptor();
}
