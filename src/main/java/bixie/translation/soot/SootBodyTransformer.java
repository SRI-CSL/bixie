
package bixie.translation.soot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.declaration.Implementation;
import bixie.boogie.ast.statement.ReturnStatement;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.enums.BinaryOperator;
import bixie.translation.GlobalsCache;
import bixie.util.Log;
import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.BinopExpr;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JEqExpr;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnNamePosTag;
import soot.tagkit.Tag;
import soot.toolkits.exceptions.UnitThrowAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Boogie Body Transformer
 * 
 * @author schaef
 */
public class SootBodyTransformer extends BodyTransformer {

	/**
	 * C-tor
	 * 
	 * @param report
	 *            Report
	 */
	public SootBodyTransformer() {
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void internalTransform(Body arg0, String arg1, Map arg2) {
		
		// report.addMethod(sootMethod);
		GlobalsCache.v().currentMethod = arg0.getMethod(); 
		try {
			transformStmtList(arg0);
		} catch (Throwable e) {
			Log.error("Failed to translate "+ arg0.getMethod().getName());
			Log.error(e.toString());
			throw e;
		}
		GlobalsCache.v().currentMethod = null;
	}

	/**
	 * Transforms a list of statements
	 * 
	 * @param body
	 *            Body
	 */
	private void transformStmtList(Body body) {
				
		SootProcedureInfo procInfo = GlobalsCache.v().lookupProcedure(
				body.getMethod());

		if (procInfo.getBoogieProcedure()!=null) {
			Log.info("Procedure "+body.getMethod().getBytecodeSignature()+" already known from Prelude");
			return;
		}
		
		
		LinkedList<Statement> boogieStatements = new LinkedList<Statement>();
		
		//now add all assumptions about the types of the in and out parameters
		boogieStatements.addAll(procInfo.typeAssumptions);
		
		ExceptionalUnitGraph tug = new ExceptionalUnitGraph(
				body, UnitThrowAnalysis.v());
		procInfo.setExceptionalUnitGraph(tug);
		
		//in a first pass, check if statements have been duplicated
		//in the bytecode, e.g. for finally-blocks, which is used
		//later to generate attributes that suppress false alarms
		//during infeasible code detection.
		TranslationHelpers.clonedFinallyBlocks.clear();
		
		
		
		TranslationHelpers.clonedFinallyBlocks.addAll(detectDuplicatedFinallyBlocks(tug.iterator(), procInfo));
//		TranslationHelpers.clonedFinallyBlocks = detectDuplicatedFinallyBlocks_new(tug.iterator(), procInfo);
		//reset the iterator
		Iterator<Unit> stmtIt = tug.iterator();
		
		while (stmtIt.hasNext()) {
			Stmt s = (Stmt) stmtIt.next();
			
			SootStmtSwitch bss = new SootStmtSwitch(procInfo);
			s.apply(bss);
			LinkedList<Statement> stmts = bss.popAll();
			boogieStatements.addAll(stmts);
			
		}

		Attribute[] attributes = TranslationHelpers.javaLocation2Attribute(body.getTags());
		
		if (procInfo.getThisReference()!=null) {
			//for non-static procedures we have to assume that .this is non-null
			boogieStatements.addFirst(
					GlobalsCache.v().getPf().mkAssumeStatement(attributes, 
								GlobalsCache.v().getPf().mkBinaryExpression( 
										GlobalsCache.v().getPf().getBoolType(), 
										BinaryOperator.COMPNEQ, 
											procInfo.getThisReference(), 
											SootPrelude.v().getNullConstant()))						
						);		
		}
		
		//to be compatible with Microsoft Boogie, we have
		//to create a unified exit and only a single return
		//per procedure.
		boogieStatements = createUnifiedExit(boogieStatements);
		
		//now create the procedure implementation that combines
		//the signature procInfo and the body.
		Implementation proc = GlobalsCache
				.v()
				.getPf()
				.mkProcedure(
						procInfo.getProcedureDeclaration(),
						boogieStatements.toArray(new Statement[boogieStatements
								.size()]), procInfo.getLocalVariables());		
		
		procInfo.setProcedureImplementation(proc);
		//GlobalsCache.v().modifiedInMonitor.clear();
	}
	
	private LinkedList<Statement> createUnifiedExit(LinkedList<Statement> stmts) {
		LinkedList<Statement> ret = new LinkedList<Statement>();
		String label = GlobalsCache.v().getBlockLabel();	
		ProgramFactory pf = GlobalsCache.v().getPf();
		
		for (Statement s : stmts) {
			if (s instanceof ReturnStatement) {				
				ret.add(pf.mkGotoStatement(label));
			} else {
				ret.add(s);
			}
			
		}		
		ret.add(pf.mkLabel(label));
		ret.add(pf.mkReturnStatement());
		return ret;
	}
	
	/**
	 * In the bytecode, finally-blocks are duplicated. To prevent false positives 
	 * during infeasible code detection it is vital to detect and flag these duplications.
	 * The challenge is that they are not exact clones. Variables might be slightly different,
	 * etc. 
	 * Hence we look for blocks of statements that:
	 * a) each statement has the same java line number
	 * b) the blocks are not connected
	 * c) they have a subsequence of instructions with exactly the same types.
	 * This is certainly unsound but seems to work so far.
	 * @param stmtIt
	 */
	private HashSet<Stmt> detectDuplicatedFinallyBlocks(Iterator<Unit> stmtIt, SootProcedureInfo procInfo) {
		SootMethod sootMethod = procInfo.getSootMethod();
		int first_trap_line = 1000000;
		for (Trap trap : sootMethod.getActiveBody().getTraps()) {
			for (Tag tag : trap.getHandlerUnit().getTags()) {
				if (tag instanceof LineNumberTag) {
					LineNumberTag t = (LineNumberTag)tag;
					if (first_trap_line > t.getLineNumber()) {
						first_trap_line = t.getLineNumber();
					}
				} else if (tag instanceof SourceLnNamePosTag) {
					SourceLnNamePosTag t = (SourceLnNamePosTag)tag;
					if (first_trap_line > t.startLn()) {
						first_trap_line = t.startLn();
					}
				}	
			}

		}
		
		//TODO: instead of just returning the set of stmts that have duplicates
		//we could group them so that we can still report infeasible code
		//if all duplicates of one statement are infeasible.
		HashSet<Stmt> duplicates = new HashSet<Stmt>();
		
		HashMap<Integer, LinkedList<Stmt>> subprogs = new HashMap<Integer, LinkedList<Stmt>>();
		
		LinkedList<Stmt> subprog = null;
		int old_line = -100; // pick a negative constant that is not a line number
				
		//GlobalsCache.v().modifiedInMonitor = new HashMap<EnterMonitorStmt, HashSet<Value>>();
		
		HashSet<IfStmt> ifstmts = new HashSet<IfStmt>();
		
		while (stmtIt.hasNext()) {
			Stmt s = (Stmt) stmtIt.next();
			
			//check for used static fields. 
			//we need to collect them to havoc them later when
			//entering a monitor.
			for (ValueBox vb : s.getUseBoxes()) {
				if (vb.getValue() instanceof StaticFieldRef) {
					StaticFieldRef sr = (StaticFieldRef)vb.getValue();
					procInfo.usedStaticFields.add(sr);
				}
			}
			
			if (s instanceof IfStmt) {
				IfStmt is = (IfStmt)s;
				for (IfStmt is2 : ifstmts) {
					if (is.getCondition().equivTo(is2.getCondition())) {
						//note that we do not add the first occurrence
						//this is a somewhat arbitrary optimization attempt.
						procInfo.duplicatedIfStatement.add(is2);
						procInfo.duplicatedIfStatement.add(is);
						break;
					} else {
						Value nonneg1 = normalizeNegations(is.getCondition());
						Value nonneg2 = normalizeNegations(is2.getCondition());						
						if (nonneg1.equivTo(nonneg2)) {
							procInfo.duplicatedIfStatement.add(is2);
							procInfo.duplicatedIfStatement.add(is);
						}
					}
				}
				ifstmts.add(is);
			}
			
			
			//now check for finally blocks
			int line=-2;			
			for (Tag tag : s.getTags()) {
				if (tag instanceof LineNumberTag) {
					LineNumberTag t = (LineNumberTag)tag;					
					line = t.getLineNumber();
				} else if (tag instanceof SourceLnNamePosTag) {
					SourceLnNamePosTag t = (SourceLnNamePosTag)tag;
					line = t.startLn();
				}	
			}
			
			
			
//			System.err.println(line+": "+s);
			if (line==old_line && subprog!=null) {
				subprog.add(s);
			} else {
				if (subprog!=null) {
					if (!subprogs.containsKey(old_line)) {
						subprogs.put(old_line, subprog);
					} else {
						if (compareSubprogs(subprog, subprogs.get(old_line))
								&& old_line>=first_trap_line) {
//							System.err.println("P1 " + old_line);
							for (Stmt st : subprogs.get(old_line)) {
//								System.err.println("\t"+st);
								duplicates.add(st);
							}
//							System.err.println("P2 " + old_line);
							for (Stmt st : subprog) {
//								System.err.println("\t"+st);
								duplicates.add(st);
							}							
						}
					}
				}
				subprog = new LinkedList<Stmt>();
				subprog.add(s);
				old_line = line;
			}

		}

		return duplicates;
	}
	
	
	private boolean compareSubprogs(LinkedList<Stmt> p1, LinkedList<Stmt> p2) {		
		LinkedList<Stmt> l1, l2;
		if (p1.size()<p2.size()) {
			l2=p1; l1=p2;
		} else {
			l2=p2; l1=p1;
		}
		
		for (int i=0; i<l1.size();i++) {
			
			if (l1.size()-i<l2.size()) {
				//then they cannot be sublists anymore
				return false;
			}
			boolean sublist = true;
						
			for (int j=0; j<l2.size();j++) {
				if (!shallowCompareStatements(l1.get(i+j),l2.get(j))) {
					sublist = false;
					break;
				}
			}
			if (sublist) {
				return true;
			}
		}
		
		return false;
	}
	
	private Value normalizeNegations(Value v) {
		if (v instanceof NegExpr) {
			return ((NegExpr)v).getOp();
		} else if (v instanceof BinopExpr) {
			BinopExpr bo = (BinopExpr)v;
			if (bo instanceof NeExpr) {
				return new JEqExpr(bo.getOp1(), bo.getOp2());
			}
		}
		return v;
	}
	
	private boolean shallowCompareStatements(Stmt s1, Stmt s2) {
		if (s1.getClass() == s2.getClass()) {
			return true;
		}
		//also consider the case that throw, return, and goto might have been changed in the
		//copies. so do not compare them
		if (isJumpStmt(s1) && isJumpStmt(s2)) {
			return true;
		}
		return false;
	}
	
	private boolean isJumpStmt(Stmt st) {
		return ( st instanceof ThrowStmt || st instanceof GotoStmt || st instanceof ReturnStmt);	
	}
	

	
}
