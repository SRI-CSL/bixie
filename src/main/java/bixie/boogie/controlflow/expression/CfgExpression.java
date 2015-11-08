/*
 * boogieamp - Parser, Factory, and Utilities to create Boogie Programs from Java
 * Copyright (C) 2013 Martin Schaeaeaef and Stephan Arlt
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

package bixie.boogie.controlflow.expression;

import java.util.HashMap;

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.CfgVariable;
import bixie.boogie.type.BoogieType;

/**
 * @author schaef
 * 
 */
public abstract class CfgExpression {

	private ILocation location;
	private BoogieType type;

	public CfgExpression(ILocation loc, BoogieType type) {
		this.location = loc;
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public BoogieType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(BoogieType type) {
		this.type = type;
	}

	/**
	 * @return the location
	 */
	public ILocation getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(ILocation location) {
		this.location = location;
	}

	public CfgExpression clone() {
		return substitute(null);
	}
	
	public abstract CfgExpression substitute(HashMap<CfgVariable, CfgExpression> substitutes);	
	
}
