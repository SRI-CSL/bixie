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
 * Represents a goto statement which is a special form of a statement.
 */
public class GotoStatement extends Statement {
	/**
	 * The serial version UID.
	 */
	// private static final long serialVersionUID = 1L;
	/**
	 * The labels of this goto statement.
	 */
	String[] labels;

	/**
	 * The constructor taking initial values.
	 * 
	 * @param loc
	 *            the node's location
	 * @param labels
	 *            the labels of this goto statement.
	 */
	public GotoStatement(ILocation loc, String[] labels) {
		super(loc);
		this.labels = labels;
	}

	/**
	 * Returns a textual description of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("GotoStatement").append('[');
		if (labels == null) {
			sb.append("null");
		} else {
			sb.append('[');
			for (int i1 = 0; i1 < labels.length; i1++) {
				if (i1 > 0)
					sb.append(',');
				sb.append(labels[i1]);
			}
			sb.append(']');
		}
		return sb.append(']').toString();
	}

	/**
	 * Gets the labels of this goto statement.
	 * 
	 * @return the labels of this goto statement.
	 */
	public String[] getLabels() {
		return labels;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		children.add(labels);
		return children;
	}
}
