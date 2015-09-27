package bixie.prover;

public interface ProverFactory {

	public Prover spawn();

	public Prover spawnWithLog(String basename);

}
