/*
 * jimple2boogie - Translates Jimple (or Java) Programs to Boogie
 * Copyright (C) 2013 Martin Schaef and Stephan Arlt
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package bixie.translation.soot;

import java.util.LinkedList;
import java.util.List;

import soot.ArrayType;
import soot.Local;
import soot.NullType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Trap;
import soot.Type;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import bixie.translation.GlobalsCache;
import bixie.translation.errormodel.AbstractErrorModel;
import bixie.translation.errormodel.AssertionErrorModel;
import bixie.util.Log;
import boogie.ProgramFactory;
import boogie.ast.Attribute;
import boogie.ast.expression.Expression;
import boogie.ast.expression.IdentifierExpression;
import boogie.ast.statement.Statement;
import boogie.enums.BinaryOperator;

/**
 * @author schaef
 */
public class SootStmtSwitch implements StmtSwitch {

	private SootProcedureInfo procInfo;
	private SootValueSwitch valueswitch;
	private ProgramFactory pf;
	private Stmt currentStatement = null; // needed to identify throw targets of
											// expressions
	private AbstractErrorModel errorModel;
	private boolean inMonitor = false;
	
	public SootStmtSwitch(SootProcedureInfo pinfo) {
		this.procInfo = pinfo;
		this.pf = GlobalsCache.v().getPf();
		this.valueswitch = new SootValueSwitch(this.procInfo, this);
		this.errorModel = new AssertionErrorModel(this.procInfo, this);
	}

	public SootProcedureInfo getProcInfo() {
		return this.procInfo;
	}

	public LinkedList<Statement> popAll() {
		LinkedList<Statement> ret = new LinkedList<Statement>();
		ret.addAll(this.boogieStatements);
		this.boogieStatements.clear();
		return ret;
	}

	public SootValueSwitch getValueSwitch() {
		return this.valueswitch;
	}

	public AbstractErrorModel getErrorModel() {
		return this.errorModel;
	}

	/**
	 * Returns true if we are in a monitor or if the whole method is
	 * synchronized.
	 * 
	 * @return
	 */
	public boolean isInMonitor() {
		return this.inMonitor || this.procInfo.getSootMethod().isSynchronized();
	}

	private LinkedList<Statement> boogieStatements = new LinkedList<Statement>();

	/**
	 * this should only be used by the SootValueSwitch if extra guards have to
	 * be created
	 * 
	 * @param guard
	 */
	public void addStatement(Statement guard) {
		this.boogieStatements.add(guard);
	}

	public Stmt getCurrentStatement() {
		return this.currentStatement;
	}

