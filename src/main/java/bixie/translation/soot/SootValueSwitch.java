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

import java.util.LinkedList;

import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.declaration.FunctionDeclaration;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.enums.BinaryOperator;
import bixie.boogie.enums.UnaryOperator;
import bixie.boogie.type.BoogieType;
import bixie.translation.GlobalsCache;
import bixie.translation.Options;
import bixie.translation.util.CustomNullnessAnalysis;
import bixie.translation.util.MhpInfo;
import bixie.util.Log;
import soot.ArrayType;
import soot.DoubleType;
import soot.FloatType;
import soot.Immediate;
import soot.Local;
import soot.NullType;
import soot.RefType;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.EqExpr;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.JimpleValueSwitch;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.MethodHandle;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

/**
 * @author schaef
 */
public class SootValueSwitch implements JimpleValueSwitch {

	public boolean isLeftHandSide = false;

	private SootProcedureInfo procInfo;
	private SootStmtSwitch stmtSwitch;
	private ProgramFactory pf;

	public SootValueSwitch(SootProcedureInfo pinfo, SootStmtSwitch stmtswitch) {
		this.procInfo = pinfo;
		this.pf = GlobalsCache.v().getPf();
		this.stmtSwitch = stmtswitch;
	}

	private LinkedList<Expression> expressionStack = new LinkedList<Expression>();

