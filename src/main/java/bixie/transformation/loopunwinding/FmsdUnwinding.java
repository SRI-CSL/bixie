package bixie.transformation.loopunwinding;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import util.Log;
import boogie.controlflow.BasicBlock;
import boogie.controlflow.CfgProcedure;
import boogie.controlflow.CfgVariable;
import boogie.controlflow.expression.CfgExpression;
import boogie.controlflow.expression.CfgIdentifierExpression;
import boogie.controlflow.statement.CfgAssignStatement;
import boogie.controlflow.statement.CfgCallStatement;
import boogie.controlflow.statement.CfgHavocStatement;
import boogie.controlflow.statement.CfgStatement;
import boogie.controlflow.util.LoopDetection;
import boogie.controlflow.util.LoopInfo;

/**
 * @author schaef
 */
public class FmsdUnwinding extends AbstractLoopUnwinding {

	/**
	 * C-tor
	 * 
	 * @param proc
	 *            Boogie procedure
	 */
	public FmsdUnwinding(CfgProcedure proc) {
		super(proc);
		this.proc = proc;
		this.maxUnwinding=1;
		this.dontVerifyClones=true;
	}

	@Override
	public void unwind() {
		BasicBlock root = proc.getRootNode();
		LoopDetection detection = new LoopDetection();
		
		List<LoopInfo> loops = detection.computeLoops(root);
		
		for (LoopInfo loop : loops) {
			abstractUnwinding(loop);
		}
		
	}

	private void abstractUnwinding(LoopInfo loop) {
		// havoc the nested loops first
		this.maxUnwinding=0;
		for (LoopInfo nest : new LinkedList<LoopInfo>(loop.nestedLoops)) {
			loop.nestedLoopHeads.remove(nest.loopHead);
			loop.nestedLoops.remove(nest);
			havocLoop(nest);
		}
		loop.refreshLoopBody(); //TODO: test
		loop.UpdateLoopEntries();

		//TOOD: recompute the loop info because the body has changed		
		this.maxUnwinding=1;
		
		for (BasicBlock b : loop.loopEntries) {			
			b.addStatement(computeHavocStatement(loop), false);	
		}
		
		unwind(loop,this.maxUnwinding);
	}
	
	private void havocLoop(LoopInfo loop) {
		// havoc the nested loops first
		for (LoopInfo nest : new LinkedList<LoopInfo>(loop.nestedLoops)) {
			loop.nestedLoopHeads.remove(nest.loopHead);
			loop.nestedLoops.remove(nest);
			havocLoop(nest);
		}
		loop.refreshLoopBody(); //TODO: test
		
		loop.loopHead.addStatement(computeHavocStatement(loop), true);
		
		for (BasicBlock b : loop.loopExit) {
			b.addStatement(computeHavocStatement(loop), true);
		}
		
		if (loop.loopExit.size()==0 && bixie.Options.v().getDebugMode()) {
			Log.debug("Loop has no exit! LoopHead "+loop.loopHead.getLabel());
		}
		
		unwind(loop,0);
		
	}	
	

	private CfgHavocStatement computeHavocStatement(LoopInfo l) {
		HashSet<CfgVariable> havocedVars = new HashSet<CfgVariable>();
		for (BasicBlock b : l.loopBody) {			
			for (CfgStatement s : b.getStatements()) {
				if (s instanceof CfgAssignStatement) {
					CfgAssignStatement ass = (CfgAssignStatement) s;
					for (CfgExpression e : ass.getLeft()) {
						CfgVariable v = expToCfgVariable(e);
						if (v != null)
							havocedVars.add(v);
					}
				} else if (s instanceof CfgHavocStatement) {
					CfgHavocStatement havoc = (CfgHavocStatement) s;
					for (CfgVariable v : havoc.getVariables()) {
						havocedVars.add(v);
					}
				} else if (s instanceof CfgCallStatement) {
					// CfgCallStatement ivk = (CfgCallStatement) s;
					throw new RuntimeException(
							"Call statements are assumed to be deleted before loop unwinding");
				}
			}
		}


		return new CfgHavocStatement(l.loopHead.getLocationTag(),
				havocedVars.toArray(new CfgVariable[havocedVars.size()]));
	}

	private CfgVariable expToCfgVariable(CfgExpression e) {
		if (e instanceof CfgIdentifierExpression) {
			return ((CfgIdentifierExpression) e).getVariable();
		} else {
			throw new RuntimeException("Don't know what to do with " + e);
		}
	}

}
