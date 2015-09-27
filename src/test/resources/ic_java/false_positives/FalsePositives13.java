package ic_java.false_positives;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class FalsePositives13<ModularResultant> {
	// from IntegerPolynomial.java line 716
	@SuppressWarnings("rawtypes")
	private static final List BIGINT_PRIMES = new ArrayList();
	public int[] coeffs;
	static final BigInteger BIGINT_ZERO = BigInteger.valueOf(0);
	static final BigInteger BIGINT_ONE = BigInteger.valueOf(1);
	static final BigDecimal BIGDEC_ONE = BigDecimal.valueOf(1);
	BigInteger max;

	@SuppressWarnings({ "unused", "unchecked" })
	public void resultantMultiThread() {
		int N = coeffs.length;

		// upper bound for resultant(f, g) = ||f, 2||^deg(g) * ||g, 2||^deg(f) =
		// squaresum(f)^(N/2) * 2^(deg(f)/2) because g(x)=x^N-1
		// see
		// http://jondalon.mathematik.uni-osnabrueck.de/staff/phpages/brunsw/CompAlg.pdf
		// chapter 3

		max = max.multiply(BigInteger.valueOf(2).pow((degree() + 1) / 2));
		BigInteger max2 = max.multiply(BigInteger.valueOf(2));

		// compute resultants modulo prime numbers
		BigInteger prime = BigInteger.valueOf(10000);
		BigInteger pProd = BIGINT_ONE;
		LinkedBlockingQueue<Future<ModularResultant>> resultantTasks = new LinkedBlockingQueue<Future<ModularResultant>>();
		Iterator<BigInteger> primes = BIGINT_PRIMES.iterator();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		while (pProd.compareTo(max2) < 0) {
			if (primes.hasNext()) {
				prime = primes.next();
			} else {
				prime = prime.nextProbablePrime();
			}
			// Future<ModularResultant> task = executor.submit(new
			// ModResultantTask(prime.intValue()));
			// resultantTasks.add(task);
			pProd = pProd.multiply(prime);
		}

		// Combine modular resultants to obtain the resultant.
		// For efficiency, first combine all pairs of small resultants to bigger
		// resultants,
		// then combine pairs of those, etc. until only one is left.
		ModularResultant overallResultant = null;
		while (!resultantTasks.isEmpty()) {
			try {
				Future<ModularResultant> modRes1 = resultantTasks.take();
				Future<ModularResultant> modRes2 = resultantTasks.poll();
				if (modRes2 == null) {
					// modRes1 is the only one left
					overallResultant = modRes1.get();
					break;
				}
				// Future<ModularResultant> newTask = executor.submit(new
				// CombineTask(modRes1.get(), modRes2.get()));
				// resultantTasks.add(newTask);
			} catch (Exception e) {
				throw new IllegalStateException(e.toString());
			}
		}
		executor.shutdown();
	}

	private int degree() {
		// TODO Auto-generated method stub
		return 0;
	}

	// In file:
	// /Users/schaef/git/jar2bpl/jar2bpl_test/org/bouncycastle/pqc/math/linearalgebra/GoppaCode.java
	// line 222
	public static void computeSystematicForm(Object h, SecureRandom sr, int n) {
		// Permutation p;
		boolean found = false;

		do {
			// p = new Permutation(n, sr);
			// hp = (GF2Matrix)h.rightMultiply(p);
			// sInv =getLeftSubMatrix();
			try {
				found = true;
				getLeftSubMatrix();
				// s = (GF2Matrix)sInv.computeInverse();
			} catch (ArithmeticException ae) {
				found = false;
			}
		} while (!found);

	}

	private static Object getLeftSubMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

}
