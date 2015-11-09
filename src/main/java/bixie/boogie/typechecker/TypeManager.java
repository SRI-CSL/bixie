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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import bixie.boogie.ast.asttypes.ASTType;
import bixie.boogie.ast.asttypes.NamedAstType;
import bixie.boogie.ast.declaration.Declaration;
import bixie.boogie.ast.declaration.TypeDeclaration;
import bixie.boogie.type.BoogieType;
import bixie.boogie.type.TypeConstructor;
import bixie.boogie.type.TypeParameters;
import bixie.util.Log;

public class TypeManager {

	private HashMap<String, TypeConstructor> typeConstructors = new HashMap<String, TypeConstructor>();
	private HashMap<String, TypeDeclaration> declarations = new HashMap<String, TypeDeclaration>();
	private Stack<String> visiting = new Stack<String>();
	private Stack<TypeParameters> typeParamScopes = new Stack<TypeParameters>();
	private HashMap<ASTType, BoogieType> typeMap = new HashMap<ASTType, BoogieType>();
	//for printing purposes only
	private LinkedList<BoogieType> allBoogieTypes = new LinkedList<BoogieType>();

	
	private static boolean throwOnError = true;

	public TypeManager(Declaration[] decls) {
		for (Declaration d : decls) {
			if (d instanceof TypeDeclaration) {
				TypeDeclaration td = (TypeDeclaration) d;
				declarations.put(td.getIdentifier(), td);
			}
		}
	}

	public void pushTypeScope(TypeParameters typeParams) {
		typeParamScopes.push(typeParams);
	}

	public void popTypeScope() {
		typeParamScopes.pop();
	}

	public List<BoogieType> getAllBoogieTypes() {
		return this.allBoogieTypes;
	}
 	
	public static BoogieType getPrimitiveType(String typeName) {
		if (typeName.equals("int"))
			return BoogieType.intType;
		else if (typeName.equals("real"))
			return BoogieType.realType;
		else if (typeName.equals("bool"))
			return BoogieType.boolType;
		else if (typeName.startsWith("bv")) {
			int length = Integer.parseInt(typeName.substring(2));
			return BoogieType.createBitvectorType(length);
		} else {
			String msg = "getPrimitiveType called with unknown type " + typeName
					+ "!";			
			if (TypeManager.throwOnError) {
				throw new TypeCheckException(msg);
			}
			Log.error(msg);
			return BoogieType.errorType;
		}
	}

	public BoogieType resolveNamedType(NamedAstType type, boolean markUsed) {
		String name = type.getName();
		int numParam = type.getTypeArgs().length;

		ListIterator<TypeParameters> it = typeParamScopes
				.listIterator(typeParamScopes.size());
		int increment = 0;
		while (it.hasPrevious()) {
			TypeParameters tp = it.previous();
			BoogieType placeholderType = tp.findType(name, increment, markUsed);
			if (placeholderType != null) {
				if (numParam != 0) {
					String msg = "Bounded type " + name + " used with arguments.";
					if (TypeManager.throwOnError) {
						throw new TypeCheckException(msg);
					}
					Log.error(msg);					
					return BoogieType.errorType;
				}
				return placeholderType;
			}
			increment += tp.getCount();
		}

		if (!typeConstructors.containsKey(name)) {
			TypeDeclaration decl = declarations.get(name);
			if (decl == null) {
				String msg = "Type " + name + " is never defined.";
				if (TypeManager.throwOnError) {
					throw new TypeCheckException(msg);
				}
				Log.error(msg);				
				return BoogieType.errorType;
			}
			resolve(decl);
		}
		TypeConstructor tc = typeConstructors.get(name);
		if (tc == null) /* cyclic definition, already reported */
			return BoogieType.errorType;

		if (tc.getParamCount() != numParam) {
			String msg = "Type " + name + " used with wrong number of arguments.";
			if (TypeManager.throwOnError) {
				throw new TypeCheckException(msg);
			}
			Log.error(msg);			
			return BoogieType.errorType;
		}
		BoogieType[] typeArgs = new BoogieType[numParam];
		for (int i : tc.getParamOrder()) {
			typeArgs[i] = resolveType(type.getTypeArgs()[i], markUsed);
		}
		for (int i = 0; i < numParam; i++) {
			/*
			 * Resolve the other type arguments without marking place holders as
			 * used. Place holders are actually instantiated as tError.
			 */
			if (typeArgs[i] == null)
				typeArgs[i] = resolveType(type.getTypeArgs()[i], false);
		}
		return BoogieType.createConstructedType(tc, typeArgs);
	}

