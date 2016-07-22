
package bixie.boogie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import bixie.boogie.ast.Attribute;
import bixie.boogie.ast.Body;
import bixie.boogie.ast.NamedAttribute;
import bixie.boogie.ast.ParentEdge;
import bixie.boogie.ast.Unit;
import bixie.boogie.ast.VarList;
import bixie.boogie.ast.VariableLHS;
import bixie.boogie.ast.asttypes.ASTType;
import bixie.boogie.ast.asttypes.ArrayAstType;
import bixie.boogie.ast.asttypes.NamedAstType;
import bixie.boogie.ast.asttypes.PrimitiveAstType;
import bixie.boogie.ast.declaration.Axiom;
import bixie.boogie.ast.declaration.ConstDeclaration;
import bixie.boogie.ast.declaration.Declaration;
import bixie.boogie.ast.declaration.FunctionDeclaration;
import bixie.boogie.ast.declaration.Implementation;
import bixie.boogie.ast.declaration.ProcedureDeclaration;
import bixie.boogie.ast.declaration.TypeDeclaration;
import bixie.boogie.ast.declaration.VariableDeclaration;
import bixie.boogie.ast.expression.ArrayAccessExpression;
import bixie.boogie.ast.expression.ArrayStoreExpression;
import bixie.boogie.ast.expression.BinaryExpression;
import bixie.boogie.ast.expression.BitVectorAccessExpression;
import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.expression.FunctionApplication;
import bixie.boogie.ast.expression.IdentifierExpression;
import bixie.boogie.ast.expression.IfThenElseExpression;
import bixie.boogie.ast.expression.QuantifierExpression;
import bixie.boogie.ast.expression.UnaryExpression;
import bixie.boogie.ast.expression.WildcardExpression;
import bixie.boogie.ast.expression.literal.BitvecLiteral;
import bixie.boogie.ast.expression.literal.BooleanLiteral;
import bixie.boogie.ast.expression.literal.IntegerLiteral;
import bixie.boogie.ast.expression.literal.RealLiteral;
import bixie.boogie.ast.expression.literal.StringLiteral;
import bixie.boogie.ast.location.ILocation;
import bixie.boogie.ast.specification.EnsuresSpecification;
import bixie.boogie.ast.specification.LoopInvariantSpecification;
import bixie.boogie.ast.specification.ModifiesSpecification;
import bixie.boogie.ast.specification.RequiresSpecification;
import bixie.boogie.ast.specification.Specification;
import bixie.boogie.ast.statement.AssertStatement;
import bixie.boogie.ast.statement.AssignmentStatement;
import bixie.boogie.ast.statement.AssumeStatement;
import bixie.boogie.ast.statement.BreakStatement;
import bixie.boogie.ast.statement.CallStatement;
import bixie.boogie.ast.statement.GotoStatement;
import bixie.boogie.ast.statement.HavocStatement;
import bixie.boogie.ast.statement.IfStatement;
import bixie.boogie.ast.statement.Label;
import bixie.boogie.ast.statement.ReturnStatement;
import bixie.boogie.ast.statement.Statement;
import bixie.boogie.ast.statement.WhileStatement;
import bixie.boogie.ast.statement.YieldStatement;
import bixie.boogie.controlflow.DefaultControlFlowFactory;
import bixie.boogie.enums.CallParameters;
import bixie.boogie.enums.UnaryOperator;
import bixie.boogie.parser.BoogieSymbolFactory;
import bixie.boogie.parser.Lexer;
import bixie.boogie.parser.Parser;
import bixie.boogie.type.ArrayType;
import bixie.boogie.type.BoogieType;
import bixie.boogie.type.ConstructedType;
import bixie.boogie.type.PlaceholderType;
import bixie.boogie.type.PrimitiveType;
import bixie.boogie.type.TypeConstructor;
import bixie.boogie.typechecker.ModifiesClauseConstruction;
import bixie.boogie.typechecker.TypeChecker;
import bixie.util.Log;

/**
 * @author schaef
 * 
 */
public class ProgramFactory {

	public static final String LocationTag = "sourceloc";

	public static final String NoVerifyTag = "noverify";

	public static final String NotInfeasible = "notinfeasible";

	public static final String Cloned = "clone";

	public static final String Comment = "comment";

	public static final String GeneratedThenBlock = "Boogieamp#THENBLOCK";
	public static final String GeneratedElseBlock = "Boogieamp#ELSENBLOCK";

	public ProgramFactory() {

	}

	public ProgramFactory(String filename) throws Exception {
		try (FileInputStream fis = new FileInputStream(filename);) {
			importBoogieFile(filename, fis);
		}
	}

	public void importBoogieFile(String filename) throws FileNotFoundException, Exception {
		try (FileInputStream fis = new FileInputStream(filename);) {
			importBoogieFile(filename, fis);
		}
	}

	public void runTypeChecker() {
		TypeChecker tc = new TypeChecker(this.getASTRoot());
		new DefaultControlFlowFactory(this.getASTRoot(), tc);
	}

