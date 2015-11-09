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

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a named attribute which is a special form of a attribute.
 */
public class NamedAttribute extends Attribute {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The name of this named attribute.
	 */
	String name;

	/**
	 * The values of this named attribute.
	 */
	Expression[] values;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param name
	 *            the name of this named attribute.
	 * @param values
	 *            the values of this named attribute.
	 */
	public NamedAttribute(ILocation loc, String name, Expression[] values) {
		super(loc);
		this.name = name;
		this.values = values;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("NamedAttribute").append('[');
		sb.append(name);
		sb.append(',');
		if (values == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < values.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(values[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the name of this named attribute.
	 * 
	 * @return the name of this named attribute.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the values of this named attribute.
	 * 
	 * @return the values of this named attribute.
	 */
	public Expression[] getValues() {
		return values;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(name);
		children.add(values);
		return children;
	}
}
