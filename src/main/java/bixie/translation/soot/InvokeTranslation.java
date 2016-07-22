/**
 * 
 */
package bixie.translation.soot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.enums.BinaryOperator;
import bixie.translation.GlobalsCache;
import bixie.util.Log;
import soot.Immediate;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

/**
 * @author schaef
 * 
 */
public class InvokeTranslation {

	public static void translateInvokeAssignment(SootStmtSwitch ss, Value lhs,
			InvokeExpr ivk, Unit statement) {
		if (specialCaseInvoke(ss, lhs, ivk))
			return;

		SootValueSwitch valueswitch = ss.getValueSwitch();
		SootProcedureInfo procInfo = ss.getProcInfo();

		// translate the left-hand side of the assignment (if there is one).
		LinkedList<IdentifierExpression> lefts = new LinkedList<IdentifierExpression>();
		IdentifierExpression stubbedvar = null;
		Expression left = null;
		if (lhs != null) {
			lhs.apply(valueswitch);
			left = valueswitch.getExpression();
			if (left instanceof IdentifierExpression) {
				lefts.add((IdentifierExpression) left);
			} else {
				/*
				 * boogie doesn't allow you to put an array access as left hand
				 * side for a function call. So, if this happens, we add a fake
				 * local and assign it back after the call statement.
				 */
				stubbedvar = procInfo.createLocalVariable(left.getType());
				lefts.add(stubbedvar);
			}
		}

		SootMethod m = ivk.getMethod();

		// translate the call args
		LinkedList<Expression> args = new LinkedList<Expression>();
		for (int i = 0; i < ivk.getArgs().size(); i++) {
			ivk.getArg(i).apply(valueswitch);
			args.add(valueswitch.getExpression());
		}

		// Translate base, if necessary.
		// SootClass baseClass = null;
		if (ivk instanceof InstanceInvokeExpr) {
			// this include Interface-, Virtual, and SpecialInvokeExpr

			InstanceInvokeExpr iivk = (InstanceInvokeExpr) ivk;
			// baseClass = getBaseClass(iivk.getBase().getType());

			iivk.getBase().apply(valueswitch);
			Expression base = valueswitch.getExpression();
			// add the "this" variable to the list of args
			args.addFirst(base);

			try {
				if (iivk.getBase() instanceof Immediate
						&& procInfo.getNullnessAnalysis().isAlwaysNonNullBefore(
								statement, (Immediate) iivk.getBase())) {
					// do not check
				} else {
					ss.getErrorModel().createNonNullViolationException(base);
				}
			} catch (Throwable e) {
				
			}
		} else if (ivk instanceof StaticInvokeExpr) {
			// Do nothing
		} else if (ivk instanceof DynamicInvokeExpr) {
			DynamicInvokeExpr divk = (DynamicInvokeExpr)ivk;
			Log.info("Ignoring dynamic invoke: "+divk.toString());
			
		} else {
			throw new RuntimeException(
					"Cannot compute instance for " + ivk.getClass().toString());
		}

		ss.addStatement(createCallStatement(ss, m, lefts,
				args.toArray(new Expression[args.size()])));

		Expression constructorInstance = null;
		if (m.isConstructor()) {
			// if it is a call to a constructor, we have to add
			// the condition that the object which is being constructed
			// will be set to null if the constructor terminates
			// with an exception.
			// That is, for: $exception = Object$init(base)
			// we create
			// if ($exception!=null && base!=this) base=null;
			constructorInstance = args.getFirst(); // the first arg must be the
													// pointer to that object
		}

		// now check if the procedure returned exceptional
		// and jump to the appropriate location
		translateCalleeExceptions(ss, statement, constructorInstance, m);

		/*
		 * if the left-hand side was an array access and we introduced a helper
		 * variable, we create and assignment here that assigns this variable to
		 * the original LHS.
		 */
		if (stubbedvar != null) {
			AssignmentTranslation.translateAssignment(ss, left, stubbedvar);
		}
	}

	// static private SootClass getBaseClass(Type type) {
	// SootClass c = null;
	// if (type instanceof RefType) {
	// RefType rt = (RefType) type;
	// c = rt.getSootClass();
	// } else if (type instanceof ArrayType) {
	// c = Scene.v().loadClass("java.lang.reflect.Array",
	// SootClass.SIGNATURES);
	// } else {
	// throw new RuntimeException(
	// "Something wrong in translateInvokeAssignment: Expected RefType or ArrayType but found "
	// + type.getClass().toString());
	// }
	// return c;
	// }

