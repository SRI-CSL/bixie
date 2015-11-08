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

package bixie.translation;

import java.util.HashMap;
import java.util.HashSet;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.enums.BinaryOperator;
import bixie.boogie.type.BoogieType;
import bixie.translation.soot.SootPrelude;
import bixie.translation.soot.SootProcedureInfo;
import bixie.translation.soot.TranslationHelpers;
import bixie.util.Log;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.NullType;
import soot.RefType;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.ClassConstant;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.FloatConstant;
import soot.jimple.LongConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

/**
 * @author schaef
 * 
 */
public class GlobalsCache {

	private ProgramFactory pf = null;

	public ProgramFactory getPf() {
		return pf;
	}

	private HashMap<SootMethod, SootProcedureInfo> procedureMap;
	private HashMap<SootField, Expression> fieldMap;
	private HashMap<Stmt, String> unitLabelMap;

	private HashMap<SootClass, IdentifierExpression> classTypeMap = new HashMap<SootClass, IdentifierExpression>();

	private HashMap<String, IdentifierExpression> cConstantTypeMap = new HashMap<String, IdentifierExpression>();
	
	
	private HashMap<String, IdentifierExpression> stringInternMap = new HashMap<String, IdentifierExpression>();
	private HashMap<String, IdentifierExpression> floatInternMap = new HashMap<String, IdentifierExpression>();
	private HashMap<String, IdentifierExpression> doubleInternMap = new HashMap<String, IdentifierExpression>();
	private HashMap<String, IdentifierExpression> longInternMap = new HashMap<String, IdentifierExpression>();
	
	private long unitLabelCounter = 0L;
	private final String blockPrefix = "block";

	private static GlobalsCache instance = null;

	public SootMethod currentMethod = null;
	
	public HashMap<EnterMonitorStmt, HashSet<Value>> modifiedInMonitor;
	
	
	public static GlobalsCache v() {
		if (GlobalsCache.instance == null) {
			GlobalsCache.instance = new GlobalsCache();
		}
		return GlobalsCache.instance;
	}
	
	public static void resetInstance() {
		if (instance != null) {
			GlobalsCache.instance.procedureMap.clear();
			GlobalsCache.instance.fieldMap.clear();
			GlobalsCache.instance.unitLabelMap.clear();
			GlobalsCache.instance.classTypeMap.clear();
			GlobalsCache.instance.classTypeMap.clear();
			GlobalsCache.instance.stringInternMap.clear();
			GlobalsCache.instance.floatInternMap.clear();
			GlobalsCache.instance.doubleInternMap.clear();
			if (GlobalsCache.instance.modifiedInMonitor!=null) GlobalsCache.instance.modifiedInMonitor.clear();
		}
		GlobalsCache.instance = null;
	}

	private GlobalsCache() {
		this.procedureMap = new HashMap<SootMethod, SootProcedureInfo>();
		this.fieldMap = new HashMap<SootField, Expression>();
		this.unitLabelMap = new HashMap<Stmt, String>();
		pf = new ProgramFactory();
	}

	public boolean hasUnitLabel(Stmt u) {
		return this.unitLabelMap.containsKey(u);
	}

	public String getUnitLabel(Stmt u) {
		if (!this.unitLabelMap.containsKey(u)) {
			this.unitLabelCounter++;
			this.unitLabelMap
					.put(u, this.blockPrefix + (this.unitLabelCounter));

		}
		return this.unitLabelMap.get(u);
	}

	public String getBlockLabel() {
		this.unitLabelCounter++;
		return this.blockPrefix + (this.unitLabelCounter);
	}
	
	public SootProcedureInfo lookupProcedure(SootMethod m) {
		if (!this.procedureMap.containsKey(m)) {
			SootProcedureInfo procinfo = new SootProcedureInfo(m);
			this.procedureMap.put(m, procinfo);
		}
		return this.procedureMap.get(m);
	}

	public IdentifierExpression lookupInternString(StringConstant s) {
		if (!stringInternMap.containsKey(s.value)) {
			String name = "$StringConst"+stringInternMap.size();
			stringInternMap.put(s.value, this.pf.mkIdentifierExpression(this.getBoogieType(s.getType()), name, true, true, true));
		}
		return stringInternMap.get(s.value);
	}
	
	public IdentifierExpression lookupInternFloat(FloatConstant s) {
		if (!floatInternMap.containsKey(s.toString())) {
			String name = "$FloatConst"+floatInternMap.size();
			floatInternMap.put(s.toString(), this.pf.mkIdentifierExpression(this.getBoogieType(s.getType()), name, true, true, true));
		}
		return floatInternMap.get(s.toString());
	}
	