	public Expression getExpression() {
		return this.expressionStack.pop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ConstantSwitch#caseClassConstant(soot.jimple.ClassConstant)
	 */
	@Override
	public void caseClassConstant(ClassConstant arg0) {
		this.expressionStack.push(GlobalsCache.v().lookupClassConstant(arg0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
	 */
	@Override
	public void caseDoubleConstant(DoubleConstant arg0) {
		this.expressionStack.push(GlobalsCache.v().lookupInternDouble(arg0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
	 */
	@Override
	public void caseFloatConstant(FloatConstant arg0) {
		this.expressionStack.push(GlobalsCache.v().lookupInternFloat(arg0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
	 */
	@Override
	public void caseIntConstant(IntConstant arg0) {
		expressionStack.push(this.pf.mkIntLiteral(String.valueOf(arg0.value)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
	 */
	@Override
	public void caseLongConstant(LongConstant arg0) {
		long value = arg0.value;
		if (value >= Integer.MAX_VALUE || value <= Integer.MIN_VALUE) {
			Log.debug("Used abstract value for long constant that didn't fit in int");
			this.expressionStack.push(GlobalsCache.v().lookupInternLong(arg0));
		} else {
			expressionStack.push(this.pf.mkIntLiteral(String.valueOf(value)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
	 */
	@Override
	public void caseNullConstant(NullConstant arg0) {
		expressionStack.push(SootPrelude.v().getNullConstant());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
	 */
	@Override
	public void caseStringConstant(StringConstant arg0) {
		this.expressionStack.push(GlobalsCache.v().lookupInternString(arg0));
	}

	private void translateBinOp(BinopExpr arg0) {
		this.isLeftHandSide = false;
		arg0.getOp1().apply(this);
		Expression lhs = this.expressionStack.pop();
		arg0.getOp2().apply(this);
		Expression rhs = this.expressionStack.pop();

		// cast if necessary. This has to be done because soot treats boolean as
		// integers
		if (lhs.getType() != rhs.getType()) {
			rhs = TranslationHelpers.castBoogieTypes(rhs, lhs.getType());
		}

		if (arg0.getType() instanceof FloatType
				|| arg0.getType() instanceof DoubleType) {
			FunctionDeclaration fun = SootPrelude.v().lookupRealOperator(
					arg0.getSymbol());
			Expression[] arguments = { lhs, rhs };
			this.expressionStack.push(this.pf.mkFunctionApplication(fun,
					arguments));
			return;
		}
		// if it is not Float or Double, proceed normally.
		createBinOp(arg0.getSymbol(), lhs, rhs);
	}

	public void createBinOp(String op, Expression left, Expression right) {
		BinaryOperator operator;
		BoogieType rettype;
		op = op.trim();
		if (op.compareTo("+") == 0) {
			rettype = left.getType();
			operator = BinaryOperator.ARITHPLUS;
		} else if (op.compareTo("-") == 0) {
			rettype = left.getType();
			operator = BinaryOperator.ARITHMINUS;
		} else if (op.compareTo("*") == 0) {
			this.expressionStack.push(SootPrelude.v().mulInt(left, right));
			return;
		} else if (op.compareTo("/") == 0) {
			// make sure that "right" is an Integer
			// then assert that it is different from 0
			if (this.stmtSwitch != null)
				this.stmtSwitch.getErrorModel().createDivByZeroGuard(right);
			this.expressionStack.push(SootPrelude.v().divInt(left, right));
			return;
		} else if (op.compareTo("%") == 0) {
			if (this.stmtSwitch != null)
				this.stmtSwitch.getErrorModel().createDivByZeroGuard(right);
			this.expressionStack.push(SootPrelude.v().modInt(left, right));
			return;
		} else if (op.compareTo("cmp") == 0 || op.compareTo("cmpl") == 0
				|| op.compareTo("cmpg") == 0) {
			this.expressionStack.push(SootPrelude.v().compareExpr(left, right));
			return;
		} else if (op.compareTo("==") == 0) {
			rettype = this.pf.getBoolType();
			operator = BinaryOperator.COMPEQ;
		} else if (op.compareTo("<") == 0) {
			rettype = this.pf.getBoolType();
			operator = BinaryOperator.COMPLT;
		} else if (op.compareTo(">") == 0) {
			rettype = this.pf.getBoolType();
			operator = BinaryOperator.COMPGT;
		} else if (op.compareTo("<=") == 0) {
			rettype = this.pf.getBoolType();
			operator = BinaryOperator.COMPLEQ;
		} else if (op.compareTo(">=") == 0) {
			rettype = this.pf.getBoolType();
			operator = BinaryOperator.COMPGEQ;
		} else if (op.compareTo("!=") == 0) {
			rettype = this.pf.getBoolType();
			operator = BinaryOperator.COMPNEQ;
		} else if (op.compareTo("&") == 0) {
			this.expressionStack.push(SootPrelude.v().bitAndExpr(left, right));
			return;
		} else if (op.compareTo("|") == 0) {
			this.expressionStack.push(SootPrelude.v().bitOrExpr(left, right));
			return;
		} else if (op.compareTo("<<") == 0) { // Shiftl
			this.expressionStack.push(SootPrelude.v().shiftLeft(left, right));
			return;
		} else if (op.compareTo(">>") == 0) { // Shiftr
			this.expressionStack.push(SootPrelude.v().shiftRight(left, right));
			return;
		} else if (op.compareTo(">>>") == 0) { // UShiftr
			this.expressionStack.push(SootPrelude.v().uShiftRight(left, right));
			return;
		} else if (op.compareTo("^") == 0) { // XOR
			this.expressionStack.push(SootPrelude.v().xorExpr(left, right));
			return;
		} else {
			throw new RuntimeException("UNKNOWN Jimple operator " + op);
		}
		this.expressionStack.push(this.pf.mkBinaryExpression(rettype, operator,
				left, right));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseAddExpr(soot.jimple.AddExpr)
	 */
	@Override
	public void caseAddExpr(AddExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
	 */
	@Override
	public void caseAndExpr(AndExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
	 */
	@Override
	public void caseCastExpr(CastExpr arg0) {
		this.isLeftHandSide = false;
		arg0.getOp().apply(this);
		Expression exp = this.getExpression();

		// if the jimple types are the same, just return the expression
		if (arg0.getOp().getType() == arg0.getCastType()) {
			this.expressionStack.push(exp);
			return;
		}

		BoogieType boogieTargetType = GlobalsCache.v().getBoogieType(
				arg0.getCastType());

		// //////////////////////////////////////////////////////////////////////
		if (boogieTargetType == this.pf.getIntType()
				|| boogieTargetType == this.pf.getRealType()
				|| boogieTargetType == this.pf.getBoolType()) {
			// in that case, exp is also of primitive type
			// otherwise, java or soot would translate it
			// into a more complex expression that uses the
			// appropriate java methods.
			this.expressionStack.push(TranslationHelpers.castBoogieTypes(exp,
					boogieTargetType));
			return;

			// //////////////////////////////////////////////////////////////////////
		} else if (arg0.getCastType() instanceof RefType) {

			final RefType targetType = (RefType) arg0.getCastType();

			if (targetType.getClassName().equals("java.lang.Object")) {
				// should always be ok, nothing to assert
				this.expressionStack.push(exp);
			} else if (arg0.getOp().getType() instanceof RefType) {
				// Guard that typeof(exp) <: targetType
				if (this.stmtSwitch != null)
					this.stmtSwitch.getErrorModel().createClassCastGuard(
							this.getClassTypeFromExpression(exp, false),
							GlobalsCache.v().lookupClassVariable(
									targetType.getSootClass()));
				this.expressionStack.push(exp);
			} else if (arg0.getOp().getType() instanceof NullType) {
				// should always be ok, nothing to assert
				this.expressionStack.push(exp);
			} else {
				/* Log.error */
				System.out.println("Don't know how to cast from "
						+ arg0.getOp().getType() + " to " + arg0.getCastType());
				this.expressionStack.push(createHavocedExpression(GlobalsCache
						.v().getBoogieType(arg0.getType())));
			}

			return;

			// //////////////////////////////////////////////////////////////////////
		} else if (arg0.getCastType() instanceof ArrayType) {

			final ArrayType targetType = (ArrayType) arg0.getCastType();
			if (this.stmtSwitch != null)
				this.stmtSwitch.getErrorModel().createClassCastGuard(
						this.getClassTypeFromExpression(exp, false),
						GlobalsCache.v().lookupArrayType(targetType));

			this.expressionStack.push(exp);

			return;

			// //////////////////////////////////////////////////////////////////////
			// old, should be removed at some point
		} else if (boogieTargetType == SootPrelude.v().getReferenceType()) {
			// ILocation loc = exp.getLocation();
			if (arg0.getCastType() instanceof RefType) {
				RefType rtype = (RefType) arg0.getCastType();
				// Guard that typeof(exp) <: targetType
				if (this.stmtSwitch != null)
					this.stmtSwitch.getErrorModel().createClassCastGuard(
							this.getClassTypeFromExpression(exp, false),
							GlobalsCache.v().lookupClassVariable(
									rtype.getSootClass()));
				this.expressionStack.push(exp);
				return;
			} else {
				throw new RuntimeException("Cast from "
						+ arg0.getOp().getType() + " to " + arg0.getCastType()
						+ " not implemented");
			}
		}

		throw new RuntimeException("Cast from " + arg0.getOp().getType()
				+ " to " + arg0.getCastType() + " not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
	 */
	@Override
	public void caseCmpExpr(CmpExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
	 */
	@Override
	public void caseCmpgExpr(CmpgExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
	 */
	@Override
	public void caseCmplExpr(CmplExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
	 */
	@Override
	public void caseDivExpr(DivExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
	 */
	@Override
	public void caseEqExpr(EqExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
	 */
	@Override
	public void caseGeExpr(GeExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
	 */
	@Override
	public void caseGtExpr(GtExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
	 */
	@Override
	public void caseInstanceOfExpr(InstanceOfExpr arg0) {
		this.isLeftHandSide = false;
		arg0.getOp().apply(this);
		Expression lhs = this.getExpression();
		if (arg0.getCheckType() instanceof RefType) {
			RefType rtype = (RefType) arg0.getCheckType();
			Expression rhs = GlobalsCache.v().lookupClassVariable(
					rtype.getSootClass());

			Expression isNonNull = this.pf.mkBinaryExpression(
					this.pf.getBoolType(), BinaryOperator.COMPNEQ, lhs,
					SootPrelude.v().getNullConstant());

			Expression isSubtype = this.pf.mkBinaryExpression(
					this.pf.getBoolType(), BinaryOperator.COMPPO,
					this.getClassTypeFromExpression(lhs, false), rhs);

			this.expressionStack.push(this.pf.mkBinaryExpression(
					this.pf.getBoolType(), BinaryOperator.LOGICAND, isNonNull,
					isSubtype));
		} else {
			Log.debug("instanceof for arrays not implemented");
			this.expressionStack.push(createHavocedExpression(GlobalsCache.v()
					.getBoogieType(arg0.getType())));
		}
	}

	/**
	 * takes a fake global of appropriate type, adds a havoc statement before it
	 * and returns the identifier expression for it.
	 * 
	 * @param t
	 * @return
	 */
	private IdentifierExpression createHavocedExpression(BoogieType t) {
		Attribute[] arrtibutes = {};
		if (this.stmtSwitch != null) {
			arrtibutes = TranslationHelpers
					.javaLocation2Attribute(this.stmtSwitch
							.getCurrentStatement());
		}
		IdentifierExpression ide = GlobalsCache.v().getHavocGlobal(t);
		if (this.stmtSwitch != null) {
			this.stmtSwitch.addStatement(this.pf.mkHavocStatement(arrtibutes,
					ide));
		}
		return ide;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.
	 * InterfaceInvokeExpr)
	 */
	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr arg0) {
		throw new RuntimeException("This must be handeled by SootStmtSwitch!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
	 */
	@Override
	public void caseLeExpr(LeExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
	 */
	@Override
	public void caseLengthExpr(LengthExpr arg0) {
		this.isLeftHandSide = false;
		arg0.getOp().apply(this);
		Expression base = this.getExpression();
		this.stmtSwitch.getErrorModel().createNonNullGuard(base);
		this.expressionStack
				.push(GlobalsCache.v().getArraySizeExpression(base));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
	 */
	@Override
	public void caseLtExpr(LtExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
	 */
	@Override
	public void caseMulExpr(MulExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
	 */
	@Override
	public void caseNeExpr(NeExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
	 */
	@Override
	public void caseNegExpr(NegExpr arg0) {
		// this is arithmetic negative!
		// logic neg is already translated by soot
		this.isLeftHandSide = false;
		arg0.getOp().apply(this);
		Expression e = this.expressionStack.pop();
		this.expressionStack.push(this.pf.mkUnaryExpression(e.getType(),
				UnaryOperator.ARITHNEGATIVE, e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
	 */
	@Override
	public void caseNewArrayExpr(NewArrayExpr arg0) {
		throw new RuntimeException("Must be handeled in SootStmtSwitch");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
	 */
	@Override
	public void caseNewExpr(NewExpr arg0) {
		throw new RuntimeException("Must be handeled in SootStmtSwitch");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr
	 * )
	 */
	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr arg0) {
		throw new RuntimeException("Must be handeled in SootStmtSwitch");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
	 */
	@Override
	public void caseOrExpr(OrExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
	 */
	@Override
	public void caseRemExpr(RemExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
	 */
	@Override
	public void caseShlExpr(ShlExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
	 */
	@Override
	public void caseShrExpr(ShrExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr
	 * )
	 */
	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr arg0) {
		throw new RuntimeException("This must be handeled by SootStmtSwitch!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
	 */
	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr arg0) {
		throw new RuntimeException("This must be handeled by SootStmtSwitch!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
	 */
	@Override
	public void caseSubExpr(SubExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
	 */
	@Override
	public void caseUshrExpr(UshrExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr
	 * )
	 */
	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr arg0) {
		throw new RuntimeException("This must be handeled by SootStmtSwitch!");
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr arg0) {
		throw new RuntimeException("This must be handeled by SootStmtSwitch!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
	 */
	@Override
	public void caseXorExpr(XorExpr arg0) {
		translateBinOp(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
	 */
	@Override
	public void caseArrayRef(ArrayRef arg0) {
		BoogieType btype = GlobalsCache.v().getBoogieType(arg0.getType());
		Expression arrayvar;
		BoogieType arrtype;
		if (btype == this.pf.getIntType()) {
			arrayvar = SootPrelude.v().getIntArrHeapVariable();
			arrtype = SootPrelude.v().getIntArrType();
		} else if (btype == this.pf.getBoolType()) {
			arrayvar = SootPrelude.v().getBoolArrHeapVariable();
			arrtype = SootPrelude.v().getBoolArrType();
			// } else if (btype == this.pf.getRealType() ) {
			// arrayvar = SootPrelude.v().getRealArrHeapVariable();
			// arrtype= SootPrelude.v().getRealArrType();
		} else if (btype == SootPrelude.v().getReferenceType()) {
			arrayvar = SootPrelude.v().getRefArrHeapVariable();
			arrtype = SootPrelude.v().getRefArrType();
		} else {
			throw new RuntimeException("do not understand array of type: "
					+ btype.toString());
		}
		// tranlate base and index
		this.isLeftHandSide = false;
		arg0.getBase().apply(this);
		Expression baseExpression = this.expressionStack.pop();
		arg0.getIndex().apply(this);
		Expression indexExpression = this.expressionStack.pop();
		// guard out-of-bounds exceptions
		this.stmtSwitch.getErrorModel().createArrayBoundGuard(baseExpression,
				indexExpression);

		// construct the expression
		Expression[] base = { baseExpression };
		Expression array = this.pf.mkArrayAccessExpression(arrtype, arrayvar,
				base);
		Expression[] indices = { indexExpression };
		this.expressionStack.push(this.pf.mkArrayAccessExpression(btype, array,
				indices));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef
	 * )
	 */
	@Override
	public void caseCaughtExceptionRef(CaughtExceptionRef arg0) {
		if (arg0.getType() instanceof RefType) {
			RefType rtype = (RefType) arg0.getType();
						
			// assume that the exception variable now has the type of the caught
			// exception
			// assume $heap[$exception,$type] <: arg0.getType()

			if (this.stmtSwitch != null) {
				// ensure that $exception is not null.
				//TODO: actually don't! Because this creates
				//a dead else branch in some cases.
				//the non-nullness is ensured by the compiler.
//				this.stmtSwitch.getErrorModel()
//						.createNonNullViolationException(
//								this.procInfo.getExceptionVariable());

				Expression typefield = this.getClassTypeFromExpression(
						this.procInfo.getExceptionVariable(), false);

				this.stmtSwitch.addStatement(pf.mkAssumeStatement(
						new Attribute[] {},
						GlobalsCache.v().compareTypeExpressions(
								typefield,
								GlobalsCache.v().lookupClassVariable(
										rtype.getSootClass()))));
			}
			this.expressionStack.push(this.procInfo.getExceptionVariable());
			return;
		}
		throw new RuntimeException(
				"this case of exception handling has not beend implemented");
	}

	private void checkFieldAnnotations(Expression expr, FieldRef fr) {
		LinkedList<SootAnnotations.Annotation> annot = SootAnnotations
				.parseFieldTags(fr.getField());
		if (annot.contains(SootAnnotations.Annotation.NonNull)) {
			this.stmtSwitch.addStatement(this.stmtSwitch.getErrorModel()
					.createAssumeNonNull(expr));
		}

	}

	private boolean checkSharedField(FieldRef ref, Expression field) {
		if (!Options.v().useSoundThreads()) {
			return false;
		} else {
			if (!this.stmtSwitch.isInMonitor()) {
				if (MhpInfo.v().getSharedFields(this.procInfo.getSootMethod())
						.contains(ref.getField())) {
					// if the field can be modified in another thread, add a
					// havoc statement before it is used.
					return true;
				} else {
					// do nothing
				}

			} else {
				Log.info("Was in monitor ... ");
			}
		}
		return false;
	}

	private void havocField(Expression field, Expression base) {
		if (base == null) {
			// the field is static
			IdentifierExpression identifier = (IdentifierExpression) field;
			if (identifier != null) {
				Statement s = this.pf.mkHavocStatement(TranslationHelpers
						.javaLocation2Attribute(this.stmtSwitch
								.getCurrentStatement()), identifier);
				this.stmtSwitch.addStatement(s);
			} else {
				throw new RuntimeException("Not Implemented");
			}
		} else {
			Expression heapAccess = this.makeHeapAccessExpression(base, field,
					true);
			IdentifierExpression havocval = GlobalsCache.v().getHavocGlobal(
					heapAccess.getType());
			this.stmtSwitch.addStatement(this.pf.mkHavocStatement(
					TranslationHelpers.javaLocation2Attribute(this.stmtSwitch
							.getCurrentStatement()), havocval));
			Expression[] indices = { base, field };
			Expression heapupdate = pf.mkArrayStoreExpression(SootPrelude.v()
					.getHeapVariable().getType(), SootPrelude.v()
					.getHeapVariable(), indices, havocval);
			Statement s = pf.mkAssignmentStatement(SootPrelude.v()
					.getHeapVariable(), heapupdate);
			this.stmtSwitch.addStatement(s);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
	 */
	@Override
	public void caseInstanceFieldRef(InstanceFieldRef arg0) {
		boolean islhs = this.isLeftHandSide;
		this.isLeftHandSide = false;

		arg0.getBase().apply(this);
		Expression base = this.getExpression();
		Expression field = GlobalsCache.v().lookupSootField(arg0.getField());

		boolean nullCheckNeeded = true;
		// check if the field may be modified by another thread.
		if (!islhs && checkSharedField(arg0, field)) {
			havocField(field, base);
			nullCheckNeeded = false;
		}

		SootProcedureInfo pinfo = this.stmtSwitch.getProcInfo();
		
		// check if base is trivially non-null
		if (pinfo != null) {
			CustomNullnessAnalysis nna = this.stmtSwitch.getProcInfo()
					.getNullnessAnalysis();
			if (nna != null && arg0.getBase() instanceof Immediate) {
				if (nna.isAlwaysNonNullBefore(
						this.stmtSwitch.getCurrentStatement(),
						(Immediate) arg0.getBase())) {
					nullCheckNeeded = false;
				}
			}

			//TODO: this is a very clumsy way of obtaining thislocal
			// but it is not obvious when it throws an exception.
			Local thislocal = null;
			try {
				thislocal =  pinfo.getSootMethod().getActiveBody().getThisLocal();
			} catch (Exception e) {
				thislocal = null;
			}
			//check if base is "this" or a local variable
			//that is an alias of "this".
			if (arg0.getBase() ==thislocal || arg0.getBase() instanceof ThisRef)  {
				nullCheckNeeded = false; // TODO: check if this is actually needed.
			}
				
			
		}
		
		
		// We are checking if this is a @NonNull field
		// if so, we add an assume to ensure that it actually is
		// not null here.
		checkFieldAnnotations(
				this.makeHeapAccessExpression(base, field, false), arg0);

		this.expressionStack.push(this.makeHeapAccessExpression(base, field,
				nullCheckNeeded));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
	 */
	@Override
	public void caseParameterRef(ParameterRef arg0) {
		this.expressionStack.push(this.procInfo.lookupParameterVariable(arg0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
	 */
	@Override
	public void caseStaticFieldRef(StaticFieldRef arg0) {
		// TODO: we are checking if this is a @NonNull field
		// if so, we add an assume to ensure that it actually is
		// not null here. May be better ways to do this.
		checkFieldAnnotations(
				GlobalsCache.v().lookupSootField(arg0.getField()), arg0);
		Expression field = GlobalsCache.v().lookupSootField(arg0.getField());
		// check if the field may be modified by another thread.
		// (unless it is a lhs expression. In that case we don't care.
		if (!this.isLeftHandSide && checkSharedField(arg0, field)) {
			havocField(field, null);
		}

		this.expressionStack.push(field);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
	 */
	@Override
	public void caseThisRef(ThisRef arg0) {
		this.expressionStack.push(this.procInfo.getThisReference());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
	 */
	@Override
	public void caseLocal(Local arg0) {
		this.expressionStack.push(this.procInfo.lookupLocalVariable(arg0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see soot.jimple.ConstantSwitch#defaultCase(java.lang.Object)
	 */
	@Override
	public void defaultCase(Object arg0) {
		Log.error("BoogieValueSwitch: case not covered");
		assert (false);
	}

	/**
	 * Make a heap access and the necessary assertion
	 * 
	 * @param field
	 * @return
	 */
	public Expression makeHeapAccessExpression(Expression base,
			Expression field, boolean guarded) {
		if (guarded && this.stmtSwitch != null) {
			this.stmtSwitch.getErrorModel().createNonNullGuard(base);
		}
		// Assemble the $heap[base, field] expression
		return SootPrelude.v().heapAccess(base, field);
	}

	/**
	 * gets the field of expr that denotes its Java type E.g., let's say we have
	 * a variable c of type C. The call getExprssionJavaClass(c) returns
	 * $heap[c, $type] which, in this case, would be C.
	 * 
	 * @param expr
	 * @param guarded
	 * @return
	 */
	public Expression getExprssionJavaClass(Expression expr) {
		return getClassTypeFromExpression(expr, true);
	}

	/**
	 * gets the field of expr that denotes its Java type E.g., let's say we have
	 * a variable c of type C. The call getExprssionJavaClass(c,false) returns
	 * $heap[c, $type] which, in this case, would be C.
	 * 
	 * @param expr
	 * @param guarded
	 * @return
	 */
	public Expression getClassTypeFromExpression(Expression expr,
			boolean guarded) {
		return makeHeapAccessExpression(expr, SootPrelude.v()
				.getFieldClassVariable(), guarded);
	}

	@Override
	public void caseMethodHandle(MethodHandle arg0) {
		throw new RuntimeException("Not implemented "+arg0);
	}

}
