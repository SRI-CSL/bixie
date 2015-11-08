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
import bixie.boogie.type.BoogieType;

/**
 * Represents a left hand side.
 */
public abstract class LeftHandSide extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The type of this left hand side.
	 */
	BoogieType type;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 */
	public LeftHandSide(ILocation loc) {
		super(loc);
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this left hand side.
	 */
	public LeftHandSide(ILocation loc, BoogieType type) {
		super(loc);
		this.type = type;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("LeftHandSide").append('[');
		sb.append(type);
		return sb.append(']').toString();
	}

	/**
	 * Gets the type of this left hand side.
	 * 
	 * @return the type of this left hand side.
	 */
	public BoogieType getType() {
		return type;
	}

	/**
	 * Sets the type of this left hand side.
	 * 
	 * @param type
	 *            the type of this left hand side.
	 */
	public void setType(BoogieType type) {
		this.type = type;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(type);
		return children;
	}
}
