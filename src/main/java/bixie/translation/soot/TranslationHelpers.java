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

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import bixie.Options;
import bixie.boogie.ProgramFactory;
import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.type.BoogieType;
import bixie.translation.GlobalsCache;
import bixie.util.Log;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.Unit;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLnNamePosTag;
import soot.tagkit.Tag;

/**
 * @author schaef
 * 
 */
public class TranslationHelpers {

	
	public static Statement havocEverything(SootProcedureInfo procInfo,
			SootValueSwitch valueswitch) {
		LinkedList<IdentifierExpression> havoc_var = new LinkedList<IdentifierExpression>();
		for (IdentifierExpression id : procInfo.getLocalVariables()) {
			havoc_var.add(id);
		}
		for (IdentifierExpression id : procInfo.getOutParamters()) {
			havoc_var.add(id);
		}
		for (StaticFieldRef sr : procInfo.usedStaticFields) {
			sr.apply(valueswitch);
			IdentifierExpression ide = (IdentifierExpression) valueswitch
					.getExpression();
			havoc_var.add(ide);
		}

		havoc_var.add(SootPrelude.v().getHeapVariable());
		return GlobalsCache
				.v()
				.getPf()
				.mkHavocStatement(
						new Attribute[] {},
						havoc_var.toArray(new IdentifierExpression[havoc_var
								.size()]));
	}

	public static void getReachableTraps(Unit s, SootMethod m,
			List<Trap> out_traps, List<Trap> out_finally) {
		if (m.getActiveBody() == null) {
			throw new RuntimeException("cannot look into " + m.getSignature());
		}
		out_traps.addAll(TrapManager.getTrapsAt(s, m.getActiveBody()));

		SootClass throwable = Scene.v().loadClass("java.lang.Throwable",
				SootClass.SIGNATURES);
		
		Unit trap_begin = null;
		Unit trap_end = null;
		SootClass trap_exception = null;
		// Unit trap_handler = null;
		for (Trap trap : new LinkedList<Trap>(out_traps)) {
			if (trap.getBeginUnit() == trap_begin
					&& trap.getEndUnit() == trap_end
					&& trap.getException() == trap_exception) {
				out_traps.remove(trap);
				if (trap.getException() == throwable) {
					// in that case the second trap was most likely
					// of type "any" and thus is a finally block, but
					// soot translated that into Throwable.
					out_finally.add(trap);
					// System.err.println("unreachable finally "+GlobalsCache.v().getUnitLabel((Stmt)
					// trap.getHandlerUnit()));
					// System.err.println("\t\t"+GlobalsCache.v().getUnitLabel((Stmt)
					// trap_handler));
				}
			} else {
				// everything fine.
			}
			trap_begin = trap.getBeginUnit();
			trap_end = trap.getEndUnit();
			trap_exception = trap.getException();
			// trap_handler = trap.getHandlerUnit();
		}

	}

	public static Statement mkLocationAssertion(Stmt s) {
		return mkLocationAssertion(s, false, null);
	}

	public static Statement mkLocationAssertion(Stmt s,
			boolean forceCloneAttribute) {
		return mkLocationAssertion(s, forceCloneAttribute, null);
	}	
	
	public static Statement mkLocationAssertion(Stmt s,
			boolean forceCloneAttribute, String comment) {
		return GlobalsCache
				.v()
				.getPf()
				.mkAssertStatement(
						javaLocation2Attribute(s, forceCloneAttribute, comment),
						GlobalsCache.v().getPf().mkBooleanLiteral(true));
	}

	public static final HashSet<Stmt> clonedFinallyBlocks = new HashSet<Stmt>();

	public static Attribute[] javaLocation2Attribute(Stmt s) {
		return javaLocation2Attribute(s, false, null);
	}

	public static Attribute[] javaLocation2Attribute(Stmt s,
			boolean forceCloneAttribute, String comment) {
		return javaLocation2Attribute(s.getTags(),
				clonedFinallyBlocks.contains(s) || forceCloneAttribute, comment);
	}