	static private void resolveBaseIfNecessary(InvokeExpr ivk) {
		if (ivk instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iivk = (InstanceInvokeExpr)ivk;
			soot.Type t = iivk.getBase().getType();
			if (t instanceof RefType) {
				RefType rt = (RefType)t;
				if (rt.getSootClass().resolvingLevel()<SootClass.SIGNATURES) {
					Scene.v().forceResolve(rt.getSootClass().getName(), SootClass.SIGNATURES);					
				}
			}
		}
	} 
	
	static private boolean specialCaseInvoke(SootStmtSwitch ss, Value lhs,
			InvokeExpr ivk) {
		SootValueSwitch valueswitch = ss.getValueSwitch();
		ProgramFactory pf = GlobalsCache.v().getPf();
		// java.lang.String.length is treated as a special case:
		
		resolveBaseIfNecessary(ivk);
		
		if (ivk.getMethod().getSignature()
				.contains("<java.lang.String: int length()>")
				&& lhs != null) {
			if (ivk instanceof SpecialInvokeExpr) {
				((SpecialInvokeExpr) ivk).getBase().apply(valueswitch);
			} else if (ivk instanceof VirtualInvokeExpr) {
				((VirtualInvokeExpr) ivk).getBase().apply(valueswitch);
			} else {
				throw new RuntimeException("Bad usage of String.length?");
			}
			Expression[] indices = { valueswitch.getExpression() };
			Expression right = pf.mkArrayAccessExpression(pf.getIntType(),
					SootPrelude.v().getStringSizeHeapVariable(), indices);

			lhs.apply(valueswitch);
			Expression left = valueswitch.getExpression();
			AssignmentTranslation.translateAssignment(ss, left, right);
			return true;
		}

		if (ivk.getMethod().getSignature()
				.contains("<java.lang.System: void exit(int)>")) {
			Log.debug("Surppressing false positive from call to System.exit");
			// this is not a return statement, it actually ends the application.
			// ss.addStatement(pf.mkAssumeStatement(new
			// Attribute[]{pf.mkNoVerifyAttribute()},
			// pf.mkBooleanLiteral(false)));
			ss.addStatement(pf.mkReturnStatement());
			return true;
		}

		if (ivk.getMethod()
				.getSignature()
				.contains(
						"java.lang.Throwable: void addSuppressed(java.lang.Throwable)")) {
			ss.addStatement(TranslationHelpers.mkLocationAssertion(
					ss.getCurrentStatement(), true));
			return true;
		}

		if (ivk.getMethod().getSignature().contains("addSuppressed")) {
			System.err.println(ivk.getMethod().getSignature());
		}
		return false;
	}

	/**
	 * Create a Boogie call statement. The trick is, that, given a call f(x) we
	 * may have to create stubs for the return value of f and the exception
	 * thrown by f because in Boogie the number of lhs variables has to match
	 * the number of return variables. E.g. if f returns an int and throws
	 * exceptions, the call above would be translated to call ret, exc := f(x);
	 * where ret and exc are fresh local variables.
	 * 
	 * @param ss
	 * @param m
	 * @param throwsclauses
	 * @param lefts
	 * @param args
	 * @return
	 */
	static private Statement createCallStatement(SootStmtSwitch ss,
			SootMethod m, List<IdentifierExpression> lefts, Expression[] args) {

		ProgramFactory pf = GlobalsCache.v().getPf();

		// this is the procInfo for the procedure from which m is called
		SootProcedureInfo procInfo = ss.getProcInfo();
		// this is the procInfo for the called procedure m
		SootProcedureInfo calleeInfo = GlobalsCache.v().lookupProcedure(m);

		Attribute[] attributes = TranslationHelpers.javaLocation2Attribute(ss
				.getCurrentStatement());

		// we have to clone the lefts because we may add thing to it here.
		List<IdentifierExpression> lefts_clone = new LinkedList<IdentifierExpression>();
		for (IdentifierExpression ide : lefts) {
			lefts_clone.add(ide);
		}

		if (calleeInfo.getReturnVariable() != null && lefts_clone.size() == 0) {
			lefts_clone.add(procInfo.createLocalVariable(calleeInfo
					.getReturnVariable().getType()));
		}
		/*
		 * now add a fake local if the callee may throw an exception
		 */
		if (calleeInfo.getExceptionVariable() != null) {
			lefts_clone.add(procInfo.getExceptionVariable());
		}

		HashMap<String, Expression> substitutes = new HashMap<String, Expression>();
		for (int i = 0; i < calleeInfo.getProcedureDeclaration().getInParams().length; i++) {
			VarList vl = calleeInfo.getProcedureDeclaration().getInParams()[i];
			Expression arg = args[i];
			if (vl.getIdentifiers().length != 1) {
				throw new RuntimeException("That aint right!");
			}
			substitutes.put(vl.getIdentifiers()[0], arg);
		}

		Statement s = pf.mkCallStatement(attributes, false, lefts_clone
				.toArray(new IdentifierExpression[lefts_clone.size()]),
				calleeInfo.getBoogieName(), args);

		return s;
	}

