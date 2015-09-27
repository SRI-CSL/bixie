/*
 * jimple2boogie - Translates Jimple (or Java) Programs to Boogie
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

package bixie.translation.soot;

import java.io.InputStream;
import java.util.HashMap;

import util.Log;
import bixie.translation.GlobalsCache;
import boogie.ProgramFactory;
import boogie.ast.Attribute;
import boogie.ast.declaration.FunctionDeclaration;
import boogie.ast.declaration.ProcedureDeclaration;
import boogie.ast.expression.Expression;
import boogie.ast.expression.IdentifierExpression;
import boogie.ast.statement.Statement;
import boogie.type.BoogieType;
import boogie.type.ConstructedType;

/**
 * @author schaef
 * 
 */
public class SootPrelude {

	private static SootPrelude instance = null;

	public static SootPrelude v() {
		if (SootPrelude.instance == null) {
			SootPrelude.instance = new SootPrelude();
		}
		return SootPrelude.instance;
	}

	public static void resetInstance() {
		if (instance != null) {
			// TODO
		}
		instance = null;
	}

	private BoogieType referenceType;
	private BoogieType voidType;
	private BoogieType fieldType;

	private BoogieType javaClassType;

	private BoogieType intArrType;
	private BoogieType refArrType;
	private BoogieType realArrType;
	private BoogieType boolArrType;

	private IdentifierExpression nullConstant;
	private IdentifierExpression heapVariable;

	private IdentifierExpression fieldAllocVariable;
	private IdentifierExpression fieldClassVariable;

	private FunctionDeclaration arrayTypeConstructor;
	private IdentifierExpression intArrayConstructor;
	private IdentifierExpression byteArrayConstructor;
	private IdentifierExpression charArrayConstructor;
	private IdentifierExpression longArrayConstructor;
	private IdentifierExpression boolArrayConstructor;

	private IdentifierExpression intArrHeapVariable;
	private IdentifierExpression realArrHeapVariable;
	private IdentifierExpression boolArrHeapVariable;
	private IdentifierExpression refArrHeapVariable;

	private IdentifierExpression stringSizeHeapVariable;

	private IdentifierExpression arrSizeHeapVariable;

	private FunctionDeclaration int2bool, bool2int, ref2bool;

	private FunctionDeclaration int2real, real2int;

	private FunctionDeclaration cmpBool, cmpInt, cmpReal, cmpRef;

	private FunctionDeclaration shlInt, shrInt, ushrInt, xorInt;

	private FunctionDeclaration mulInt, divInt, modInt;
	
	private FunctionDeclaration bitAnd, bitOr;

	private ProcedureDeclaration newObject;

	private String fieldTypeName = "Field";

	private void loadPreludeFile() {
		if (bixie.translation.Options.v().getPreludeFileName() != null) {
			try {
				Log.info("Loading user prelude: "
						+ bixie.translation.Options.v().getPreludeFileName());
				GlobalsCache
						.v()
						.getPf()
						.importBoogieFile(
								bixie.translation.Options.v().getPreludeFileName());
			} catch (Exception e) {
				throw new RuntimeException("Loading prelude failed: "
						+ e.toString());
			}
		} else {
			loadPreludeFromResources("/basic_prelude.bpl");
		}
	}

	private void loadPreludeFromResources(String name) {
		try {
			InputStream stream = SootPrelude.class.getResourceAsStream(name);
			GlobalsCache.v().getPf().importBoogieFile(name, stream);
			stream.close();

		} catch (Exception e1) {
			throw new RuntimeException(
					"Prelude file not available. Something failed during the build!");
		}

	}