	/*
	 * imports a boogie file and merges it with the existing AST.
	 */
	public void importBoogieFile(String filename, InputStream stream) throws Exception {
		BoogieSymbolFactory symFactory = new BoogieSymbolFactory();
		Lexer lexer;
		Parser parser;
		Unit rootNode = null;
		try {
			lexer = new Lexer(stream);
			lexer.setSymbolFactory(symFactory);

			parser = new Parser(lexer, symFactory);
			parser.setFileName(filename);
			rootNode = (Unit) parser.parse().value;
		} catch (Exception e) {
			rootNode = null;
			throw e;
		}

		// now import the declarations without adding duplicates
		// and update the this.boogieType2ASTTypeMap
		for (Declaration d : rootNode.getDeclarations()) {
			if (d instanceof TypeDeclaration) {
				TypeDeclaration td = (TypeDeclaration) d;
				// load all type declaration into boogieType2ASTTypeMap
				LinkedList<BoogieType> typeParams = new LinkedList<BoogieType>();
				HashMap<String, BoogieType> placeholders = new HashMap<String, BoogieType>();
				for (String tparam : td.getTypeParams()) {
					if (findTypeDeclaration(tparam) == null) {
						// this type is not declared, so generate a placeholder
						// instead.
						if (!placeholders.containsKey(tparam)) {
							placeholders.put(tparam, this.mkNamedPlaceholderType(tparam));
						}
						Log.debug("Undeclared type parameter found: " + tparam + ". Assuming its a placeholder.");
						typeParams.add(placeholders.get(tparam));
					} else {
						typeParams.add(this.getNamedType(tparam));
					}
				}
				BoogieType btype = this.getNamedType(td.getIdentifier(),
						typeParams.toArray(new BoogieType[typeParams.size()]), td.isFinite(), td.getSynonym());
				this.boogieType2ASTTypeMap.put(btype, this.astTypeFromBoogieType(btype));
			} else {
				// make sure that functions, procedures, and variables are
				// imported properly
				this.globalDeclarations.add(d);
				// Declaration existing = containsDeclaration(
				// this.globalDeclarations, d);
				// if (existing == null) {
				// if (!this.globalDeclarations.contains(d)) {
				// this.globalDeclarations.add(d);
				// } else {
				// Log.error("Trying to add duplicate " + d.toString());
				// }
				// } else if (d instanceof Implementation) {
				// if (d == existing) {
				// throw new RuntimeException("DOUBLE "
				// + ((Implementation) d).getIdentifier() + " / "
				// + ((Implementation) existing).getIdentifier());
				// }
				// this.globalDeclarations.add(d);
				// } else {
				// System.err
				// .println("Double decl " + d.getClass().toString());
				// System.err.println("this: " + d);
				// System.err.println("other: " + existing);
				// throw new RuntimeException();
				// }
			}
		}

		if (this.astRootNode != null) {
			Log.debug("Import of " + filename + " made the AST invalid. New one will be created.");
			this.astRootNode = null;
		}
	}

	public BoogieType findTypeByName(String typename) {
		for (Entry<BoogieType, ASTType> entry : this.boogieType2ASTTypeMap.entrySet()) {
			if ((entry.getKey()) instanceof ConstructedType) {
				ConstructedType contype = (ConstructedType) (entry.getKey());
				if (contype.getConstr().getName().equals(typename)) {
					return contype;
				}
			}
		}
		return null;
	}