	static private void translateCalleeExceptions(SootStmtSwitch ss,
			Unit statement, Expression constructorInstance,
			SootMethod calledMethod) {
		SootValueSwitch valueswitch = ss.getValueSwitch();
		ProgramFactory pf = GlobalsCache.v().getPf();
		SootProcedureInfo procInfo = ss.getProcInfo();

		// first we collect all possible exceptions.
		// then we create one statement if($exception!=null) statements
		LinkedList<Statement> statements = new LinkedList<Statement>();
		List<Trap> traps = new LinkedList<Trap>();
		List<Trap> finally_traps = new LinkedList<Trap>(); // TODO: do we have
															// to use them here?
		TranslationHelpers.getReachableTraps(statement,
				procInfo.getSootMethod(), traps, finally_traps);

		List<SootClass> possibleExceptions = sortExceptions(calledMethod
				.getExceptions());

		// in case the method throws something unexpected, we
		// add Throwable to the list of possible exceptions.
		// NOTE: this causes a ridiculous blow-up of the boogie program
		// and we don't gain anything for the infeasible code detection,
		// so we're not doing it for now.
		// SootClass throwableException =
		// Scene.v().loadClass("java.lang.Throwable",
		// SootClass.SIGNATURES);
		// if (!possibleExceptions.contains(throwableException)) {
		// possibleExceptions.add(throwableException);
		// }
		SootClass interuptException = Scene.v().loadClass(
				"java.lang.InterruptedException", SootClass.SIGNATURES);

		SootClass largestCaughtException = null;

		LinkedList<Trap> usedTraps = new LinkedList<Trap>();

		for (SootClass c : possibleExceptions) {
			String transferlabel = null;
			// for each possible exception, check if there is a catch block.
			for (Trap trap : new LinkedList<Trap>(traps)) {
				if (GlobalsCache.v().isSubTypeOrEqual(c, trap.getException())) {
					transferlabel = GlobalsCache.v().getUnitLabel(
							(Stmt) trap.getHandlerUnit());
					// mark that we used that trap already, so we don't
					// add another transition for it later.
					// System.err.println("Exception "+c.getName() +
					// " is caught by "+trap.getException());
					usedTraps.add(trap);
					break;
				}
			}

			if (largestCaughtException == null
					|| GlobalsCache.v().isProperSubType(largestCaughtException,
							c)) {
				largestCaughtException = c;
			} else {
				// System.err.println("Not catching "+c.getName() +
				// " because we already caught "+largestCaughtException.getName());
				continue;
			}

			Statement transferStatement;
			if (transferlabel == null) {
				// if the exception is not caught, leave the procedure
				// that is, re-throw.
				transferStatement = pf.mkReturnStatement();
			} else {
				// if the exception is caught, create a goto
				transferStatement = pf.mkGotoStatement(transferlabel);
			}

			// now make a statement of the form
			// if($exception<:c) transferStatement
			// and add it to the list statements
			Expression condition = pf.mkBinaryExpression(
					pf.getBoolType(),
					BinaryOperator.COMPPO,
					valueswitch.getClassTypeFromExpression(
							procInfo.getExceptionVariable(), false),
					GlobalsCache.v().lookupClassVariable(c));
			
			LinkedList<Statement> then = new LinkedList<Statement>();
			
			// Small hack to avoid false positives form
			// code that is only reachable if an interleaving happens
			if (interuptException == c
					&& !bixie.translation.Options.v().useSoundThreads()) {
				
				then.add(TranslationHelpers.havocEverything(procInfo,
								valueswitch));
			} else {			
				if (constructorInstance != null
						&& constructorInstance != procInfo.getThisReference()) {
					then.add(pf.mkAssignmentStatement(constructorInstance, SootPrelude.v()
									.getNullConstant()));
				}
			}
			then.add(transferStatement);
			Statement[] thenPart = then.toArray(new Statement[then.size()]); 
			
//			Statement[] thenPart = { transferStatement };
//			//Small hack to avoid false positives form
//			//code that is only reachable if an interleaving happens
//			if (interuptException==c && !org.joogie.Options.v().useSoundThreads()) {
//				thenPart = new Statement[]{TranslationHelpers.havocEverything(procInfo, valueswitch), transferStatement};
//			}			
			
			Statement[] elsePart = { TranslationHelpers
					.createClonedAttribAssert()};
			statements.add(pf.mkIfStatement(condition, thenPart, elsePart));
		}

		// now remove all the traps that we have already used
		// and generate transitions for the remaining ones if
		// necessary.
		traps.removeAll(usedTraps);

		// now check if there are traps of type Exception or Throwable left, or
		// if we might not know the throws clause
		// then we have to create an edge to them as well. Otherwise
		// we might create unreachable catch blocks
		// also check if runtime exceptions are caught. In that case,
		// we have to allow a transition to them as well
		SootClass exception = Scene.v().loadClass("java.lang.Exception",
				SootClass.SIGNATURES);
		SootClass throwable = Scene.v().loadClass("java.lang.Throwable",
				SootClass.SIGNATURES);
		SootClass runtimeexception = Scene.v().loadClass(
				"java.lang.RuntimeException", SootClass.SIGNATURES);

		for (Trap trap : traps) {

			if (trap.getException() == exception
					|| trap.getException() == throwable
					|| !calledMethod.hasActiveBody()
					|| GlobalsCache.v().isSubTypeOrEqual(trap.getException(),
							runtimeexception)) {

				if (largestCaughtException == null
						|| GlobalsCache.v().isProperSubType(
								largestCaughtException, trap.getException())) {
					largestCaughtException = trap.getException();
				} else {
					// System.err.println("Not catching "+trap.getException().getName()
					// +
					// " because we already caught "+largestCaughtException.getName());
					continue;
				}

				Expression condition = pf.mkBinaryExpression(
						pf.getBoolType(),
						BinaryOperator.COMPPO,
						valueswitch.getClassTypeFromExpression(
								procInfo.getExceptionVariable(), false),
						GlobalsCache.v().lookupClassVariable(
								trap.getException()));
				Statement transferStatement = pf.mkGotoStatement(GlobalsCache.v().getUnitLabel(
						(Stmt) trap.getHandlerUnit()));
				Statement[] thenPart = {
						TranslationHelpers.createClonedAttribAssert(),
						transferStatement };
				
				if (constructorInstance != null
						&& constructorInstance != procInfo.getThisReference()) {					
					thenPart = new Statement[]{
							TranslationHelpers.createClonedAttribAssert(),
							pf.mkAssignmentStatement(constructorInstance, SootPrelude.v()
									.getNullConstant()),
							transferStatement };
				}
				
				
				Statement[] elsePart = { TranslationHelpers
						.createClonedAttribAssert() };
				statements.add(pf.mkIfStatement(condition, thenPart, elsePart));
			}
		}

		// finally check if there is a finally_trap that we didn't account for
		// and create an unconditional goto.
		if (finally_traps.size() > 0) {
			if (finally_traps.size() > 1) {
//				Log.error("more than one finally trap for "
//						+ procInfo.getBoogieName());
			}
			Trap trap = finally_traps.get(0);

			if (largestCaughtException == null
					|| GlobalsCache.v().isProperSubType(largestCaughtException,
							trap.getException())) {
				if (constructorInstance != null
						&& constructorInstance != procInfo.getThisReference()) {
					statements.addFirst(pf.mkAssignmentStatement(
							constructorInstance, SootPrelude.v()
									.getNullConstant()));
				}				
				
				largestCaughtException = trap.getException();
				statements.add(pf.mkGotoStatement(GlobalsCache.v()
						.getUnitLabel((Stmt) trap.getHandlerUnit())));
			} else {
				// System.err.println("Not catching finally "+trap.getException().getName()
				// +
				// " because we already caught "+largestCaughtException.getName());
			}

		}

//		if (statements.size() == 0)
//			return;
//		// if the call was a constructor, add an assignment that sets
//		// the created variable back to null
//		if (constructorInstance != null
//				&& constructorInstance != procInfo.getThisReference()) {
//			statements.addFirst(pf.mkAssignmentStatement(constructorInstance,
//					SootPrelude.v().getNullConstant()));
//		}

		// now add all the exceptional checks in a block where
		// we ensure that $excpeiton was not null
		if (statements.size()>0) {
			Expression condition = pf.mkBinaryExpression(pf.getBoolType(),
					BinaryOperator.COMPNEQ, procInfo.getExceptionVariable(),
					SootPrelude.v().getNullConstant());
	
			ss.addStatement(pf.mkIfStatement(condition,
					statements.toArray(new Statement[statements.size()]),
					new Statement[] {}));
		}
	}


