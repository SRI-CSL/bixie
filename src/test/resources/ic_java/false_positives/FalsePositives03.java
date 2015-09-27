package ic_java.false_positives;

import java.awt.Container;

import javax.naming.Context;

public class FalsePositives03 {

	/*
	 * there is an axiom missing that states that  
	 * null <: X for all X, otherwise the case where
	 * container==null would inevitably crash the cast.
	 */
	@SuppressWarnings("unused")
	public void foo(Container container) {
        if (container instanceof Context || container == null) {
            Context ctx = ((Context) container); //not sure
        } else {
          //nothing
        }		
	}
	
}