	public IdentifierExpression lookupInternDouble(DoubleConstant s) {
		if (!doubleInternMap.containsKey(s.toString())) {
			String name = "$DoubleConst"+doubleInternMap.size();
			doubleInternMap.put(s.toString(), this.pf.mkIdentifierExpression(this.getBoogieType(s.getType()), name, true, true, true));
		}
		return doubleInternMap.get(s.toString());
	}

	public IdentifierExpression lookupInternLong(LongConstant s) {
		if (!longInternMap.containsKey(s.toString())) {
			String name = "$LongConst"+longInternMap.size();
			longInternMap.put(s.toString(), this.pf.mkIdentifierExpression(this.getBoogieType(s.getType()), name, true, true, true));
		}
		return longInternMap.get(s.toString());
	}
	
	
	/**
	 * Lookup a filed in a class.
	 * 
	 * @param field
	 * @return
	 */
	public Expression lookupSootField(SootField field) {
		if (!this.fieldMap.containsKey(field)) {
			String cleanname = TranslationHelpers.getQualifiedName(field);
			BoogieType btype;
			if (!field.isStatic()) {
				BoogieType[] params = { this.getBoogieType(field.getType()) };
				btype = pf.mkSubstituteType(SootPrelude.v().getFieldType(),
						params);
			} else {
				btype = this.getBoogieType(field.getType());
			}
			this.fieldMap.put(field, pf.mkIdentifierExpression(
					btype, cleanname,
					false, true, true));
		}
		return this.fieldMap.get(field);
	}

	private int freshglobalcounter = 0;

	public IdentifierExpression makeFreshGlobal(BoogieType type,
			boolean isConst, boolean isUnique) {
		return pf.mkIdentifierExpression( type, "$freshglobal_"
				+ (this.freshglobalcounter++), isConst, true, isUnique);
	}

	
	private HashMap<BoogieType, IdentifierExpression> havocGloabls = new HashMap<BoogieType, IdentifierExpression>();
	/**
	 * Get a special global in case you want to havoc something and need a
	 * otherwise unused gloabl of the same type.
	 * @param type
	 * @return
	 */
	public IdentifierExpression getHavocGlobal(BoogieType type) {
		if (!this.havocGloabls.containsKey(type)) {
			this.havocGloabls.put(type,  pf.mkIdentifierExpression( type, "$havoc"
				+ (this.freshglobalcounter++), false, true, false));
		}
		return this.havocGloabls.get(type);
	}
	
	
	public BoogieType getBoogieType(Type type) {
		BoogieType ret = null;
		if (type instanceof DoubleType || type instanceof FloatType) {
			// ret = pf.getRealType();
			ret = pf.getIntType();
		} else if (type instanceof IntType || type instanceof LongType
				|| type instanceof ByteType || type instanceof CharType
				|| type instanceof ShortType || type instanceof BooleanType) {
			ret = pf.getIntType();
		} else if (type instanceof RefType) {
			ret = SootPrelude.v().getReferenceType();
		} else if (type instanceof ArrayType) {
			ret = SootPrelude.v().getReferenceType();
		} else if (type == NullType.v()) {
			ret = SootPrelude.v().getNullConstant().getType();
		} else if (type instanceof VoidType) {
			ret = SootPrelude.v().getVoidType();
		} else {
			Log.error("Unknown Type " + type.toString()
					+ ": BoogieTypeFactory.lookupPrimitiveType");
			ret = null;
		}
		return ret;
	}

	public IdentifierExpression lookupClassVariable(SootClass c) {
		if (this.classTypeMap.containsKey(c)) {
			return this.classTypeMap.get(c);
		}		
		HashSet<IdentifierExpression> parents = new HashSet<IdentifierExpression>();
		if (c.hasSuperclass()) {
			parents.add(lookupClassVariable(c.getSuperclass()));
		}
		for (SootClass interf : c.getInterfaces()) {
			parents.add(lookupClassVariable(interf));
		}

		Attribute[] attributes = TranslationHelpers.javaLocation2Attribute(c.getTags());
		
		IdentifierExpression cvar = pf.mkIdentifierExpression(attributes, SootPrelude
				.v().getJavaClassType(),
				TranslationHelpers.getQualifiedName(c), true, true, true,
				parents.toArray(new IdentifierExpression[parents.size()]));
		this.classTypeMap.put(c, cvar);
		return cvar;
	}
	

