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

package bixie.boogie.ast.declaration;

import java.util.List;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a variable declaration which is a special form of a declaration.
 */
public class VariableDeclaration extends Declaration {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The variables of this variable declaration.
	 */
	VarList[] variables;
	
	public boolean isUnique = false;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            the attributes of this declaration.
	 * @param variables
	 *            the variables of this variable declaration.
	 */
	public VariableDeclaration(ILocation loc, Attribute[] attributes,
			VarList[] variables) {
		super(loc, attributes);
		this.variables = variables;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("VariableDeclaration").append('[');
		if (variables == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < variables.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(variables[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the variables of this variable declaration.
	 * 
	 * @return the variables of this variable declaration.
	 */
	public VarList[] getVariables() {
		return variables;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(variables);
		return children;
	}
}
