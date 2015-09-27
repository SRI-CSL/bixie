package bixie.prover;

import java.math.BigInteger;

public interface ProverExpr {
	ProverType getType();

	BigInteger getIntLiteralValue();

	boolean getBooleanLiteralValue();

	// to add: more functions for querying the kind, structure, contents of
	// expression
}
