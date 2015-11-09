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

import bixie.boogie.ast.declaration.VariableDeclaration;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.ast.statement.Statement;

/**
 * Represents a body.
 */
public class Body extends ASTNode {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The local vars of this body.
	 */
	VariableDeclaration[] localVars;

	/**
	 * The block of this body.
	 */
	Statement[] block;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param localVars
	 *            the local vars of this body.
	 * @param block
	 *            the block of this body.
	 */
	public Body(ILocation loc, VariableDeclaration[] localVars,
			Statement[] block) {
		super(loc);
		this.localVars = localVars;
		this.block = block;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Body").append('[');
		if (localVars == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < localVars.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(localVars[i1]);
			}
			sb.append(']');
		}
		sb.append(',');
		if (block == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < block.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(block[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the local vars of this body.
	 * 
	 * @return the local vars of this body.
	 */
	public VariableDeclaration[] getLocalVars() {
		return localVars;
	}

	/**
	 * Gets the block of this body.
	 * 
	 * @return the block of this body.
	 */
	public Statement[] getBlock() {
		return block;
	}

	/**
	 * Sets the block of this body.
	 * 
	 * @param block
	 *            the block of this body.
	 */
	public void setBlock(Statement[] block) {
		this.block = block;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(localVars);
		children.add(block);
		return children;
	}
}
