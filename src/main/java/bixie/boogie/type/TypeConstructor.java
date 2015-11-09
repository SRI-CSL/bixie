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

package bixie.boogie.type;

public class TypeConstructor  {
	/**
	 * long serialVersionUID
	 */	
	private final String name;
	private final boolean isFinite;
	private final int paramCount;
	private final int[] paramOrder;
	private final BoogieType synonym;

	public TypeConstructor(String name, boolean isFinite, int paramCount,
			int[] paramOrder) {
		this(name, isFinite, paramCount, paramOrder, null);
	}

	public TypeConstructor(String name, boolean isFinite, int paramCount,
			int[] paramOrder, BoogieType synonym) {
		this.name = name;
		this.isFinite = isFinite;
		this.paramCount = paramCount;
		this.paramOrder = paramOrder;
		this.synonym = synonym;
	}

	public String getName() {
		return name;
	}

	public int getParamCount() {
		return paramCount;
	}

	public int[] getParamOrder() {
		return paramOrder;
	}

	public BoogieType getSynonym() {
		return synonym;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (paramOrder.length > 0) {
			sb.append('<');
			String comma = "";
			for (int i = 0; i < paramOrder.length; i++) {
				sb.append(comma).append(paramOrder[i]);
				comma = ",";
			}
			sb.append('>');
		}
		if (synonym != null)
			sb.append('=').append(synonym);
		return sb.toString();
	}

	public boolean isFinite() {
		return isFinite;
	}
}