	public static Attribute[] javaLocation2Attribute(List<Tag> list) {
		return javaLocation2Attribute(list, false, null);
	}

	private static String getFileName(SootClass sc) {
		if (sc.hasOuterClass() && sc.getOuterClass()!=sc)
			return getFileName(sc.getOuterClass());
		String filename = null;
		for (Tag t_ : sc.getTags()) {
			if (t_ instanceof SourceFileTag) {
				filename = ((SourceFileTag) t_).getSourceFile();
				if (((SourceFileTag) t_).getAbsolutePath() != null) {
					filename = ((SourceFileTag) t_).getAbsolutePath();
				}
				break;
			} else if (t_ instanceof SourceLnNamePosTag) {
				filename = ((SourceLnNamePosTag) t_).getFileName();
				// don't break, mybe there is still a source file tag.
				
			}
		}
		File srcFile = new File(filename);
				
		if (!srcFile.exists() && Options.v().getSrcDir()!=null) {
			StringBuilder sb = new StringBuilder();
			sb.append(Options.v().getSrcDir());
			sb.append(File.separator);
			sb.append(sc.getPackageName().replace(".", File.separator));
			sb.append(File.separator);
			sb.append(filename);
			srcFile = new File(sb.toString());
			if (!srcFile.exists()) {
				Log.error("Source file not found: "+srcFile.getAbsolutePath() + ".\nCheck your settings.");
			}
			filename = srcFile.getAbsolutePath();
		}

		return filename;
	}

	public static Statement createClonedAttribAssert() {
		ProgramFactory pf = GlobalsCache.v().getPf();
		Attribute[] res = new Attribute[] { pf
				.mkCustomAttribute(ProgramFactory.Cloned) };
		return pf.mkAssertStatement(res, pf.mkBooleanLiteral(true));
	}

	public static Attribute[] javaLocation2Attribute(List<Tag> list,
			boolean isCloned, String comment) {
		// if the taglist is empty return no location
		int startln, endln, startcol, endcol;

		startln = -1;
		endln = -1;
		startcol = -1;
		endcol = -1;
		String filename = null;

		if (GlobalsCache.v().currentMethod != null) {
			filename = getFileName(GlobalsCache.v().currentMethod
					.getDeclaringClass());
		}

		for (Tag tag : list) {
			if (tag instanceof LineNumberTag) {
				startln = ((LineNumberTag) tag).getLineNumber();
				break;
			} else if (tag instanceof SourceLnNamePosTag) {
				if (filename == null) {
					filename = ((SourceLnNamePosTag) tag).getFileName();
				}
				startln = ((SourceLnNamePosTag) tag).startLn();
				endln = ((SourceLnNamePosTag) tag).endLn();
				startcol = ((SourceLnNamePosTag) tag).startPos();
				endcol = ((SourceLnNamePosTag) tag).endPos();
				break;
			} else if (tag instanceof SourceFileTag) {
				if (filename == null) {
					filename = ((SourceFileTag) tag).getSourceFile();
					if (((SourceFileTag) tag).getAbsolutePath() != null) {
						filename = ((SourceFileTag) tag).getAbsolutePath();
					}
				}
				break;
			} else {
				// Log.info("Tag ignored: " + tag.getClass().toString());
			}
		}

		if (filename == null && startln == -1 && endln == -1 && startcol == -1
				&& endcol == -1) {
			return new Attribute[0];
		}

		if (filename == null && GlobalsCache.v().currentMethod != null) {
			filename = GlobalsCache.v().currentMethod.getDeclaringClass()
					.getName();
		}

		ProgramFactory pf = GlobalsCache.v().getPf();
		Attribute loc = pf.mkLocationAttribute(filename, startln, endln,
				startcol, endcol);

		Attribute[] res;
		if (isCloned) {
			if (comment!=null)  {				
				res = new Attribute[] { loc,
						pf.mkCustomAttribute(ProgramFactory.Cloned), pf.mkCommentAttribute(comment)};
				
			} else {
				res = new Attribute[] { loc,
						pf.mkCustomAttribute(ProgramFactory.Cloned) };
			}			
		} else {
			if (comment!=null)  {
				res = new Attribute[] { loc, pf.mkCommentAttribute(comment) };
			} else {
				res = new Attribute[] { loc };
			}
		}

		return res;
	}