	public IdentifierExpression findGlobalByName(String name) {
		VariableDeclaration vd = findVariableDeclaration(name);
		if (vd != null) {
			for (VarList vl : vd.getVariables()) {
				for (String s : vl.getIdentifiers()) {
					if (s.equals(name)) {
						return new IdentifierExpression(vd.getLocation(), boogieTypeFromAstType(vl.getType()), name);
					}
				}
			}
		}
		ConstDeclaration cd = findConstDeclaration(name);
		if (cd != null) {
			for (String s : cd.getVarList().getIdentifiers()) {
				if (s.equals(name)) {
					return new IdentifierExpression(cd.getLocation(), boogieTypeFromAstType(cd.getVarList().getType()),
							name);
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	public BoogieType boogieTypeFromAstType(ASTType t) {
		BoogieType ret = findBoogieTypeFromAstType(t);
		if (ret == null) {
			// the boogietype was not found, so it has to be created.
			ret = createBoogieTypeFromAstType(t);
			this.boogieType2ASTTypeMap.put(ret, t);
		}
		return ret;
	}

	private BoogieType findBoogieTypeFromAstType(ASTType t) {
		for (Entry<BoogieType, ASTType> entry : this.boogieType2ASTTypeMap.entrySet()) {
			if (compareAstTypes(entry.getValue(), t)) {
				return entry.getKey();
			}
		}
		return null;
	}

	private boolean compareAstTypes(ASTType a, ASTType b) {
		if (a instanceof NamedAstType && b instanceof NamedAstType) {
			NamedAstType one = (NamedAstType) a;
			NamedAstType other = (NamedAstType) b;
			if (one.getName().equals(other.getName())) {
				if (one.getTypeArgs() == other.getTypeArgs()) {
					return true;
				} else if (one.getTypeArgs().length == 0 && other.getTypeArgs().length == 0) {
					// TODO: somehow two equals doesn't work on empty type args,
					// so we have to treat that in a special case.
					return true;
				}
			}
			if (a.toString().equals(b.toString()))
				throw new RuntimeException("something went wrong with compareAstTypes");
			return false;
		} else if (a instanceof ArrayAstType && b instanceof ArrayAstType) {
			if (a.toString().equals(b.toString())) {
				// TODO: Super HACK!
				return true;
			}
		}
		return a == b;
	}

	// never call this directly. This should only be used by
	// boogieTypeFromAstType
	private BoogieType createBoogieTypeFromAstType(ASTType t) {
		if (t instanceof NamedAstType) {
			NamedAstType typ = (NamedAstType) t;

			LinkedList<BoogieType> tparams = new LinkedList<BoogieType>();
			int torder[] = new int[typ.getTypeArgs().length];
			int i = 0;
			for (ASTType tp : typ.getTypeArgs()) {
				tparams.add(boogieTypeFromAstType(tp));
				torder[i] = i;
				i++;
			}
			TypeConstructor tc = new TypeConstructor(typ.getName(), true, typ.getTypeArgs().length, torder);

			return BoogieType.createConstructedType(tc, tparams.toArray(new BoogieType[tparams.size()]));
		} else if (t instanceof PrimitiveAstType) {
			PrimitiveAstType typ = (PrimitiveAstType) t;
			if (typ.getName().equals("bool")) {
				return BoogieType.boolType;
			}
			if (typ.getName().equals("int")) {
				return BoogieType.intType;
			}
			if (typ.getName().equals("real")) {
				return BoogieType.realType;
			}

			throw new RuntimeException("Cannot create BoogieType for " + t.toString());
		} else if (t instanceof ArrayAstType) {
			ArrayAstType typ = (ArrayAstType) t;
			for (String s : typ.getTypeParams()) {
				throw new RuntimeException("TODO: type parameters still need to be implemented: " + s);
			}

			LinkedList<BoogieType> tparams = new LinkedList<BoogieType>();
			for (ASTType tp : typ.getIndexTypes()) {
				tparams.add(boogieTypeFromAstType(tp));
			}
			BoogieType[] indexTypes = tparams.toArray(new BoogieType[tparams.size()]);
			BoogieType valueType = boogieTypeFromAstType(typ.getValueType());

			HashSet<PlaceholderType> placeholders = new HashSet<PlaceholderType>();
			int numplaceholders = countPlaceHolderTypes(indexTypes, placeholders)
					+ countPlaceHolderTypes(valueType, placeholders);

			return BoogieType.createArrayType(numplaceholders, indexTypes, valueType);
		}
		return null;
	}

	/**
	 * Find a global TypeDeclaration by name if one exists
	 * 
	 * @param typename
	 * @return TypeDeclaration named typename or null
	 */
	public TypeDeclaration findTypeDeclaration(String typename) {
		for (Declaration d : this.globalDeclarations) {
			if (d instanceof TypeDeclaration && ((TypeDeclaration) d).getIdentifier().equals(typename)) {
				return ((TypeDeclaration) d);
			}
		}
		return null;
	}

	/**
	 * Returns the VariableDeclaration of name varname or null
	 * 
	 * @param varname
	 * @return VariableDeclaration named varname or null
	 */
	public VariableDeclaration findVariableDeclaration(String varname) {
		for (Declaration d : this.globalDeclarations) {
			if (d instanceof VariableDeclaration) {
				VariableDeclaration vd = (VariableDeclaration) d;
				for (VarList vl : vd.getVariables()) {
					for (String s : vl.getIdentifiers()) {
						if (s.equals(varname))
							return vd;
					}
				}
			}

		}
		return null;
	}

	/**
	 * Returns ConstDeclaration of constname or null
	 * 
	 * @param constname
	 * @return ConstDeclaration or null
	 */
	public ConstDeclaration findConstDeclaration(String constname) {
		for (Declaration d : this.globalDeclarations) {
			if (d instanceof ConstDeclaration) {
				ConstDeclaration cd = ((ConstDeclaration) d);
				for (String s : cd.getVarList().getIdentifiers()) {
					if (s.equals(constname))
						return cd;
				}
			}
		}
		return null;
	}

	/**
	 * Returns FunctionDeclaration with name funname or null
	 * 
	 * @param funname
	 * @return FunctionDeclaration or null
	 */
	public FunctionDeclaration findFunctionDeclaration(String funname) {
		for (Declaration d : this.globalDeclarations) {
			if (d instanceof FunctionDeclaration && ((FunctionDeclaration) d).getIdentifier().equals(funname)) {
				return ((FunctionDeclaration) d);
			}
		}
		return null;
	}

	/**
	 * Returns the ProcedureDeclaration with name funname or null
	 * 
	 * @param funname
	 * @return
	 */
	public ProcedureDeclaration findProcedureDeclaration(String funname) {
		for (Declaration d : this.globalDeclarations) {
			if (d instanceof ProcedureDeclaration) {
				if (((ProcedureDeclaration) d).getIdentifier().equals(funname)) {
					return ((ProcedureDeclaration) d);
				} else {
					// nothing
				}
			}
		}
		return null;
	}

	// private Declaration containsDeclaration(LinkedList<Declaration> decls,
	// Declaration d) {
	// for (Declaration d_ : decls) {
	// if (d instanceof TypeDeclaration
	// && d_ instanceof TypeDeclaration
	// && ((TypeDeclaration) d).getIdentifier().equals(
	// ((TypeDeclaration) d_).getIdentifier()))
	// return d_;
	// else if (d instanceof ConstDeclaration
	// && d_ instanceof ConstDeclaration
	// && ((ConstDeclaration) d)
	// .getVarList()
	// .toString()
	// .equals(((ConstDeclaration) d_).getVarList()
	// .toString()))
	// return d_;
	// else if (d instanceof VariableDeclaration
	// && d_ instanceof VariableDeclaration
	// && ((VariableDeclaration) d)
	// .getVariables()
	// .toString()
	// .equals(((VariableDeclaration) d_).getVariables()
	// .toString()))
	// return d_;
	// else if (d instanceof FunctionDeclaration
	// && d_ instanceof FunctionDeclaration
	// && ((FunctionDeclaration) d).toString().equals(
	// ((FunctionDeclaration) d_).toString()))
	// return d_;
	// else if (d instanceof Axiom && d_ instanceof Axiom
	// && ((Axiom) d).toString().equals(((Axiom) d_).toString()))
	// return d_;
	// else if (d instanceof ProcedureDeclaration
	// && d_ instanceof ProcedureDeclaration
	// && ((ProcedureDeclaration) d).toString().equals(
	// ((ProcedureDeclaration) d_).toString()))
	// return d_;
	// else if (d instanceof Implementation
	// && d_ instanceof Implementation
	// && ((Implementation) d).toString().equals(
	// ((Implementation) d_).toString()))
	// return d_;
	// }
	// return null;
	// }

	private Unit astRootNode = null;
	private final ILocation dummyLocation = null;

	private HashMap<BoogieType, ASTType> boogieType2ASTTypeMap = new HashMap<BoogieType, ASTType>();
	private HashMap<IdentifierExpression, VariableDeclaration> varDeclMap = new HashMap<IdentifierExpression, VariableDeclaration>();

	private int placeholderTypeCounter = 0;

	private LinkedList<Declaration> globalDeclarations = new LinkedList<Declaration>();

	/**
	 * Returns the ast root. If the program has been constructed via the API,
	 * the root does not contain modifies clauses so they will be created on the
	 * fly.
	 * 
	 * @return
	 */
	public Unit getASTRoot() {
		if (astRootNode == null) {
			astRootNode = new Unit(dummyLocation,
					globalDeclarations.toArray(new Declaration[globalDeclarations.size()]));
			ModifiesClauseConstruction.createModifiesClause(astRootNode);
		}
		return astRootNode;
	}

	/*
	 * create procedures and other global stuff
	 */

	/**
	 * 
	 * @param e
	 * @return
	 */
	public Axiom mkAxiom(Expression e) {
		return mkAxiom(null, e);
	}

	/**
	 * 
	 * @param attributes
	 * @param e
	 * @return
	 */
	public Axiom mkAxiom(Attribute[] attributes, Expression e) {
		Axiom a = new Axiom(dummyLocation, attributes, e);
		globalDeclarations.add(a);
		return a;
	}

	/**
	 * Make a procedure declaration (without body)
	 * 
	 * @param identifier
	 * @param inParams
	 * @param outParams
	 * @param specification
	 * @return
	 */
	public ProcedureDeclaration mkProcedureDeclaration(String identifier, IdentifierExpression[] inParams,
			IdentifierExpression[] outParams, Specification[] specification) {
		Attribute[] attributes = {};
		PlaceholderType[] typeparams = {};
		return mkProcedureDeclaration(attributes, typeparams, identifier, inParams, outParams, specification);
	}

	/**
	 * Make a procedure declaration (without body). Procedure names must be
	 * unique, but there can be several implementations for the same procedure.
	 * 
	 * @param attributes
	 * @param typeparams
	 * @param identifier
	 * @param inParams
	 * @param outParams
	 * @param specification
	 * @return
	 */
	public ProcedureDeclaration mkProcedureDeclaration(Attribute[] attributes, PlaceholderType[] typeparams,
			String identifier, IdentifierExpression[] inParams, IdentifierExpression[] outParams,
			Specification[] specification) {
		String[] tparams = new String[typeparams.length];
		for (int i = 0; i < typeparams.length; i++) {
			tparams[i] = typeparams[i].getIdentifier();
		}

		VarList[] in = new VarList[inParams.length];
		for (int i = 0; i < inParams.length; i++) {
			String[] names = { inParams[i].getIdentifier() };
			in[i] = new VarList(inParams[i].getLocation(), names, this.astTypeFromBoogieType(inParams[i].getType()));
		}
		VarList[] out = new VarList[outParams.length];
		for (int i = 0; i < outParams.length; i++) {
			String[] names = { outParams[i].getIdentifier() };
			out[i] = new VarList(outParams[i].getLocation(), names, this.astTypeFromBoogieType(outParams[i].getType()));
		}

		if (specification == null) {
			specification = new Specification[0];
		}

		ProcedureDeclaration decl = new ProcedureDeclaration(this.dummyLocation, attributes, identifier, tparams, in,
				out, specification, null);
		globalDeclarations.add(decl);
		return decl;
	}

	/**
	 * Make a procedure implementation. Implementations are used to represent
	 * methods of the language you are coming from. One procedure declaration
	 * can have several implementations of the same name.
	 * 
	 * @param procdecl
	 *            the procedure declaration which has to be created before an
	 *            implementation can be created.
	 * @param stmts
	 *            the body of the implementation
	 * @param localvars
	 *            the local variables used in the implementation
	 * @return
	 */
	public Implementation mkProcedure(ProcedureDeclaration procdecl, Statement[] stmts,
			IdentifierExpression[] localvars) {

		if (procdecl == null || procdecl.getBody() != null) {
			throw new RuntimeException("Illegal Prcedure Declaration. Procdecl must not have a body!");
		}

		VariableDeclaration[] decls = new VariableDeclaration[localvars.length];
		for (int i = 0; i < localvars.length; i++) {
			decls[i] = this.varDeclMap.get(localvars[i]);
		}
		Body body = new Body(procdecl.getLocation(), decls, stmts);

		Implementation impl = new Implementation(procdecl.getLocation(), procdecl.getAttributes(),
				procdecl.getIdentifier(), procdecl.getTypeParams(), procdecl.getInParams(), procdecl.getOutParams(),
				new Specification[0], body);

		globalDeclarations.add(impl);
		return impl;
	}

	/**
	 * This is used to make a FunctionDeclaration. Function declarations can be
	 * used to create helper functions such as function intToBool(int i) {
	 * (i==0) ? false : true }
	 * 
	 * @param attributes
	 * @param identifier
	 *            name of the function
	 * @param typeParams
	 *            generic type parameters
	 * @param inParams
	 *            function arguments
	 * @param outParam
	 *            return argument
	 * @param body
	 *            the expression that is evaluated when the function is called
	 * @return a function declaration
	 */
	public FunctionDeclaration mkFunctionDeclaration(Attribute[] attributes, String identifier,
			PlaceholderType[] typeParams, IdentifierExpression[] inParams, IdentifierExpression outParam,
			Expression body) {
		String[] tparams = new String[typeParams.length];
		for (int i = 0; i < typeParams.length; i++) {
			tparams[i] = typeParams[i].getIdentifier();
		}
		VarList[] in = new VarList[inParams.length];
		for (int i = 0; i < inParams.length; i++) {
			String[] names = { inParams[i].getIdentifier() };
			in[i] = new VarList(inParams[i].getLocation(), names, this.astTypeFromBoogieType(inParams[i].getType()));
		}
		String[] names = { outParam.getIdentifier() };
		VarList out = new VarList(outParam.getLocation(), names, this.astTypeFromBoogieType(outParam.getType()));
		FunctionDeclaration fundec = new FunctionDeclaration(this.dummyLocation, attributes, identifier, tparams, in,
				out, body);
		globalDeclarations.add(fundec);
		return fundec;
	}

	/**
	 * Make an attribute to keeps a reference to some source location for which
	 * a boogie statement was generated. Use this if you translate a source
	 * language into boogie to get error reports that refer to your actual
	 * source code rather than the boogie code
	 * 
	 * @param filename
	 * @param startLine
	 * @param endline
	 * @param startColumn
	 * @param endColumn
	 * @return
	 */
	public Attribute mkLocationAttribute(String filename, int startLine, int endline, int startColumn, int endColumn) {
		Expression[] args = { new StringLiteral(dummyLocation, filename),
				new IntegerLiteral(dummyLocation, BoogieType.intType, Integer.toString(startLine)),
				new IntegerLiteral(dummyLocation, BoogieType.intType, Integer.toString(startColumn)),
				new IntegerLiteral(dummyLocation, BoogieType.intType, Integer.toString(endline)),
				new IntegerLiteral(dummyLocation, BoogieType.intType, Integer.toString(endColumn)) };
		return new NamedAttribute(dummyLocation, ProgramFactory.LocationTag, args);
	}

	/**
	 * Make a custom attribute
	 * 
	 * @param attributeName
	 * @return
	 */
	public Attribute mkCustomAttribute(String attributeName) {
		return new NamedAttribute(dummyLocation, attributeName, new Expression[] {});
	}

	/**
	 * Make an attribute that indicates the the statement associated with it has
	 * no corresponding location in the source code. Use this, e.g., to suppress
	 * false positives in infeasible code detection.
	 * 
	 * @return
	 */
	public Attribute mkNoVerifyAttribute() {
		return new NamedAttribute(dummyLocation, ProgramFactory.NoVerifyTag, new Expression[] {});
	}

	/**
	 * Make an attribute that contains a comment.
	 * 
	 * @param str
	 * @return
	 */
	public Attribute mkCommentAttribute(String str) {
		return new NamedAttribute(dummyLocation, ProgramFactory.Comment,
				new Expression[] { new StringLiteral(dummyLocation, str) });
	}

	/**
	 * This is used to make a FunctionDeclaration. Function declarations can be
	 * used to create helper functions such as function intToBool(int i) {
	 * (i==0) ? false : true }
	 * 
	 * @param attributes
	 * @param identifier
	 *            name of the function
	 * @param inParams
	 *            function arguments
	 * @param outParam
	 *            return argument
	 * @param body
	 *            the expression that is evaluated when the function is called
	 * @return a function declaration
	 */
	public FunctionDeclaration mkFunctionDeclaration(Attribute[] attributes, String identifier,
			IdentifierExpression[] inParams, IdentifierExpression outParam, Expression body) {
		PlaceholderType[] typeparams = {};
		return mkFunctionDeclaration(attributes, identifier, typeparams, inParams, outParam, body);
	}

	/**
	 * This is used to make a FunctionDeclaration.
	 * 
	 * @param attributes
	 * @param identifier
	 *            name of the function
	 * @param inParams
	 *            function arguments
	 * @param outParam
	 *            return argument
	 * @return a function declaration
	 */
	public FunctionDeclaration mkFunctionDeclaration(Attribute[] attributes, String identifier,
			IdentifierExpression[] inParams, IdentifierExpression outParam) {
		return mkFunctionDeclaration(attributes, identifier, inParams, outParam, null);
	}

	/*
	 * create specification statements
	 */
	/**
	 * Make an ensures clause (or postcondition).
	 * 
	 * @param attributes
	 * @param isFree
	 * @param formula
	 * @return
	 */
	public Specification mkEnsuresSpecification(Attribute[] attributes, boolean isFree, Expression formula) {
		return new EnsuresSpecification(this.dummyLocation, attributes, isFree, formula);
	}

	/**
	 * Make a requires clause (or precondition).
	 * 
	 * @param attributes
	 * @param isFree
	 * @param formula
	 * @return
	 */
	public Specification mkRequiresSpecification(Attribute[] attributes, boolean isFree, Expression formula) {
		return new RequiresSpecification(this.dummyLocation, attributes, isFree, formula);
	}

	/**
	 * Make a loop invariant.
	 * 
	 * @param attributes
	 * @param isFree
	 * @param formula
	 * @return
	 */
	public Specification mkLoopInvariantSpecification(Attribute[] attributes, boolean isFree, Expression formula) {
		return new LoopInvariantSpecification(this.dummyLocation, attributes, isFree, formula);
	}

	/**
	 * Make a modifies clause which is to be added to a procedure later. In
	 * general, it is not necessary to provide modifies clauses as they can be
	 * computed by the type checker. You only need to use this, if you want to
	 * add modifies clauses for procedures that do not have a body.
	 * 
	 * @param isFree
	 * @param identifiers
	 *            list of global variables that can be modified by a procedure
	 * @return the ModifesSpecification
	 */
	public Specification mkModifiesSpecification(boolean isFree, IdentifierExpression[] identifiers) {
		String[] names = new String[identifiers.length];
		for (int i = 0; i < identifiers.length; i++) {
			names[i] = identifiers[i].getIdentifier();
		}
		return new ModifiesSpecification(this.dummyLocation, isFree, names);
	}

	/*
	 * create statement
	 */
	/**
	 * Create an Assert statement.
	 * 
	 * @param attributes
	 * @param formula
	 * @return
	 */
	public Statement mkAssertStatement(Attribute[] attributes, Expression formula) {
		return new AssertStatement(this.dummyLocation, attributes, formula);
	}

	/**
	 * Create an Assignment. Boogie allows multi-assignments such as: x,y :=
	 * 3,2;
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public Statement mkAssignmentStatement(Expression[] lhs, Expression[] rhs) {
		VariableLHS[] vars = new VariableLHS[lhs.length];
		for (int i = 0; i < lhs.length; i++) {
			if (lhs[i] instanceof IdentifierExpression) {
				vars[i] = new VariableLHS(lhs[i].getLocation(), ((IdentifierExpression) lhs[i]).getIdentifier());
			} else {
				throw new RuntimeException("ERROR:" + lhs[i].getClass());
			}
		}
		return new AssignmentStatement(this.dummyLocation, vars, rhs);
	}

	/**
	 * Create a simple assingment of the form x := y
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public Statement mkAssignmentStatement(Expression lhs, Expression rhs) {
		Expression[] l = { lhs };
		Expression[] r = { rhs };
		return mkAssignmentStatement(l, r);
	}

	/**
	 * Create an Assume statement.
	 * 
	 * @param attributes
	 * @param formula
	 * @return
	 */
	public Statement mkAssumeStatement(Attribute[] attributes, Expression formula) {
		return new AssumeStatement(this.dummyLocation, attributes, formula);
	}

	/**
	 * Create a Break statement.
	 * 
	 * @param label
	 * @return
	 */
	public Statement mkBreakStatement(String label) {
		return new BreakStatement(this.dummyLocation, label);
	}

	/**
	 * Create a procedure call (or quantifier) statement.
	 * 
	 * @param attributes
	 * @param isForall
	 * @param lhs
	 * @param methodName
	 * @param arguments
	 * @return
	 */
	public Statement mkCallStatement(Attribute[] attributes, boolean isForall, IdentifierExpression[] lhs,
			String methodName, Expression[] arguments) {
		String[] vars = new String[lhs.length];
		for (int i = 0; i < lhs.length; i++) {
			vars[i] = lhs[i].getIdentifier();
		}
		return new CallStatement(this.dummyLocation, isForall, vars, methodName, arguments, CallParameters.NONE,
				attributes);
	}

	/**
	 * Create a procedure call (or quantifier) statement.
	 * 
	 * @param attributes
	 * @param isForall
	 * @param lhs
	 * @param methodName
	 * @param arguments
	 * @param cp
	 * @return
	 */
	public Statement mkCallStatement(Attribute[] attributes, boolean isForall, IdentifierExpression[] lhs,
			String methodName, Expression[] arguments, CallParameters cp) {
		String[] vars = new String[lhs.length];
		for (int i = 0; i < lhs.length; i++) {
			vars[i] = lhs[i].getIdentifier();
		}
		return new CallStatement(this.dummyLocation, isForall, vars, methodName, arguments, cp, attributes);
	}

	/**
	 * Create a deterministic goto statement.
	 * 
	 * @param loc
	 * @param label
	 * @return
	 */
	public Statement mkGotoStatement(String label) {
		String[] labels = { label };
		return new GotoStatement(this.dummyLocation, labels);
	}

	/**
	 * Create a non-deterministic assignment for several variables at once.
	 * 
	 * @param attributes
	 * @param identifiers
	 * @return
	 */
	public Statement mkHavocStatement(Attribute[] attributes, IdentifierExpression[] identifiers) {
		String[] hvars = new String[identifiers.length];
		for (int i = 0; i < identifiers.length; i++) {
			hvars[i] = identifiers[i].getIdentifier();
		}
		return new HavocStatement(this.dummyLocation, attributes, hvars);
	}

	/**
	 * Create a non-deterministic assignment.
	 * 
	 * @param attributes
	 * @param identifier
	 * @return
	 */
	public Statement mkHavocStatement(Attribute[] attributes, IdentifierExpression identifier) {
		IdentifierExpression[] identifiers = { identifier };
		return mkHavocStatement(attributes, identifiers);
	}

	/**
	 * 
	 * @param condition
	 * @param thenPart
	 * @param elsePart
	 * @return
	 */
	public Statement mkIfStatement(Expression condition, Statement[] thenPart, Statement[] elsePart) {
		return new IfStatement(this.dummyLocation, condition, thenPart, elsePart);
	}

	/**
	 * Make a label that can be targeted by Goto's.
	 * 
	 * @param identifier
	 * @return
	 */
	public Statement mkLabel(String identifier) {
		return new Label(this.dummyLocation, identifier);
	}

	/**
	 * 
	 * @return
	 */
	public Statement mkReturnStatement() {
		return new ReturnStatement(this.dummyLocation);
	}

	/**
	 * 
	 * @return
	 */
	public Statement mkYieldStatement() {
		return new YieldStatement(this.dummyLocation);
	}

	/**
	 * 
	 * @param condition
	 * @param invariants
	 * @param body
	 * @return
	 */
	public Statement mkWhileStatement(Expression condition, LoopInvariantSpecification[] invariants, Statement[] body) {
		return new WhileStatement(this.dummyLocation, condition, invariants, body);
	}

	/*
	 * create expressions
	 */
	/**
	 * 
	 * @param type
	 * @param array
	 * @param indices
	 * @return
	 */
	public Expression mkArrayAccessExpression(BoogieType type, Expression array, Expression[] indices) {
		return new ArrayAccessExpression(this.dummyLocation, type, array, indices);
	}

	/**
	 * 
	 * @param type
	 * @param array
	 * @param indices
	 * @param value
	 * @return
	 */
	public Expression mkArrayStoreExpression(BoogieType type, Expression array, Expression[] indices,
			Expression value) {
		if (value == null) {
			throw new RuntimeException("Value must not be null!");
		}
		return new ArrayStoreExpression(this.dummyLocation, type, array, indices, value);
	}

	/**
	 * 
	 * @param type
	 * @param operator
	 * @param left
	 * @param right
	 * @return
	 */
	public Expression mkBinaryExpression(BoogieType type, bixie.boogie.enums.BinaryOperator operator, Expression left,
			Expression right) {
		return new BinaryExpression(this.dummyLocation, type, operator, left, right);
	}

	/**
	 * 
	 * @param type
	 * @param bitvec
	 * @param end
	 * @param start
	 * @return
	 */
	public Expression mkBitVectorAccessExpression(BoogieType type, Expression bitvec, int end, int start) {
		return new BitVectorAccessExpression(this.dummyLocation, type, bitvec, end, start);
	}

	/**
	 * 
	 * @param fun
	 * @param arguments
	 * @return
	 */
	public Expression mkFunctionApplication(FunctionDeclaration fun, Expression[] arguments) {
		return new FunctionApplication(this.dummyLocation, this.boogieTypeFromAstType(fun.getOutParam().getType()),
				fun.getIdentifier(), arguments);
	}

	/**
	 * 
	 * @param type
	 * @param condition
	 * @param thenPart
	 * @param elsePart
	 * @return
	 */
	public Expression mkIfThenElseExpression(BoogieType type, Expression condition, Expression thenPart,
			Expression elsePart) {
		return new IfThenElseExpression(this.dummyLocation, type, condition, thenPart, elsePart);
	}

	/**
	 * 
	 * @param isUniversal
	 * @param typeParams
	 * @param parameters
	 * @param attributes
	 * @param subformula
	 * @return
	 */
	public Expression mkQuantifierExpression(boolean isUniversal, String[] typeParams,
			IdentifierExpression[] parameters, Attribute[] attributes, Expression subformula) {

		VarList[] vl = new VarList[parameters.length];
		int i = 0;
		for (IdentifierExpression ide : parameters) {
			vl[i++] = new VarList(dummyLocation, null, new String[] { ide.getIdentifier() },
					astTypeFromBoogieType(ide.getType()));
		}

		return new QuantifierExpression(this.dummyLocation, isUniversal, typeParams, vl, attributes, subformula);
	}

	/**
	 * 
	 * @param type
	 * @param operator
	 * @param expr
	 * @return
	 */
	public Expression mkUnaryExpression(BoogieType type, UnaryOperator operator, Expression expr) {
		return new UnaryExpression(this.dummyLocation, type, operator, expr);
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public Expression mkWildcardExpression(BoogieType type) {
		return new WildcardExpression(this.dummyLocation, type);
	}

	/*
	 * create literals
	 */
	/**
	 * WARNING: Boogie does not have a built in String type or something. these
	 * literals are only to be used in Attributes they are not related to the
	 * String type in the language you are comming from!
	 * 
	 * @param s
	 * @return
	 */
	public Expression mkStringLiteral(String s) {
		return new StringLiteral(this.dummyLocation, s);
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public Expression mkRealLiteral(String s) {
		return new RealLiteral(this.dummyLocation, this.getRealType(), s);
	}

	/**
	 * 
	 * @param b
	 * @return
	 */
	public Expression mkBooleanLiteral(boolean b) {
		return new BooleanLiteral(this.dummyLocation, this.getBoolType(), b);
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public Expression mkIntLiteral(String s) {
		return new IntegerLiteral(this.dummyLocation, this.getIntType(), s);
	}

	public Expression mkBitVecorLiteral(BoogieType typ, String s, int len) {
		return new BitvecLiteral(this.dummyLocation, typ, s, len);
	}

	/*
	 * create a variables and types
	 */

	/**
	 * 
	 * @param type
	 * @param name
	 * @param isConst
	 * @param isGlobal
	 * @param isUnique
	 * @return
	 */
	public IdentifierExpression mkIdentifierExpression(BoogieType type, String name, boolean isConst, boolean isGlobal,
			boolean isUnique) {
		IdentifierExpression[] parents = {};
		Attribute[] attributes = {};
		return mkIdentifierExpression(attributes, type, name, isConst, isGlobal, isUnique, parents);
	}

	/**
	 * Creates an IdentifierExpression that can be used in a Quantifier.
	 * 
	 * @param name
	 * @param t
	 * @return
	 */
	public IdentifierExpression mkQuantifiedIdentifierExpression(String name, BoogieType t) {
		return new IdentifierExpression(dummyLocation, t, name);
	}

	/**
	 * 
	 * @param attributes
	 * @param type
	 * @param name
	 * @param isConst
	 * @param isGlobal
	 * @param isUnique
	 * @param parents
	 * @return
	 */
	public IdentifierExpression mkIdentifierExpression(Attribute[] attributes, BoogieType type, String name,
			boolean isConst, boolean isGlobal, boolean isUnique, IdentifierExpression[] parents) {

		boolean isComplete = true;
		Expression whereClause = null;
		ParentEdge[] parentsEdges = new ParentEdge[parents.length];
		for (int i = 0; i < parents.length; i++) {
			// TODO: not sure if isUnique is allways true.
			parentsEdges[i] = new ParentEdge(parents[i].getLocation(), true, parents[i].getIdentifier());

		}
		return mkIdentifierExpression(attributes, type, name, isConst, isGlobal, isUnique, isComplete, parentsEdges,
				whereClause);
	}

	/**
	 * 
	 * @param attributes
	 * @param type
	 * @param name
	 * @param isConst
	 * @param isGlobal
	 * @param isUnique
	 * @param isComplete
	 * @param parents
	 * @param whereClause
	 * @return
	 */
	public IdentifierExpression mkIdentifierExpression(Attribute[] attributes, BoogieType type, String name,
			boolean isConst, boolean isGlobal, boolean isUnique, boolean isComplete, ParentEdge[] parents,
			Expression whereClause) {
		IdentifierExpression id = new IdentifierExpression(this.dummyLocation, type, name);
		if (isConst) {
			String[] names = { name };
			VarList varlist = new VarList(this.dummyLocation, names, astTypeFromBoogieType(type), whereClause);
			ConstDeclaration constdecl = new ConstDeclaration(this.dummyLocation, attributes, isUnique, varlist,
					parents, isComplete);
			this.globalDeclarations.add(constdecl);
		} else {
			String[] names = { name };
			VarList[] variables = { new VarList(this.dummyLocation, names, astTypeFromBoogieType(type), whereClause) };
			VariableDeclaration vdecl = new VariableDeclaration(this.dummyLocation, attributes, variables);
			vdecl.isUnique = isUnique;
			this.varDeclMap.put(id, vdecl);
			if (isGlobal) {
				this.globalDeclarations.add(vdecl);
			}
		}
		return id;
	}

	/**
	 * 
	 * @return
	 */
	public BoogieType getIntType() {
		return BoogieType.intType;
	}

	/**
	 * 
	 * @return
	 */
	public BoogieType getRealType() {
		return BoogieType.realType;
	}

	/**
	 * 
	 * @return
	 */
	public BoogieType getBoolType() {
		return BoogieType.boolType;
	}

	/**
	 * 
	 * @param indexTypes
	 * @param valueType
	 * @return
	 */
	public BoogieType getArrayType(BoogieType[] indexTypes, BoogieType valueType) {
		HashSet<PlaceholderType> placeholders = new HashSet<PlaceholderType>();
		int numplaceholders = countPlaceHolderTypes(indexTypes, placeholders)
				+ countPlaceHolderTypes(valueType, placeholders);
		ArrayType arrtype = BoogieType.createArrayType(numplaceholders, indexTypes, valueType);
		return arrtype;
	}

	/**
	 * This function counts the number different placeholders in a type this is
	 * needed by the BoogieType. Placeholders are somewhat like generics in Java
	 * 
	 * @param types
	 * @param alreadyfound
	 * @return
	 */
	private int countPlaceHolderTypes(BoogieType[] types, HashSet<PlaceholderType> alreadyfound) {
		int ret = 0;
		for (int i = 0; i < types.length; i++) {
			ret += countPlaceHolderTypes(types[i], alreadyfound);
		}
		return ret;
	}

	/**
	 * This function counts the number different placeholders in a type this is
	 * needed by the BoogieType. Placeholders are somewhat like generics in Java
	 * 
	 * @param type
	 * @param alreadyfound
	 * @return
	 */
	private int countPlaceHolderTypes(BoogieType type, HashSet<PlaceholderType> alreadyfound) {
		if (type instanceof PrimitiveType) {
			return 0;
		} else if (type instanceof ArrayType) {
			ArrayType arrtype = (ArrayType) type;
			return arrtype.getNumPlaceholders();
		} else if (type instanceof ConstructedType) {
			ConstructedType contype = (ConstructedType) type;
			int ret = 0;
			for (int i = 0; i < contype.getConstr().getParamCount(); i++) {
				ret += countPlaceHolderTypes(contype.getParameter(i), alreadyfound);
			}
			return ret;
		} else if (type instanceof PlaceholderType) {
			if (alreadyfound.contains(type)) {
				return 0;
			}
			alreadyfound.add((PlaceholderType) type);
			return 1;
		}
		return 0;
	}

	/**
	 * 
	 * @return
	 */
	public BoogieType mkPlaceholderType() {
		return new PlaceholderType(placeholderTypeCounter++);
	}

	public BoogieType mkNamedPlaceholderType(String name) {
		return new PlaceholderType(name, placeholderTypeCounter++);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public BoogieType getNamedType(String name) {
		BoogieType[] empty = {};
		return getNamedType(name, empty, false, null);
	}

	/**
	 * 
	 * @param name
	 * @param isFinite
	 * @return
	 */
	public BoogieType getNamedType(String name, boolean isFinite) {
		BoogieType[] empty = {};
		return getNamedType(name, empty, isFinite, null);
	}

	/**
	 * 
	 * @param name
	 * @param parameters
	 * @param isFinite
	 * @param synonym
	 * @return
	 */
	public BoogieType getNamedType(String name, BoogieType[] parameters, boolean isFinite, ASTType synonym) {
		for (Entry<BoogieType, ASTType> entry : this.boogieType2ASTTypeMap.entrySet()) {
			if ((entry.getKey()) instanceof ConstructedType) {
				ConstructedType contype = (ConstructedType) (entry.getKey());
				if (contype.getConstr().getName().equals(name)) {
					if (contype.getConstr().getParamCount() == parameters.length) {
						boolean same = true;
						for (int i = 0; i < parameters.length; i++) {
							if (!contype.getParameter(i).equals(parameters[i])) {
								same = false;
								break;
							}
						}
						if (same) {
							return contype;
						}
					}
				}
			}
		}
		TypeConstructor tc;
		if (parameters.length > 0) {
			int[] order = new int[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				order[i] = i;
			}
			tc = new TypeConstructor(name, false, parameters.length, order);
		} else {
			int[] order = {};
			tc = new TypeConstructor(name, false, parameters.length, order);
		}
		BoogieType namedtype = BoogieType.createConstructedType(tc, parameters);
		// now we have to make a global TypeDeclaration to make this type
		// visible:
		ILocation loc = dummyLocation;
		String[] tparams = new String[parameters.length];
		Attribute[] attributes = {};

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] instanceof PlaceholderType) {
				tparams[i] = ((PlaceholderType) parameters[i]).getIdentifier();
			} else {
				throw new RuntimeException(
						"that's not working! you have to use substitutePlaceholders on the original type!");
				// tparams[i]= astTypeFromBoogieType(parameters[i]).toString();
			}
		}
		this.boogieType2ASTTypeMap.put(namedtype, this.astTypeFromBoogieType(namedtype));
		globalDeclarations.add(new TypeDeclaration(loc, attributes, isFinite, name, tparams, synonym));

		return namedtype;
	}

	/**
	 * 
	 * @param original
	 * @param substtypes
	 * @return
	 */
	public BoogieType mkSubstituteType(BoogieType original, BoogieType[] substtypes) {
		return original.substitutePlaceholders(substtypes);
	}

	/*
	 * helper functions
	 */

	/**
	 * This function is used to create an ASTType for BoogieTypes that have been
	 * created using the API.
	 * 
	 * @param type
	 * @return
	 */
	private ASTType astTypeFromBoogieType(BoogieType type) {
		LinkedList<NamedAstType> typeparams = new LinkedList<NamedAstType>();
		return astTypeFromBoogieType((BoogieType) type, typeparams);
	}

	/**
	 * This function must only be called by astTypeFromBoogieType(BoogieType
	 * type)
	 * 
	 * @param type
	 * @param typeparams
	 * @return
	 */
	private ASTType astTypeFromBoogieType(BoogieType type, LinkedList<NamedAstType> typeparams) {
		if (boogieType2ASTTypeMap.containsKey(type)) {
			return boogieType2ASTTypeMap.get(type);
		}
		ASTType astType;
		ILocation loc = dummyLocation;
		if (type instanceof PrimitiveType) {
			astType = new bixie.boogie.ast.asttypes.PrimitiveAstType(loc, ((PrimitiveType) type).toString(0, false));
		} else if (type instanceof ArrayType) {
			ArrayType arrtype = (ArrayType) type;
			ASTType[] idxtype = new ASTType[arrtype.getIndexCount()];
			for (int i = 0; i < arrtype.getIndexCount(); i++) {
				idxtype[i] = astTypeFromBoogieType(arrtype.getIndexType(i), typeparams);
			}
			ASTType valuetype = astTypeFromBoogieType(arrtype.getValueType(), typeparams);
			String[] usedparams = new String[typeparams.size()];
			for (int i = 0; i < typeparams.size(); i++) {
				usedparams[i] = typeparams.get(i).getName();
			}
			astType = new ArrayAstType(loc, usedparams, idxtype, valuetype);
		} else if (type instanceof ConstructedType) {
			ConstructedType contype = (ConstructedType) type;
			ASTType[] param = new ASTType[contype.getConstr().getParamCount()];
			for (int i = 0; i < contype.getConstr().getParamCount(); i++) {
				param[i] = astTypeFromBoogieType(contype.getParameter(i), typeparams);
			}
			astType = new NamedAstType(loc, contype.getConstr().getName(), param);
		} else if (type instanceof PlaceholderType) {
			PlaceholderType phtype = (PlaceholderType) type;
			String typename = phtype.getIdentifier();
			ASTType[] param = {};
			astType = new NamedAstType(loc, typename, param);
			typeparams.add((NamedAstType) astType);
		} else {
			throw new RuntimeException("not implemented: " + ((null == type) ? "null" : type.getClass().toString()));
		}

		boogieType2ASTTypeMap.put(type, astType);
		return astType;
	}

}
