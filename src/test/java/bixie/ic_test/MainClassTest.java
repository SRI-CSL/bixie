/**
 * 
 */
package bixie.ic_test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import bixie.Main;

/**
 * @author schaef
 *
 */
public class MainClassTest extends AbstractIcTest {

	@Test
	public void test() {
		final File source_file = new File(testRoot + "ic_java/true_positives/TruePositives02.java");
		File classFileDir = null;
		try {
			classFileDir = compileJavaFile(source_file);
			Main.main(new String[]{
					"-j", classFileDir.getAbsolutePath(),
					"-logProver", "test"
					});
		} catch (IOException e) {		
			e.printStackTrace();
			fail("Not yet implemented");
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
