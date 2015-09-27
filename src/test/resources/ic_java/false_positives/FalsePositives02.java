package ic_java.false_positives;

/**
 * @author schaef
 *
 */
public abstract class FalsePositives02 {

	protected boolean foundNext;
	protected Object next;
	
	//computing the modifies clause for this one is tricky.
	public abstract void findNext();
	
	public Object foo() {
        if (foundNext) {
            foundNext = false;
            return next;
        }
        findNext();
        if (foundNext) {
            foundNext = false;  ///bug in modifies clause
            return next; 
        }
        return null;
	}
	
}