	private SootPrelude() {
		ProgramFactory pf = GlobalsCache.v().getPf();

		// now load the prelude file.
		loadPreludeFile();

		this.referenceType = pf.findTypeByName("ref");
		this.voidType = pf.findTypeByName("void");
		this.javaClassType = pf.getNamedType("javaType");
		this.fieldType = pf.findTypeByName("Field");
		// this.heapType = pf.findTypeByName("$heap_type");

		this.nullConstant = pf.findGlobalByName("$null");
		this.heapVariable = pf.findGlobalByName("$heap");
		this.fieldAllocVariable = pf.findGlobalByName("$alloc");

		this.fieldClassVariable = pf.findGlobalByName("$type");

		// functions to represent Java/Soot array types
		this.arrayTypeConstructor = pf.findFunctionDeclaration("$arrayType");

		this.intArrayConstructor = pf.findGlobalByName("$intArrayType");
		this.byteArrayConstructor = pf.findGlobalByName("$byteArrayType");
		this.charArrayConstructor = pf.findGlobalByName("$charArrayType");
		this.longArrayConstructor = pf.findGlobalByName("$longArrayType");
		this.boolArrayConstructor = pf.findGlobalByName("$boolArrayType");

		// the following creates the heap variables for arrays:
		// each array is represented by one variable of type "ref" on the $heap.
		// this variable refers to the actual array on the array heap of
		// corresponding
		// type. That is:
		// a[x] = 3 translates to a variable a : ref and the read access whould
		// be
		// $intArrHeap[a][x] := 3
		this.intArrType = pf.findTypeByName("intArrHeap_type");
		this.refArrType = pf.findTypeByName("reflArrHeap_type");
		this.realArrType = pf.findTypeByName("realArrHeap_type");
		this.boolArrType = pf.findTypeByName("boolArrHeap_type");

		this.intArrHeapVariable = pf.findGlobalByName("$intArrHeap");
		this.refArrHeapVariable = pf.findGlobalByName("$refArrHeap");
		this.realArrHeapVariable = pf.findGlobalByName("$realArrHeap");
		this.boolArrHeapVariable = pf.findGlobalByName("$boolArrHeap");

		// an array that stores the size of java arrays.

		// an array that stores the size of java string.
		this.arrSizeHeapVariable = pf.findGlobalByName("$arrSizeHeap");
		this.stringSizeHeapVariable = pf.findGlobalByName("$stringSizeHeap");

		this.int2bool = pf.findFunctionDeclaration("$intToBool");
		this.bool2int = pf.findFunctionDeclaration("$boolToInt");
		this.ref2bool = pf.findFunctionDeclaration("$refToBool");
		this.int2real = pf.findFunctionDeclaration("$intToReal");
		this.real2int = pf.findFunctionDeclaration("$realToInt");

		this.cmpInt = pf.findFunctionDeclaration("$cmpInt");
		this.cmpReal = pf.findFunctionDeclaration("$cmpReal");
		this.cmpRef = pf.findFunctionDeclaration("$cmpRef");
		this.cmpBool = pf.findFunctionDeclaration("$cmpBool");

		this.mulInt = pf.findFunctionDeclaration("$mulInt");
		this.divInt = pf.findFunctionDeclaration("$divInt");
		this.modInt = pf.findFunctionDeclaration("$modInt");
		
		this.shlInt = pf.findFunctionDeclaration("$shlInt");
		this.shrInt = pf.findFunctionDeclaration("$shrInt");
		this.ushrInt = pf.findFunctionDeclaration("$ushrInt");

		this.xorInt = pf.findFunctionDeclaration("$xorInt");
		this.bitAnd = pf.findFunctionDeclaration("$bitAnd");
		this.bitOr = pf.findFunctionDeclaration("$bitOr");

		this.newObject = pf.findProcedureDeclaration("$new");

		if (bixie.translation.Options.v().getPreludeFileName() == null) {
			loadPreludeFromResources("/java_lang.bpl");
		}

	}

	private HashMap<String, FunctionDeclaration> realOperators = new HashMap<String, FunctionDeclaration>();

