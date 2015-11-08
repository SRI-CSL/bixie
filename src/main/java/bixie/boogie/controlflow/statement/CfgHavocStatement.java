/*
 * boogieamp - Parser, Factory, and Utilities to create Boogie Programs from Java
 * Copyright (C) 2013 Martin Schaeaeaef and Stephan Arlt
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
import bixie.boogie.controlflow.CfgVariable;

/**
 * @author schaef
 * 
 */
public class CfgHavocStatement extends CfgStatement {

	private CfgVariable[] variables;
	private CfgStatement replacedStatement = null;
	
	
	public CfgHavocStatement(ILocation loc, CfgVariable[] vars) {
		super(loc);
		this.variables = vars;
	}

	/**
	 * @return the variables
	 */
	public CfgVariable[] getVariables() {
		return variables;
	}

	/**
	 * @param variables
	 *            the variables to set
	 */
	public void setVariables(CfgVariable[] variables) {
		this.variables = variables;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("havoc ");
		for (int i=0; i<this.variables.length; i++) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(this.variables[i].getVarname());
		}
		return sb.toString();
	}

	public CfgStatement duplicate() {
		CfgHavocStatement ret = new CfgHavocStatement(this.getLocation(), this.getVariables());
		ret.setReplacedStatement(this.getReplacedStatement());		
		return ret; 
	}

	/**
	 * @return the replacedStatement
	 */
	public CfgStatement getReplacedStatement() {
		return replacedStatement;
	}

	/**
	 * @param replacedStatement the replacedStatement to set
	 */
	public void setReplacedStatement(CfgStatement replacedStatement) {
		this.replacedStatement = replacedStatement;
	}

}
