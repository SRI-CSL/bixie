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

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.type.BoogieType;

/**
 * Represents a named type which is a special form of a a s t type.
 */
public class NamedAstType extends ASTType {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The name of this named type.
	 */
	String name;

	/**
	 * The type args of this named type.
	 */
	ASTType[] typeArgs;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param name
	 *            the name of this named type.
	 * @param typeArgs
	 *            the type args of this named type.
	 */
	public NamedAstType(ILocation loc, String name, ASTType[] typeArgs) {
		super(loc);
		this.name = name;
		this.typeArgs = typeArgs;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param boogieType
	 *            the boogie type of this a s t type.
	 * @param name
	 *            the name of this named type.
	 * @param typeArgs
	 *            the type args of this named type.
	 */
	public NamedAstType(ILocation loc, BoogieType boogieType, String name,
			ASTType[] typeArgs) {
		super(loc, boogieType);
		this.name = name;
		this.typeArgs = typeArgs;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("NamedType").append('[');
		sb.append(name);
		sb.append(',');
		if (typeArgs == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < typeArgs.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(typeArgs[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the name of this named type.
	 * 
	 * @return the name of this named type.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type args of this named type.
	 * 
	 * @return the type args of this named type.
	 */
	public ASTType[] getTypeArgs() {
		return typeArgs;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(name);
		children.add(typeArgs);
		return children;
	}
}