	/**
	 * TODO: Maybe these guys should be removed later once we have a proper way
	 * of dealing with floats and doubles.
	 * 
	 * @param op
	 * @param left
	 * @param right
	 * @return
	 */
	public FunctionDeclaration lookupRealOperator(String op) {
		if (!this.realOperators.containsKey(op)) {
			Attribute[] attributes = {};

			BoogieType integer = GlobalsCache.v().getPf().getIntType();
			IdentifierExpression x = GlobalsCache.v().getPf()
					.mkIdentifierExpression(integer, "x", false, false, false);
			IdentifierExpression y = GlobalsCache.v().getPf()
					.mkIdentifierExpression(integer, "y", false, false, false);
			IdentifierExpression[] in = { x, y };
			IdentifierExpression outParam = GlobalsCache
					.v()
					.getPf()
					.mkIdentifierExpression(integer, "$ret", false, false,
							false);
			this.realOperators.put(
					op,
					GlobalsCache
							.v()
							.getPf()
							.mkFunctionDeclaration(attributes,
									"$realOp" + op.hashCode(), in, outParam,
									null));
			Log.error("Created function that should be in Prelude: " + op);
		}
		return this.realOperators.get(op);
	}

	public Expression getNullConstant() {
		return this.nullConstant;
	}

	public IdentifierExpression getHeapVariable() {
		return this.heapVariable;
	}

	public BoogieType getReferenceType() {
		return this.referenceType;
	}

	public BoogieType getVoidType() {
		return this.voidType;
	}

	public BoogieType getFieldType() {
		return this.fieldType;
	}

	public BoogieType getJavaClassType() {
		return this.javaClassType;
	}

	public IdentifierExpression getFieldAllocVariable() {
		return fieldAllocVariable;
	}

	public IdentifierExpression getFieldClassVariable() {
		return fieldClassVariable;
	}

	public FunctionDeclaration getArrayTypeConstructor() {
		return arrayTypeConstructor;
	}

	public IdentifierExpression getIntArrayConstructor() {
		return intArrayConstructor;
	}

	public IdentifierExpression getByteArrayConstructor() {
		return byteArrayConstructor;
	}

	public IdentifierExpression getCharArrayConstructor() {
		return charArrayConstructor;
	}

	public IdentifierExpression getLongArrayConstructor() {
		return longArrayConstructor;
	}

	public IdentifierExpression getBoolArrayConstructor() {
		return boolArrayConstructor;
	}

	public IdentifierExpression getIntArrHeapVariable() {
		return this.intArrHeapVariable;
	}

	public IdentifierExpression getRealArrHeapVariable() {
		return this.realArrHeapVariable;
	}

	public IdentifierExpression getBoolArrHeapVariable() {
		return this.boolArrHeapVariable;
	}

	public IdentifierExpression getRefArrHeapVariable() {
		return this.refArrHeapVariable;
	}

	public IdentifierExpression getStringSizeHeapVariable() {
		return this.stringSizeHeapVariable;
	}

	public IdentifierExpression getArrSizeHeapVariable() {
		return this.arrSizeHeapVariable;
	}

	public BoogieType getIntArrType() {
		return this.intArrType;
	}

	public BoogieType getRefArrType() {
		return this.refArrType;
	}

	public BoogieType getRealArrType() {
		return this.realArrType;
	}

	public BoogieType getBoolArrType() {
		return this.boolArrType;
	}

	public BoogieType getFieldType(BoogieType type) {
		if (type instanceof ConstructedType) {
			ConstructedType ctype = (ConstructedType) type;
			if (ctype.getConstr().getName().equals(this.fieldTypeName)) {
				if (ctype.getConstr().getParamCount() == 1) {
					return ctype.getParameter(0);
				}
			}
		}
		throw new RuntimeException("The type " + type
				+ " is not a Field type. ");
	}

