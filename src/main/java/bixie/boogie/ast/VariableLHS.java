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
 * Represents a variable l h s which is a special form of a left hand side.
 */
public class VariableLHS extends LeftHandSide {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifier of this variable l h s.
	 */
	String identifier;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param identifier
	 *            the identifier of this variable l h s.
	 */
	public VariableLHS(ILocation loc, String identifier) {
		super(loc);
		this.identifier = identifier;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param type
	 *            the type of this left hand side.
	 * @param identifier
	 *            the identifier of this variable l h s.
	 */
	public VariableLHS(ILocation loc, BoogieType type, String identifier) {
		super(loc, type);
		this.identifier = identifier;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("VariableLHS").append('[');
		sb.append(identifier);
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifier of this variable l h s.
	 * 
	 * @return the identifier of this variable l h s.
	 */
	public String getIdentifier() {
		return identifier;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifier);
		return children;
	}
}
