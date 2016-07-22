
package bixie.translation;

import org.kohsuke.args4j.Option;

/**
 * Options
 * 
 * @author schaef, schaef
 */
public class Options {

	@Option(name = "-threads", usage = "havoc variables that may be modified by other threads.", required = false)
	private boolean soundThreads=false;
	public boolean useSoundThreads() {
		return soundThreads;
	}
	public void setSoundThreads(boolean st) {
		this.soundThreads = st;
	}

	
	@Option(name = "-main", usage = "Set the Main class for full program analysis.")
	private String mainClassName;
	public String getMainClassName() {
		return mainClassName;
	}
	public void setMainClassName(String name) {
		mainClassName = name;
	}
	
	
	@Option(name = "-tc", usage = "Perfrom type check after translation", required = false)
	private boolean runTypeChecker=false;
	public boolean getRunTypeChecker() {
		return runTypeChecker;
	}
	public void setRunTypeChecker(boolean b) {
		runTypeChecker = b;
	}

	
	@Option(name = "-android-jars", usage = "Path to the jars that stub the android platform.")
	private String androidStubPath=null;
	
	public String getAndroidStubPath() {
		return androidStubPath;
	}

	public void setAndroidStubPath(String path) {
		this.androidStubPath = path;
	}
	
	
	@Option(name = "-prelude", usage = "Use custom prelude instead of the built-in one.")
	private String preludeFileName;
	public String getPreludeFileName() {
		return preludeFileName;
	}
	
	
	/**
	 * JAR file
	 */
	@Option(name = "-j", usage = "JAR file", required = false)
	private String jarFile;

	/**
	 * Boogie file
	 */
	@Option(name = "-b", usage = "Boogie file")
	private String boogieFile;

	/**
	 * Classpath
	 */
	@Option(name = "-cp", usage = "Classpath")
	private String classpath;


	/**
	 * Returns the JAR file
	 * 
	 * @return JAR file
	 */
	public String getJarFile() {
		return jarFile;
	}

	/**
	 * Returns the Boogie file
	 * 
	 * @return Boogie file
	 */
	public String getBoogieFile() {
		return boogieFile;
	}



	/**
	 * Determines, whether Joogie has an additional classpath
	 * 
	 * @return true = Joogie has an additional classpath
	 */
	public boolean hasClasspath() {
		return (null != classpath);
	}

	/**
	 * Returns the additional classpath
	 * 
	 * @return Additional classpath
	 */
	public String getClasspath() {
		return classpath;
	}

	/**
	 * Assigns the additional classpath
	 * 
	 * @param classpath
	 *            Additional classpath
	 */
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}
	
	
	
	
	/**
	 * Option object
	 */
	private static Options options;

	public static void resetInstance() {
		options = null;	
	}
	
	
	/**
	 * Singleton method
	 * 
	 * @return Options
	 */
	public static Options v() {
		if (null == options) {
			options = new Options();
		}
		return options;
	}

	/**
	 * C-tor
	 */
	private Options() {
		// do nothing
	}

}
