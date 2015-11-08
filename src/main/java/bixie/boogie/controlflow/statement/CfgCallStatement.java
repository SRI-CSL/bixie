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

package bixie.boogie.controlflow.statement;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.boogie.controlflow.expression.CfgExpression;
import bixie.boogie.controlflow.expression.CfgIdentifierExpression;

/**
 * @author schaef
 * 
 */
public class CfgCallStatement extends CfgStatement {

	private CfgIdentifierExpression[] leftHandSide;
	private CfgProcedure callee;
	private CfgExpression[] arguments;

	public CfgCallStatement(ILocation loc, CfgIdentifierExpression[] lvars,
			CfgProcedure callee, CfgExpression[] args) {
		super(loc);
		// TODO Auto-generated constructor stub
		this.leftHandSide = lvars;
		this.callee = callee;
		this.arguments = args;
	}

	/**
	 * @return the leftHandSide
	 */
	public CfgIdentifierExpression[] getLeftHandSide() {
		return leftHandSide;
	}

	/**
	 * @param leftHandSide
	 *            the leftHandSide to set
	 */
	public void setLeftHandSide(CfgIdentifierExpression[] leftHandSide) {
		this.leftHandSide = leftHandSide;
	}

	/**
	 * @return the callee
	 */
	public CfgProcedure getCallee() {
		return callee;
	}

	/**
	 * @param callee
	 *            the callee to set
	 */
	public void setCallee(CfgProcedure callee) {
		this.callee = callee;
	}

	/**
	 * @return the arguments
	 */
	public CfgExpression[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(CfgExpression[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (CfgIdentifierExpression lhs : this.leftHandSide) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(lhs.getVariable().getVarname());
		}
		if (this.leftHandSide.length > 0) {
			sb.append(" := ");
		}
		sb.append("call ");
		sb.append(this.callee.getProcedureName());
		first = true;
		sb.append("(");
		for (CfgExpression exp : this.getArguments()) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(exp);
		}
		sb.append(");");
		return sb.toString();
	}

	public CfgStatement duplicate() {
		CfgIdentifierExpression[] clonelvars = new CfgIdentifierExpression[this.leftHandSide.length];
		for (int i=0; i<this.leftHandSide.length; i++) {
			clonelvars[i] = (CfgIdentifierExpression) this.leftHandSide[i].clone();
		}
		CfgExpression[] cloneargs = new CfgExpression[this.arguments.length];
		for (int i=0; i<this.arguments.length; i++) {
			cloneargs[i] = this.arguments[i].clone();
		}		
		return new CfgCallStatement(this.getLocation(), this.leftHandSide, this.callee, this.arguments);
	}
}
 