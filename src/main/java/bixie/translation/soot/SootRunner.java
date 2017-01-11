
package bixie.translation.soot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import bixie.translation.Options;
import bixie.util.Log;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

/**
 * The Soot Runner
 * 
 * @author schaef
 */
public class SootRunner {

	protected final soot.options.Options sootOpt = soot.options.Options.v();
	
	public void run(String input) {
		if (null == input || input.isEmpty()) {
			return;
		}
		
		if (input.endsWith(".jar")) {
			// run with JAR file
			runWithJar(input);
		} else {
			File file = new File(input);
			if (file.isDirectory()) {				
				runWithPath(input);
			} else {
				throw new RuntimeException("Don't know what to do with: "
						+ input);
			}
		}
		
	}
	
	
	/**
	 * Runs Soot by using a JAR file
	 * 
	 * @param jarFile
	 *            JAR file
	 * @param smtFile
	 *            Boogie file
	 */
	public void runWithJar(String jarFile) {
		try {
			// extract dependent JARs
			List<File> jarFiles = new ArrayList<File>();
			jarFiles.addAll(extractClassPath(new File(jarFile)));
			jarFiles.add(new File(jarFile));

			// additional classpath available?
			String cp = buildClassPath(jarFiles);
			if (Options.v().hasClasspath()) {
				cp += File.pathSeparatorChar + Options.v().getClasspath();
			}
			
			// set soot-class-path
			sootOpt.set_soot_classpath(cp);			
			
			// finally, run soot
			runSootAndAnalysis(enumClasses(new File(jarFile)));

		} catch (Exception e) {
			Log.error(e.toString());
		}
	}



	/**
	 * Runs Soot by using a path (e.g., from Joogie)
	 * 
	 * @param path
	 *            Path
	 * @param smtFile
	 *            Boogie file
	 */
	public void runWithPath(String path) {
		try {
			// dependent JAR files
			List<File> jarFiles = new ArrayList<File>();

			// additional classpath available?
			String cp = buildClassPath(jarFiles);
			if (Options.v().hasClasspath()) {
				cp += File.pathSeparatorChar + Options.v().getClasspath();
			}

			// set soot-class-path
			sootOpt.set_soot_classpath(cp);
			sootOpt.set_src_prec(soot.options.Options.src_prec_class);

			List<String> processDirs = new LinkedList<String>();
			processDirs.add(path);
			sootOpt.set_process_dir(processDirs);

			// finally, run soot
			runSootAndAnalysis(new LinkedList<String>());

		} catch (Exception e) {
			Log.error(e.toString());
		}		
	}

	/**
	 * Run Soot and creates an inter-procedural callgraph
	 * that could be loaded by Soot.
	 * @param classes additional classes that need to be loaded (e.g., when analyzing jars)
	 */
	protected void runSootAndAnalysis(List<String> classes) {
		sootOpt.set_keep_line_number(true);
		sootOpt.set_prepend_classpath(true); //-pp
		sootOpt.set_output_format(soot.options.Options.output_format_none);
		sootOpt.set_allow_phantom_refs(true);
		sootOpt.setPhaseOption("jop.cpf", "enabled:false");
		
		for (String s : classes) {
			Scene.v().addBasicClass(s, SootClass.BODIES);
		}
		


		// Iterator Hack
		Scene.v().addBasicClass(
				"org.eclipse.jdt.core.compiler.CategorizedProblem",
				SootClass.HIERARCHY);
		Scene.v().addBasicClass("java.lang.Iterable", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.util.Iterator", SootClass.SIGNATURES);
		Scene.v()
				.addBasicClass("java.lang.reflect.Array", SootClass.SIGNATURES);

		try {
			// redirect soot output into a stream.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			soot.G.v().out = new PrintStream(baos, true, "utf-8");				
			//Now load the soot classes.
			Scene.v().loadNecessaryClasses();
			Scene.v().loadBasicClasses();
			
			for (SootClass sc : Scene.v().getClasses()) {
				if (classes.contains(sc.getName())) {
					sc.setApplicationClass();
				}				
			}
			for (SootClass sc : new LinkedList<SootClass>(Scene.v().getClasses())) {			
				if (sc.resolvingLevel()<SootClass.SIGNATURES) {			
					continue;
				}
				if (sc.isApplicationClass()) {					
					for (SootMethod sm : sc.getMethods()) {
						if (sm.isConcrete()) {							
							try {
								Body body = sm.retrieveActiveBody();
								sm.setActiveBody(body);
								if (body!=null) {
									SootBodyTransformer sbt = new SootBodyTransformer();
									sbt.transform(body);
								}
							} catch (Throwable t) {
								Log.error("Failed to process "+sm.getSignature());
								t.printStackTrace();
							}
						}

					}
				}

			}			
			Log.info("Done.");
		} catch (UnsupportedEncodingException e) {
			Log.error(e.toString());
		} catch (Throwable e) {
			Log.error("Soot could not process the input. STOPPING");
			e.printStackTrace();
		}				
	}



	/**
	 * Returns the class path argument for Soot
	 * 
	 * @param files
	 *            Files in the class path
	 * @return Class path argument for Soot
	 */
	protected String buildClassPath(List<File> files) {
		StringBuilder sb = new StringBuilder();
		for (File file : files) {
			sb.append(file.getPath() + File.pathSeparatorChar);
		}
		return sb.toString();
	}

	/**
	 * Extracts dependent JARs from the JAR's manifest
	 * 
	 * @param file
	 *            JAR file object
	 * @returns jarFiles
	 *            List of dependent JARs
	 */
	protected List<File> extractClassPath(File file) {
		List<File> jarFiles = new LinkedList<File>();
		try {
			// open JAR file
			JarFile jarFile = new JarFile(file);

			// get manifest and their main attributes
			Manifest manifest = jarFile.getManifest();
			if (manifest == null) {
				jarFile.close();
				return jarFiles;
			}
			Attributes mainAttributes = manifest.getMainAttributes();
			if (mainAttributes == null) {
				jarFile.close();
				return jarFiles;
			}
			String classPath = mainAttributes
					.getValue(Attributes.Name.CLASS_PATH);

			// close JAR file
			jarFile.close();

			// empty class path?
			if (null == classPath)
				return jarFiles;

			// look for dependent JARs
			String[] classPathItems = classPath.split(" ");
			for (String classPathItem : classPathItems) {
				if (classPathItem.endsWith(".jar")) {
					// add jar
					Log.debug("Adding " + classPathItem
							+ " to Soot's class path");
					jarFiles.add(new File(file.getParent(), classPathItem));
				}
			}

		} catch (IOException e) {
			Log.error(e.toString());
		}
		return jarFiles;
	}


	/**
	 * Enumerates all classes in a JAR file
	 * @param file a Jar file
	 * @returns list of classes in the Jar file.
	 */
	protected List<String> enumClasses(File file) {
		List<String> classes = new LinkedList<String>();
		try {
			// open JAR file
			Log.debug("Opening jar " + file.getPath());
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();

			// iterate JAR entries
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String entryName = entry.getName();

				if (entryName.endsWith(".class")) {
					// get class
					String className = entryName.substring(0,
							entryName.length() - ".class".length());
					className = className.replace('/', '.');

					// add class
					Log.debug("Adding class " + className);
					classes.add(className);
				}
			}

			// close JAR file
			jarFile.close();

		} catch (IOException e) {
			Log.error(e.toString());
		}
		return classes;
	}

}
