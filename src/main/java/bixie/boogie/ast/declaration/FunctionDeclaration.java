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
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * Represents a function declaration which is a special form of a declaration.
 */
public class FunctionDeclaration extends Declaration {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The identifier of this function declaration.
	 */
	String identifier;

	/**
	 * The type params of this function declaration.
	 */
	String[] typeParams;

	/**
	 * The in params of this function declaration.
	 */
	VarList[] inParams;

	/**
	 * The out param of this function declaration.
	 */
	VarList outParam;

	/**
	 * The body of this function declaration.
	 */
	Expression body;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            the attributes of this declaration.
	 * @param identifier
	 *            the identifier of this function declaration.
	 * @param typeParams
	 *            the type params of this function declaration.
	 * @param inParams
	 *            the in params of this function declaration.
	 * @param outParam
	 *            the out param of this function declaration.
	 */
	public FunctionDeclaration(ILocation loc, Attribute[] attributes,
			String identifier, String[] typeParams, VarList[] inParams,
			VarList outParam) {
		super(loc, attributes);
		this.identifier = identifier;
		this.typeParams = typeParams;
		this.inParams = inParams;
		this.outParam = outParam;
	}

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param attributes
	 *            the attributes of this declaration.
	 * @param identifier
	 *            the identifier of this function declaration.
	 * @param typeParams
	 *            the type params of this function declaration.
	 * @param inParams
	 *            the in params of this function declaration.
	 * @param outParam
	 *            the out param of this function declaration.
	 * @param body
	 *            the body of this function declaration.
	 */
	public FunctionDeclaration(ILocation loc, Attribute[] attributes,
			String identifier, String[] typeParams, VarList[] inParams,
			VarList outParam, Expression body) {
		super(loc, attributes);
		this.identifier = identifier;
		this.typeParams = typeParams;
		this.inParams = inParams;
		this.outParam = outParam;
		this.body = body;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("FunctionDeclaration").append('[');
		sb.append(identifier);
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
		sb.append(',');
		if (inParams == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < inParams.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(inParams[i1]);
			}
			sb.append(']');
		}
		sb.append(',').append(outParam);
		sb.append(',').append(body);
		return sb.append(']').toString();
	}

	/**
	 * Gets the identifier of this function declaration.
	 * 
	 * @return the identifier of this function declaration.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the type params of this function declaration.
	 * 
	 * @return the type params of this function declaration.
	 */
	public String[] getTypeParams() {
		return typeParams;
	}

	/**
	 * Gets the in params of this function declaration.
	 * 
	 * @return the in params of this function declaration.
	 */
	public VarList[] getInParams() {
		return inParams;
	}

	/**
	 * Gets the out param of this function declaration.
	 * 
	 * @return the out param of this function declaration.
	 */
	public VarList getOutParam() {
		return outParam;
	}

	/**
	 * Gets the body of this function declaration.
	 * 
	 * @return the body of this function declaration.
	 */
	public Expression getBody() {
		return body;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(identifier);
		children.add(typeParams);
		children.add(inParams);
		children.add(outParam);
		children.add(body);
		return children;
	}
}
