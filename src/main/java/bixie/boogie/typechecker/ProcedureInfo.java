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

package bixie.boogie.typechecker;

import bixie.boogie.ast.declaration.ProcedureDeclaration;
import bixie.boogie.type.TypeParameters;

public class ProcedureInfo {
	private final ProcedureDeclaration declaration;
	private final TypeParameters typeParams;
	private final VariableInfo[] inParams;
	private final VariableInfo[] outParams;

	public TypeParameters getTypeParameters() {
		return typeParams;
	}

	public ProcedureDeclaration getDeclaration() {
		return declaration;
	}

	public VariableInfo[] getInParams() {
		return inParams;
	}

	public VariableInfo[] getOutParams() {
		return outParams;
	}

	public ProcedureInfo(ProcedureDeclaration declaration, TypeParameters typeParams,
			VariableInfo[] inParams, VariableInfo[] outParams) {
		this.declaration = declaration;
		this.typeParams = typeParams;
		this.inParams = inParams;
		this.outParams = outParams;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(declaration.getIdentifier()).append('<')
				.append(typeParams.getCount());
		sb.append(">(");
		String comma = "";
		for (VariableInfo vi : inParams) {
			sb.append(comma);
			if (vi.getName() != null) {
				sb.append(vi.getName()).append(":");
			}
			sb.append(vi.getType());
			comma = ",";
		}
		if (outParams.length > 0) {
			sb.append(") returns (");
			comma = "";
			for (VariableInfo vi : outParams) {
				sb.append(comma);
				if (vi.getName() != null) {
					sb.append(vi.getName()).append(":");
				}
				sb.append(vi.getType());
				comma = ",";
			}
		}
		sb.append(")");
		return sb.toString();
	}
}
