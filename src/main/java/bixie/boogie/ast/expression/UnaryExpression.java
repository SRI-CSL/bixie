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

package bixie.boogie.ast.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.enums.UnaryOperator;
import bixie.boogie.type.BoogieType;

/**
 * Represents a unary expression which is a special form of a expression.
 */
public class UnaryExpression extends Expression {
	/**
	 * The operator of this unary expression.
	 */
	UnaryOperator operator;

	/**
	 * The expr of this unary expression.
	 */
	Expression expr;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param operator
	 *            the operator of this unary expression.
	 * @param expr
	 *            the expr of this unary expression.
	 */
	public UnaryExpression(ILocation loc, UnaryOperator operator,
			Expression expr) {
		super(loc);
		this.operator = operator;
		this.expr = expr;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param operator
	 *            the operator of this unary expression.
	 * @param expr
	 *            the expr of this unary expression.
	 */
	public UnaryExpression(ILocation loc, BoogieType type,
			UnaryOperator operator, Expression expr) {
		super(loc, type);
		this.operator = operator;
		this.expr = expr;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("UnaryExpression").append('[');
		sb.append(operator);
		sb.append(',').append(expr);
		return sb.append(']').toString();
	}

	/**
	 * Gets the operator of this unary expression.
	 * 
	 * @return the operator of this unary expression.
	 */
	public UnaryOperator getOperator() {
		return operator;
	}

	/**
	 * Gets the expr of this unary expression.
	 * 
	 * @return the expr of this unary expression.
	 */
	public Expression getExpr() {
		return expr;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(operator);
		children.add(expr);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		return new UnaryExpression(this.getLocation(), this.type, this.operator, this.expr.substitute(s));
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		ret.addAll(this.expr.getFreeVariables());
		return ret;
	}
	
}
