package bixie.prover.princess;

import bixie.prover.Prover;
import bixie.prover.ProverFactory;

public class PrincessProverFactory implements ProverFactory {

	@Override
	public Prover spawn() {
		return new PrincessProver();
	}

	@Override
	public Prover spawnWithLog(String basename) {
		return new PrincessProver(basename);
	}

}
