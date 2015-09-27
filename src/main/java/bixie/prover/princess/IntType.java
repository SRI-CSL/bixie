package bixie.prover.princess;

import bixie.prover.ProverType;

class IntType implements ProverType {

	public static IntType INSTANCE = new IntType();

	private IntType() {
	}

	public String toString() {
		return "Int";
	}

}
