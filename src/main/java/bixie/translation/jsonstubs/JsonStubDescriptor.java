/**
 * 
 */
package bixie.translation.jsonstubs;

import bixie.boogie.enums.BinaryOperator;

/**
 * @author schaef
 *         Inteface for json descriptions of stubs.
 */
public class JsonStubDescriptor {
	
	public static enum StubType {
		Boolean("Boolean"), NonNull("NonNull"), Comparison("Comparison");
		
		final String name;
		StubType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return this.name;
		}
	}

	
	public String methodSignature;
	public StubType type;
	
	
	public int param1;
	public int param2;
	public BinaryOperator be;
	public boolean guardNegated;

	public JsonStubDescriptor(StubType tp, String signature, int p1, boolean neg) {
		this.type = tp;
		this.methodSignature = signature;
		this.param1 = p1;
		this.guardNegated = neg;
	}

	public JsonStubDescriptor(StubType tp, String signature, BinaryOperator be, int p1, int p2,  boolean neg) {
		this.type = tp;
		this.methodSignature = signature;
		this.param1 = p1;
		this.param2 = p2;
		this.guardNegated = neg;
		this.be = be;
	}

	@Override 
	public boolean equals(Object other) {
		if (other instanceof JsonStubDescriptor) {
			JsonStubDescriptor o = (JsonStubDescriptor)other;
			return this.type.equals(o.type) && this.methodSignature.equals(o.methodSignature) && this.param1==o.param1 && this.param2==o.param2 && this.guardNegated==o.guardNegated; 
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.methodSignature.hashCode()+37*this.param1+37*this.param2+ this.be.hashCode()+(this.guardNegated?37:0);
	}
}
