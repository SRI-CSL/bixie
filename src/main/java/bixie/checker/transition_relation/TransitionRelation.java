package bixie.checker.transition_relation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.joogie.cfgPlugin.Util.Dag;

import ap.parser.IFormula;
import bixie.boogie.controlflow.AbstractControlFlowFactory;
import bixie.boogie.controlflow.BasicBlock;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.controlflow.expression.CfgBooleanLiteral;
import bixie.boogie.controlflow.expression.CfgExpression;
import bixie.boogie.controlflow.expression.CfgIdentifierExpression;
import bixie.boogie.controlflow.statement.CfgAssertStatement;
import bixie.boogie.controlflow.statement.CfgAssignStatement;
import bixie.boogie.controlflow.statement.CfgAssumeStatement;
import bixie.boogie.controlflow.statement.CfgHavocStatement;
import bixie.boogie.controlflow.statement.CfgStatement;
import bixie.prover.Prover;
import bixie.prover.ProverExpr;

/**
 * @author schaef
 * TODO: if we plan to do interprocedural analysis, we have
 * to change the way globals are handled here.
 */
public class TransitionRelation extends AbstractTransitionRelation {
	
	public HashMap<BasicBlock, ProverExpr> blockTransitionReleations = new HashMap<BasicBlock, ProverExpr>();	
	public HashMap<BasicBlock, ProverExpr> abstractTransitionReleations = new HashMap<BasicBlock, ProverExpr>();
	
	
	public ProverExpr assertionFlag;
	
	protected Dag<IFormula> proverDAG;	
	
//	protected ProverExpr expetionalReturnFlag = null;
	
	//TODO: this is a hack, like the creation
	//of this variable in the constructor
//	public ProverExpr getExpetionalReturnFlag() {
//		return expetionalReturnFlag;
//	}

	public Dag<IFormula> getProverDAG() {
		return proverDAG;
	}

	public TransitionRelation(CfgProcedure cfg, AbstractControlFlowFactory cff, Prover p) {
		super(cfg, cff, p);
		makePrelude();
		
		this.assertionFlag = prover.mkVariable("MartinsAssertionFlag", prover.getBooleanType());
		
		//create the ProverExpr for the precondition 
		ProverExpr[] prec = new ProverExpr[cfg.getRequires().size()];
		int i=0;
		for (CfgExpression expr : cfg.getRequires()) {
			prec[i]=this.expression2proverExpression(expr);
			i++;
		}
		this.requires = this.prover.mkAnd(prec);

		//create the ProverExpr for the precondition 
		ProverExpr[] post = new ProverExpr[cfg.getEnsures().size()];
		i=0;
		for (CfgExpression expr : cfg.getEnsures()) {
			post[i]=this.expression2proverExpression(expr);
			i++;
		}
		this.ensures = this.prover.mkAnd(post);
		
		//encode the forward reachability
		ProverExpr firstok = block2transitionRelation(cfg.getRootNode(), this.proofObligations);
		
		//the proof obligation for root als must contain that the block variable for root is true		
		this.proofObligations.get(cfg.getRootNode()).add(firstok);

		//bfs through all blocks
		LinkedList<BasicBlock> todo = new LinkedList<BasicBlock>();
		HashSet<BasicBlock> done = new HashSet<BasicBlock>();
		todo.add(cfg.getRootNode()); 
		while (!todo.isEmpty()) {
			BasicBlock current = todo.pop();
			done.add(current);
			for (BasicBlock b : current.getSuccessors()) {
				if (!done.contains(b) && !todo.contains(b)) {
					todo.push(b);
				}
			}
			
			this.addBlock(current);			
		}				
		
		this.proverDAG = procToPrincessDag(cfg, this.reachabilityVariables );

		finalizeAxioms();		
	}	
	
