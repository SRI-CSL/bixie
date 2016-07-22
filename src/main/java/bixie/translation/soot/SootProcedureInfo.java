
package bixie.translation.soot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.declaration.Implementation;
import bixie.boogie.ast.declaration.ProcedureDeclaration;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.specification.Specification;
import bixie.boogie.ast.statement.AssumeStatement;
import bixie.boogie.enums.BinaryOperator;
import bixie.boogie.type.BoogieType;
import bixie.translation.GlobalsCache;
import bixie.translation.util.CustomNullnessAnalysis;
import soot.Local;
import soot.PrimType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import soot.jimple.IfStmt;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.toolkits.exceptions.UnitThrowAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * @author schaef
 * 
 */
public class SootProcedureInfo {

	private HashMap<Local, IdentifierExpression> localVariable = new HashMap<Local, IdentifierExpression>();
	private LinkedList<IdentifierExpression> inParameters;
	private LinkedList<IdentifierExpression> outParameters;
	private LinkedList<Specification> specification;
	private IdentifierExpression returnVariable;
	private IdentifierExpression exceptionVariable;

	private IdentifierExpression containingClassVariable;

	private ExceptionalUnitGraph exceptionalUnitGraph;
	private CustomNullnessAnalysis nullnessAnalysis;

	private IdentifierExpression thisVariable;
	private final SootMethod sootMethod;
	private final String cleanName;

	private ProcedureDeclaration procedureDeclaration;
	private Implementation boogieProcedure = null;

	public HashSet<AssumeStatement> typeAssumptions = new HashSet<AssumeStatement>();
	
	public HashSet<StaticFieldRef> usedStaticFields = new HashSet<StaticFieldRef>(); 
	
	//this is used to suppress false positives originating from
	//else if cases.
	public HashSet<IfStmt> duplicatedIfStatement = new HashSet<IfStmt>(); 

	private LinkedList<IdentifierExpression> idexpFromVarlist(VarList[] vla) {
		LinkedList<IdentifierExpression> ret = new LinkedList<IdentifierExpression>();
		ProgramFactory pf = GlobalsCache.v().getPf();
		for (VarList vl : vla) {
			for (String name : vl.getIdentifiers()) {
				ret.add(pf.mkIdentifierExpression(
						pf.boogieTypeFromAstType(vl.getType()), name, false,
						false, false));
			}
		}
		return ret;
	}

	
	private void assumeParameterType(IdentifierExpression id, Type t) {
		ProgramFactory pf = GlobalsCache.v().getPf();
		//create an assumption about the type to put in the body.
		if (t instanceof RefType) {				
			RefType rtype = (RefType) t;
			Expression rhs = GlobalsCache.v().lookupClassVariable(
					rtype.getSootClass());
			Expression ofType = pf.mkBinaryExpression(
					pf.getBoolType(), BinaryOperator.COMPPO,
					SootPrelude.v().heapAccess(id, SootPrelude.v()
							.getFieldClassVariable()), rhs);			
			typeAssumptions.add((AssumeStatement) pf.mkAssumeStatement(new Attribute[0], ofType));
		}		
	}
	
