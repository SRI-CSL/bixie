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
import bixie.boogie.ast.asttypes.ASTType;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a type declaration which is a special form of a declaration.
 */
public class TypeDeclaration extends Declaration {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * True iff this type declaration is finite.
	 */
	boolean isFinite;

	/**
	 * The identifier of this type declaration.
	 */
	String identifier;

	/**
	 * The type params of this type declaration.
	 */
	String[] typeParams;

	/**
	 * The synonym of this type declaration.
	 */
	ASTType synonym;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            the attributes of this declaration.
	 * @param isFinite
	 *            true iff this type declaration is finite.
	 * @param identifier
	 *            the identifier of this type declaration.
	 * @param typeParams
	 *            the type params of this type declaration.
	 */
	public TypeDeclaration(ILocation loc, Attribute[] attributes,
			boolean isFinite, String identifier, String[] typeParams) {
		super(loc, attributes);
		this.isFinite = isFinite;
		this.identifier = identifier;
		this.typeParams = typeParams;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            the attributes of this declaration.
	 * @param isFinite
	 *            true iff this type declaration is finite.
	 * @param identifier
	 *            the identifier of this type declaration.
	 * @param typeParams
	 *            the type params of this type declaration.
	 * @param synonym
	 *            the synonym of this type declaration.
	 */
	public TypeDeclaration(ILocation loc, Attribute[] attributes,
			boolean isFinite, String identifier, String[] typeParams,
			ASTType synonym) {
		super(loc, attributes);
		this.isFinite = isFinite;
		this.identifier = identifier;
		this.typeParams = typeParams;
		this.synonym = synonym;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("TypeDeclaration").append('[');
		sb.append(isFinite);
		sb.append(',').append(identifier);
		sb.append(',');
		if (typeParams == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < typeParams.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(typeParams[i1]);
			}
			sb.append(']');
		}
		sb.append(',').append(synonym);
		return sb.append(']').toString();
	}

	/**
	 * Checks iff this type declaration is finite.
	 * 
	 * @return true iff this type declaration is finite.
	 */
	public boolean isFinite() {
		return isFinite;
	}

	/**
	 * Gets the identifier of this type declaration.
	 * 
	 * @return the identifier of this type declaration.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the type params of this type declaration.
	 * 
	 * @return the type params of this type declaration.
	 */
	public String[] getTypeParams() {
		return typeParams;
	}

	/**
	 * Gets the synonym of this type declaration.
	 * 
	 * @return the synonym of this type declaration.
	 */
	public ASTType getSynonym() {
		return synonym;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(isFinite);
		children.add(identifier);
		children.add(typeParams);
		children.add(synonym);
		return children;
	}
}
