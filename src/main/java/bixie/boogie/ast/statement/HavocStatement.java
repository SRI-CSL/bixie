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

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a havoc statement which is a special form of a statement.
 */
public class HavocStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifiers of this havoc statement.
	 */
	String[] identifiers;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param identifiers
	 *            the identifiers of this havoc statement.
	 */
	public HavocStatement(ILocation loc, String[] identifiers) {
		super(loc);
		this.identifiers = identifiers;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            attributes
	 * @param formula
	 *            the formula of this assert statement.
	 */
	public HavocStatement(ILocation loc, Attribute[] attributes, String[] identifiers) {
		super(loc, attributes);
		this.identifiers = identifiers;
	}
	
	
	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("HavocStatement").append('[');
		if (identifiers == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < identifiers.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(identifiers[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifiers of this havoc statement.
	 * 
	 * @return the identifiers of this havoc statement.
	 */
	public String[] getIdentifiers() {
		return identifiers;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifiers);
		return children;
	}
}