	private ProverExpr block2transitionRelation(BasicBlock b,
			HashMap<BasicBlock, LinkedList<ProverExpr>> proofobligations) {
		if (reachabilityVariables.containsKey(b)) {
			return reachabilityVariables.get(b);
		}

		ProverExpr post;
		if (b.getSuccessors().size() == 0  ) {
			post = this.prover.mkLiteral(true);
		} else if (b.getSuccessors().size() == 1) {
			post = block2transitionRelation(
					b.getSuccessors().toArray(new BasicBlock[1])[0], 
					proofobligations);
		} else {
			/*
			 * compute \not (/\ (\not B_succ)) that is\/ ( B_succ)
			 */
			ProverExpr[] succs = new ProverExpr[b.getSuccessors().size()];
			int i = 0;
			for (BasicBlock next : b.getSuccessors()) {
				succs[i++] = block2transitionRelation(next, 
						proofobligations);
			}
			post = this.prover.mkOr(succs);
		}
		List<ProverExpr> stmts = statements2proverExpression(b.getStatements());
		stmts.add(post);
		ProverExpr[] conj = stmts.toArray(new ProverExpr[stmts.size()]);		

		
		ProverExpr blockvar = this.prover.mkVariable(b.getLabel() + "_fwd",
					this.prover.getBooleanType());	
		reachabilityVariables.put(b, blockvar);

		LinkedList<ProverExpr> obligations = new LinkedList<ProverExpr>();
		obligations.add(this.prover.mkOr(this.prover.mkNot(blockvar),
				this.prover.mkAnd(conj)));
		proofobligations.put(b, obligations);
		return blockvar;
	}

	
	protected ProverExpr mkConjunction(Collection<ProverExpr> conjuncts) {
		if (conjuncts.size() == 0) {
			return prover.mkLiteral(true);
		}
		if (conjuncts.size() == 1) {
			return conjuncts.iterator().next();
		}
		return prover.mkAnd(conjuncts.toArray(new ProverExpr[conjuncts.size()]));
	}
	
	
	public void addBlock(BasicBlock b) {
		LinkedList<CfgStatement> bStatements = b.getStatements();
		
		// Add the concrete
		List<ProverExpr> concreteStmts = statements2proverExpression(bStatements);
     	this.blockTransitionReleations.put(b, mkConjunction(concreteStmts));
		
		// Add the abstract
		LinkedList<CfgStatement> bAbstractStatements = abstractStatements(bStatements);
		List<ProverExpr> abstractStmts = statements2proverExpression(bAbstractStatements);
		this.abstractTransitionReleations.put(b, mkConjunction(abstractStmts));
		
		// Add the variable
//		if (!this.reachabilityVariables.containsKey(b)) {
//			this.reachabilityVariables.put(b, this.prover.mkVariable(b.getLabel() + "_fwd", this.prover.getBooleanType()));
//		}
	}
	
	/**
	 * Isolate the abstraction of the statements (for example, just keep the frame statements).
	 */
	private LinkedList<CfgStatement> abstractStatements(LinkedList<CfgStatement> bStatements) {
		LinkedList<CfgStatement> abstractStatements = new LinkedList<CfgStatement>();
	
		for (CfgStatement stmnt : bStatements) {
			if (stmnt instanceof CfgAssignStatement) {
				CfgAssignStatement asgn = (CfgAssignStatement) stmnt;
				CfgIdentifierExpression[] left = asgn.getLeft();
				CfgExpression[] right = asgn.getRight();
				
				boolean ok = true;
				if (left.length == right.length) {
					for (int i = 0; ok && i < left.length; ++ i) {
						CfgIdentifierExpression left_i = left[i];
						CfgExpression right_i = right[i];
						if (right_i instanceof CfgIdentifierExpression) {
							CfgVariable left_var = left_i.getVariable();
							CfgVariable right_var = ((CfgIdentifierExpression)right_i).getVariable();
							if (left_var!=right_var) {
								ok = false;
							}
						} else {
							ok = false;
						}
					}
				} else {
					ok = false;
				}
				
				if (ok) {
					abstractStatements.add(stmnt);
				}
			}
		}
		
		return abstractStatements;
	}

//	public void removeBlock(BasicBlock b) {
//		this.blockTransitionReleations.remove(b);
//		this.reachabilityVariables.remove(b);
//	}
		
	/*
	 * (non-Javadoc)
	 * @see bixie.checker.verificationcondition.AbstractTransitionRelation#statements2proverExpression(java.util.List)
	 * 
	 * override the original ones to add a flag to all assertions so that we can disable them.
	 */
	@Override
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
	
	
	/*
	 * (non-Javadoc)
	 * @see bixie.checker.verificationcondition.AbstractTransitionRelation#statements2proverExpression(java.util.List)
	 * 
	 * override the original ones to add a flag to all assertions so that we can disable them.
	 */	
	@Override
	protected ProverExpr statement2proverExpression(CfgStatement s) {
		if (s instanceof CfgAssertStatement) {
			CfgAssertStatement assrt = (CfgAssertStatement) s;
			/*!!!!! Here we translate 'assert(e)' to 'e\/assertionFlag' so that we can disable all
			 * assertions at once by setting the flag to true.
			 */
			return this.prover.mkOr(expression2proverExpression(assrt.getCondition()), this.assertionFlag);
//			return expression2proverExpression(assrt.getCondition());
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
			//eg CfgCallStatement
			throw new RuntimeException("Unknown statement type: "
					+ s.getClass().toString());
		}
	}	
	
	
	public CfgVariable findVariableByName(String name) {
		for (Entry<CfgVariable, SortedMap<Integer, ProverExpr>> entry : this.proverVariables.entrySet()) {
			if (entry.getKey().getVarname().equals(name)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