	public Expression intToBool(Expression exp) {
		Expression args[] = { exp };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.int2bool, args);
	}

	public Expression boolToInt(Expression exp) {
		Expression args[] = { exp };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.bool2int, args);
	}

	public Expression refToBool(Expression exp) {
		Expression args[] = { exp };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.ref2bool, args);
	}

	public Expression intToReal(Expression exp) {
		Expression args[] = { exp };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.int2real, args);
	}

	public Expression realToInt(Expression exp) {
		Expression args[] = { exp };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.real2int, args);
	}

	public Expression compareExpr(Expression left, Expression right) {
		if (left.getType() != right.getType()) {
			throw new RuntimeException(
					"can only compare expression of same type");
		}
		FunctionDeclaration operator;
		if (left.getType() == GlobalsCache.v().getPf().getIntType()) {
			operator = this.cmpInt;
		} else if (left.getType() == GlobalsCache.v().getPf().getRealType()) {
			operator = this.cmpReal;
		} else if (left.getType() == GlobalsCache.v().getPf().getBoolType()) {
			operator = this.cmpBool;
		} else if (left.getType() == this.referenceType) {
			operator = this.cmpRef;
		} else {
			throw new RuntimeException("cannot compare expressions of type "
					+ left.getType());
		}
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf().mkFunctionApplication(operator, args);
	}

	
	public Expression mulInt(Expression left, Expression right) {
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.mulInt, args);
	}

	public Expression divInt(Expression left, Expression right) {
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.divInt, args);
	}
	
	public Expression modInt(Expression left, Expression right) {
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.modInt, args);
	}
	

	
	public Expression shiftLeft(Expression left, Expression right) {
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.shlInt, args);
	}

	public Expression shiftRight(Expression left, Expression right) {
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.shrInt, args);
	}

	public Expression uShiftRight(Expression left, Expression right) {
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf()
				.mkFunctionApplication(this.ushrInt, args);
	}

	public Expression xorExpr(Expression left, Expression right) {
		if (left.getType() != right.getType()) {
			throw new RuntimeException(
					"can only compare expression of same type");
		}
		FunctionDeclaration operator;
		if (left.getType() == GlobalsCache.v().getPf().getIntType()) {
			operator = this.xorInt;
		} else {
			throw new RuntimeException("cannot compare expressions of type "
					+ left.getType());
		}
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf().mkFunctionApplication(operator, args);
	}

	public Expression bitAndExpr(Expression left, Expression right) {
		if (left.getType() != right.getType()) {
			throw new RuntimeException(
					"can only compare expression of same type");
		}
		FunctionDeclaration operator;
		if (left.getType() == GlobalsCache.v().getPf().getIntType()) {
			operator = this.bitAnd;
		} else {
			throw new RuntimeException("cannot compare expressions of type "
					+ left.getType());
		}
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf().mkFunctionApplication(operator, args);
	}

	public Expression bitOrExpr(Expression left, Expression right) {
		if (left.getType() != right.getType()) {
			throw new RuntimeException(
					"can only compare expression of same type");
		}
		FunctionDeclaration operator;
		if (left.getType() == GlobalsCache.v().getPf().getIntType()) {
			operator = this.bitOr;
		} else {
			throw new RuntimeException("cannot compare expressions of type "
					+ left.getType());
		}
		Expression args[] = { left, right };
		return GlobalsCache.v().getPf().mkFunctionApplication(operator, args);
	}

	public Statement newObject(Attribute[] attr, IdentifierExpression var,
			Expression obj_type) {
		ProgramFactory pf = GlobalsCache.v().getPf();
		return pf.mkCallStatement(attr, false,
				new IdentifierExpression[] { var },
				this.newObject.getIdentifier(), new Expression[] { obj_type });
	}

	public Expression heapAccess(Expression base, Expression field) {
		ProgramFactory pf = GlobalsCache.v().getPf();
		// Assemble the $heap[base, field] expression
		Expression[] indices = { base, field };
		return pf.mkArrayAccessExpression(
				SootPrelude.v().getFieldType(field.getType()), SootPrelude.v()
						.getHeapVariable(), indices);
	}

}