	private void injectLabelStatements(Stmt arg0) {
		this.currentStatement = arg0;
		if (arg0.getBoxesPointingToThis().size() > 0) {
			String label = GlobalsCache.v().getUnitLabel(arg0);

			this.boogieStatements.add(this.pf.mkLabel(label));
		}

		this.boogieStatements.add(TranslationHelpers.mkLocationAssertion(arg0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	@Override
	public void caseAssignStmt(AssignStmt arg0) {
		injectLabelStatements(arg0);
		AssignmentTranslation.translateAssignment(this, arg0.getLeftOp(),
				arg0.getRightOp(), arg0);
	}

	public IdentifierExpression createAllocatedVariable(Type sootType) {
		// create fresh local variable for "right"
		Attribute[] attributes = {};
		if (this.currentStatement != null) {
			TranslationHelpers.javaLocation2Attribute(this.currentStatement);
		}

		IdentifierExpression newexpr = this.procInfo
				.createLocalVariable(SootPrelude.v().getReferenceType());

		Expression obj_type;
		if (sootType instanceof RefType) {
			obj_type = GlobalsCache.v().lookupClassVariable(
					((RefType) sootType).getSootClass());
			if (obj_type == null) {
				throw new RuntimeException("Not a class variable: "
						+ ((RefType) sootType).getSootClass());
			}
		} else if (sootType instanceof ArrayType) {
			obj_type = GlobalsCache.v().lookupArrayType((ArrayType) sootType);
			if (obj_type == null) {
				throw new RuntimeException("Not a type: "
						+ (ArrayType) sootType);
			}
		} else {
			throw new RuntimeException("Translation of Array Access failed!");
		}

		this.boogieStatements.add(SootPrelude.v().newObject(attributes,
				newexpr, obj_type));

		return newexpr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
	 */
	@Override
	public void caseBreakpointStmt(BreakpointStmt arg0) {
		injectLabelStatements(arg0);
		Log.info("Joogie does not translate BreakpointStmt");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 * If this is only for synchronization, we don't need to translate it
	 */
	@Override
	public void caseEnterMonitorStmt(EnterMonitorStmt arg0) {
		injectLabelStatements(arg0);		
		arg0.getOp().apply(this.valueswitch);
		this.valueswitch.getExpression();

		this.inMonitor = true;
		// TODO: this is a very aggressive hack
		// to avoid false positives that we encountered in Tomcat.
		// For example: if (A) synchronized() { if (A) ...
		// havoc everything.
		this.boogieStatements.add(TranslationHelpers.havocEverything(this.getProcInfo(), this.valueswitch));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 * If this is only for synchronization, we don't need to translate it
	 */
	@Override
	public void caseExitMonitorStmt(ExitMonitorStmt arg0) {
		injectLabelStatements(arg0);		
		arg0.getOp().apply(this.valueswitch);
		this.valueswitch.getExpression();
		// TODO:

		this.inMonitor = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
	 */
	@Override
	public void caseGotoStmt(GotoStmt arg0) {		
		injectLabelStatements(arg0);
		String labelName = GlobalsCache.v().getUnitLabel(
				(Stmt) arg0.getTarget());
		this.boogieStatements.add(this.pf.mkGotoStatement(labelName));
		// if (labelName.contains("block324")) throw new
		// RuntimeException("there it is!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	@Override
	public void caseIdentityStmt(IdentityStmt arg0) {
		injectLabelStatements(arg0);
		AssignmentTranslation.translateAssignment(this, arg0.getLeftOp(),
				arg0.getRightOp(), arg0);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
	 */
	@Override
	public void caseIfStmt(IfStmt arg0) {

		injectLabelStatements(arg0);
		boolean forceCloneAttibute = false;
		if (TranslationHelpers.clonedFinallyBlocks.contains(arg0)) {
			forceCloneAttibute = true;
		} else if (this.procInfo.duplicatedIfStatement.contains(arg0)) {
			forceCloneAttibute = true;
		}
		Statement[] thenPart = {
				TranslationHelpers.mkLocationAssertion(arg0,
						forceCloneAttibute, "thenblock"),
				this.pf.mkGotoStatement(GlobalsCache.v().getUnitLabel(
						arg0.getTarget())) };

		Statement[] elsePart = {TranslationHelpers.mkLocationAssertion(arg0,
				forceCloneAttibute, "elseblock")};
		// now check if we can find a source location for the else block.
//		Stmt else_loc = findSuccessorStatement(arg0);
//		if (else_loc != null) {
//			// elsePart = new Statement[] { TranslationHelpers
//			// .mkLocationAssertion(else_loc, forceCloneAttibute) };
//
//			elsePart = new Statement[] {}; // TODO: test
//		}

		arg0.getCondition().apply(this.valueswitch);
		Expression cond = TranslationHelpers.castBoogieTypes(
				this.valueswitch.getExpression(), this.pf.getBoolType());
		
		if (isTrivialNullCheck(arg0.getCondition())) {
			Log.debug("Ignore trivial check "+arg0);
			for (Statement s : thenPart) {
				this.boogieStatements.add(s);
			}
		} else {
			this.boogieStatements.add(this.pf.mkIfStatement(cond, thenPart,
					elsePart));
		}

//		this.boogieStatements.add(TranslationHelpers.mkLocationAssertion(arg0.getTarget(),
//				forceCloneAttibute, "elseblock"));

	}
	
	
	/**
	 * This is a helper function to suppress false positives: Sometimes,
	 * when try-catch with resources is being used
	 * @param v
	 * @return
	 */
	private boolean isTrivialNullCheck(Value v) {
		 if (v instanceof BinopExpr) {
			 BinopExpr bo = (BinopExpr)v;
			 if (bo.getOp2() instanceof NullConstant && bo.getSymbol().equals(" == ")) {
				 //now it gets itchy. We want to catch only that case
				 //where the bytecode introduces a renaming of null and
				 //does this unreachable null check.
				 if (bo.getOp1() instanceof Local) {
					 Local l = (Local)bo.getOp1();
					 if (l.getType() instanceof NullType) {						 
						 return true;
					 } 
				 }
			 }
		 }
		 return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	@Override
	public void caseInvokeStmt(InvokeStmt arg0) {
		injectLabelStatements(arg0);
		AssignmentTranslation.translateAssignment(this, null,
				arg0.getInvokeExpr(), arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
	 */
	@Override
	public void caseLookupSwitchStmt(LookupSwitchStmt arg0) {
		injectLabelStatements(arg0);
		LinkedList<Expression> cases = new LinkedList<Expression>();
		LinkedList<Statement[]> targets = new LinkedList<Statement[]>();

		arg0.getKey().apply(this.valueswitch);
		Expression key = this.valueswitch.getExpression();
		for (int i = 0; i < arg0.getTargetCount(); i++) {
			Expression cond = this.pf.mkBinaryExpression(

			this.pf.getBoolType(), BinaryOperator.COMPEQ, key, this.pf
					.mkIntLiteral(Integer.toString(arg0.getLookupValue(i))));
			cases.add(cond);
			Statement[] gototarget = { this.pf.mkGotoStatement(GlobalsCache.v()
					.getUnitLabel((Stmt) arg0.getTarget(i))) };
			targets.add(gototarget);
		}
		{
			Statement[] gototarget = { this.pf.mkGotoStatement(

			GlobalsCache.v().getUnitLabel((Stmt) arg0.getDefaultTarget())) };
			targets.add(gototarget);
		}
		translateSwitch(cases, targets);
	}

	/**
	 * note that there is one more target than cases because of the default
	 * cases
	 * 
	 * @param cases
	 * @param targets
	 */
	private void translateSwitch(LinkedList<Expression> cases,
			LinkedList<Statement[]> targets) {
		Statement[] elseblock = targets.getLast();
		Statement ifstatement = null;
		int max = cases.size() - 1;
		for (int i = max; i >= 0; i--) {
			Statement[] thenblock = targets.get(i);
			ifstatement = this.pf.mkIfStatement(cases.get(i), thenblock,
					elseblock);
			elseblock = new Statement[1];
			elseblock[0] = ifstatement;
		}
		if (ifstatement != null) {
			this.boogieStatements.add(ifstatement);
		} else {
			Log.info("Warning: Found empty switch statement (or only default case).");
			for (int i = 0; i < elseblock.length; i++) {
				this.boogieStatements.add(elseblock[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
	 */
	@Override
	public void caseNopStmt(NopStmt arg0) {
		injectLabelStatements(arg0);
		// Log.error("NopStmt: " + arg0.toString());
		// assert (false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
	 */
	@Override
	public void caseRetStmt(RetStmt arg0) {
		injectLabelStatements(arg0);
		Log.error("This is deprecated: " + arg0.toString());
		throw new RuntimeException(
				"caseRetStmt is not implemented. Contact developers!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	@Override
	public void caseReturnStmt(ReturnStmt arg0) {
		injectLabelStatements(arg0);
		if (this.procInfo.getReturnVariable() != null) {
			Expression lhs = this.procInfo.getReturnVariable();
			arg0.getOp().apply(this.valueswitch);
			Expression rhs = this.valueswitch.getExpression();
			AssignmentTranslation.translateAssignment(this, lhs, rhs);
		}
		this.boogieStatements.add(this.pf.mkReturnStatement());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
	 */
	@Override
	public void caseReturnVoidStmt(ReturnVoidStmt arg0) {
		injectLabelStatements(arg0);
		this.boogieStatements.add(this.pf.mkReturnStatement());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
	 * The TableSwitch is a special case of the LookupSwitch, where all cases
	 * are consecutive.
	 */
	@Override
	public void caseTableSwitchStmt(TableSwitchStmt arg0) {
		injectLabelStatements(arg0);
		LinkedList<Expression> cases = new LinkedList<Expression>();
		LinkedList<Statement[]> targets = new LinkedList<Statement[]>();

		arg0.getKey().apply(this.valueswitch);
		Expression key = this.valueswitch.getExpression();
		int counter = 0;
		for (int i = arg0.getLowIndex(); i <= arg0.getHighIndex(); i++) {
			Expression cond = this.pf.mkBinaryExpression(this.pf.getBoolType(),
					BinaryOperator.COMPEQ, key,
					this.pf.mkIntLiteral(Integer.toString(i)));
			cases.add(cond);
			Statement[] gototarget = { this.pf.mkGotoStatement(

			GlobalsCache.v().getUnitLabel((Stmt) arg0.getTarget(counter))) };
			targets.add(gototarget);
			counter++;
		}
		{
			Statement[] gototarget = { this.pf.mkGotoStatement(

			GlobalsCache.v().getUnitLabel((Stmt) arg0.getDefaultTarget())) };
			targets.add(gototarget);
		}
		translateSwitch(cases, targets);
	}

	private SootClass findExceptionType(ThrowStmt s) {
		if (s.getOp() instanceof NewExpr) {
			NewExpr ne = (NewExpr) s.getOp();
			return ne.getBaseType().getSootClass();
		} else if (s.getOp() instanceof Local) {
			Local l = (Local) s.getOp();
			if (l.getType() instanceof RefType) {
				return ((RefType) l.getType()).getSootClass();
			}
		}
//		System.err.println("Unexpected value in throw stmt " + s.getOp());
		return Scene.v().loadClass("java.lang.Throwable", SootClass.SIGNATURES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	@Override
	public void caseThrowStmt(ThrowStmt arg0) {
		injectLabelStatements(arg0);

		// find the type of the exception that is thrown.
		SootClass c = findExceptionType(arg0);

		arg0.getOp().apply(this.valueswitch);
		Expression right = this.valueswitch.getExpression();
		// assign the value from arg0.getOp() to the $exception variable of
		// the current procedure.
		// Note that this only works because soot moves the "new" statement
		// to a new local variable.
		AssignmentTranslation.translateAssignment(this,
				this.procInfo.getExceptionVariable(), right);
		// Add a goto statement to the exceptional successors.
		List<Trap> traps = new LinkedList<Trap>();
		List<Trap> finally_traps = new LinkedList<Trap>(); // TODO: do we have
		TranslationHelpers.getReachableTraps(arg0,
				this.procInfo.getSootMethod(), traps, finally_traps);
		// TODO, maybe we need to consider the case that
		// we don't know the exact type of arg0.getOp at this point?
		for (Trap trap : traps) {
			if (GlobalsCache.v().isSubTypeOrEqual(c, trap.getException())) {
				this.boogieStatements.add(this.pf.mkGotoStatement(GlobalsCache
						.v().getUnitLabel((Stmt) trap.getHandlerUnit())));
				return;
			}
		}
		this.boogieStatements.add(this.pf.mkReturnStatement());

	}

	// public void caseThrowStmt(ThrowStmt arg0) {
	// injectLabelStatements(arg0);
	// arg0.getOp().apply(this.valueswitch);
	// Expression right = this.valueswitch.getExpression();
	// // assign the value from arg0.getOp() to the $exception variable of
	// // the current procedure.
	// // Note that this only works because soot moves the "new" statement
	// // to a new local variable.
	// AssignmentTranslation.translateAssignment(this,
	// this.procInfo.getExceptionVariable(), right);
	// // Add a goto statement to the exceptional successors.
	// List<Unit> exc_succ = procInfo.getExceptionalUnitGraph()
	// .getExceptionalSuccsOf((Unit) arg0);
	// String[] labels = new String[exc_succ.size()];
	// if (exc_succ.size() > 0) {
	// for (int i = 0; i < exc_succ.size(); i++) {
	// labels[i] = GlobalsCache.v().getUnitLabel(
	// (Stmt) exc_succ.get(i));
	// }
	// if (exc_succ.size() > 1) {
	//
	// for (int i = 0; i < exc_succ.size(); i++) {
	// Unit u = exc_succ.get(i);
	//
	// if (u instanceof IdentityStmt) {
	//
	// IdentityStmt istmt = (IdentityStmt) u;
	// if (istmt.getRightOp() instanceof CaughtExceptionRef) {
	// // sb.append("... catches exception! " +
	// // istmt.getLeftOp().getType()+"\n");
	// Type caughttype = istmt.getLeftOp().getType();
	// if (!(caughttype instanceof RefType)) {
	// throw new RuntimeException(
	// "Bug in translation of ThrowStmt!");
	// }
	// RefType caught = (RefType) caughttype;
	// Expression cond = GlobalsCache
	// .v()
	// .compareTypeExpressions(
	// this.valueswitch.getClassTypeFromExpression(
	// right, false),
	// GlobalsCache
	// .v()
	// .lookupClassVariable(
	// caught.getSootClass()));
	// Statement[] thenPart = new Statement[] { this.pf
	// .mkGotoStatement(labels[i]) };
	// Statement ifstmt = this.pf.mkIfStatement(cond,
	// thenPart, new Statement[0]);
	// // sb.append("created choice: "+ifstmt+"\n");
	// this.boogieStatements.add(ifstmt);
	// } else {
	// throw new RuntimeException(
	// "Bug in translation of ThrowStmt!");
	// }
	// } else if (u instanceof NopStmt) {
	// String filename = "";
	// int startln = -1;
	// for (Tag tag : u.getTags()) {
	// if (tag instanceof LineNumberTag) {
	// if (GlobalsCache.v().currentMethod != null) {
	// filename = GlobalsCache.v().currentMethod
	// .getDeclaringClass().getName();
	// }
	// startln = ((LineNumberTag) tag).getLineNumber();
	// break;
	// } else if (tag instanceof SourceLnNamePosTag) {
	// startln = ((SourceLnNamePosTag) tag).startLn();
	// filename = ((SourceLnNamePosTag) tag)
	// .getFileName();
	// break;
	// } else if (tag instanceof SourceFileTag) {
	// filename = ((SourceFileTag) tag)
	// .getSourceFile();
	// break;
	// }
	// }
	// Log.error("Catch block starts with Nop instead of assignment. Maybe unsound. "
	// + filename + ": " + startln);
	// } else {
	// throw new RuntimeException(
	// "Bug in translation of ThrowStmt! "
	// + u.getClass().toString());
	// }
	// }
	// // throw new RuntimeException(sb.toString());
	// // Log.error(sb);
	// // Make sure that the execution does not continue after the
	// // throw statement
	// this.boogieStatements.add(this.pf.mkReturnStatement());
	// } else {
	// this.boogieStatements.add(this.pf.mkGotoStatement(labels[0]));
	// }
	// } else {
	//
	// this.boogieStatements.add(this.pf.mkReturnStatement());
	// }
	//
	// }
	//
	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.StmtSwitch#defaultCase(java.lang.Object)
	 */
	@Override
	public void defaultCase(Object arg0) {
		assert (false);
	}

}
