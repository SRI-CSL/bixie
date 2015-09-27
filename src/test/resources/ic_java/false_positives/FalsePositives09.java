package ic_java.false_positives;

import java.io.IOException;

/**
 * @author schaef
 *
 */
public class FalsePositives09 {

	
//	In file: /Users/schaef/git/jar2bpl/jar2bpl_test/org/bouncycastle/crypto/tls/TlsProtocol.java
//	line 389	
	short myshort;
	boolean mybool;
	private short foo() {return 0;}
    @SuppressWarnings("unused")
	private void processChangeCipherSpec(byte[] buf, int off, int len)
            throws IOException
        {
            for (int i = 0; i < len; ++i)
            {
                short message = foo();

                if (message != myshort)
                {
                    throw new RuntimeException();
                }

                if (this.mybool)
//                    || alertQueue.size() > 0
//                    || handshakeQueue.size() > 0)
                {
                    throw new RuntimeException();
                }

                //recordStream.receivedReadCipherSpec();

                this.mybool = true;

                //handleChangeCipherSpecMessage();
            }
        }

    
//  In file: /Users/schaef/git/jar2bpl/jar2bpl_test/org/bouncycastle/pqc/crypto/rainbow/util/ComputeInField.java
//	line 72
    protected short foo(short a, short b) {return a;}
    protected void fp02(short[][] B, short[] b)
    {
        try
        {

            if (B.length != b.length)
            {
                throw new RuntimeException(
                    "The equation system is not solvable");
            }

	       short[][]  A = new short[B.length][B.length + 1];
	        // stores the solution of the LES
	       @SuppressWarnings("unused")
			short[] x = new short[B.length];
	
	
	        /** copy the vector b into the global A **/
	        //the free coefficient, stored in the last column of A( A[i][b.length]
	        // is to be subtracted from b
	        for (int i = 0; i < b.length; i++)
	        {
	            A[i][b.length] = foo(b[i], A[i][b.length]);
	        }   
        } catch(RuntimeException e) {
        	
        }
    }

    private FalsePositives09 impl;
    private boolean foo(int svc) {return true;}
    
    public void stop(int svc) {
        try  {
            if ( impl != null && impl.foo(svc) ) impl = null;
        } catch ( Exception x)  {
        	return;
        }
    }
    
}