	public SootProcedureInfo(SootMethod m) {
		ProgramFactory pf = GlobalsCache.v().getPf();

		Attribute[] attributes = TranslationHelpers.javaLocation2Attribute(m
				.getTags());

		// Check if this procedure has already been declared in the prelude:
		this.sootMethod = m;
		this.cleanName = TranslationHelpers.getQualifiedName(this.sootMethod);

		// If the procedure has already been declared in the prelude, use this
		// information instead.
		ProcedureDeclaration decl = pf.findProcedureDeclaration(this.cleanName);
		if (decl != null) {

			this.specification = new LinkedList<Specification>(
					Arrays.asList(decl.getSpecification()));
			this.inParameters = new LinkedList<IdentifierExpression>(
					idexpFromVarlist(decl.getInParams()));
			this.outParameters = new LinkedList<IdentifierExpression>(
					idexpFromVarlist(decl.getOutParams()));
			this.procedureDeclaration = decl;
			return;
		}
		if (this.cleanName
				.contains("java.lang.Object$java.lang.Object$clone$43"))
			throw new RuntimeException(
					"This was in the prelude! should not reach this line! "
							+ this.cleanName);

		this.specification = new LinkedList<Specification>();
		this.inParameters = new LinkedList<IdentifierExpression>();
		this.containingClassVariable = GlobalsCache.v().lookupClassVariable(
				this.sootMethod.getDeclaringClass());

		if (!this.sootMethod.isStatic()) {
			this.thisVariable = pf.mkIdentifierExpression(SootPrelude.v()
					.getReferenceType(), "$this", false, false, false);
			this.inParameters.add(this.thisVariable);
		} else {
			this.thisVariable = null;
		}

		if (this.sootMethod.isAbstract()) {
			// if the method is abstract assume the worst possible modifies
			// clause
			this.specification.add(pf.mkModifiesSpecification(true,
					new IdentifierExpression[] { SootPrelude.v()
							.getHeapVariable() }));
		}

		// collect the annotations
		LinkedList<LinkedList<SootAnnotations.Annotation>> pannot = SootAnnotations
				.parseParameterAnnotations(this.sootMethod);

		for (int i = 0; i < this.sootMethod.getParameterCount(); i++) {
			BoogieType type = GlobalsCache.v().getBoogieType(
					this.sootMethod.getParameterType(i));

			String param_name = "$in_parameter__" + i;
			IdentifierExpression id = pf.mkIdentifierExpression(type,
					param_name, false, false, false);
			this.inParameters.add(id);

			assumeParameterType(id, this.sootMethod.getParameterType(i));
			
			// add precondition if parameter has @NonNull annotation
			if (pannot.size() != 0) { // may be null if there are no annotations
										// at all
				LinkedList<SootAnnotations.Annotation> annot = pannot.get(i);
				if (annot.contains(SootAnnotations.Annotation.NonNull)) {
					Expression formula = pf.mkBinaryExpression(
							pf.getBoolType(), BinaryOperator.COMPNEQ, id,
							SootPrelude.v().getNullConstant());

					this.specification.add(pf.mkRequiresSpecification(
							attributes, false, formula));
				}
			}

		}

		this.outParameters = new LinkedList<IdentifierExpression>();
		if (!(this.sootMethod.getReturnType() instanceof VoidType)) {

			Type returntype = this.sootMethod.getReturnType();
			BoogieType type = GlobalsCache.v().getBoogieType(returntype);
			String param_name = "$return";
			IdentifierExpression id = pf.mkIdentifierExpression(type,
					param_name, false, false, false);
			this.outParameters.add(id);
			this.returnVariable = id;

			assumeParameterType(id, returntype);
			
			// compute the type variable if the method return a reference type
			// this is used to ensure the type in assume-guarantee reasoning
//			this.returnTypeVariable = null;
//			if (returntype instanceof RefType) {
//				RefType rt = (RefType) returntype;
//				this.returnTypeVariable = GlobalsCache.v().lookupClassVariable(
//						rt.getSootClass());
//			}

			// only makes sense to add non-null annotation for non-prim types
			if (!(returntype instanceof PrimType)) {
				// check if the procedure retrun has @NonNull annotation
				LinkedList<SootAnnotations.Annotation> annot = null;
				for (Tag tag : this.sootMethod.getTags()) {
					if (tag instanceof VisibilityAnnotationTag) {
						if (annot != null) {
//							Log.error("Didn't expect so many tags for procedure! Check that!");
							break;
						}
						annot = SootAnnotations
								.parseAnnotations((VisibilityAnnotationTag) tag);
					}
				}
				if (annot != null
						&& annot.contains(SootAnnotations.Annotation.NonNull)) {

					Expression formula = pf.mkBinaryExpression(
							pf.getBoolType(), BinaryOperator.COMPNEQ,
							this.returnVariable, SootPrelude.v()
									.getNullConstant());
					this.specification.add(pf.mkEnsuresSpecification(
							attributes, false, formula));
				}
			}
		} else {
			this.returnVariable = null;
		}
		/*
		 * We create an additional out variable called $exception. This variable
		 * is used to handle all kinds of exceptions that may be thrown in the
		 * procedure.
		 */
		String exname = "$exception";
		IdentifierExpression id = pf.mkIdentifierExpression(SootPrelude.v()
				.getReferenceType(), exname, false, false, false);
		this.outParameters.add(id);
		this.exceptionVariable = id;

		Specification[] spec = this.specification
				.toArray(new Specification[this.specification.size()]);

		this.procedureDeclaration = pf.mkProcedureDeclaration(this.cleanName,
				this.getInParamters(), this.getOutParamters(), spec);

		// TODO: make the exceptional return flag a postcondition instead!

		// now create the exceptional unit graph, which will be used later to
		// check
		// where throw statements can jump to
		if (this.sootMethod.hasActiveBody()) {
			this.exceptionalUnitGraph = new ExceptionalUnitGraph(
					this.sootMethod.getActiveBody(), UnitThrowAnalysis.v());
			this.nullnessAnalysis = new CustomNullnessAnalysis(this.exceptionalUnitGraph);
		} else {
			this.exceptionalUnitGraph = null;
			this.nullnessAnalysis = null;
		}
	}
	

	
	public SootMethod getSootMethod() {
		return this.sootMethod;
	}

