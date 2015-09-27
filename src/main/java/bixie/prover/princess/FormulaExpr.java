package bixie.prover.princess;

import java.math.BigInteger;

import ap.SimpleAPI$;
import ap.parser.IBoolLit;
import ap.parser.IFormula;
import bixie.prover.ProverExpr;
import bixie.prover.ProverType;

class FormulaExpr implements ProverExpr {

	protected final IFormula formula;

	FormulaExpr(IFormula formula) {
		this.formula = formula;
	}

	public String toString() {
            return SimpleAPI$.MODULE$.pp(formula);
	}

	public ProverType getType() {
		return BoolType.INSTANCE;
	}

	public BigInteger getIntLiteralValue() {
		throw new RuntimeException();
	}

	public boolean getBooleanLiteralValue() {
		if (formula instanceof IBoolLit)
			return ((IBoolLit) formula).value();
		throw new RuntimeException();
	}

  public int hashCode() {
    return formula.hashCode();
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FormulaExpr other = (FormulaExpr) obj;
    if (formula == null) {
      if (other.formula != null)
        return false;
    } else if (!formula.equals(other.formula))
      return false;
    return true;
  }
}
