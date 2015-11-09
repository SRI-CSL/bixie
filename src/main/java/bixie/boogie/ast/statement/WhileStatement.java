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

package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.ast.specification.LoopInvariantSpecification;

/**
 * Represents a while statement which is a special form of a statement.
 */
public class WhileStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The condition of this while statement.
	 */
	Expression condition;

	/**
	 * The invariants of this while statement.
	 */
	LoopInvariantSpecification[] invariants;

	/**
	 * The body of this while statement.
	 */
	Statement[] body;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param condition
	 *            the condition of this while statement.
	 * @param invariants
	 *            the invariants of this while statement.
	 * @param body
	 *            the body of this while statement.
	 */
	public WhileStatement(ILocation loc, Expression condition,
			LoopInvariantSpecification[] invariants, Statement[] body) {
		super(loc);
		this.condition = condition;
		this.invariants = invariants;
		this.body = body;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WhileStatement").append('[');
		sb.append(condition);
		sb.append(',');
		if (invariants == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < invariants.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(invariants[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (body == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < body.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(body[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the condition of this while statement.
	 * 
	 * @return the condition of this while statement.
	 */
	public Expression getCondition() {
		return condition;
	}

	/**
	 * Gets the invariants of this while statement.
	 * 
	 * @return the invariants of this while statement.
	 */
	public LoopInvariantSpecification[] getInvariants() {
		return invariants;
	}

	/**
	 * Gets the body of this while statement.
	 * 
	 * @return the body of this while statement.
	 */
	public Statement[] getBody() {
		return body;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(condition);
		children.add(invariants);
		children.add(body);
		return children;
	}
}