	static public void translateCalleeExceptions2(SootStmtSwitch ss,
			Unit statement, Expression constructorInstance,
			SootMethod calledMethod) {
		SootValueSwitch valueswitch = ss.getValueSwitch();
		ProgramFactory pf = GlobalsCache.v().getPf();
		SootProcedureInfo procInfo = ss.getProcInfo();

		// first we collect all possible exceptions.
		// then we create one statement if($exception!=null) statements
		LinkedList<Statement> statements = new LinkedList<Statement>();
		List<Trap> traps = new LinkedList<Trap>();
		List<Trap> finally_traps = new LinkedList<Trap>(); // TODO: do we have
															// to use them here?
		TranslationHelpers.getReachableTraps(statement,
				procInfo.getSootMethod(), traps, finally_traps);

		List<SootClass> possibleExceptions = calledMethod.getExceptions();

		SootClass interuptException = Scene.v().loadClass(
				"java.lang.InterruptedException", SootClass.SIGNATURES);

		SootClass exception = Scene.v().loadClass("java.lang.Exception",
				SootClass.SIGNATURES);
		SootClass throwable = Scene.v().loadClass("java.lang.Throwable",
				SootClass.SIGNATURES);
		SootClass runtimeexception = Scene.v().loadClass(
				"java.lang.RuntimeException", SootClass.SIGNATURES);

		LinkedList<SootClass> largestCaughtExceptions = new LinkedList<SootClass>();

		LinkedList<Trap> usedTraps = new LinkedList<Trap>();

		for (SootClass c : possibleExceptions) {
			String transferlabel = null;
			// for each possible exception, check if there is a catch block.
			for (Trap trap : new LinkedList<Trap>(traps)) {
				if (GlobalsCache.v().isSubTypeOrEqual(c, trap.getException())) {
					transferlabel = GlobalsCache.v().getUnitLabel(
							(Stmt) trap.getHandlerUnit());
					// mark that we used that trap already, so we don't
					// add another transition for it later.
					usedTraps.add(trap);
					break;
				}
			}

			if (subsumedByLargestKnonwExceptions(largestCaughtExceptions, c)) {
				continue;
			}

			Statement transferStatement;
			if (transferlabel == null) {
				// if the exception is not caught, leave the procedure
				// that is, re-throw.
				transferStatement = pf.mkReturnStatement();
			} else {
				// if the exception is caught, create a goto
				transferStatement = pf.mkGotoStatement(transferlabel);
			}

			// now make a statement of the form
			// if($exception<:c) transferStatement
			// and add it to the list statements
			Expression condition = pf.mkBinaryExpression(
					pf.getBoolType(),
					BinaryOperator.COMPPO,
					valueswitch.getClassTypeFromExpression(
							procInfo.getExceptionVariable(), false),
					GlobalsCache.v().lookupClassVariable(c));

			Statement[] thenPart = { transferStatement };
			// Small hack to avoid false positives form
			// code that is only reachable if an interleaving happens
			if (interuptException == c
					&& !bixie.translation.Options.v().useSoundThreads()) {
				thenPart = new Statement[] {
						TranslationHelpers.havocEverything(procInfo,
								valueswitch), transferStatement };
			}

			Statement[] elsePart = {};
			statements.add(pf.mkIfStatement(condition, thenPart, elsePart));
		}

		// now remove all the traps that we have already used
		// and generate transitions for the remaining ones if
		// necessary.
		traps.removeAll(usedTraps);

		// now check if there are traps of type Exception or Throwable left, or
		// if we might not know the throws clause
		// then we have to create an edge to them as well. Otherwise
		// we might create unreachable catch blocks
		// also check if runtime exceptions are caught. In that case,
		// we have to allow a transition to them as well

		for (Trap trap : traps) {

			if (trap.getException() == exception
					|| trap.getException() == throwable
					|| !calledMethod.hasActiveBody()
					|| GlobalsCache.v().isSubTypeOrEqual(trap.getException(),
							runtimeexception)) {

				if (subsumedByLargestKnonwExceptions(largestCaughtExceptions,
						trap.getException())) {
					continue;
				}

				Expression condition = pf.mkBinaryExpression(
						pf.getBoolType(),
						BinaryOperator.COMPPO,
						valueswitch.getClassTypeFromExpression(
								procInfo.getExceptionVariable(), false),
						GlobalsCache.v().lookupClassVariable(
								trap.getException()));
				Statement[] thenPart = {
						TranslationHelpers.createClonedAttribAssert(),
						pf.mkGotoStatement(GlobalsCache.v().getUnitLabel(
								(Stmt) trap.getHandlerUnit())) };
				Statement[] elsePart = { TranslationHelpers
						.createClonedAttribAssert() };
				statements.add(pf.mkIfStatement(condition, thenPart, elsePart));
			}
		}

		// finally check if there is a finally_trap that we didn't account for
		// and create an unconditional goto.
		if (finally_traps.size() > 0) {
			if (finally_traps.size() > 1) {
//				Log.error("more than one finally trap for "
//						+ procInfo.getBoogieName());
			}
			Trap trap = finally_traps.get(0);

			if (!subsumedByLargestKnonwExceptions(largestCaughtExceptions,
					trap.getException())) {
				statements.add(pf.mkGotoStatement(GlobalsCache.v()
						.getUnitLabel((Stmt) trap.getHandlerUnit())));
			}

		}

		if (statements.size() == 0)
			return;
		// if the call was a constructor, add an assignment that sets
		// the created variable back to null
		if (constructorInstance != null
				&& constructorInstance != procInfo.getThisReference()) {
			statements.addFirst(pf.mkAssignmentStatement(constructorInstance,
					SootPrelude.v().getNullConstant()));
		}

		// now add all the exceptional checks in a block where
		// we ensure that $excpeiton was not null
		Expression condition = pf.mkBinaryExpression(pf.getBoolType(),
				BinaryOperator.COMPNEQ, procInfo.getExceptionVariable(),
				SootPrelude.v().getNullConstant());

		ss.addStatement(pf.mkIfStatement(condition,
				statements.toArray(new Statement[statements.size()]),
				new Statement[] {}));

	}

