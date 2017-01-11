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
import bixie.boogie.enums.BinaryOperator;
import bixie.translation.GlobalsCache;
import bixie.translation.jsonstubs.JsonStubDescriptor;
import bixie.translation.jsonstubs.JsonStubDescriptor.StubType;

@RunWith(Parameterized.class)
public class StubTruePositives extends AbstractIcTest {

	private File sourceFile, goldenFile;

	@Parameterized.Parameters(name = "{index}: check ({1})")
	public static Collection<Object[]> data() {
		List<Object[]> filenames = new LinkedList<Object[]>();
		final File source_dir = new File(testRoot + "ic_java/stubs/");
		File[] directoryListing = source_dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.isFile() && child.getName().endsWith(".java")) {
					filenames.add(new Object[] { child, child.getName() });
				} else {
					// Ignore
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

	public StubTruePositives(File source, String name) {
		this.sourceFile = source;
		this.goldenFile = new File(source.getAbsolutePath().replace(".java", ".gold"));
	}

	@Test
	public void test1() {
		testWithChecker(1);
	}

//	@Test
//	public void test2() {
//		testWithChecker(2);
//	}

	
	public void testWithChecker(int i) {
		System.out.println("Running test: "+sourceFile.getName());
		File classFileDir = null;
		File outFile = null;
		try {
			outFile = File.createTempFile("bixie_test", ".txt");
			classFileDir = compileJavaFile(this.sourceFile);
			if (classFileDir==null || !classFileDir.isDirectory()) {
				assertTrue(false);
			}
			Main bx = new Main();
			bixie.Options.v().setSelectedChecker(i);
			bixie.Options.v().setTimeout(600);
			
			bixie.Options.v().callsLogFileName = "logLibCalls.txt";
			bixie.Options.v().exportStubsFileName = "stubs.json";
			bixie.Options.v().importStubsFileName = "stubs.json";
			createStubs();
			
			
			String outFilePath = outFile.getAbsolutePath();			
			// if no golden output has been generated for this test,
			// generate one using the current result.
			boolean firstRun = false;
			if (!this.goldenFile.isFile()) {
				firstRun = true;
				outFilePath = this.goldenFile.getAbsolutePath();
			}
			
			bx.translateAndRun(classFileDir.getAbsolutePath(),
					classFileDir.getAbsolutePath(), outFilePath);
			if (!firstRun) {
				String outputString = fileToString(outFile); 
				assertTrue("Report does not match Golden output:\n"+outputString, this.compareFiles(outFile, this.goldenFile));
			} else {
				assertTrue(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		} finally {
			if (classFileDir != null) {
				try {
					delete(classFileDir);
					if (outFile != null && outFile.isFile()) {
						if (!outFile.delete()) {
							System.err.println("Failed to delete file");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}		
	}

	private void createStubs() {
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Verify: void verify(boolean)>", 0, false)); 
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Verify: void verify(boolean,java.lang.String,java.lang.Object[])>", 0, false));
		
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.NonNull, "<com.google.common.base.Verify: java.lang.Object verifyNotNull(java.lang.Object,java.lang.String,java.lang.Object[])>", 0, true));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.NonNull, "<com.google.common.base.Verify: java.lang.Object verifyNotNull(java.lang.Object)>", 0, true));
		
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<org.junit.Assert: void assertTrue(boolean)>", 0, false)); 
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<org.junit.Assert: void assertFalse(boolean)>", 0, true));
				
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Comparison, "<org.junit.Assert: void assertEquals(java.lang.Object,java.lang.Object)>", BinaryOperator.COMPEQ, 0, 1, false));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Comparison, "<org.junit.Assert: void assertEquals(long,long)>", BinaryOperator.COMPEQ, 0, 1, false));
		
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Preconditions: void checkArgument(boolean,java.lang.Object)>", 0, false));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Preconditions: void checkArgument(boolean,java.lang.String,java.lang.Object[])>", 0, false));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Preconditions: void checkArgument(boolean)>", 0, false));

		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Preconditions: void checkState(boolean,java.lang.Object)>", 0, false));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Preconditions: void checkState(boolean,java.lang.String,java.lang.Object[])>", 0, false));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.Boolean, "<com.google.common.base.Preconditions: void checkState(boolean)>", 0, false));

		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.NonNull, "<com.google.common.base.Preconditions: java.lang.Object checkNotNull(java.lang.Object,java.lang.Object)>", 0, false));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.NonNull, "<com.google.common.base.Preconditions: java.lang.Object checkNotNull(java.lang.Object,java.lang.String,java.lang.Object[])>", 0, false));
		GlobalsCache.v().jsonStubber.addStub(new JsonStubDescriptor(StubType.NonNull, "<com.google.common.base.Preconditions: java.lang.Object checkNotNull(java.lang.Object)>", 0, false));
	}
	
}
