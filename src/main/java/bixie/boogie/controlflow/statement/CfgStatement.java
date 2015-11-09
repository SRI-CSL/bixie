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

package bixie.boogie.controlflow.statement;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.location.ILocation;

/**
 * @author schaef
 * 
 */
public abstract class CfgStatement {

	private ILocation location;
	private Attribute[] attributes;

	public CfgStatement(ILocation loc) {
		this.location = loc;
	}

	public CfgStatement(ILocation loc, Attribute[] attributes) {
		this.location = loc;
		this.attributes = attributes;
	}
	
	
	public ILocation getLocation() {
		return this.location;
	}

	public abstract CfgStatement duplicate();

	/**
	 * @return the attributes
	 */
	public Attribute[] getAttributes() {
		return attributes;
	}	
	
}
