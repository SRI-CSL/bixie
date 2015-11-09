/*
 * boogieamp - Parser, Factory, and Utilities to create Boogie Programs from Java
 * Copyright (C) 2013 Martin Schaeaeaeaeaeaeaeaeaef and Stephan Arlt
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

package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.boogie.controlflow.CfgVariable;

/**
 * @author schaef
 * 
 */
public class CfgIdentifierExpression extends CfgExpression {

	private int currentIncarnation = 0;
	private CfgVariable variable = null;

	public CfgIdentifierExpression(ILocation loc, CfgVariable v) {
		super(loc, v.getType());
		this.variable = v;
	}

	public CfgIdentifierExpression(ILocation loc, CfgVariable v, int incarnation) {
		super(loc, v.getType());
		this.variable = v;
		this.currentIncarnation = incarnation;
	}

	public int getCurrentIncarnation() {
		return currentIncarnation;
	}

	public void setCurrentIncarnation(int currentIncarnation) {
		this.currentIncarnation = currentIncarnation;
	}

	public CfgVariable getVariable() {
		return variable;
	}

	public void setVariable(CfgVariable variable) {
		this.variable = variable;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.variable.getVarname());
		if (CfgProcedure.printSSA) {
			sb.append("__"+ this.getCurrentIncarnation());
		}
		 
		return sb.toString();
	}


	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		if (substitutes!=null && substitutes.containsKey(this.variable)) {
			return substitutes.get(this.variable).clone();
		}
		return new CfgIdentifierExpression(this.getLocation(), this.variable);
	}
		
}