	/**
	 * Updates the list of largest known exceptions. It scans through the list
	 * and checks if it already contains a superclass of c. If so, it returns
	 * true. Otherwise, it adds c to the list and removes all sublcasses of c
	 * from the list, and returns false
	 * 
	 * @param largestCaughtExceptions
	 * @param c
	 */
	private static boolean subsumedByLargestKnonwExceptions(
			List<SootClass> largestCaughtExceptions, SootClass c) {
		if (largestCaughtExceptions.isEmpty()) {
			largestCaughtExceptions.add(c);
		} else {
			SootClass largerKnown = null;
			boolean replacedOld = false;
			for (SootClass large : new LinkedList<SootClass>(
					largestCaughtExceptions)) {
				if (GlobalsCache.v().isProperSubType(c, large)) {
					largerKnown = large;
					break;
				} else if (GlobalsCache.v().isProperSubType(large, c)) {
					largestCaughtExceptions.remove(large);
					replacedOld = true;
				}
			}
			if (largerKnown != null) {
				// System.err.println("Not catching "+c.getName() +
				// " because we already caught "+largerKnown.getName());
				return true;
			}
			if (replacedOld) {
				largestCaughtExceptions.add(c);
			}
		}
		return false;
	}

	/**
	 * Sort a list of exceptions such that a class always occurs before its
	 * superclass.
	 * 
	 * @param excpetions
	 * @return
	 */
	private static List<SootClass> sortExceptions(List<SootClass> exceptions) {
		LinkedList<SootClass> sorted = new LinkedList<SootClass>();
		LinkedList<SootClass> todo = new LinkedList<SootClass>(exceptions);
		while (!todo.isEmpty()) {
			SootClass largest = null;
			for (SootClass c : todo) {
				if (largest == null) {
					largest = c;
					continue;
				}
				if (GlobalsCache.v().isProperSubType(largest, c)) {
					largest = c;
				}
			}
			todo.remove(largest);
			sorted.addFirst(largest);
		}
		// System.err.print("unsorted: " );
		// for (SootClass c : exceptions) System.err.print(c.getName()+", ");
		// System.err.println();
		// System.err.print("sorted: " );
		// for (SootClass c : sorted) System.err.print(c.getName()+", ");
		// System.err.println();

		return sorted;
	}

}
