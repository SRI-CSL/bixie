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

import bixie.boogie.ast.declaration.Declaration;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a unit.
 */
public class Unit extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The declarations of this unit.
	 */
	Declaration[] declarations;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param declarations
	 *            the declarations of this unit.
	 */
	public Unit(ILocation loc, Declaration[] declarations) {
		super(loc);
		this.declarations = declarations;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Unit").append('[');
		if (declarations == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < declarations.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(declarations[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the declarations of this unit.
	 * 
	 * @return the declarations of this unit.
	 */
	public Declaration[] getDeclarations() {
		return declarations;
	}

	/**
	 * Sets the declarations of this unit.
	 * 
	 * @param declarations
	 *            the declarations of this unit.
	 */
	public void setDeclarations(Declaration[] declarations) {
		this.declarations = declarations;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(declarations);
		return children;
	}
}
