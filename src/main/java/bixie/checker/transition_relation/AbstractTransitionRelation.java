/**
 * 
 */
package bixie.checker.transition_relation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joogie.cfgPlugin.CFGPlugin;
import org.joogie.cfgPlugin.Util.Dag;

import util.Log;
import ap.parser.IFormula;
import bixie.checker.GlobalsCache;
import bixie.prover.Prover;
import bixie.prover.ProverExpr;
import bixie.prover.ProverFun;
import bixie.prover.ProverType;
import bixie.prover.princess.PrincessProver;
import boogie.ProgramFactory;
import boogie.controlflow.AbstractControlFlowFactory;
import boogie.controlflow.BasicBlock;
import boogie.controlflow.CfgAxiom;
import boogie.controlflow.CfgFunction;
import boogie.controlflow.CfgParentEdge;
import boogie.controlflow.CfgProcedure;
import boogie.controlflow.CfgVariable;
import boogie.controlflow.expression.CfgArrayAccessExpression;
import boogie.controlflow.expression.CfgArrayStoreExpression;
import boogie.controlflow.expression.CfgBinaryExpression;
import boogie.controlflow.expression.CfgBooleanLiteral;
import boogie.controlflow.expression.CfgExpression;
import boogie.controlflow.expression.CfgFunctionApplication;
import boogie.controlflow.expression.CfgIdentifierExpression;
import boogie.controlflow.expression.CfgIfThenElseExpression;
import boogie.controlflow.expression.CfgIntegerLiteral;
import boogie.controlflow.expression.CfgQuantifierExpression;
import boogie.controlflow.expression.CfgUnaryExpression;
import boogie.controlflow.statement.CfgAssertStatement;
import boogie.controlflow.statement.CfgAssignStatement;
import boogie.controlflow.statement.CfgAssumeStatement;
import boogie.controlflow.statement.CfgHavocStatement;
import boogie.controlflow.statement.CfgStatement;
import boogie.controlflow.util.HasseDiagram;
import boogie.type.ArrayType;
import boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class AbstractTransitionRelation {

	
	protected Prover prover;
	protected CfgProcedure procedure;
	// TODO: bad idea to use HashMap<Integer, ProverExpr> because
	// we want to be able to iterate over the iterations of a variable
	protected HashMap<CfgVariable, SortedMap<Integer, ProverExpr>> proverVariables = new HashMap<CfgVariable, SortedMap<Integer, ProverExpr>>();
	protected HashMap<String, ProverFun> proverFunctions = new HashMap<String, ProverFun>();
	protected AbstractControlFlowFactory controlFlowFactory;
	protected ProverFun partialOrderOperator;
	protected HashMap<CfgAxiom, ProverExpr> preludeAxioms = new HashMap<CfgAxiom, ProverExpr>();

	protected HashMap<BasicBlock, LinkedList<ProverExpr>> proofObligations = new HashMap<BasicBlock, LinkedList<ProverExpr>>();
	protected HashMap<BasicBlock, ProverExpr> reachabilityVariables = new HashMap<BasicBlock, ProverExpr>();
	
	protected ProverExpr requires, ensures;

	private HashSet<CfgVariable> usedPOVariables = new HashSet<CfgVariable>();

	// helper maps for subsitution.
	protected HashMap<ProverExpr, CfgVariable> invertProverVariables = new HashMap<ProverExpr, CfgVariable>();
	protected HashMap<ProverExpr, Integer> invertIncarnationMap = new HashMap<ProverExpr, Integer>();
	
	protected HasseDiagram hasse;
	
	protected String procedureName;
	
	
	public AbstractTransitionRelation(CfgProcedure cfg, AbstractControlFlowFactory cff, Prover p) {
		this.prover = p;
		this.controlFlowFactory = cff;
		this.procedure = cfg;
		this.hasse = new HasseDiagram(cfg);
		this.procedureName = cfg.getProcedureName();		
	}
	
	public AbstractControlFlowFactory getControlFlowFactory() {
		return this.controlFlowFactory;
	}
	
	public CfgProcedure getProcedure() {
		return this.procedure;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getProcedureName() {
		return this.procedureName;
	}

	/**
	 * Returns the prover expression of the ssa-version of the precondition.
	 * This is meant to be asserted with the verification condition, it is not what
	 * you want to use on the caller side.
	 * @return ProverExpr of the procedure precondition
	 */
	public ProverExpr getRequires() {
		return this.requires;
	}

	/**
	 * Returns the prover expression of the ssa-version of the postcondition.
	 * This is meant to be asserted with the verification condition, it is not what
	 * you want to use on the caller side.
	 * @return ProverExpr of the procedure postcondition
	 */	
	public ProverExpr getEnsures() {
		return this.ensures;
	}
	
	public Set<BasicBlock> getEffectualSet() {
		return this.hasse.getEffectualSet();
	}
	
	public HasseDiagram getHasseDiagram() {
		return this.hasse;
	}
	
	public ProverExpr getProverExpr(CfgVariable v, Integer i) {
		if (i == null)
			return null;
		// TODO DSN fix this to not crash if non existant.
		SortedMap<Integer, ProverExpr> m = proverVariables.get(v);
		if (m == null) {
			return null;
		} else {
			return m.get(i);
		}
	}

//	public HashMap<ProverExpr, CfgVariable> getInvertProverVariables() {
//		return invertProverVariables;
//	}
//
//	public HashMap<ProverExpr, Integer> getInvertIncarnationMap() {
//		return invertIncarnationMap;
//	}

	/**
	 * returns the map from BasicBlocks to their corresponding ProverExpression
	 * 
	 * @return
	 */
	public HashMap<BasicBlock, ProverExpr> getReachabilityVariables() {
		return reachabilityVariables;
	}

	/**
	 * This returns a map from basic block to the corresponding proof
	 * obligation. The conjunction of all proof obligations together with the
	 * prelude axioms is the verification condition.
	 * 
	 * @return
	 */
	public HashMap<BasicBlock, LinkedList<ProverExpr>> getProofObligations() {
		return proofObligations;
	}

	/**
	 * This returns the assertions for all axioms The conjunction of all proof
	 * obligations together with the prelude axioms is the verification
	 * condition.
	 * 
	 * @return
	 */
	public HashMap<CfgAxiom, ProverExpr> getPreludeAxioms() {
		return preludeAxioms;
	}




	protected void makePrelude() {
		ProverType[] argTypes = { this.prover.getIntType(),
				this.prover.getIntType() };
		this.partialOrderOperator = this.prover.mkUnintFunction("$poCompare",
				argTypes, this.prover.getBooleanType());

		// Full partial order axioms
		// Does not work very well in this version, too many axioms, slows
		// down everything drastically
		//
		//genFullPOConstraints();

		// First, translate all prelude axioms
		// TODO: This could be done more efficiently.
		for (CfgAxiom axiom : this.controlFlowFactory.getGlobalAxioms()) {
			this.preludeAxioms.put(axiom,
					expression2proverExpression(axiom.getFormula()));
		}

	}

	protected Dag<IFormula> procToPrincessDag(CfgProcedure proc,
			HashMap<BasicBlock, ProverExpr> reachVars) {
		// First transform the CFG into a list and record
		// the index of each block
		// it is imporatant that the list starts with the
		// exitblock
		
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		LinkedList<BasicBlock> done = new LinkedList<BasicBlock>();
		todo.add(proc.getRootNode());
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pollLast();
			boolean allDone = true;
			for (BasicBlock pre : current.getPredecessors()) {
				if (!done.contains(pre)) {
					allDone = false;
					continue;
				}
			}
			if (!allDone) {
				todo.addFirst(current);
				continue;
			}
			// store the position the block will have in the 'done' list.
			done.addLast(current);
			for (BasicBlock suc : current.getSuccessors()) {
				if (!todo.contains(suc) && !done.contains(suc)) {
					if (suc != current) {
						todo.addLast(suc);
					} else {
						// This has to be checked
						Log.debug("The node has a self-loop! This is not supposed to happen.");
					}
				}
			}
		}

		Dag<IFormula> currentNode = CFGPlugin.mkDagEmpty();
		// TODO: assert that the first one in the list is actually the ExitBlock
		for (int j = done.size() - 1; j >= 0; j--) {
			BasicBlock b = done.get(j);
			List<Integer> succIndices = new LinkedList<Integer>();
			for (BasicBlock suc : b.getSuccessors()) {
				// TODO: @Philipp willst du die absolute position oder den
				// offset?
				int idx = done.indexOf(suc) - done.indexOf(b);
				succIndices.add(idx);
			}
			// TODO: review. can be done better
			if (reachVars.get(b)==null) throw new RuntimeException("Cannot find var for "+b.getLabel());
			IFormula d = ((PrincessProver) this.prover)
					.proverExpToIFormula(reachVars.get(b));
			int[] succidx = new int[succIndices.size()];
			for (int i = 0; i < succIndices.size(); i++) {
				succidx[i] = succIndices.get(i);
			}
			currentNode = CFGPlugin.mkDagNode(d, succidx, currentNode);
		}
		// currentNode.prettyPrint();
		return currentNode;
	}
	/*
	 * private void genFullPOConstraints() { //TODO: @Philipp:
	 * this.usedPOVariables haelt alle variablen die in dem aktuellen //scope
	 * benutzt werden und ParentEdges haben. final Prover p = this.prover; final
	 * ProverFun po = this.partialOrderOperator; final ProverType IT =
	 * p.getIntType(); final ProverExpr v0 = p.mkBoundVariable(0,
	 * p.getIntType()); final ProverExpr v1 = p.mkBoundVariable(1,
	 * p.getIntType()); final ProverExpr v2 = p.mkBoundVariable(2,
	 * p.getIntType());
	 * 
	 * { // $poCompare is a reflexive, transitive, anti-symmetric relation
	 * 
	 * // reflexivity p.addAssertion(p.mkAll(po.mkExpr(new ProverExpr[] { v0, v0
	 * }), IT));
	 * 
	 * // transitivity p.addAssertion(p.mkAll(p.mkAll(p.mkAll(p.mkTrigger(
	 * p.mkImplies( p.mkAnd(po.mkExpr(new ProverExpr[] { v0, v1 }),
	 * po.mkExpr(new ProverExpr[] { v1, v2 })), po.mkExpr(new ProverExpr[] { v0,
	 * v2 })), new ProverExpr[] { // Triggers po.mkExpr(new ProverExpr[] { v0,
	 * v1 }), po.mkExpr(new ProverExpr[] { v1, v2 }) }), IT), IT), IT));
	 * 
	 * // anti-symmetry p.addAssertion(p.mkAll(p.mkAll(p.mkTrigger( p.mkImplies(
	 * p.mkAnd(po.mkExpr(new ProverExpr[] { v0, v1 }), po.mkExpr(new
	 * ProverExpr[] { v1, v0 })), p.mkEq(v0, v1)), new ProverExpr[] { //
	 * Triggers po.mkExpr(new ProverExpr[] { v0, v1 }), po.mkExpr(new
	 * ProverExpr[] { v1, v0 }) }), IT), IT)); }
	 * 
	 * // Declare constants // TODO: we need axioms about uniqueness for
	 * (Entry<String, CfgVariable> entry :
	 * this.controlFlowFactory.getGlobalVars().entrySet()) { CfgVariable var =
	 * entry.getValue(); if (var.isConstant() &&
	 * !var.getParentEdges().isEmpty()) { createProverVar(var, 0); for
	 * (CfgParentEdge edge : var.getParentEdges()) { final CfgVariable parent =
	 * edge.getVaraible(); createProverVar(parent, 0); } } }
	 * 
	 * // Add partial-order constraints for (Entry<String, CfgVariable> entry :
	 * this.controlFlowFactory.getGlobalVars().entrySet()) { CfgVariable var =
	 * entry.getValue(); if (var.isConstant() &&
	 * !var.getParentEdges().isEmpty()) { final ProverExpr proverVar =
	 * createProverVar(var, 0);
	 * 
	 * ProverExpr parentDisj = p.mkEq(v0, proverVar);
	 * 
	 * for (CfgParentEdge edge : var.getParentEdges()) { final CfgVariable
	 * parent = edge.getVaraible(); final ProverExpr parentVar =
	 * createProverVar(parent, 0); p.addAssertion(po.mkExpr(new ProverExpr[] {
	 * proverVar, parentVar })); parentDisj = p.mkOr(parentDisj, po.mkExpr(new
	 * ProverExpr[] { parentVar, v0 })); }
	 * 
	 * p.addAssertion(p.mkAll(p.mkTrigger( p.mkImplies(po.mkExpr(new
	 * ProverExpr[] { proverVar, v0 }), parentDisj), new ProverExpr[] {
	 * po.mkExpr(new ProverExpr[] { proverVar, v0 }) }), IT)); } } }
	 */

	protected void finalizeAxioms() {
		genGroundPOConstraints();
	}

	private void genGroundPOConstraints() {
		final Prover p = this.prover;
		final ProverFun po = this.partialOrderOperator;

		for (CfgVariable var : new LinkedList<CfgVariable>(usedPOVariables)) {
			if (var.isConstant() && var.isGlobal()) {
				// transitively compute all parents
				final LinkedHashSet<CfgVariable> todo = new LinkedHashSet<CfgVariable>();
				final LinkedHashSet<CfgVariable> ancestors = new LinkedHashSet<CfgVariable>();

				todo.add(var);
				while (!todo.isEmpty()) {
					CfgVariable v = todo.iterator().next();
					todo.remove(v);
					if (ancestors.add(v)) {
						for (CfgParentEdge edge : v.getParentEdges())
							todo.add(edge.getVaraible());
					}
				}

				// add constraints
				for (CfgVariable var2 : new LinkedList<CfgVariable>(usedPOVariables)) {
					final ProverExpr pred = po
							.mkExpr(new ProverExpr[] { createProverVar(var, 0),
									createProverVar(var2, 0) });
					if (ancestors.contains(var2)) {
						//System.err.println("  "+pred);
						p.addAssertion(pred);
					} else {
						//System.err.println("  !"+pred);
						p.addAssertion(p.mkNot(pred));
					}
				}
			} else {
				throw new RuntimeException ("unexpected.");
			}
		}
	}

	public HashMap<ProverExpr, CfgStatement> pe2StmtMap = new HashMap<ProverExpr, CfgStatement>();
	
	protected List<ProverExpr> statements2proverExpression(List<CfgStatement> stmts) {
		LinkedList<ProverExpr> res = new LinkedList<ProverExpr>(); 
		for (CfgStatement s : stmts) {
			if (s instanceof CfgAssumeStatement 
					&& ((CfgAssumeStatement)s).getCondition() instanceof CfgBooleanLiteral 
					&& ((CfgBooleanLiteral)((CfgAssumeStatement)s).getCondition()).getValue()==true) {
				//do nothing
				continue;
			}
			if (s instanceof CfgAssertStatement 
					&& ((CfgAssertStatement)s).getCondition() instanceof CfgBooleanLiteral 
					&& ((CfgBooleanLiteral)((CfgAssertStatement)s).getCondition()).getValue()==true) {
				//do nothing
				continue;
			}
			ProverExpr pe = statement2proverExpression(s);
			this.pe2StmtMap.put(pe, s);
			res.add(pe);
		}
		return res;
	}
	
	protected ProverExpr statement2proverExpression(CfgStatement s) {
		if (s instanceof CfgAssertStatement) {
			CfgAssertStatement assrt = (CfgAssertStatement) s;
			return expression2proverExpression(assrt.getCondition());
		} else if (s instanceof CfgAssignStatement) {
			CfgAssignStatement assgn = (CfgAssignStatement) s;
			if (assgn.getLeft().length != assgn.getRight().length) {
				throw new RuntimeException("malformed assignment.");
			}
			ProverExpr[] conj = new ProverExpr[assgn.getLeft().length];
			for (int i = 0; i < assgn.getLeft().length; i++) {
				ProverExpr left = expression2proverExpression(assgn.getLeft()[i]);
				ProverExpr right = expression2proverExpression(assgn.getRight()[i]);
				conj[i] = this.prover.mkEq(left, right);
			}
			return this.prover.mkAnd(conj);

		} else if (s instanceof CfgAssumeStatement) {
			CfgAssumeStatement assme = (CfgAssumeStatement) s;
			return expression2proverExpression(assme.getCondition());
		} else if (s instanceof CfgHavocStatement) {
			// s Log.error("BUG: no havoc should be in the passive program!");
			// Havoc is a no-op after SSA, so no need to keep it
			// in the transition relation
			return prover.mkLiteral(true);
		} else {
			//E.g. s instanceof CfgCallStatement
			throw new RuntimeException("Unknown statement type: "
					+ s.getClass().toString());
		}
	}

	/**
	 * wraps an expression into an int-type expression using a cast TODO: only
	 * implemented for boolean
	 * 
	 * @param e
	 *            the expression
	 * @param type
	 *            the type of the expression
	 * @return a new expression casting e to int
	 */
	protected ProverExpr wrapInInt(ProverExpr e, BoogieType type) {
		if (type == null) throw new RuntimeException("wrapInInt has type null! Did you forget to run the typechecker?");
		if (GlobalsCache.v().getProgramFactory()!=null && type == GlobalsCache.v().getProgramFactory().getBoolType())
			return this.prover.mkIte(e, this.prover.mkLiteral(0),
					this.prover.mkLiteral(1));
		else
			return e;
	}

	/**
	 * casts expressions of Bool sort to Int expressions
	 * 
	 * @param e
	 * @param type
	 * @return
	 */
	protected ProverExpr unwrapFromInt(ProverExpr e, BoogieType type) {
		if (type == null) throw new RuntimeException("wrapInInt has type null! Did you forget to run the typechecker? "+e.toString() );
		if (GlobalsCache.v().getProgramFactory()!=null && type == GlobalsCache.v().getProgramFactory().getBoolType())
			return this.prover.mkEq(e, this.prover.mkLiteral(0));
		else
			return e;
	}

	protected ProverExpr expression2proverExpression(CfgExpression e) {
		return expression2proverExpression(e,
				new LinkedHashMap<CfgVariable, ProverExpr>());
	}

	protected ProverExpr expression2proverExpression(CfgExpression e,
			LinkedHashMap<CfgVariable, ProverExpr> boundVariables) {
		if (e instanceof CfgArrayAccessExpression) {
			CfgArrayAccessExpression exp = (CfgArrayAccessExpression) e;
			ProverExpr ar = expression2proverExpression(
					exp.getBaseExpression(), boundVariables);
			ProverExpr[] indexes = new ProverExpr[exp.getIndices().length];
			for (int i = 0; i < indexes.length; i++) {
				CfgExpression ind = exp.getIndices()[i];
				indexes[i] = wrapInInt(
						expression2proverExpression(ind, boundVariables),
						ind.getType());
			}
			return unwrapFromInt(this.prover.mkSelect(ar, indexes), e.getType());
		} else if (e instanceof CfgArrayStoreExpression) {
			CfgArrayStoreExpression exp = (CfgArrayStoreExpression) e;
			ProverExpr ar = expression2proverExpression(
					exp.getBaseExpression(), boundVariables);
			ProverExpr[] indexes = new ProverExpr[exp.getIndices().length];
			for (int i = 0; i < indexes.length; i++) {
				CfgExpression ind = exp.getIndices()[i];
				indexes[i] = wrapInInt(
						expression2proverExpression(ind, boundVariables),
						ind.getType());
			}
			CfgExpression val = exp.getValueExpression();
			ProverExpr value = wrapInInt(
					expression2proverExpression(val, boundVariables),
					val.getType());
			return this.prover.mkStore(ar, indexes, value);
		} else if (e instanceof CfgBinaryExpression) {
			CfgBinaryExpression exp = (CfgBinaryExpression) e;
			return binopExpression2proverExpression(exp, boundVariables);
		} else if (e instanceof CfgBooleanLiteral) {
			CfgBooleanLiteral exp = (CfgBooleanLiteral) e;
			return this.prover.mkLiteral(exp.getValue());
		} else if (e instanceof CfgFunctionApplication) {
			CfgFunctionApplication exp = (CfgFunctionApplication) e;
			return functionApplication2proverExpression(exp, boundVariables);
		} else if (e instanceof CfgIdentifierExpression) {
			CfgIdentifierExpression exp = (CfgIdentifierExpression) e;
			return indentifierExpression2proverExpression(exp, boundVariables);
		} else if (e instanceof CfgIfThenElseExpression) {
			CfgIfThenElseExpression exp = (CfgIfThenElseExpression) e;
			return this.prover.mkIte(
					expression2proverExpression(exp.getCondition(),
							boundVariables),
					expression2proverExpression(exp.getThenExpression(),
							boundVariables),
					expression2proverExpression(exp.getElseExpression(),
							boundVariables));
		} else if (e instanceof CfgIntegerLiteral) {
			CfgIntegerLiteral exp = (CfgIntegerLiteral) e;
			return this.prover.mkLiteral(exp.getValue().intValue());
		} else if (e instanceof CfgQuantifierExpression) {
			CfgQuantifierExpression exp = (CfgQuantifierExpression) e;
			LinkedHashMap<CfgVariable, ProverExpr> boundVariables2 = new LinkedHashMap<CfgVariable, ProverExpr>(boundVariables); 
			for (CfgVariable cfgvar : exp.getParameters()) {
				boundVariables2.put(cfgvar, createProverVar(cfgvar, 0));
			}
			ProverExpr body = expression2proverExpression(exp.getSubformula(), boundVariables2);
			ProverType type = boogieType2ProverType(exp.getType());
			if (exp.isUniversal()) {
				return this.prover.mkAll(body, type);
			} else {
				return this.prover.mkEx(body, type);
			}
		} else if (e instanceof CfgUnaryExpression) {
			CfgUnaryExpression exp = (CfgUnaryExpression) e;
			if (exp.getOperator() == boogie.enums.UnaryOperator.ARITHNEGATIVE) {
				return this.prover.mkMult(
						expression2proverExpression(exp.getExpression(),
								boundVariables), this.prover.mkLiteral(-1));
			} else if (exp.getOperator() == boogie.enums.UnaryOperator.LOGICNEG) {
				return this.prover.mkNot(expression2proverExpression(
						exp.getExpression(), boundVariables));
			} else {
				throw new RuntimeException("Unknown Unary Operator "+e);
			}
		} else {
			throw new RuntimeException("Unknown CfgExpression type "
					+ e.getClass().toString());
		}
	}
	
	private boolean insideOldExpression = false;

	protected ProverExpr binopExpression2proverExpression(
			CfgBinaryExpression exp,
			LinkedHashMap<CfgVariable, ProverExpr> boundVariables) {
		ProverExpr left = expression2proverExpression(exp.getLeftOp(),
				boundVariables);
		ProverExpr right = expression2proverExpression(exp.getRightOp(),
				boundVariables);
//		if (exp.getOperator() == boogie.enums.BinaryOperator.ARITHDIV) {
//			return this.prover.mkTDiv(left, right);
		if (exp.getOperator() == boogie.enums.BinaryOperator.ARITHMINUS) {
			return this.prover.mkMinus(left, right);
//		} else if (exp.getOperator() == boogie.enums.BinaryOperator.ARITHMOD) {
//			return this.prover.mkTMod(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.ARITHMUL) {
			return this.prover.mkMult(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.ARITHPLUS) {
			return this.prover.mkPlus(left, right);
//		} else if (exp.getOperator() == boogie.enums.BinaryOperator.BITVECCONCAT) {
//			throw new RuntimeException("BITVECCONCAT not imeplemented");
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.COMPEQ) {
			return this.prover.mkEq(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.COMPGEQ) {
			return this.prover.mkGeq(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.COMPGT) {
			return this.prover.mkGt(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.COMPLEQ) {
			return this.prover.mkLeq(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.COMPLT) {
			return this.prover.mkLt(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.COMPNEQ) {
			return this.prover.mkNot(this.prover.mkEq(left, right));
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.COMPPO) {
			ProverExpr[] args = { left, right };
			ProverExpr pe = this.partialOrderOperator.mkExpr(args);			
			return pe;
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.LOGICAND) {
			return this.prover.mkAnd(left, right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.LOGICIFF) {				
			ProverExpr l = this.prover.mkImplies(left, right);
			ProverExpr r = this.prover.mkImplies(right, left);
			return this.prover.mkAnd(l, r);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.LOGICIMPLIES) {
			return this.prover.mkOr(this.prover.mkNot(left), right);
		} else if (exp.getOperator() == boogie.enums.BinaryOperator.LOGICOR) {
			return this.prover.mkOr(left, right);
		} else {
			throw new RuntimeException("Unknown binary operator: "
					+ exp.getOperator());
		}
	}

	protected ProverExpr functionApplication2proverExpression(
			CfgFunctionApplication exp,
			LinkedHashMap<CfgVariable, ProverExpr> boundVariables) {
		String funname = sanitizeName(exp.getFunction().getIndentifier());
		if (!this.proverFunctions.containsKey(funname)) {

			ProverType[] argTypes = new ProverType[exp.getFunction()
					.getInParams().length];
			for (int i = 0; i < exp.getFunction().getInParams().length; i++) {
				argTypes[i] = boogieType2ProverType(exp.getFunction()
						.getInParams()[i].getType());
			}

			ProverFun fun;

			CfgFunction cfgfun = this.controlFlowFactory.findCfgFunction(exp
					.getFunction().getIndentifier());
			if (cfgfun != null && cfgfun.getBody() != null) {
				LinkedHashMap<CfgVariable, ProverExpr> localbound = new LinkedHashMap<CfgVariable, ProverExpr>();
				int boundvarcounter = 0;
				for (CfgVariable invar : cfgfun.getInParams()) {
					localbound.put(invar, this.prover.mkBoundVariable(
							boundvarcounter++,
							boogieType2ProverType(invar.getType())));
				}
				ProverExpr body = expression2proverExpression(cfgfun.getBody(),
						localbound);
				fun = this.prover.mkDefinedFunction(funname, argTypes, body);
			} else {
				fun = this.prover.mkUnintFunction(funname, argTypes,
						boogieType2ProverType(exp.getFunction().getOutParam()
								.getType()));
			}
			this.proverFunctions.put(funname, fun);
		}
		ProverFun fun = this.proverFunctions.get(funname);
		ProverExpr[] args = new ProverExpr[exp.getArguments().length];

		for (int i = 0; i < exp.getArguments().length; i++) {
			args[i] = expression2proverExpression(exp.getArguments()[i],
					boundVariables);
		}

		return fun.mkExpr(args);
	}

	protected ProverExpr indentifierExpression2proverExpression(
			CfgIdentifierExpression exp,
			LinkedHashMap<CfgVariable, ProverExpr> boundVariables) {
		// First check if we are talking about a bound variable
		if (boundVariables.containsKey(exp.getVariable())) {
			return boundVariables.get(exp.getVariable());
		}

		return createProverVar(exp.getVariable(), exp.getCurrentIncarnation());
	}

	private ProverExpr createProverVar(CfgVariable var, int incarnation) {
		final BoogieType type = var.getType();

		if (insideOldExpression) {
			incarnation = 0;
			
		}
		
		if (!this.proverVariables.containsKey(var)) {
			this.proverVariables.put(var, new TreeMap<Integer, ProverExpr>());
		}
		if (!this.proverVariables.get(var).containsKey(incarnation)) {
			// don't add a ssa suffix if the incarnation in 0
			String varname = sanitizeName(var.getVarname())
					+ ((incarnation == 0) ? "" : ("__" + incarnation));
			ProverExpr newvar = this.prover.mkVariable(varname,
					boogieType2ProverType(type));
			this.proverVariables.get(var).put(incarnation, newvar);
			this.invertProverVariables.put(newvar, var);
			this.invertIncarnationMap.put(newvar, incarnation);

			if (var.getParentEdges() != null && var.getParentEdges().size() > 0) {				
				this.usedPOVariables.add(var);
			}
		}
		return this.proverVariables.get(var).get(incarnation);
	}

	/*
	 * TODO: check if it is safe to do this In the prover, we treat references
	 * as integers. TODO: Actually, we must not use SootPrelude types here, as
	 * this part must work on generic Boogie programs without knowing anything
	 * joogie-specific.
	 */
	protected ProverType boogieType2ProverType(BoogieType type) {
		ProgramFactory pf = GlobalsCache.v().getProgramFactory();
		if (pf == null) {
			throw new RuntimeException(" bug ");
		}
		if (type == pf.getIntType()) {
			return this.prover.getIntType();
		} else if (type == pf.getBoolType()) {
			return this.prover.getBooleanType();
		} else if (type == pf.getRealType()) {
			// TODO
			return this.prover.getIntType();
		} else if (type instanceof ArrayType) {
			ArrayType atype = (ArrayType) type;
			ProverType[] args = new ProverType[atype.getIndexCount()];
			for (int i = 0; i < atype.getIndexCount(); i++) {
				args[i] = boogieType2ProverType(atype.getIndexType(i));
			}
			return this.prover.getArrayType(args,
					boogieType2ProverType(atype.getValueType()));
		} else {
			//TOOD maybe we should distinguish other built-in types as well.
		}
		return this.prover.getIntType();
	}

	protected String sanitizeName(String name) {
		return name;
	}

}
