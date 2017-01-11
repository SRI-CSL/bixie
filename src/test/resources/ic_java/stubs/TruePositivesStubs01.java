/**
 * 
 */
package ic_java.stubs;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;

/**
 * @author schaef
 * Snippets from SMTInterpol that triggered exceptions 
 * in the fault localization.
 */
public class TruePositivesStubs01 {

	public void guavaContracts01(int x) {
		Verify.verify(x==0);
		if (x!=0) {
			System.err.println(x);
		}
	}

	public void guavaContracts02(int x) {
		Verify.verify(x==0, "Error {} {}", "1", "2");
		if (x!=0) {
			System.err.println(x);
		}
	}

	public void guavaContracts03(Object o) {
		Verify.verifyNotNull(o, "Error {} {}", "1", "2");
		o.toString();
	}
	
	public void guavaContracts04(Object o) {
		Verify.verifyNotNull(o);
		o.toString();
	}

	public void guavaContracts05(Object o) {
		Preconditions.checkArgument(false);
		Preconditions.checkArgument(false, "");
		Preconditions.checkArgument(false, "{}", 5);
		
		Preconditions.checkNotNull(o);

		Preconditions.checkState(false);
		o.toString();
	}

	
	public void junitContract01(int x) {
		org.junit.Assert.assertTrue(x==0);
		if (x>5) {
			System.err.println(x);
		}
	}
	
	public void junitContract02(int x) {
		org.junit.Assert.assertEquals(x, 2);
		org.junit.Assert.assertEquals(this, this);
		if (x>5) {
			System.err.println(x);
		}
	}
	
}
