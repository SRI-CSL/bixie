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
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.enums.UnaryOperator;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public class CfgUnaryExpression extends CfgExpression {

	private UnaryOperator operator;
	private CfgExpression expression;

	public CfgUnaryExpression(ILocation loc, BoogieType type, UnaryOperator op,
			CfgExpression exp) {
		super(loc, type);
		this.operator = op;
		this.expression = exp;
	}

	/**
	 * @return the operator
	 */
	public UnaryOperator getOperator() {
		return operator;
	}

	/**
	 * @param operator
	 *            the operator to set
	 */
	public void setOperator(UnaryOperator operator) {
		this.operator = operator;
	}

	/**
	 * @return the expression
	 */
	public CfgExpression getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 *            the expression to set
	 */
	public void setExpression(CfgExpression expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {		
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		if (this.operator == UnaryOperator.ARITHNEGATIVE) {
			sb.append("-"); //TODO	
		} else 	if (this.operator == UnaryOperator.LOGICNEG) {
			sb.append("!"); //TODO
		} else 	if (this.operator == UnaryOperator.OLD) {
			sb.append("\\old"); //TODO
		} else {
			throw new RuntimeException("Unknown unary operator");
		}
		sb.append(this.expression.toString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public CfgExpression substitute(
			HashMap<CfgVariable, CfgExpression> substitutes) {
		return new CfgUnaryExpression(this.getLocation(), this.getType(), this.operator, this.expression.substitute(substitutes));
	}
	
	
}
