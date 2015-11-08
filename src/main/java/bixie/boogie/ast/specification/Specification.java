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

package bixie.boogie.ast.specification;

import java.util.List;

import bixie.boogie.ast.ASTNode;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a specification.
 */
public abstract class Specification extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * True iff this specification is free.
	 */
	protected boolean isFree;
	protected Attribute[] attributes;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param isFree
	 *            true iff this specification is free.
	 */
	public Specification(ILocation loc, boolean isFree) {
		super(loc);
		this.isFree = isFree;
	}

	public Specification(ILocation loc, Attribute[] attributes, boolean isFree) {
		super(loc);
		this.isFree = isFree;
		this.attributes = attributes;
	}
	
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Specification").append('[');
		sb.append(isFree);
		return sb.append(']').toString();
	}

	/**
	 * Checks iff this specification is free.
	 * 
	 * @return true iff this specification is free.
	 */
	public boolean isFree() {
		return isFree;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(isFree);
		return children;
	}
}
