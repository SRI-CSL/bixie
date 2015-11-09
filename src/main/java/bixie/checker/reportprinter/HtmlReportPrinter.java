/**
 * 
 */
package bixie.checker.reportprinter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import bixie.checker.report.Report;
import bixie.checker.report.Report.FaultExplanation;
import bixie.util.Log;

/**
 * @author schaef
 *
 */
public class HtmlReportPrinter implements ReportPrinter {

	/*
	 * Sorted report is a map from severity (Integer) to a map from file name to
	 * list of reports (list of integer). This data structure is used to ensure
	 * that the printed report is always deterministic and not dependent on the
	 * order in which reports come in.
	 */
	private final Map<Integer, Map<String, List<List<Integer>>>> sortedReport = new HashMap<Integer, Map<String, List<List<Integer>>>>();

	int cirtical, errorhandling, unreachable, snippetCounter;

	private final File reportDir, indexHtml;

	/**
	 * 
	 */
	public HtmlReportPrinter(File outDir) {
		reportDir = outDir;
		indexHtml = new File(reportDir.getAbsolutePath() + File.separator + "index.html");
		cirtical = 0;
		errorhandling = 0;
		unreachable = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bixie.checker.reportprinter.ReportPrinter#printReport(bixie.checker.
	 * report.Report)
	 */
	@Override
	public void printReport(Report r) {
		consumeReport(r);
		String s = r.toString();
		if (s != null && !s.isEmpty())
			Log.info(r.toString());
	}

	public Map<Integer, Map<String, List<List<Integer>>>> getSortedReports() {
		return this.sortedReport;
	}

