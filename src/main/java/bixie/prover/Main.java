package bixie.prover;

import java.util.HashMap;
import java.util.Map;

import bixie.prover.princess.PrincessProverFactory;

public class Main {

	public void test01(Prover p) {
		System.out.println("\n\n\nDSN Testing interpolation and abduction");
		Map<String, ProverExpr> vars = new HashMap<String, ProverExpr>();
		ProverType intType = p.getIntType();
		final ProverExpr ap = getVar("a'", intType, vars, p);
		final ProverExpr bp = getVar("b'", intType, vars, p);
		final ProverExpr a = getVar("a", intType, vars, p);
		final ProverExpr b = getVar("b", intType, vars, p);
		final ProverExpr i = getVar("i", intType, vars, p);
		final ProverExpr j = getVar("j", intType, vars, p);
		final ProverExpr a1 = getVar("a1", intType, vars, p);
		final ProverExpr b1 = getVar("b1", intType, vars, p);
		final ProverExpr zero = p.mkLiteral(0);
		final ProverExpr one = p.mkLiteral(1);
		final ProverExpr two = p.mkLiteral(2);
		// ProverExpr[] toIgnore = new ProverExpr[] { a1, b1 };

		// ProverExpr hypo = p.mkEq(ap,bp);
		ProverExpr hypo = p.mkImplies(p.mkEq(a, b), p.mkEq(ap, bp));
		ProverExpr axiomAnds[] = { p.mkEq(a, b),
				p.mkNot(p.mkEq(p.mkEMod(i, two), zero)),
				p.mkEq(a1, p.mkPlus(a, one)),
				p.mkEq(b1, p.mkPlus(b, p.mkMinus(j, i))),
				// p.mkEq(j, p.mkMinus(i, one)),
				/*
				 * p.mkImplies( p.mkAnd(new ProverExpr [] { p.mkEq(a1,b1),
				 * p.mkNot(p.mkEq(p.mkEMod(p.mkPlus(i,two), two),zero)),
				 * p.mkEq(j, i)}), p.mkEq(a1,b1))
				 */
				p.mkImplies(p.mkEq(a1, b1), p.mkEq(ap, bp)) };

		final ProverExpr axiom = p.mkAnd(axiomAnds);
		System.out.println("Hypo is: " + hypo);
		System.out.println("Axioms are: " + axiom);

		// ProverExpr[] explanations =
		// ((PrincessProver)p).explainSimply(axiom, hypo, toIgnore);
		//
		// System.out.println("There were " + explanations.length +
		// " abductions");
		// for (ProverExpr e : explanations){
		// System.out.println("Explanation was: " + e);
		// System.out.println(p.mkImplies(e, hypo));
		// }
	}

	public void test02(Prover p) {
		ProverExpr c = p.mkVariable("c", p.getIntType());
		ProverExpr d = p.mkVariable("d", p.getIntType());
		ProverExpr r = p.mkVariable("r", p.getBooleanType());
		ProverExpr s = p.mkVariable("s", p.getBooleanType());

		p.addAssertion(p.mkAnd(r, p.mkEq(c, p.mkPlus(d, p.mkLiteral(15)))));
		p.addAssertion(p.mkGeq(d, p.mkLiteral(100)));

		p.addAssertion(p.mkOr(p.mkNot(r), s));
		System.out.println(p.checkSat(true));

		System.out.println("c = " + p.evaluate(c));
		System.out.println("r = " + p.evaluate(r));

		p.push();

		p.addAssertion(p.mkOr(p.mkNot(s), p.mkLeq(c, p.mkLiteral(-100))));
		System.out.println(p.checkSat(true));

		p.pop();
		System.out.println(p.checkSat(true));
	}

	public void test03(Prover p) {
		p.push();
		ProverExpr c = p.mkVariable("c", p.getIntType());
		ProverExpr d = p.mkVariable("d", p.getIntType());

		ProverFun f = p.mkUnintFunction("f",
				new ProverType[] { p.getIntType() }, p.getIntType());
		p.addAssertion(p.mkEq(f.mkExpr(new ProverExpr[] { c }), p.mkLiteral(5)));
		p.addAssertion(p.mkEq(f.mkExpr(new ProverExpr[] { d }), p.mkLiteral(6)));

		System.out.println(p.checkSat(true));

		System.out.println("f(c) = "
				+ p.evaluate(f.mkExpr(new ProverExpr[] { c })));
		p.addAssertion(p.mkEq(c, d));

		System.out.println(p.checkSat(true));

		p.pop();

	}

