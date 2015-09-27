/**
 * 
 */
package ic_java.complex_flow;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;



/**
 * @author schaef
 *
 */
public class Complex01 {

	/**
	 * Stub
	 */
	public abstract class RemoteSampleListener {
		public abstract void testEnded(String host) throws RemoteException ;
		public abstract void sampleOccurred(SampleEvent obj)  throws RemoteException;
	}

	private static class SampleEvent {
		
	}

	public abstract class Logger {
		public abstract void info(Object...s);
		public abstract void error(Object...s);
		public abstract void warn(Object...s);
	}
	
	public Logger log;
	private RemoteSampleListener listener;
	private transient volatile ObjectOutputStream oos;
    private transient volatile File temporaryFile;
    private transient volatile ExecutorService singleExecutor;
	
	public void testEnded(String host) {
		log.info("Test Ended on " + host);
		singleExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					oos.close(); // ensure output is flushed
				} catch (IOException e) {
					log.error("Failed to close data file ", e);
				}
			}
		});
		singleExecutor.shutdown(); // finish processing samples
		try {
			if (!singleExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
				log.error("Executor did not terminate in a timely fashion");
			}
		} catch (InterruptedException e1) {
			log.error("Executor did not terminate in a timely fashion", e1);
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(temporaryFile));
			Object obj = null;
			while ((obj = ois.readObject()) != null) {
				if (obj instanceof SampleEvent) {
					try {
						listener.sampleOccurred((SampleEvent) obj);
					} catch (RemoteException err) {
						if (err.getCause() instanceof java.net.ConnectException) {
							throw new RuntimeException("Could not return sample",
									err);
						}
						log.error("returning sample", err);
					}
				} else {
					log.error("Unexpected object type found in data file "
							+ obj.getClass().getName());
				}
			}
		} catch (EOFException err) {
			// expected
		} catch (IOException err) {
			log.error("returning sample", err);
		} catch (ClassNotFoundException err) {
			log.error("returning sample", err);
		} finally {
			try {
				listener.testEnded(host);
			} catch (RemoteException e) {
				log.error("returning sample", e);
			}
//			IOUtils.closeQuietly(ois);
			try {
				ois.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!temporaryFile.delete()) {
				log.warn("Could not delete file:"
						+ temporaryFile.getAbsolutePath());
			}
		}
	}

}