	public String printSummary() {
		SortedSet<String> knownFiles = new TreeSet<String>();
		// now sort the list of lists of lines to ensure that
		// printing is always deterministic.
		for (Map<String, List<List<Integer>>> perFile : sortedReport.values()) {
			for (List<List<Integer>> lines : perFile.values()) {
				sortListOfInts(lines);
				knownFiles.addAll(perFile.keySet());
			}
		}

		StringBuilder bodyText = new StringBuilder();
		StringBuilder jsText = new StringBuilder();

		snippetCounter = 0;
		for (String fname : knownFiles) {

			bodyText.append("<h4>In file:</h4>");
			bodyText.append("<p>");
			bodyText.append("<a href=\"file://" + fname + "\">" + fname + "</a>\n");
			bodyText.append("</p>");
			bodyText.append("<h6>Critical warnings</h6>\n");
			int count;
			count = createSnippet(fname, bodyText, jsText, 0);
			if (count == 0) {
				bodyText.append("<p>Nothing found</p>\n");
			}
			cirtical += count;
			bodyText.append("<h6>Unreachability warnings</h6>\n");
			count = createSnippet(fname, bodyText, jsText, 1);
			if (count == 0) {
				bodyText.append("<p>Nothing found</p>\n");
			}
			unreachable += count;

		}

		// first extract the template from the Jar
		extractHtmlBoilerplate();
		StringBuilder prefix = new StringBuilder();
		StringBuilder middle = new StringBuilder();
		StringBuilder suffix = new StringBuilder();
		readIndexHtmlStub(prefix, middle, suffix);

		StringBuilder sb = new StringBuilder();
		sb.append(prefix.toString());
		sb.append(bodyText.toString());
		sb.append(middle.toString());
		sb.append(jsText.toString());
		sb.append(suffix.toString());

		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(indexHtml), StandardCharsets.UTF_8), true);) {
			pw.println(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		sb = new StringBuilder();
		sb.append("Summary: critical inconsistencies =");
		sb.append(cirtical);
		sb.append("\tunreachable code=");
		sb.append(unreachable);
		sb.append("\n");
		sb.append("Report written to ");
		sb.append(indexHtml.getAbsolutePath());
		sb.append("\n");
		return sb.toString();
	}

	private static final Integer snippetPadding = 2;

	private int createSnippet(String fname, StringBuilder bodyText, StringBuilder jsText, int severity) {
		List<List<Integer>> lines = getSortedLineNumbers(fname, severity);
		for (List<Integer> l : lines) {

			try {
				List<Integer> sorted = new LinkedList<Integer>(l);
				Collections.sort(sorted);
				String snippet = extractSnippet(fname, sorted);
				bodyText.append("<p><textarea class=\"snippet\">\n");
				bodyText.append(snippet);
				bodyText.append("</textarea></p>\n");
				int min = l.get(0) - snippetPadding;
				for (Integer lnumber : l) {
					// editor[0].addLineClass(3, 'background', 'line-warning');
					jsText.append("editor[");
					jsText.append(snippetCounter);
					jsText.append("].addLineClass(");
					jsText.append(lnumber - min);
					jsText.append(", 'background', 'line-warning');\n");

					// editor[0].setOption("firstLineNumber",20);
					jsText.append("editor[");
					jsText.append(snippetCounter);
					jsText.append("].setOption('firstLineNumber', ");
					jsText.append(min);
					jsText.append(");\n");
				}
				snippetCounter++;
			} catch (IOException e) {
				bodyText.append("<p>\nLines ");
				String comma = "";
				for (int i : l) {
					bodyText.append(comma);
					bodyText.append(i);
					comma = ", ";
				}
				bodyText.append("\n");
				bodyText.append("</p>\n");
			}
		}
		return lines.size();
	}

	/**
	 * Reads the stub for index.html and splits it into three parts: The header
	 * and boiler plate before the actual report goes into prefix. The boiler
	 * plate after the report that sets up codemirror goes into middle. The
	 * closing tags, etc goes into suffix.
	 * 
	 * @param prefix
	 * @param middle
	 * @param suffix
	 */
	private void readIndexHtmlStub(StringBuilder prefix, StringBuilder middle, StringBuilder suffix) {
		StringBuilder sb = prefix;
		try (FileInputStream fis = new FileInputStream(indexHtml);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains("<!-- GENERATED BODY HERE -->")) {
					sb = middle;
				} else if (line.contains("//GENERATED JS HERE")) {
					sb = suffix;
				} else {
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String extractSnippet(String sourceFile, List<Integer> sortedLineNumbers) throws IOException {
		StringBuilder sb = new StringBuilder();
		File f = new File(sourceFile);
		if (!f.exists()) {
			throw new IOException("File not found " + f.getAbsolutePath());
		}

		int min = Math.max(1, sortedLineNumbers.get(0) - snippetPadding);
		int max = sortedLineNumbers.get(sortedLineNumbers.size() - 1) + snippetPadding;

		try (FileInputStream fis = new FileInputStream(f);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));) {
			String line = null;
			int counter = 0;
			while ((line = br.readLine()) != null) {
				counter++;
				if (counter >= min && counter <= max) {
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * extracts the css and js files for the report from the zip inside the jar
	 * and copies them into the report folder.
	 */
	private void extractHtmlBoilerplate() {
		try (InputStream is = getClass().getResourceAsStream("/report_html.pack");
				ZipInputStream zis = new ZipInputStream(is);) {
			ZipEntry entry;

			try {
				while ((entry = zis.getNextEntry()) != null) {
					File target = new File(reportDir.getAbsolutePath() + File.separator + entry.getName());
					// don't overwrite stuff other than main.html
					if (!target.exists() || target.getAbsolutePath().equals(indexHtml.getAbsolutePath())) {
						if (entry.isDirectory()) {
							if (!target.mkdir()) {
								throw new RuntimeException("Failed to create dir: " + target.getAbsolutePath());
							}
						} else {
							try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));) {
								byte[] bytesIn = new byte[1024];
								int read = 0;
								while ((read = zis.read(bytesIn)) != -1) {
									bos.write(bytesIn, 0, read);
								}

							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
		}
	}

	private List<List<Integer>> getSortedLineNumbers(String fname, Integer severity) {
		List<List<Integer>> ret = new LinkedList<List<Integer>>();
		if (sortedReport.containsKey(severity) && sortedReport.get(severity).containsKey(fname)) {
			ret.addAll(sortedReport.get(severity).get(fname));
		}
		return ret;
	}

	protected void consumeReport(Report r) {
		for (Entry<Integer, List<FaultExplanation>> entry : r.getReports().entrySet()) {
			if (!sortedReport.containsKey(entry.getKey())) {
				sortedReport.put(entry.getKey(), new HashMap<String, List<List<Integer>>>());
			}
			Map<String, List<List<Integer>>> sortedLinesPerFile = sortedReport.get(entry.getKey());
			for (FaultExplanation fe : entry.getValue()) {
				if (fe.locations.isEmpty()) {
					continue;
				}
				// get the sorted list of line numbers for this report.
				LinkedHashSet<Integer> lines = new LinkedHashSet<Integer>();
				for (SourceLocation line : fe.locations) {
					lines.add(line.StartLine);
				}
				LinkedList<Integer> sortedLines = new LinkedList<Integer>(lines);
				Collections.sort(sortedLines);
				// get the file name for this report.
				String fname = fe.fileName;

				if (!sortedLinesPerFile.containsKey(fname)) {
					sortedLinesPerFile.put(fname, new LinkedList<List<Integer>>());
				}
				sortedLinesPerFile.get(fname).add(sortedLines);
			}

		}
	}

	/**
	 * Sort a list of sorted lists of integers. Given two lists l1 and l2, l1 is
	 * before l2, if the first integer that is different in l1 and l2 is smaller
	 * in l1. If one element is a subset of the other, then the shorter list
	 * comes first.
	 * 
	 * @param list
	 */
	private void sortListOfInts(List<List<Integer>> list) {
		Collections.sort(list, new Comparator<List<Integer>>() {
			@Override
			public int compare(List<Integer> l1, List<Integer> l2) {
				Iterator<Integer> i1 = l1.iterator();
				Iterator<Integer> i2 = l2.iterator();
				while (i1.hasNext() && i2.hasNext()) {
					int v1 = i1.next();
					int v2 = i2.next();
					if (v1 < v2)
						return -1;
					if (v1 > v2)
						return 1;
				}
				if (i1.hasNext()) {
					return 1;
				}
				if (i2.hasNext()) {
					return -1;
				}
				return 0;
			}
		});
	}

	@Override
	public int countReports() {
		return this.cirtical + this.errorhandling + this.unreachable;
	}

}
