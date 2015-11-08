/**
 * 
 */
package bixie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import bixie.boogie.ProgramFactory;
import bixie.checker.ProgramAnalysis;
import bixie.checker.reportprinter.BasicReportPrinter;
import bixie.checker.reportprinter.HtmlReportPrinter;
import bixie.checker.reportprinter.ReportPrinter;
import bixie.util.Log;

/**
 * @author schaef
 * 
 */
public class Main {

	/**
	 * 
	 */
	public Main() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		bixie.Options options = bixie.Options.v();
		CmdLineParser parser = new CmdLineParser(options);

		if (args.length == 0) {
			parser.printUsage(System.err);
			return;
		}

		try {
			parser.parseArgument(args);

			Main bixie = new Main();
			if (options.getBoogieFile() != null && options.getJarFile() != null) {
				Log.error("Can only take either Java or Boogie input. Not both");
				return;
			} else if (options.getBoogieFile() != null) {
				bixie.run(options.getBoogieFile(), options.getOutputFile());
			} else {
				String cp = options.getClasspath();
				if (cp != null && !cp.contains(options.getJarFile())) {
					cp += File.pathSeparatorChar + options.getJarFile();
				}
				bixie.translateAndRun(options.getJarFile(), cp,
						options.getOutputFile());
			}
		} catch (CmdLineException e) {
			bixie.util.Log.error(e.toString());
			parser.printUsage(System.err);
		} catch (Throwable e) {
			Log.error("bixie failed: "+ e.toString());
		}
	}

	public void run(String input, String output) {
		bixie.Options.v().setOutputFile(output);
		if (input != null && input.endsWith(".bpl")) {
			try {
				ProgramFactory pf = new ProgramFactory(input);
				ReportPrinter jp = runChecker(pf);
				report2File(jp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				bixie.util.Log.error(e.toString());
			}
		} else {
			bixie.util.Log.error("Not a valid Boogie file: " + input);
		}
	}

	protected void report2File(ReportPrinter reportPrinter) {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(bixie.Options.v().getOutputFile()),
				"UTF-8"));) {
			String str = reportPrinter.printSummary();
			if (str!=null && !str.isEmpty()) {
				out.println(str);
				bixie.util.Log.info(str);	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			bixie.util.Log.error(e.toString());
		}
	}
	
	public void translateAndRun(String input, String classpath, String output) {
		ReportPrinter reportPrinter = translateAndRun(input, classpath);
		if (reportPrinter!=null) {
			bixie.Options.v().setOutputFile(output);
			report2File(reportPrinter);
		} else {
			Log.error("Could not generate report.");
		}
	}

	private ReportPrinter reportPrinter = null;
	
	protected ReportPrinter getReportPrinter() {
		if (reportPrinter==null) {
			if (bixie.Options.v().getHtmlDir()!=null) {
				File dir = new File(bixie.Options.v().getHtmlDir());
				if (!dir.exists() || !dir.isDirectory()) {
					try {
						if (!dir.mkdir()) {
							throw new RuntimeException("Failed to generate folder for HTML output at "+dir+". Using command line output instead.");	
						} 
					} catch (Throwable t) {
						Log.error(t);						
						reportPrinter = new BasicReportPrinter();							
						return reportPrinter;
					}
				}
				reportPrinter = new HtmlReportPrinter(dir);
			} else {
				reportPrinter = new BasicReportPrinter();
			}
		}
		return reportPrinter;
	}
	
	public ReportPrinter translateAndRun(String input, String classpath) {
		return translateAndRun(input, classpath, getReportPrinter());
	}

	public ReportPrinter translateAndRun(String input, String classpath,
			ReportPrinter reportPrinter) {
		bixie.util.Log.info("Translating. This may take a while.");
		bixie.translation.Main.setClassPath(classpath);
		ProgramFactory pf = bixie.translation.Main.run(input);
		bixie.util.Log.info("... translation finished.");
		if (pf == null) {
			bixie.util.Log.error("Internal Error: Parsing failed");
			return null;
		}
		ReportPrinter jp = runChecker(pf, reportPrinter);
		return jp;
	}

	public ReportPrinter runChecker(ProgramFactory pf) {
		return runChecker(pf, getReportPrinter());
	}

	public ReportPrinter runChecker(ProgramFactory pf,
			ReportPrinter reportPrinter) {
		bixie.util.Log.info("Checking");

		try {
			ProgramAnalysis.runFullProgramAnalysis(pf, reportPrinter);
		} catch (Exception e) {
			bixie.util.Log.error(e.toString());
		}

		return reportPrinter;
	}

}