	public void test04(Prover p) {
		p.push();
		final ProverExpr a = p.mkVariable("a", p.getIntType());
		final ProverExpr b = p.mkVariable("b", p.getIntType());

		final ProverFun geInt = p.mkDefinedFunction("geInt", new ProverType[] {
				p.getIntType(), p.getIntType() }, p.mkIte(
				p.mkGeq(p.mkBoundVariable(0, p.getIntType()),
						p.mkBoundVariable(1, p.getIntType())), p.mkLiteral(1),
				p.mkLiteral(0)));
		System.out.println(geInt);
		p.addAssertion(p.mkEq(geInt.mkExpr(new ProverExpr[] { a, b }),
				p.mkLiteral(1)));
		p.addAssertion(p.mkEq(geInt.mkExpr(new ProverExpr[] { b, a }),
				p.mkLiteral(1)));

		System.out.println(p.checkSat(true));
		p.addAssertion(p.mkNot(p.mkEq(a, b)));
		System.out.println(p.checkSat(true));
		p.pop();
	}

	public void test05(Prover p) {
		System.out.println(p.checkSat(false));
		ProverResult res;
		while ((res = p.getResult(false)) == ProverResult.Running) {
			System.out.println("Running ... ");
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}

		System.out.println(res);
		System.out.println("-----");

		System.out.println(p.checkSat(false));
		try {
			Thread.sleep(3);
		} catch (InterruptedException e) {
		}
		System.out.println(p.stop());
		System.out.println("-----");

		System.out.println(p.checkSat(false));
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
		}
		System.out.println(p.stop());
		System.out.println("-----");
	}

	public void test06(Prover p) {
		p.setConstructProofs(true);
		final ProverExpr a = p.mkVariable(
				"a",
				p.getArrayType(new ProverType[] { p.getIntType() },
						p.getIntType()));
		final ProverExpr b = p.mkVariable(
				"b",
				p.getArrayType(new ProverType[] { p.getIntType() },
						p.getIntType()));

		p.setPartitionNumber(0);
		p.addAssertion(p.mkEq(
				p.mkStore(a, new ProverExpr[] { p.mkLiteral(0) },
						p.mkLiteral(1)), b));
		p.setPartitionNumber(1);
		p.addAssertion(p.mkEq(
				p.mkSelect(b, new ProverExpr[] { p.mkLiteral(0) }),
				p.mkLiteral(2)));

		System.out.println(p.checkSat(true));

		final ProverExpr interpolant = p.interpolate(new int[][] {
				new int[] { 0 }, new int[] { 1 } })[0];
		System.out.println(interpolant);
		System.out.print("Variables: ");
		final ProverExpr[] vars = p.freeVariables(interpolant);
		for (int i = 0; i < vars.length; ++i)
			System.out.print("" + vars[i] + " ");
		System.out.println();

		System.out.println(p.substitute(interpolant, new ProverExpr[] { b },
				new ProverExpr[] { a }));
	}

	private static ProverExpr getVar(String name, ProverType type,
			Map<String, ProverExpr> m, Prover p) {
		ProverExpr var = m.get(name);
		if (var == null) {
			var = p.mkVariable(name, type);
			m.put(name, var);
		}
		if (!var.getType().equals(type)) {
			throw new RuntimeException("Wrong type on var: " + name);
		}
		return var;
	}	
	
	public void runTests(ProverFactory factory) {
		final Prover p = factory.spawn();
		test01(p);
		p.reset();
		test02(p);
		p.reset();
		test03(p);
		p.reset();
		test04(p);
		p.reset();
		test05(p);
		p.reset();
		test06(p);
		p.reset();

		p.shutdown();
	}

	public static void main(String[] args) {
		final ProverFactory factory = new PrincessProverFactory();
		Main m = new Main();
		m.runTests(factory);
	}
}
