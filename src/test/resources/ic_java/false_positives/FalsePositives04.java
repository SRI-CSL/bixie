package ic_java.false_positives;

public abstract class FalsePositives04 {
    
    Object first;
	abstract Object foo();
    
//    public Object peekFirst() {
//        try {
//        	first = foo();
//            return first.toString();
//        } finally {
//        	if (first==null) {
//        		return null;
//        	}
//        }        
//    }
 }