	public BoogieType resolveArrayType(bixie.boogie.ast.asttypes.ArrayAstType type,
			boolean markUsed) {
		TypeParameters typeParams = new TypeParameters(type.getTypeParams());
		pushTypeScope(typeParams);
		int numIndices = type.getIndexTypes().length;
		BoogieType[] indexTypes = new BoogieType[numIndices];
		for (int i = 0; i < numIndices; i++) {
			indexTypes[i] = resolveType(type.getIndexTypes()[i], markUsed);
		}
		if (!typeParams.fullyUsed()) {
			String msg = "ArrayType generics not used in index types: " + type;
			Log.error(msg);			
		}
		BoogieType resultType = resolveType(type.getValueType(), markUsed);
		popTypeScope();

		return BoogieType.createArrayType(type.getTypeParams().length,
				indexTypes, resultType);
	}

	public BoogieType resolveType(ASTType type, boolean markUsed) {
		BoogieType boogieType;
		if (type instanceof bixie.boogie.ast.asttypes.PrimitiveAstType)
			boogieType = getPrimitiveType(((bixie.boogie.ast.asttypes.PrimitiveAstType) type)
					.getName());
		else if (type instanceof NamedAstType)
			boogieType = resolveNamedType((NamedAstType) type, markUsed);
		else if (type instanceof bixie.boogie.ast.asttypes.ArrayAstType)
			boogieType = resolveArrayType(
					(bixie.boogie.ast.asttypes.ArrayAstType) type, markUsed);
		else {
			String msg = "Unknown ASTType " + type;
			if (TypeManager.throwOnError) {
				throw new TypeCheckException(msg);
			}
			Log.error(msg);			
			boogieType = BoogieType.errorType;
		}
		type.setBoogieType(boogieType);
		return boogieType;
	}
	
	public BoogieType resolveType(ASTType type) {
		if (!this.typeMap.containsKey(type)) {
			BoogieType t = resolveType(type, true);
			if (!allBoogieTypes.contains(t)) allBoogieTypes.add(t);
			this.typeMap.put(type, t);
		}
		return this.typeMap.get(type);
	}

	public void resolve(TypeDeclaration td) {
		if (visiting.contains(td.getIdentifier())) {
			Log.error("Cyclic type definition: " + visiting);
			typeConstructors.put(td.getIdentifier(), null);
		}
		visiting.push(td.getIdentifier());
		String name = td.getIdentifier();
		String[] typeParams = td.getTypeParams();
		BoogieType synonym = null;
		int[] order;
		if (td.getSynonym() != null) {
			TypeParameters tp = new TypeParameters(typeParams, true);
			pushTypeScope(tp);
			synonym = resolveType(td.getSynonym());
			order = new int[tp.getNumUsed()];
			System.arraycopy(tp.getOrder(), 0, order, 0, order.length);
			popTypeScope();
		} else {
			order = new int[typeParams.length];
			for (int i = 0; i < order.length; i++)
				order[i] = i;
		}
		visiting.pop();
		typeConstructors.put(name, new TypeConstructor(name, td.isFinite(),
				typeParams.length, order, synonym));
	}

	public void init() {
		for (TypeDeclaration td : declarations.values()) {
			if (typeConstructors.containsKey(td.getIdentifier()))
				continue;
			resolve(td);
		}
	}

}
