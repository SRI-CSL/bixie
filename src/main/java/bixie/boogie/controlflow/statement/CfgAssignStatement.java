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

import bixie.boogie.ast.location.ILocation;
import bixie.boogie.controlflow.expression.CfgExpression;
import bixie.boogie.controlflow.expression.CfgIdentifierExpression;

/**
 * @author schaef
 * 
 */
public class CfgAssignStatement extends CfgStatement {

	private CfgIdentifierExpression[] left;
	private CfgExpression[] right;

	public CfgAssignStatement(ILocation loc, CfgIdentifierExpression[] lhs,
			CfgExpression[] rhs) {
		super(loc);
		this.left = lhs;
		this.right = rhs;
	}

	public CfgAssignStatement(ILocation loc, CfgIdentifierExpression lhs,
			CfgExpression rhs) {
		super(loc);
		this.left = new CfgIdentifierExpression[]{ lhs };
		this.right = new CfgExpression[]{ rhs };
	}
	
	
	/**
	 * @return the left
	 */
	public CfgIdentifierExpression[] getLeft() {
		return left;
	}

	/**
	 * @param left
	 *            the left to set
	 */
	public void setLeft(CfgIdentifierExpression[] left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public CfgExpression[] getRight() {
		return right;
	}

	/**
	 * @param right
	 *            the right to set
	 */
	public void setRight(CfgExpression[] right) {
		this.right = right;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();		
		for (int i=0; i<this.left.length;i++) {
			if (i!=0) sb.append(", ");
			sb.append(this.left[i].toString());
		}		
		sb.append(" := ");
		for (int i=0; i<this.right.length;i++) {
			if (i!=0) sb.append(", ");
			sb.append(this.right[i].toString());
		}		
		return sb.toString();
	}

	public CfgStatement duplicate() {
		CfgIdentifierExpression[] cloneleft = new CfgIdentifierExpression[this.left.length];
		for (int i=0; i<this.left.length; i++) {
			cloneleft[i] = (CfgIdentifierExpression) this.left[i].clone();
		}
		CfgExpression[] cloneright = new CfgExpression[this.right.length];
		for (int i=0; i<this.right.length; i++) {
			cloneright[i] = this.right[i].clone();
		}				
		return new CfgAssignStatement(this.getLocation(), cloneleft, cloneright);
	}
	
	
}
