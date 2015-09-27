/**
 * 
 */
package ic_java.fault_localization;

import java.math.BigInteger;

/**
 * @author schaef
 * Snippets from SMTInterpol that triggered exceptions 
 * in the fault localization.
 */
public class FaultLocalization01 {

	public void updateUpperLowerSet(BigInteger coeff, LinVar nb) {
		InfinitNumber ubound = nb.getUpperBound();
		InfinitNumber lbound = nb.getLowerBound();
		if (coeff.signum() < 0) {
			InfinitNumber swap = ubound;
			ubound = lbound;
			lbound = swap;
		}
		if (ubound.isInfinity()) {
			mNumUpperInf++;
		} else {
			mUpperComposite.addmul(ubound.mA, coeff);
		}
		mNumUpperEps += ubound.mEps * coeff.signum();
		if (lbound.isInfinity()) {
			mNumLowerInf++;
		} else {
			mLowerComposite.addmul(lbound.mA, coeff);
		}
		mNumLowerEps += lbound.mEps * coeff.signum();
	}
	
	public final void updateUpper(
			BigInteger coeff, InfinitNumber oldBound, InfinitNumber newBound) {
		if (oldBound.isInfinity()) {
			if (newBound.isInfinity())
				return;
			mNumUpperInf--;
			mUpperComposite.addmul(newBound.mA, coeff);
		} else if (newBound.isInfinity()) {
			mNumUpperInf++;
			mUpperComposite.addmul(oldBound.mA.negate(), coeff);
		} else {
			mUpperComposite.addmul(newBound.mA.sub(oldBound.mA), coeff);
		}
		mNumUpperEps += (newBound.mEps - oldBound.mEps) * coeff.signum();
	}
	
	public void fixEpsilon() {
		if (mBasic) {
			BigInteger epsilons = BigInteger.ZERO;
			for (MatrixEntry entry = mHeadEntry.mNextInRow; entry != mHeadEntry;
				entry = entry.mNextInRow) {
				int eps = entry.mColumn.mCurval.mEps;
				if (eps > 0)
					epsilons = epsilons.subtract(entry.mCoeff);
				else if (eps < 0)
					epsilons = epsilons.add(entry.mCoeff);
			}
			mCurval = new InfinitNumber(mCurval.mA, 
					epsilons.signum() * mHeadEntry.mCoeff.signum());
		}
	}	
	
	
	/* Ignore the stuff below.
	 * Only stub variables and method needed for the tested procedures above.
	 */
	
	public int mNumUpperInf, mNumUpperEps, mNumLowerInf, mNumLowerEps;
	private boolean mBasic;
	private MutableRational mLowerComposite, mUpperComposite;
	private InfinitNumber mCurval;
	private MatrixEntry mHeadEntry;
	
	/**
	 * @author schaef
	 * Stubs for the actual analysis
	 */
	public static class LinVar {
		public InfinitNumber mCurval;
		public InfinitNumber getUpperBound() {
			return null;
		}
		public InfinitNumber getLowerBound() {
			return null;
		}		
	}

	/**
	 * @author schaef
	 * Stubs for the actual analysis
	 */
	public static class InfinitNumber {
		public int mEps;
		public Rational mA;

		public InfinitNumber(Rational mA2, int i) {
			// TODO Auto-generated constructor stub
		}

		public boolean isInfinity() {
			return false;
		}
	}

	/**
	 * @author schaef
	 * Stubs for the actual analysis
	 */
	public static class Rational {

		public Object negate() {
			return null;
		}

		public Object sub(Rational mA) {
			return null;
		}
	}

	/**
	 * @author schaef
	 * Stubs for the actual analysis
	 */
	public static class MatrixEntry {
		BigInteger mCoeff;
		LinVar     mRow;
		LinVar     mColumn;
		
		MatrixEntry mPrevInRow;
		MatrixEntry mNextInRow;
		MatrixEntry mPrevInCol;
		MatrixEntry mNextInCol;		
	}
	
	
	/**
	 * @author schaef
	 * Stubs for the actual analysis
	 */
	public static class MutableRational {
		public void addmul(Object mA, BigInteger coeff) {
		}

	}
		
}
