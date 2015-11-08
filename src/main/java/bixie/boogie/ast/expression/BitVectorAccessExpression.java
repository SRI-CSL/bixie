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
 * Represents a bit vector access expression which is a special form of a
 * expression.
 */
public class BitVectorAccessExpression extends Expression {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The sub expression representing the bit-vector.
	 */
	Expression bitvec;

	/**
	 * The end index of this bit-vector access.
	 */
	int end;

	/**
	 * The start index of this bit-vector access.
	 */
	int start;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param bitvec
	 *            the sub expression representing the bit-vector.
	 * @param end
	 *            the end index of this bit-vector access.
	 * @param start
	 *            the start index of this bit-vector access.
	 */
	public BitVectorAccessExpression(ILocation loc, Expression bitvec, int end,
			int start) {
		super(loc);
		this.bitvec = bitvec;
		this.end = end;
		this.start = start;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this expression.
	 * @param bitvec
	 *            the sub expression representing the bit-vector.
	 * @param end
	 *            the end index of this bit-vector access.
	 * @param start
	 *            the start index of this bit-vector access.
	 */
	public BitVectorAccessExpression(ILocation loc, BoogieType type,
			Expression bitvec, int end, int start) {
		super(loc, type);
		this.bitvec = bitvec;
		this.end = end;
		this.start = start;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BitVectorAccessExpression").append('[');
		sb.append(bitvec);
		sb.append(',').append(end);
		sb.append(',').append(start);
		return sb.append(']').toString();
	}

	/**
	 * Gets the sub expression representing the bit-vector.
	 * 
	 * @return the sub expression representing the bit-vector.
	 */
	public Expression getBitvec() {
		return bitvec;
	}

	/**
	 * Gets the end index of this bit-vector access.
	 * 
	 * @return the end index of this bit-vector access.
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Gets the start index of this bit-vector access.
	 * 
	 * @return the start index of this bit-vector access.
	 */
	public int getStart() {
		return start;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(bitvec);
		children.add(end);
		children.add(start);
		return children;
	}

	@Override
	public Expression substitute(HashMap<String, Expression> s) {		
		return new BitVectorAccessExpression(this.getLocation(), 
				this.getType(), 
				this.bitvec.substitute(s), this.end, this.start);
	}
	
	@Override
	public HashSet<IdentifierExpression> getFreeVariables() {
		HashSet<IdentifierExpression> ret = new HashSet<IdentifierExpression>();
		ret.addAll(this.bitvec.getFreeVariables());		
		return ret;
	}
	
}
