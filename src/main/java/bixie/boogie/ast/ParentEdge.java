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

package bixie.boogie.ast;

import java.util.List;

import bixie.boogie.ast.location.ILocation;

/**
 * Represents a parent edge.
 */
public class ParentEdge extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * True if this parent edge is unique. In that case the
	 * <emph>children</emph> of this constant are disjoint from the children of
	 * any other constant declared with the same unique parentNode.
	 */
	boolean isUnique;

	/**
	 * The name of the parent.
	 */
	String identifier;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param isUnique
	 *            true if this parent edge is unique.
	 * @param identifier
	 *            the name of the parent.
	 */
	public ParentEdge(ILocation loc, boolean isUnique, String identifier) {
		super(loc);
		this.isUnique = isUnique;
		this.identifier = identifier;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ParentEdge").append('[');
		sb.append(isUnique);
		sb.append(',').append(identifier);
		return sb.append(']').toString();
	}

	/**
	 * Checks if this parent edge is unique. In that case the
	 * <emph>children</emph> of this constant are disjoint from the children of
	 * any other constant declared with the same unique parentNode.
	 * 
	 * @return true if this parent edge is unique.
	 */
	public boolean isUnique() {
		return isUnique;
	}

	/**
	 * Gets the name of the parent.
	 * 
	 * @return the name of the parent.
	 */
	public String getIdentifier() {
		return identifier;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(isUnique);
		children.add(identifier);
		return children;
	}
}