	public String getBoogieName() {
		return cleanName;
	}

	public List<SootClass> getThrowsClasses() {
		return this.sootMethod.getExceptions();
	}

	public IdentifierExpression[] getLocalVariables() {
		HashSet<IdentifierExpression> alllocals = new HashSet<IdentifierExpression>(
				this.localVariable.values());
		alllocals.addAll(this.fakeLocals);
		return alllocals.toArray(new IdentifierExpression[alllocals.size()]);
	}

	public IdentifierExpression[] getInParamters() {
		return this.inParameters
				.toArray(new IdentifierExpression[this.inParameters.size()]);
	}

	public IdentifierExpression[] getOutParamters() {
		return this.outParameters
				.toArray(new IdentifierExpression[this.outParameters.size()]);
	}

	public IdentifierExpression getReturnVariable() {
		return this.returnVariable;
	}

	public IdentifierExpression getExceptionVariable() {
		return this.exceptionVariable;
	}

	public IdentifierExpression getThisReference() {
		return thisVariable;
	}

	public IdentifierExpression getContainingClassVariable() {
		return containingClassVariable;
	}

	public ExceptionalUnitGraph getExceptionalUnitGraph() {
		return exceptionalUnitGraph;
	}

	public void setExceptionalUnitGraph(ExceptionalUnitGraph tug) {
		exceptionalUnitGraph = tug;
	}

	
	public CustomNullnessAnalysis getNullnessAnalysis() {
		return this.nullnessAnalysis;
	}
	
	public boolean isStatic() {
		return this.sootMethod.isStatic();
	}

	//public IdentifierExpression thisRefLocal = null;
	
	
	public IdentifierExpression lookupLocalVariable(Local local) {
		if (!this.localVariable.containsKey(local)) {
			BoogieType type = GlobalsCache.v().getBoogieType(local.getType());
			String cleanname = TranslationHelpers.getQualifiedName(local);
			IdentifierExpression id = GlobalsCache
					.v()
					.getPf()
					.mkIdentifierExpression(type, cleanname, false, false,
							false);
			this.localVariable.put(local, id);
		}
		return this.localVariable.get(local);
	}

	private int fakeLocalCount = 0;
	private HashSet<IdentifierExpression> fakeLocals = new HashSet<IdentifierExpression>();

	public IdentifierExpression createLocalVariable(BoogieType type) {
		IdentifierExpression id = GlobalsCache
				.v()
				.getPf()
				.mkIdentifierExpression(type,
						"$fakelocal_" + (fakeLocalCount++), false, false, false);
		this.fakeLocals.add(id);
		return id;
	}

	public IdentifierExpression lookupParameterVariable(ParameterRef param) {
		return this.inParameters.get(param.getIndex()
				+ ((this.sootMethod.isStatic()) ? 0 : 1));
	}

	public ProcedureDeclaration getProcedureDeclaration() {
		return this.procedureDeclaration;
	}

	public Implementation getBoogieProcedure() {
		return boogieProcedure;
	}

	public void setProcedureImplementation(Implementation impl) {
		this.boogieProcedure = impl;
	}

}