    public Expression lookupArrayType(ArrayType t) {
      Type elementType = t.getElementType();
      Expression elementTypeExpr;
      if (elementType instanceof ArrayType) {
        elementTypeExpr = lookupArrayType((ArrayType)elementType);
      } else if (elementType instanceof RefType) {
        elementTypeExpr = lookupClassVariable(((RefType)elementType).getSootClass());
      } else if (elementType instanceof DoubleType || elementType instanceof FloatType) {
    	  //TODO: @Philipp macht das Sinn? Ich mache es so wie in getBoogieType.
    	  return SootPrelude.v().getIntArrayConstructor();  
		} else if (elementType instanceof IntType || elementType instanceof LongType
				|| elementType instanceof ByteType || elementType instanceof CharType
				|| elementType instanceof ShortType || elementType instanceof BooleanType) {
	     //TODO: @Philipp macht das Sinn? Ich mache es so wie in getBoogieType.
        return SootPrelude.v().getIntArrayConstructor();
      } else if (elementType instanceof ByteType) {
        return SootPrelude.v().getByteArrayConstructor();
      } else if (elementType instanceof CharType) {
        return SootPrelude.v().getCharArrayConstructor();
      } else if (elementType instanceof LongType) {
        return SootPrelude.v().getLongArrayConstructor();
      } else if (elementType instanceof BooleanType) {
        return SootPrelude.v().getBoolArrayConstructor();
      } else {
        System.out.println(elementType);
        assert(false);
        return null;
      }
      
      return
        pf.mkFunctionApplication( SootPrelude.v().getArrayTypeConstructor(),
                                 new Expression[] { elementTypeExpr });
    }

	public Statement setArraySizeStatement(Expression arrayvar,
			Expression arraysize) {
		Expression[] indices = { arrayvar };
		return pf.mkAssignmentStatement( SootPrelude.v()
				.getArrSizeHeapVariable(), pf.mkArrayStoreExpression(
				pf.getIntType(), SootPrelude.v().getArrSizeHeapVariable(),
				indices, arraysize));
	}

	public Expression getArraySizeExpression(Expression expression) {
		Expression[] indices = { expression };
		return pf.mkArrayAccessExpression(
				pf.getIntType(), SootPrelude.v().getArrSizeHeapVariable(),
				indices);
	}

	
	/**
	 * checks if subtype is a subtype of supertype
	 * @param subtype
	 * @param supertype
	 * @return
	 */
	public Expression compareTypeExpressions(Expression subtype, Expression supertype) {
		return GlobalsCache
			.v()
			.getPf()
			.mkBinaryExpression(
					GlobalsCache.v().getPf().getBoolType(),
					BinaryOperator.COMPPO, subtype,
					supertype);
	}
	
	public Expression sameTypeExpression(Expression typeA, Expression typeB) {
		//TODO: can this be done with equals as well?
		return GlobalsCache
				.v()
				.getPf()
				.mkBinaryExpression( 
						GlobalsCache.v().getPf().getBoolType(), 
						BinaryOperator.LOGICAND, 
						compareTypeExpressions(typeA, typeB), 
						compareTypeExpressions(typeB, typeA));
	}
	
	public boolean isProperSubType(SootClass sub, SootClass sup) {
		if (isSubTypeOrEqual(sub,sup) && sup!=sub) {
			return true;
		}
		return false;
	}
		
	public boolean isSubTypeOrEqual(SootClass sub, SootClass sup) {
		SootClass c = sub; 
		while (c!=null) {
			if (c==sup) return true;
			try {
				c = c.getSuperclass();
			} catch (Exception e) {
				//the exception it thrown, if we try 
				//to access the superclass of object
				return false;
			}
		}
		return false;
	}
	
	public boolean inThrowsClause(SootClass exception, SootProcedureInfo procinfo) {		
		for (SootClass sc : procinfo.getThrowsClasses()) {
			if (GlobalsCache.v().isSubTypeOrEqual(exception, sc)) {
				return true;
			}
		}
		return false;
	}
	
	public Expression lookupClassConstant(ClassConstant cc) {
		if (!cConstantTypeMap.containsKey(cc.getValue())) {
			String name = TranslationHelpers.replaceIllegalChars("CC$"+cc.value);			
			IdentifierExpression ide = this.pf.mkIdentifierExpression(this.getBoogieType(cc.getType()), name, true, true, true);			
			cConstantTypeMap.put(cc.getValue(), ide);
		}
		return cConstantTypeMap.get(cc.getValue());
	}

	
}
 