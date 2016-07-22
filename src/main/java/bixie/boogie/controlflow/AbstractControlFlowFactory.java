
package bixie.boogie.controlflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.ParentEdge;
import bixie.boogie.ast.Unit;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.asttypes.ASTType;
import bixie.boogie.ast.declaration.Axiom;
import bixie.boogie.ast.declaration.ConstDeclaration;
import bixie.boogie.ast.declaration.Declaration;
import bixie.boogie.ast.declaration.FunctionDeclaration;
import bixie.boogie.ast.declaration.ProcedureOrImplementationDeclaration;
import bixie.boogie.ast.declaration.TypeDeclaration;
import bixie.boogie.ast.declaration.VariableDeclaration;
import bixie.boogie.ast.expression.ArrayAccessExpression;
import bixie.boogie.ast.expression.ArrayStoreExpression;
import bixie.boogie.ast.expression.BinaryExpression;
import bixie.boogie.ast.expression.BitVectorAccessExpression;
import bixie.boogie.ast.expression.CodeExpression;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.FunctionApplication;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.expression.IfThenElseExpression;
import bixie.boogie.ast.expression.QuantifierExpression;
import bixie.boogie.ast.expression.UnaryExpression;
import bixie.boogie.ast.expression.WildcardExpression;
import bixie.boogie.ast.expression.literal.BitvecLiteral;
import bixie.boogie.ast.expression.literal.BooleanLiteral;
import bixie.boogie.ast.expression.literal.IntegerLiteral;
import bixie.boogie.ast.expression.literal.RealLiteral;
import bixie.boogie.ast.expression.literal.StringLiteral;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.controlflow.expression.CfgArrayAccessExpression;
import bixie.boogie.controlflow.expression.CfgArrayStoreExpression;
import bixie.boogie.controlflow.expression.CfgBinaryExpression;
import bixie.boogie.controlflow.expression.CfgBitVectorAccessExpression;
import bixie.boogie.controlflow.expression.CfgBitvecLiteral;
import bixie.boogie.controlflow.expression.CfgBooleanLiteral;
import bixie.boogie.controlflow.expression.CfgExpression;
import bixie.boogie.controlflow.expression.CfgFunctionApplication;
import bixie.boogie.controlflow.expression.CfgIdentifierExpression;
import bixie.boogie.controlflow.expression.CfgIfThenElseExpression;
import bixie.boogie.controlflow.expression.CfgIntegerLiteral;
import bixie.boogie.controlflow.expression.CfgQuantifierExpression;
import bixie.boogie.controlflow.expression.CfgRealLiteral;
import bixie.boogie.controlflow.expression.CfgStringLiteral;
import bixie.boogie.controlflow.expression.CfgUnaryExpression;
import bixie.boogie.controlflow.expression.CfgWildcardExpression;
import bixie.boogie.controlflow.statement.CfgStatement;
import bixie.boogie.enums.UnaryOperator;
import bixie.boogie.type.BoogieType;
import bixie.boogie.typechecker.TypeChecker;
import bixie.util.Log;

/**
 * @author schaef
 * 
 */
public abstract class AbstractControlFlowFactory {

	protected TypeChecker typechecker;
	protected HashMap<String, CfgProcedure> procedureGraphs = new HashMap<String, CfgProcedure>();
	protected HashMap<String, CfgFunction> cfgFunctions = new HashMap<String, CfgFunction>();
	protected HashMap<String, BasicBlock> blockMap = new HashMap<String, BasicBlock>();
	protected HashMap<String, CfgVariable> globalVars = new HashMap<String, CfgVariable>();
	protected LinkedList<CfgAxiom> globalAxioms = new LinkedList<CfgAxiom>();

	private HashMap<CfgStatement, Statement> astStatementMap = new HashMap<CfgStatement, Statement>();

	public Statement findAstStatement(CfgStatement stmt) {
		return this.astStatementMap.get(stmt);
	}

	protected void mapCfgToAstStatement(CfgStatement cfg, Statement ast) {
		if (this.astStatementMap.containsKey(cfg)) {
			throw new RuntimeException("double mapping not allowed " + cfg.toString());
		}
		this.astStatementMap.put(cfg, ast);
	}

