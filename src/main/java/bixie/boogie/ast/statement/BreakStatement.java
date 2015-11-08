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

import bixie.boogie.ast.location.ILocation;

/**
 * Represents a break statement which is a special form of a statement.
 */
public class BreakStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The label. If null breaks the immediate surrounding while loop.
	 */
	String label;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 */
	public BreakStatement(ILocation loc) {
		super(loc);
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param label
	 *            the label.
	 */
	public BreakStatement(ILocation loc, String label) {
		super(loc);
		this.label = label;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BreakStatement").append('[');
		sb.append(label);
		return sb.append(']').toString();
	}

	/**
	 * Gets the label. If null breaks the immediate surrounding while loop.
	 * 
	 * @return the label.
	 */
	public String getLabel() {
		return label;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(label);
		return children;
	}
}
