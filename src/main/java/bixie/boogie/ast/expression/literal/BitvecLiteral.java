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

package bixie.boogie.ast.expression.literal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a bitvec literal which is a special form of a expression.
 */
public class BitvecLiteral extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The value given as String. This representation is used to support
	 * arbitrarily large numbers. We do not need to compute with them but give
	 * them 1-1 to the decision procedure.
	 */
	String value;

	/**
	 * The number of bits in this bitvector.
	 */
	int length;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param value
	 *            the value given as String.
	 * @param length
	 *            the number of bits in this bitvector.
	 */
	public BitvecLiteral(ILocation loc, String value, int length) {
		super(loc);
		this.value = value;
		this.length = length;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param value
	 *            the value given as String.
	 * @param length
	 *            the number of bits in this bitvector.
	 */
	public BitvecLiteral(ILocation loc, BoogieType type, String value,
			int length) {
		super(loc, type);
		this.value = value;
		this.length = length;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BitvecLiteral").append('[');
		sb.append(value);
		sb.append(',').append(length);
		return sb.append(']').toString();
	}

	/**
	 * Gets the value given as String. This representation is used to support
	 * arbitrarily large numbers. We do not need to compute with them but give
	 * them 1-1 to the decision procedure.
	 * 
	 * @return the value given as String.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the number of bits in this bitvector.
	 * 
	 * @return the number of bits in this bitvector.
	 */
	public int getLength() {
		return length;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(value);
		children.add(length);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {
		return 	new BitvecLiteral(this.getLocation(), this.value, this.length);
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		return new HashSet<IdentifierExpression>();
	}
}
