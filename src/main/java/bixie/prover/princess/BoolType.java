package bixie.prover.princess;

import bixie.prover.ProverType;

class BoolType implements ProverType {

	public static BoolType INSTANCE = new BoolType();

	private BoolType() {
	}

	public String toString() {
		return "Bool";
	}

}
