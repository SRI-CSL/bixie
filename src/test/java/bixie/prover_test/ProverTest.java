/**
 * 
 */
package bixie.prover_test;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author schaef
 *
 */
public class ProverTest {

	@Test
	public void test() {
		try {
		bixie.prover.Main.main(new String[]{});
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Princess threw an exception");
		}		
	}

}
