package bixie;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.kohsuke.args4j.Option;

/**
 * Options
 * 
 * @author schaef, schaef
 */
public class Options {
	
	/**
	 * JAR file
	 */
	@Option(name = "-j", usage = "JAR file", required = false)
	private String jarFile=null;

	/**
	 * Boogie file
	 */
	@Option(name = "-b", usage = "Boogie file")
	private String boogieFile=null;

	/**
	 * Boogie file
	 */
	@Option(name = "-o", usage = "Output file")
	private String outputFile = "bixie_output.txt";
	
	
	/**
	 * Classpath
	 */
	@Option(name = "-cp", usage = "Classpath")
	private String classpath="";

	/**
	 * Report output
	 */
	@Option(name = "-html", usage = "Html output directory")
	private String htmlDir=null;
	public String getHtmlDir() {
		return htmlDir;
	}

	/**
	 * Report output
	 */
	@Option(name = "-json", usage = "JSON output directory")
	private String jsonDir=null;
	public String getJSONDir() {
		return jsonDir;
	}

	/**
	 * Location of the source files for reporting.
	 */
	@Option(name = "-src", usage = "List of all source files")
	private String srcFilesString=null;
	private Set<String> sourceFiles = null;
	public Set<String> getSrcFilesString() {
		if (srcFilesString!=null && sourceFiles==null) {
			String[] files = srcFilesString.split(File.pathSeparator);
			sourceFiles = new HashSet<String>();
			if (files!=null) {
				for (String s : files) {
					sourceFiles.add(s);
				}
			}
		}
		return sourceFiles;
	}

	
	/**
	 * Classpath
	 */
	@Option(name = "-t", usage = "Timeout per procedure. Use 0 for no timeout. (Default is 30s)")
	private int timeout=30;
	
	public int getTimeout() {
		return this.timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	
	@Option(name = "-debug", usage = "Print Debug output and files")
	private boolean debugMode = false;	
	public boolean getDebugMode() {
		return debugMode;
	}
	
	@Option(name = "-logProver", usage = "Write prover querries to tmp with given prefix")
	protected String proverLogPrefix = null; 
	
	public String getProverLogPrefix() {
		return this.proverLogPrefix;
	}
	public void setProverLogPrefix(String prefix) {
		this.proverLogPrefix = prefix;
	}

	
	@Option(name = "-checker", usage = "Checker to be used during analysis. Ask the waiter for daily menu.")
	protected int selectedChecker = 1; 
	
	public int getSelectedChecker() {
		return this.selectedChecker;
	}

	public void setSelectedChecker(int checker) {
		this.selectedChecker = checker;
	}
	
	
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

	public String getOutputFile() {
		return this.outputFile;
	}
	
	public void setOutputFile(String s) {
		this.outputFile = s;
	}
	
	/**
	 * Determines, whether Joogie has an additional classpath
	 * 
	 * @return true = Joogie has an additional classpath
	 */
	public boolean hasClasspath() {
		return null != classpath;
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
