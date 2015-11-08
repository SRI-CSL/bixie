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

package bixie.boogie.controlflow;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.expression.CfgExpression;

/**
 * @author schaef
 * 
 */
public class CfgAxiom {

	private ILocation location;
	private CfgExpression formula;

	public CfgAxiom(ILocation location, CfgExpression formula) {
		this.location = location;
		this.formula = formula;
	}

	/**
	 * @return the location
	 */
	public ILocation getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(ILocation location) {
		this.location = location;
	}

	/**
	 * @return the formula
	 */
	public CfgExpression getFormula() {
		return formula;
	}

	/**
	 * @param formula
	 *            the formula to set
	 */
	public void setFormula(CfgExpression formula) {
		this.formula = formula;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("assert (");
		sb.append(this.formula.toString());
		sb.append(");\n");
		return sb.toString();
	}
}