	public static String getQualifiedName(SootClass c) {
		StringBuilder sb = new StringBuilder();
		sb.append(c.getName());
		return replaceIllegalChars(sb.toString());
	}

	public static String getQualifiedName(SootMethod m) {
		StringBuilder sb = new StringBuilder();
		sb.append(m.getReturnType().toString() + "$");
		sb.append(m.getDeclaringClass().getName() + "$");
		sb.append(m.getName() + "$");
		sb.append(m.getNumber());
		return replaceIllegalChars(sb.toString());
	}

	public static String getQualifiedName(Local l) {
		// TODO: check if the name is really unique
		StringBuilder sb = new StringBuilder();
		sb.append(replaceIllegalChars(l.getName()));

		sb.append(l.getNumber());
		return sb.toString();
	}

	public static String getQualifiedName(StaticFieldRef f) {
		return getQualifiedName(f.getField());
	}

	public static String getQualifiedName(SootField f) {
		StringBuilder sb = new StringBuilder();
		sb.append(f.getType() + "$");
		sb.append(f.getDeclaringClass().getName() + "$");
		sb.append(f.getName());
		sb.append(f.getNumber());
		return replaceIllegalChars(sb.toString());
	}

	public static String replaceIllegalChars(String s) {
		String ret = s.replace("<", "$la$");
		ret = ret.replace("@", "$at$");
		ret = ret.replace(">", "$ra$");
		ret = ret.replace("[", "$lp$");
		ret = ret.replace("]", "$rp$");
		ret = ret.replace("/", "$_$");
		ret = ret.replace(";", "$");
		return ret;
	}

	/**
	 * This is a helper function to cast between bool and int if soot does not
	 * distinguish them. TODO: extend to ref if necessary
	 * 
	 * @param expr
	 * @param target
	 * @return
	 */
	public static Expression castBoogieTypes(Expression expr, BoogieType target) {
		if (expr.getType() == target) {
			return expr;
		} else if (expr.getType() == GlobalsCache.v().getPf().getIntType()
				&& target == GlobalsCache.v().getPf().getBoolType()) {
			return SootPrelude.v().intToBool(expr);
		} else if (expr.getType() == GlobalsCache.v().getPf().getBoolType()
				&& target == GlobalsCache.v().getPf().getIntType()) {
			return SootPrelude.v().boolToInt(expr);
		} else if (expr.getType() == GlobalsCache.v().getPf().getIntType()
				&& target == GlobalsCache.v().getPf().getRealType()) {
			return SootPrelude.v().intToReal(expr);
		} else if (expr.getType() == GlobalsCache.v().getPf().getRealType()
				&& target == GlobalsCache.v().getPf().getIntType()) {
			return SootPrelude.v().realToInt(expr);
		} else if (expr.getType() == SootPrelude.v().getReferenceType()
				&& target == GlobalsCache.v().getPf().getBoolType()) {
			return SootPrelude.v().refToBool(expr);
		} else if (expr == SootPrelude.v().getNullConstant()
				&& target == GlobalsCache.v().getPf().getRealType()) {
			return GlobalsCache.v().getPf().mkRealLiteral("0.0");
		} else if (expr == SootPrelude.v().getNullConstant()
				&& target == GlobalsCache.v().getPf().getIntType()) {
			return GlobalsCache.v().getPf().mkIntLiteral("0");
		}

		throw new RuntimeException("Cannot cast "
				+ expr.toString()
				+ " from: "
				+ ((expr.getType() == null) ? "null" : expr.getType()
						.getClass().toString()) + " to "
				+ target.getClass().toString());
		// return expr;
	}

}
