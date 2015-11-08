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

package bixie.boogie.type;

import java.util.ArrayList;

/**
 * A placeholder type represents a type bounded by some outer type parameters,
 * like by an ArrayType, by a function signature, a procedure signature or a
 * forall/exists quantifier.
 * 
 * The type args are represented in de Bruijn style, giving only the number of
 * type parameter declarations between the placeholder and its binder.
 * 
 * @author hoenicke
 * 
 */
public class PlaceholderType extends BoogieType {

	private int depth;
	private String identifier;

	public PlaceholderType(String identifier, int depth) {
		this.identifier = identifier;
		this.depth = depth;		
	}

	public PlaceholderType(int depth) {
		this.depth = depth;
		this.identifier = "$GenericType__" + depth;
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
	
	/**
	 * Get the depth of the declaration where this placeholder points to.
	 * 
	 * @return the depth.
	 */
	public int getDepth() {
		return depth;
	}

	// @Override
	protected BoogieType substitutePlaceholders(int deltaDepth,
			BoogieType[] substType) {
		int relDepth = depth - deltaDepth;
		if (relDepth < 0) {
			/* Placeholder matches some inner scope */
			return this;
		} else if (relDepth < substType.length) {
			/* Substitute this placeholder */
			BoogieType subst = substType[relDepth];
			/*
			 * This should only happen if error type was involved when computing
			 * substitution.
			 */
			if (subst == null)
				return errorType;
			if (deltaDepth > 0)
				subst = subst.incrementPlaceholders(0, deltaDepth);
			return subst;
		} else {
			/* Placeholder matches some outer scope; but this scope moves */
			return createPlaceholderType(depth - substType.length);
		}
	}

	// @Override
	protected BoogieType incrementPlaceholders(int deltaDepth, int incDepth) {
		int relDepth = depth - deltaDepth;
		if (relDepth < 0) {
			/* Placeholder matches some inner scope */
			return this;
		} else {			
			/* Substitute this placeholder */
			return createPlaceholderType(depth + incDepth);
		}
	}

	// @Override
	protected boolean unify(int deltaDepth, BoogieType other,
			BoogieType[] substitution) {
		if (other == errorType)
			return true;
		int relDepth = depth - deltaDepth;
		if (relDepth < 0 || relDepth >= substitution.length) {
			/* This placeholder is not substituted */
			if (!(other instanceof PlaceholderType))
				return false;
			PlaceholderType type = (PlaceholderType) other;
			return (type.depth == (relDepth < 0 ? depth : depth
					- substitution.length));
		} else {
			/* Check freedom of inner bounded variable */
			if (other.hasPlaceholder(0, deltaDepth - 1))
				return false;
			if (deltaDepth != 0)
				other = other.incrementPlaceholders(0, -deltaDepth);
			/* Substitute this placeholder */
			if (substitution[relDepth] == null) {
				substitution[relDepth] = other;
				return true;
			}
			return substitution[relDepth] == other;
		}
	}

	protected boolean hasPlaceholder(int minDepth, int maxDepth) {
		return depth >= minDepth && depth <= maxDepth;
	}

	// @Override
	protected boolean isUnifiableTo(int deltaDepth, BoogieType other,
			ArrayList<BoogieType> substitution) {
		/* fast path first */
		if (other == this || other == errorType)
			return true;

		int relDepth = depth - deltaDepth;
		if (relDepth < 0) {
			/* This placeholder is not substituted */
			return false;
		} else {
			/* Get the real types */
			BoogieType[] subst = substitution
					.toArray(new BoogieType[substitution.size()]);
			BoogieType me = substitutePlaceholders(deltaDepth, subst);
			other = other.substitutePlaceholders(deltaDepth, subst);
			if (me == other)
				return true;
			if (!(me instanceof PlaceholderType)) {
				/*
				 * we are no longer a placeholder type, let the unification
				 * process continue;
				 */
				return other.isUnifiableTo(deltaDepth, me, substitution);
			}
			/* We are a currently unsubstituted placeholder */
			relDepth = ((PlaceholderType) me).depth - deltaDepth;
			/* Inner placeholders cannot be substituted */
			if (relDepth < 0)
				return false;

			/* Check that other is free of inner bounded variable */
			if (other.hasPlaceholder(0, deltaDepth - 1))
				return false;

			/* Bring other to the right depth */
			if (deltaDepth != 0)
				other = other.incrementPlaceholders(0, -deltaDepth);

			/* Occur check */
			if (other.hasPlaceholder(relDepth, relDepth))
				return false;

			while (relDepth >= substitution.size())
				substitution.add(null);
			substitution.set(relDepth, other);
			return true;
		}
	}

	public BoogieType getUnderlyingType() {
		return this;
	}

	/**
	 * Computes a string representation. It uses depth to compute artificial
	 * names for the placeholders.
	 * 
	 * @param depth
	 *            the number of placeholders outside this expression.
	 * @param needParentheses
	 *            true if parentheses should be set for constructed types
	 * @return a string representation of this type.
	 */
	public String toString(int depth, boolean needParentheses) {
		int paramNumber = depth - this.depth - 1;

		if (paramNumber >= 0)
			return "$" + paramNumber;
		else
			return "$_" + (-paramNumber);
	}

	// @Override
	public boolean isFinite() {
		return true;
	}
}
