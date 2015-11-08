/*
 * boogieamp - Parser, Factory, and Utilities to create Boogie Programs from Java
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

package bixie.boogie.typechecker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import bixie.boogie.ast.ArrayLHS;
import bixie.boogie.ast.Body;
import bixie.boogie.ast.LeftHandSide;
import bixie.boogie.ast.Unit;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.VariableLHS;
import bixie.boogie.ast.declaration.Declaration;
import bixie.boogie.ast.declaration.Implementation;
import bixie.boogie.ast.declaration.ProcedureDeclaration;
import bixie.boogie.ast.declaration.ProcedureOrImplementationDeclaration;
import bixie.boogie.ast.declaration.VariableDeclaration;
import bixie.boogie.ast.specification.ModifiesSpecification;
import bixie.boogie.ast.specification.Specification;
import bixie.boogie.ast.statement.AssignmentStatement;
import bixie.boogie.ast.statement.CallStatement;
import bixie.boogie.ast.statement.HavocStatement;
import bixie.boogie.ast.statement.IfStatement;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.ast.statement.WhileStatement;

/**
 * @author schaef
 * 
 */
public class ModifiesClauseConstruction {

	public static void createModifiesClause(Unit root) {

		ModifiesClauseConstruction instance = new ModifiesClauseConstruction();

		HashMap<String, ProcedureDeclaration> proceduredecls = new HashMap<String, ProcedureDeclaration>(); 
		HashMap<String, LinkedList<Implementation>> implementations = new HashMap<String, LinkedList<Implementation>>();
		
		// collect the names of all global variables.
		for (Declaration c : root.getDeclarations()) {
			if (c instanceof VariableDeclaration) {
				VariableDeclaration var = (VariableDeclaration) c;
				for (VarList v : var.getVariables()) {
					for (int i = 0; i < v.getIdentifiers().length; i++) {
						instance.globalIdentifier.add(v.getIdentifiers()[i]);
					}
				}
			} else if (c instanceof ProcedureDeclaration) {
				ProcedureDeclaration decl = (ProcedureDeclaration)c;
				if (proceduredecls.containsKey(decl.getIdentifier())) {
					throw new RuntimeException("Double declaration of procedure "+decl.getIdentifier());
				}
				proceduredecls.put(decl.getIdentifier(), decl);
			} else if (c instanceof Implementation) {
				Implementation impl = (Implementation)c;
				if (!implementations.containsKey(impl.getIdentifier())) {
					implementations.put(impl.getIdentifier(), new LinkedList<Implementation>());
				}
				implementations.get(impl.getIdentifier()).add(impl);
			}
		}		
		
		// now build the non-transitive modifies set for each
		// procedure
		for (Declaration c : root.getDeclarations()) {
			if (c instanceof ProcedureOrImplementationDeclaration) {
				ProcedureOrImplementationDeclaration p = (ProcedureOrImplementationDeclaration) c;
				instance.computeModifiedGlobalsAndCalls(p);
			}
		}

		// now build the transitive modifies set for each
		// procedure
		for (Declaration c : root.getDeclarations()) {
			if (c instanceof ProcedureOrImplementationDeclaration) {
				ProcedureOrImplementationDeclaration p = (ProcedureOrImplementationDeclaration) c;
				instance.computeTransitiveModifies(p.getIdentifier(),
						new HashSet<String>());
			}
		}

		// now create new procedure declarations with the
		// updated modifies clauses.
		LinkedList<Declaration> newdecls = new LinkedList<Declaration>();
		for (Declaration c : root.getDeclarations()) {
			if (c instanceof ProcedureDeclaration) {
				ProcedureDeclaration p = (ProcedureDeclaration) c;
				HashSet<Specification> newspec = new HashSet<Specification>();
				if (p.getSpecification() != null) {
					for (Specification spec : p.getSpecification()) {
						// preserve everything but the modifes clauses.
						if (!(spec instanceof ModifiesSpecification)) {
							newspec.add(spec);
						}
					}
				}
				HashSet<String> identifiers = instance.procedureInfoMap.get(p
						.getIdentifier()).modifiedGlobals;
				ModifiesSpecification modspec = new ModifiesSpecification(
						p.getLocation(), false,
						identifiers.toArray(new String[identifiers.size()]));
				newspec.add(modspec);
				ProcedureDeclaration newproc = new ProcedureDeclaration(p.getLocation(),
						p.getAttributes(), p.getIdentifier(),
						p.getTypeParams(), p.getInParams(), p.getOutParams(),
						newspec.toArray(new Specification[newspec.size()]),
						p.getBody());
				newdecls.add(newproc);
			} else {
				newdecls.add(c);
			}
		}
		// write the new decls to the root node.
		root.setDeclarations(newdecls.toArray(new Declaration[newdecls.size()]));
	}


