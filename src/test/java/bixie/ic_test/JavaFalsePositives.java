package bixie.ic_test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import bixie.Main;
import bixie.checker.reportprinter.ReportPrinter;

@RunWith(Parameterized.class)
public class JavaFalsePositives extends AbstractIcTest{

	private File sourceFile;
	
	@Parameterized.Parameters (name = "{index}: check ({1})")
	public static Collection<Object[]> data() {
		List<Object[]> filenames = new LinkedList<Object[]>();
		final File source_dir = new File(testRoot + "ic_java/false_positives/");			
		  File[] directoryListing = source_dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {		    	
		    	if (child.isFile() && child.getName().endsWith(".java")) {
		    		filenames.add(new Object[] {child, child.getName()});		    		
		    	} else {
		    		//Ignore
		    	}
		    }
		  } else {			  
		    // Handle the case where dir is not really a directory.
		    // Checking dir.isDirectory() above would not be sufficient
		    // to avoid race conditions with another process that deletes
		    // directories.
			  throw new RuntimeException("Test data not found!");
		  }				  
	   return filenames;
   }
	
	public JavaFalsePositives(File source, String shortname) {
		this.sourceFile = source;
	}
	
	@Test
	public void test() {

		File classFileDir = null;
		try {
			classFileDir = compileJavaFile(this.sourceFile);
			Main bx = new Main();
			ReportPrinter rp = bx.translateAndRun(classFileDir.getAbsolutePath(),
					classFileDir.getAbsolutePath());
			//assert that nothing is reported for the potential 
			//false positives.
			assertTrue(rp.countReports()==0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (classFileDir != null) {
				try {
					delete(classFileDir);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
