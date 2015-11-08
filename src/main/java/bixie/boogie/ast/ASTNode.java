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

import java.util.LinkedList;
import java.util.List;

import bixie.boogie.ast.location.BoogieLocation;
import bixie.boogie.ast.location.ILocation;

public abstract class ASTNode {

	BoogieLocation location = null;

	public ASTNode(ILocation location) {

		if (location instanceof BoogieLocation) {
			this.location = ((BoogieLocation) location);
		}
	}

	public List<Object> getChildren() {
		return new LinkedList<Object>();
	}

	// public Payload getPayload() {
	// if (payload == null) {
	// //payload = new Payload(null, this.getClass().getName().toUpperCase());
	// }
	// return null;
	// }

	public ILocation getLocation() {
		return this.location;
	}
}
