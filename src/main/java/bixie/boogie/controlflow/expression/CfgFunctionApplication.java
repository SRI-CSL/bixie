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

package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgFunction;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgFunctionApplication extends CfgExpression {

	private CfgFunction function;
	private CfgExpression[] arguments;

	public CfgFunctionApplication(ILocation loc, BoogieType type,
			CfgFunction callee, CfgExpression[] args) {
		super(loc, type);
		this.function = callee;
		this.arguments = args;
	}

	/**
	 * @return the function
	 */
	public CfgFunction getFunction() {
		return function;
	}

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(CfgFunction function) {
		this.function = function;
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
		sb.append(this.function.getIndentifier());
		sb.append("(");
		for (int i=0; i<this.arguments.length; i++) {
			if (i!=0) {
				sb.append(", ");
			}
			sb.append(this.arguments[i].toString());
		}
		sb.append(")");
		return sb.toString();
	}


	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		CfgExpression[] cloneargs = new CfgExpression[this.arguments.length];
		for (int i=0;i<this.arguments.length;i++) {
			cloneargs[i] = this.arguments[i].substitute(substitutes);
		}
		return new CfgFunctionApplication(this.getLocation(), this.getType(), this.function, cloneargs);
	}
	
}
