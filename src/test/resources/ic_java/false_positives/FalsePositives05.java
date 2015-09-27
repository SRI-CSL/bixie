package ic_java.false_positives;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

//import org.apache.log4j.helpers.Loader;




public abstract class FalsePositives05 {

	private final Object welcomeFilesLock = new Object();
    private String welcomeFiles[] = new String[0];
    abstract Object foo();
    abstract InputStream bar();
	
    
    public String[] ex01() {
        synchronized (welcomeFilesLock) {
            return (welcomeFiles);
        }
    }
    
    
//    private ReadWriteLock loaderLock;
//	private Loader loader;
	
    
    public Object ex02() {
//        Lock readLock = loaderLock.readLock();
//        readLock.lock();
    	foo();
        try {
            return lock;
        } finally {
            bar();
        }
    }   
    
    private Object lock;
    private Object[] array = new Object[0];
    public Object[] ex03() {
        synchronized (lock) {
            return array;
        }
    }
    
    
    public String ex04() {

        InputStream stream = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String strRead = "";
            while (strRead != null) {
                strRead = br.readLine();
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    
    abstract int integer() throws Exception;
    public void ex05() {
        boolean success = false;
        try {
            if (integer() < 10) {
                foo();
            }
            success = true;

        } catch (Exception e) {
        	foo();
        } finally {
            // detect other types of Throwable and cancel this Timer
            if (!success) {
            	foo();
            }
        }
    }
    
    public void ex06(String name) throws Exception {
    	File config = new File(name);
        try (FileOutputStream fos = new FileOutputStream(config);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                        fos , "rw"))) {
            bar();
            
        }
    }
    
}