	private HashSet<String> globalIdentifier = new HashSet<String>();

	private class ProcedureInfo {
		public HashSet<String> localVariables = new HashSet<String>();
		public HashSet<String> modifiedGlobals = new HashSet<String>();
		public HashSet<String> calledProcedures = new HashSet<String>();
	}

	private HashMap<String, ProcedureInfo> procedureInfoMap = new HashMap<String, ProcedureInfo>();

	private void computeModifiedGlobalsAndCalls(ProcedureOrImplementationDeclaration p) {
		if (!this.procedureInfoMap.containsKey(p.getIdentifier())) {			
			this.procedureInfoMap.put(p.getIdentifier(), new ProcedureInfo());			
		}
		ProcedureInfo pi = this.procedureInfoMap.get(p.getIdentifier());
		for (Specification spec : p.getSpecification()) {
			if (spec instanceof ModifiesSpecification) {
				ModifiesSpecification mspec = (ModifiesSpecification)spec;
				for (String id : mspec.getIdentifiers()) {
					pi.modifiedGlobals.add(id);
				}
			}
		}
		Body body = p.getBody();
		if (body == null) {
			return;
		}

		computeModifiedGlobalsAndCalls(body.getBlock(), pi);
	}

	private void computeModifiedGlobalsAndCalls(Statement[] statements,
			ProcedureInfo pi) {
		for (Statement s : statements) {
			if (s instanceof AssignmentStatement) {
				AssignmentStatement assignstatement = (AssignmentStatement) s;
				for (LeftHandSide lhs : assignstatement.getLhs()) {
					computeModifiedGlobalsAndCalls(lhs, pi);
				}
			} else if (s instanceof HavocStatement) {
				HavocStatement havocstmt = (HavocStatement) s;
				for (String str : havocstmt.getIdentifiers()) {
					if (this.globalIdentifier.contains(str)) {
						pi.modifiedGlobals.add(str);
					}
				}
			} else if (s instanceof CallStatement) {
				CallStatement callstatement = (CallStatement) s;
				pi.calledProcedures.add(callstatement.getMethodName());
			} else if (s instanceof IfStatement) {
				IfStatement ifstmt = (IfStatement) s;
				computeModifiedGlobalsAndCalls(ifstmt.getThenPart(), pi);
				computeModifiedGlobalsAndCalls(ifstmt.getElsePart(), pi);
			} else if (s instanceof WhileStatement) {
				WhileStatement whilestmt = (WhileStatement) s;
				computeModifiedGlobalsAndCalls(whilestmt.getBody(), pi);
			} else {
				// do nothing
			}
		}
	}

	private void computeModifiedGlobalsAndCalls(LeftHandSide lhs,
			ProcedureInfo pi) {
		if (lhs instanceof ArrayLHS) {
			ArrayLHS alhs = (ArrayLHS) lhs;
			computeModifiedGlobalsAndCalls(alhs.getArray(), pi);
		} else if (lhs instanceof VariableLHS) {
			VariableLHS vlhs = (VariableLHS) lhs;
			if (!pi.localVariables.contains(vlhs.getIdentifier())
					&& this.globalIdentifier.contains(vlhs.getIdentifier())) {
				pi.modifiedGlobals.add(vlhs.getIdentifier());
			}
		}
	}

	// --------- transitive computation -----------
	/**
	 * recomputes recursively the transitive set of modified globals for a
	 * procedure
	 * 
	 * @param identifier
	 *            the name of the procedure
	 * @param visited
	 *            bookkeeping of visited procedures to avoid endless loops
	 * @return the new (transitive) set of modified globals
	 */
	private HashSet<String> computeTransitiveModifies(String identifier,
			HashSet<String> visited) {
		ProcedureInfo pi = this.procedureInfoMap.get(identifier);
		if (pi==null) {
			throw new RuntimeException("Procedure "+identifier+" not found");
		}
		if (visited.contains(identifier)) {
			return pi.modifiedGlobals;
		}
		visited.add(identifier);
		for (String callee : pi.calledProcedures) {
			pi.modifiedGlobals.addAll(this.computeTransitiveModifies(callee,
					visited));
		}
		return pi.modifiedGlobals;
	}

}
