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
import bixie.boogie.type.BoogieType;

/**
 * Represents a identifier expression which is a special form of a expression.
 */
public class IdentifierExpression extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifier of this identifier expression.
	 */
	String identifier;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param identifier
	 *            the identifier of this identifier expression.
	 */
	public IdentifierExpression(ILocation loc, String identifier) {
		super(loc);
		this.identifier = identifier;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param identifier
	 *            the identifier of this identifier expression.
	 */
	public IdentifierExpression(ILocation loc, BoogieType type,
			String identifier) {
		super(loc, type);
		this.identifier = identifier;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("IdentifierExpression").append('[');
		sb.append(identifier);
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifier of this identifier expression.
	 * 
	 * @return the identifier of this identifier expression.
	 */
	public String getIdentifier() {
		return identifier;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifier);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		if (s!=null && s.containsKey(this.identifier)) {
			return s.get(this.identifier).clone();
		}
		return new IdentifierExpression(this.getLocation(), this.type, this.identifier);
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		ret.add(this);
		return ret;
	}
	
}
