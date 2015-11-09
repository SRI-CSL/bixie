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

package bixie.boogie.ast.asttypes;

import java.util.List;

import bixie.boogie.ast.ASTNode;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents an ast type. This is different from BoogieType, as it is not
 * unified and still contains the names of the type parameters.
 */
public abstract class ASTType extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The boogie type of this a s t type.
	 */
	BoogieType boogieType;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 */
	public ASTType(ILocation loc) {
		super(loc);
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param boogieType
	 *            the boogie type of this a s t type.
	 */
	public ASTType(ILocation loc, BoogieType boogieType) {
		super(loc);
		this.boogieType = boogieType;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ASTType").append('[');
		sb.append(boogieType);
		return sb.append(']').toString();
	}

	/**
	 * Gets the boogie type of this a s t type.
	 * 
	 * @return the boogie type of this a s t type.
	 */
	public BoogieType getBoogieType() {
		return boogieType;
	}

	/**
	 * Sets the boogie type of this a s t type.
	 * 
	 * @param boogieType
	 *            the boogie type of this a s t type.
	 */
	public void setBoogieType(BoogieType boogieType) {
		this.boogieType = boogieType;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(boogieType);
		return children;
	}
}