	public void Hack_ThrowAwayProcedureBody(CfgProcedure proc) {
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> done = new LinkedList<BasicBlock>();
		todo.add(proc.getRootNode());
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			done.add(current);
			for (CfgStatement st : current.getStatements()) {
				this.astStatementMap.remove(st);
			}
			current.setStatements(new LinkedList<CfgStatement>());
			for (BasicBlock succ : current.getSuccessors()) {
				if (!todo.contains(succ) && !done.contains(succ)) {
					todo.push(succ);
				}
			}
		}
		proc.setRootNode(null);
		proc.setExitNode(null);
	}

	// TODO: refactor this into something that is only constructed per procedure

	protected class ProcedureContext {
		public LinkedList<BasicBlock> currentBreakDestinations = new LinkedList<BasicBlock>();
		public BasicBlock currentUnifiedExit = null;
		public HashMap<String, CfgVariable> localVars = null;
		public HashMap<String, CfgVariable> inParamVars = null;
		public HashMap<String, CfgVariable> outParamVars = null;
	}

	protected ProcedureContext context;

	public AbstractControlFlowFactory(Unit astroot, TypeChecker tc) {
		this.typechecker = tc;
		// first, collect all global variables and constants
		for (Declaration decl : astroot.getDeclarations()) {
			if (decl instanceof ConstDeclaration) {
				// TODO: do something with attributes
				ConstDeclaration cdecl = (ConstDeclaration) decl;
				CfgVariable[] vars = varList2CfgVariables(cdecl.getVarList(), true, true, cdecl.isUnique(),
						cdecl.isComplete());
				for (int i = 0; i < cdecl.getVarList().getIdentifiers().length; i++) {
					this.globalVars.put(cdecl.getVarList().getIdentifiers()[i], vars[i]);
				}
			} else if (decl instanceof VariableDeclaration) {
				VariableDeclaration vdecl = (VariableDeclaration) decl;
				CfgVariable[] vars = varList2CfgVariables(vdecl.getVariables(), false, true, false, false);
				for (int i = 0; i < vars.length; i++) {
					this.globalVars.put(vars[i].getVarname(), vars[i]);
				}
			} else if (decl instanceof TypeDeclaration) {
				// do nothing
			}
		}

		// now in a second pass, deal with the parent edges.
		for (Declaration decl : astroot.getDeclarations()) {
			if (decl instanceof ConstDeclaration) {
				ConstDeclaration cdecl = (ConstDeclaration) decl;
				LinkedList<CfgParentEdge> cfgedges = new LinkedList<CfgParentEdge>();
				if (cdecl.getParentInfo() != null) {
					for (ParentEdge edge : cdecl.getParentInfo()) {

						CfgParentEdge pedge = new CfgParentEdge(this.globalVars.get(edge.getIdentifier()),
								edge.isUnique());
						cfgedges.add(pedge);
					}
				}

				for (String s : cdecl.getVarList().getIdentifiers()) {
					this.globalVars.get(s).setParentEdges(cfgedges);
				}
			}
		}

		// after previously collecting all global vars, build the cfg for each
		// procedure
		for (Declaration decl : astroot.getDeclarations()) {
			if (decl instanceof ProcedureOrImplementationDeclaration) {
				ProcedureOrImplementationDeclaration proc = (ProcedureOrImplementationDeclaration) decl;
				if (!this.procedureGraphs.containsKey(proc.getIdentifier())) {
					CfgProcedure cfg = new CfgProcedure(proc.getIdentifier());
					this.procedureGraphs.put(proc.getIdentifier(), cfg);
				}
				constructCfg(proc, this.procedureGraphs.get(proc.getIdentifier()));
				Log.debug("Build CFG for: " + this.procedureGraphs.get(proc.getIdentifier()).getProcedureName());
			} else if (decl instanceof FunctionDeclaration) {
				if (!this.cfgFunctions.containsKey(((FunctionDeclaration) decl).getIdentifier())) {
					CfgFunction cfg = new CfgFunction(((FunctionDeclaration) decl).getIdentifier());
					this.cfgFunctions.put(((FunctionDeclaration) decl).getIdentifier(), cfg);
				}
				constructCfg((FunctionDeclaration) decl,
						this.cfgFunctions.get(((FunctionDeclaration) decl).getIdentifier()));
			} else if (decl instanceof Axiom) {
				Axiom ax = (Axiom) decl;
				CfgAxiom axiom = new CfgAxiom(ax.getLocation(), expression2CfgExpression(ax.getFormula()));
				this.globalAxioms.add(axiom);
			}
		}
	}

	public HashMap<String, CfgVariable> getGlobalVars() {
		return globalVars;
	}

	public Collection<CfgProcedure> getProcedureCFGs() {
		return this.procedureGraphs.values();
	}

	public Collection<CfgAxiom> getGlobalAxioms() {
		return this.globalAxioms;
	}

	public CfgFunction findCfgFunction(String name) {
		if (!this.cfgFunctions.containsKey(name)) {
			return null;
		}
		return this.cfgFunctions.get(name);
	}

	protected void constructCfg(FunctionDeclaration fun, CfgFunction cfgfun) {
		this.context = new ProcedureContext();
		context.inParamVars = new HashMap<String, CfgVariable>();
		CfgVariable[] vars = varList2CfgVariables(fun.getInParams(), false, false, false, false, "$in");
		cfgfun.setInParams(vars);
		for (int i = 0; i < vars.length; i++) {
			context.inParamVars.put(vars[i].getVarname(), vars[i]);
		}
		context.outParamVars = new HashMap<String, CfgVariable>();
		vars = varList2CfgVariables(fun.getOutParam(), false, false, false, false, "$return");
		if (vars.length == 1) {
			cfgfun.setOutParam(vars[0]);
		} else {

			throw new RuntimeException(
					"Function " + cfgfun.getIndentifier() + " has " + vars.length + " instead of 1 out param!");
		}
		for (int i = 0; i < vars.length; i++) {
			context.outParamVars.put(vars[i].getVarname(), vars[i]);
		}
		// reset the locals because CfgFunctions dont have locals.
		context.localVars = new HashMap<String, CfgVariable>();
		context.currentUnifiedExit = null;
		if (fun.getBody() != null) {
			cfgfun.setBody(expression2CfgExpression(fun.getBody()));
		}

		cfgfun.setLocation(fun.getLocation());
	}

	abstract protected void constructCfg(ProcedureOrImplementationDeclaration proc, CfgProcedure cfg);

	protected CfgExpression[] expression2CfgExpression(Expression[] exp) {
		CfgExpression[] ret = new CfgExpression[exp.length];
		for (int i = 0; i < exp.length; i++) {
			ret[i] = expression2CfgExpression(exp[i]);
		}
		return ret;
	}

	protected HashSet<CfgVariable> boundVariables = new HashSet<CfgVariable>();

	protected CfgExpression expression2CfgExpression(Expression exp) {
		if (exp instanceof ArrayAccessExpression) {
			ArrayAccessExpression aee = (ArrayAccessExpression) exp;
			CfgExpression base = expression2CfgExpression(aee.getArray());
			CfgExpression[] indices = expression2CfgExpression(aee.getIndices());
			return new CfgArrayAccessExpression(exp.getLocation(), exp.getType(), base, indices);
		} else if (exp instanceof ArrayStoreExpression) {
			ArrayStoreExpression ase = (ArrayStoreExpression) exp;
			CfgExpression base = expression2CfgExpression(ase.getArray());
			CfgExpression[] indices = expression2CfgExpression(ase.getIndices());
			CfgExpression value = expression2CfgExpression(ase.getValue());
			return new CfgArrayStoreExpression(exp.getLocation(), exp.getType(), base, indices, value);
		} else if (exp instanceof BinaryExpression) {
			BinaryExpression bexp = (BinaryExpression) exp;
			return new CfgBinaryExpression(exp.getLocation(), exp.getType(), bexp.getOperator(),
					expression2CfgExpression(bexp.getLeft()), expression2CfgExpression(bexp.getRight()));
		} else if (exp instanceof BitVectorAccessExpression) {
			BitVectorAccessExpression bva = (BitVectorAccessExpression) exp;
			return new CfgBitVectorAccessExpression(exp.getLocation(), exp.getType(),
					expression2CfgExpression(bva.getBitvec()), bva.getStart(), bva.getEnd());
		} else if (exp instanceof FunctionApplication) {
			FunctionApplication funapp = (FunctionApplication) exp;
			if (!this.cfgFunctions.containsKey(funapp.getIdentifier())) {
				// if the function has not been visited, create a stub here.
				this.cfgFunctions.put(funapp.getIdentifier(), new CfgFunction(funapp.getIdentifier()));
			}
			CfgFunction callee = this.cfgFunctions.get(funapp.getIdentifier());
			CfgExpression[] args = expression2CfgExpression(funapp.getArguments());
			return new CfgFunctionApplication(exp.getLocation(), exp.getType(), callee, args);
		} else if (exp instanceof IdentifierExpression) {
			IdentifierExpression idexp = (IdentifierExpression) exp;
			return new CfgIdentifierExpression(idexp.getLocation(), lookupVariable(idexp.getIdentifier()));
		} else if (exp instanceof IfThenElseExpression) {
			IfThenElseExpression ite = (IfThenElseExpression) exp;
			return new CfgIfThenElseExpression(exp.getLocation(), exp.getType(),
					expression2CfgExpression(ite.getCondition()), expression2CfgExpression(ite.getThenPart()),
					expression2CfgExpression(ite.getElsePart()));
		} else if (exp instanceof QuantifierExpression) {
			QuantifierExpression qexp = (QuantifierExpression) exp;
			qexp.getAttributes();
			BoogieType[] typeparams = null; // TODO

			LinkedList<CfgVariable> params = new LinkedList<CfgVariable>();
			for (int i = 0; i < qexp.getParameters().length; i++) {
				for (CfgVariable var : varList2CfgVariables(qexp.getParameters()[i], false, false, false, false)) {
					params.add(var);
					this.boundVariables.add(var); // add the quantifier
													// variables to the list of
													// bound variables
				}
			}
			CfgVariable[] parameters = params.toArray(new CfgVariable[params.size()]);

			Attribute[] attributes = null; // TODO
			CfgExpression subformula = expression2CfgExpression(qexp.getSubformula());

			for (CfgVariable var : params) {
				if (this.boundVariables.contains(var)) {
					this.boundVariables.remove(var);
					// remove the quantified variables again after the
					// quantified expression has been translated.
				}
			}

			return new CfgQuantifierExpression(exp.getLocation(), exp.getType(), qexp.isUniversal(), typeparams,
					parameters, attributes, subformula);

		} else if (exp instanceof UnaryExpression) {
			UnaryExpression uexp = (UnaryExpression) exp;
			if (uexp.getOperator() == UnaryOperator.ARITHNEGATIVE && uexp.getExpr() instanceof IntegerLiteral) {
				// this is a special case if the integer literal if MIN_LONG.
				// using the unary expression and then trying to parse
				// abs(MIN_LONG)
				// will cause a parse exception because its MAX_LONG+1
				IntegerLiteral il = (IntegerLiteral) uexp.getExpr();
				return new CfgIntegerLiteral(il.getLocation(), il.getType(), Long.parseLong("-" + il.getValue()));
			}
			return new CfgUnaryExpression(exp.getLocation(), exp.getType(), uexp.getOperator(),
					expression2CfgExpression(uexp.getExpr()));
		} else if (exp instanceof WildcardExpression) {
			WildcardExpression wce = (WildcardExpression) exp;
			return new CfgWildcardExpression(wce.getLocation(), wce.getType());
		} else if (exp instanceof BitvecLiteral) {
			BitvecLiteral bvl = (BitvecLiteral) exp;
			return new CfgBitvecLiteral(bvl.getLocation(), bvl.getType(), bvl.getLength(), bvl.getValue());
		} else if (exp instanceof BooleanLiteral) {
			BooleanLiteral bl = (BooleanLiteral) exp;
			return new CfgBooleanLiteral(bl.getLocation(), bl.getType(), bl.getValue());
		} else if (exp instanceof IntegerLiteral) {
			IntegerLiteral il = (IntegerLiteral) exp;
			return new CfgIntegerLiteral(il.getLocation(), il.getType(), Long.parseLong(il.getValue()));
		} else if (exp instanceof RealLiteral) {
			RealLiteral rl = (RealLiteral) exp;
			return new CfgRealLiteral(rl.getLocation(), rl.getType(), rl.getValue());
		} else if (exp instanceof StringLiteral) {
			StringLiteral sl = (StringLiteral) exp;
			return new CfgStringLiteral(sl.getLocation(), sl.getType(), sl.getValue());
		} else if (exp instanceof CodeExpression) {
			Log.error("CodeExpression in CFG not implemented!");
			return new CfgBooleanLiteral(exp.getLocation(), BoogieType.boolType, true);
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	protected BoogieType getBoogieType(ASTType asttype) {
		return this.typechecker.getBoogieType(asttype);
	}

	protected CfgVariable[] lookupVariable(String[] name) {
		CfgVariable[] ret = new CfgVariable[name.length];
		for (int i = 0; i < name.length; i++) {
			ret[i] = lookupVariable(name[i]);
		}
		return ret;
	}

	protected CfgVariable lookupVariable(String name) {
		if (this.boundVariables != null) {
			for (CfgVariable v : this.boundVariables) {
				if (v.getVarname().equals(name))
					return v;
			}
		}
		if (context == null) {
			if (this.globalVars.containsKey(name)) {
				return this.globalVars.get(name);
			} else {
				throw new RuntimeException("Var " + name + " not known");
			}
		}
		if (context.localVars != null && context.localVars.containsKey(name)) {
			return context.localVars.get(name);
		} else if (context.inParamVars != null && context.inParamVars.containsKey(name)) {
			return context.inParamVars.get(name);
		} else if (context.outParamVars != null && context.outParamVars.containsKey(name)) {
			return context.outParamVars.get(name);
		} else if (this.globalVars.containsKey(name)) {
			return this.globalVars.get(name);
		}
		throw new RuntimeException("Variable " + name + " is not known to me.");
	}

	protected CfgVariable[] varList2CfgVariables(VarList[] vl, boolean constant, boolean global, boolean unique,
			boolean complete) {
		return varList2CfgVariables(vl, constant, global, unique, complete, "");
	}

	protected CfgVariable[] varList2CfgVariables(VarList[] vl, boolean constant, boolean global, boolean unique,
			boolean complete, String dummyName) {
		LinkedList<CfgVariable> ret = new LinkedList<CfgVariable>();
		for (int i = 0; i < vl.length; i++) {
			CfgVariable[] tmp = varList2CfgVariables(vl[i], constant, global, unique, complete, dummyName + i);
			for (int j = 0; j < tmp.length; j++)
				ret.add(tmp[j]);
		}
		return ret.toArray(new CfgVariable[ret.size()]);
	}

	protected CfgVariable[] varList2CfgVariables(VarList vl, boolean constant, boolean global, boolean unique,
			boolean complete) {
		return varList2CfgVariables(vl, constant, global, unique, complete, "");
	}

	protected CfgVariable[] varList2CfgVariables(VarList vl, boolean constant, boolean global, boolean unique,
			boolean complete, String dummyName) {
		CfgVariable[] vars;
		if (vl.getIdentifiers().length > 0) {
			vars = new CfgVariable[vl.getIdentifiers().length];
			for (int i = 0; i < vl.getIdentifiers().length; i++) {
				BoogieType type = this.getBoogieType(vl.getType());
				vars[i] = new CfgVariable(vl.getIdentifiers()[i], type, constant, global, unique, complete);
			}
		} else {
			vars = new CfgVariable[] {
					new CfgVariable(dummyName, this.getBoogieType(vl.getType()), constant, global, unique, complete) };
		}
		return vars;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// TODO: print the declared types.
		for (BoogieType bt : this.typechecker.getAllBoogieTypes()) {
			sb.append("type ");
			sb.append(bt.toString());
			sb.append("; \n");
		}
		sb.append("; \n");

		// print the global variables
		for (Entry<String, CfgVariable> entry : this.globalVars.entrySet()) {
			sb.append("var ");
			sb.append(entry.getValue().getVarname());
			sb.append(" : ");
			sb.append(entry.getValue().getType());
			sb.append("; \n");
		}
		sb.append("; \n");

		for (CfgAxiom axiom : this.globalAxioms) {
			sb.append("axiom ");
			sb.append(axiom.getFormula().toString());
			sb.append("; \n");
		}
		sb.append("; \n");

		for (Entry<String, CfgFunction> entry : this.cfgFunctions.entrySet()) {
			sb.append(entry.getValue().toString());
			sb.append("\n");
		}
		sb.append("; \n");

		for (Entry<String, CfgProcedure> entry : this.procedureGraphs.entrySet()) {
			sb.append(entry.getValue().toString());
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Write the boogie program to a file.
	 * 
	 * @param filename
	 */
	public void toFile(String filename) {
		File fpw = new File(filename);

		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(fpw), 
						StandardCharsets.UTF_8), true);) {
			pw.println(this.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
