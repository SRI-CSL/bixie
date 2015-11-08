/**
 * 
 */
package bixie.checker.reportprinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

import bixie.checker.report.Report;
import bixie.checker.report.Report.FaultExplanation;
import bixie.util.Log;

/**
 * @author schaef
 *
 */
public class HtmlReportPrinter implements ReportPrinter {

	/* Sorted report is a map from severity (Integer) to
	 * a map from file name to list of reports (list of integer). 
	 * This data structure is used to ensure that the printed report
	 * is always deterministic and not dependent on the order in which
	 * reports come in. 
	*/
	private final  Map<Integer, Map<String, List<List<Integer>>>> sortedReport = new HashMap<Integer, Map<String, List<List<Integer>>>>();
	
	int cirtical, errorhandling, unreachable;
	
	private final File mainHtml;
	
	/**
	 * 
	 */
	public HtmlReportPrinter(File outDir) {
		mainHtml = new File(outDir.getAbsolutePath() + File.separator + "main.html");
		cirtical = 0;
		errorhandling = 0;
		unreachable = 0;
	}

	/* (non-Javadoc)
	 * @see bixie.checker.reportprinter.ReportPrinter#printReport(bixie.checker.report.Report)
	 */
	@Override
	public void printReport(Report r) {
		consumeReport(r);
		String s = r.toString();
		if (s!=null && !s.isEmpty()) Log.info(r.toString());
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
		StringBuilder sb = new StringBuilder();
		appendHtmlPrefix(sb);
		for (String fname : knownFiles) {

//			cirtical+= printReportForFileBySeverity(sb, fname, 0, "** Critical **");
//			unreachable += printReportForFileBySeverity(sb, fname, 1, " - Unreachable -");

			
			
			//critical lines
			List<List<Integer>> lines = getSortedLineNumbers(fname, 0);
			for (List<Integer> l : lines ) {
				sb.append("<tr>\n");
				sb.append("<td>\n");
				sb.append("<a href=\"file://"+fname+"\">"+fname+"</a>\n");
				sb.append("<td>\n");
				
				sb.append("<td>\n");
				sb.append("bug\n");
				sb.append("<td>\n");
				
				sb.append("<td>\n");
				String comma = "";
				for (int i : l) {
					sb.append(comma);
					sb.append(i);
					comma = ", ";
				}
				sb.append("\n");
				sb.append("<td>\n");
				sb.append("</tr>\n");
			}
			
			//unreachable lines
			lines = getSortedLineNumbers(fname, 1);
			for (List<Integer> l : lines ) {
				sb.append("<tr>\n");
				sb.append("<td>\n");
				sb.append("<a href=\"file://"+fname+"\">"+fname+"</a>\n");
				sb.append("<td>\n");
				
				sb.append("<td>\n");
				sb.append("warning\n");
				sb.append("<td>\n");
				
				sb.append("<td>\n");
				String comma = "";
				for (int i : l) {
					sb.append(comma);
					sb.append(i);
					comma = ", ";
				}
				sb.append("\n");
				sb.append("<td>\n");
				sb.append("</tr>\n");
			}
		}
		
		appendHtmlSuffix(sb);
		
		try (PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(new FileOutputStream(mainHtml), 
						StandardCharsets.UTF_8), true);) {
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
		sb.append(mainHtml.getAbsolutePath());
		sb.append("\n");
		return sb.toString();
	}
	
	private void appendHtmlPrefix(StringBuilder sb) {
		sb.append("<!DOCTYPE html>\n");
		sb.append("<html>\n");
		sb.append("<body>\n");
		sb.append("<table style=\"width:100%\">\n");
	}

	private void appendHtmlSuffix(StringBuilder sb) {
		sb.append("</table>\n");
		sb.append("</body>\n");
		sb.append("</html>\n");
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
				//get the sorted list of line numbers for this report.
				LinkedHashSet<Integer> lines = new LinkedHashSet<Integer>();
				for (SourceLocation line : fe.locations) {
					lines.add(line.StartLine);
				}
				LinkedList<Integer> sortedLines = new LinkedList<Integer>(lines);
				Collections.sort(sortedLines);
				//get the file name for this report.
				String fname = fe.fileName;
				
				if (!sortedLinesPerFile.containsKey(fname)) {
					sortedLinesPerFile.put(fname, new LinkedList<List<Integer>>());
				}
				sortedLinesPerFile.get(fname).add(sortedLines);
			}
			
		}
	}
	
	/**
	 * Sort a list of sorted lists of integers. Given two lists l1 and l2,
	 * l1 is before l2, if the first integer that is different in l1 and l2
	 * is smaller in l1. If one element is a subset of the other, then the
	 * shorter list comes first.
	 * @param list
	 */
	private void sortListOfInts(List<List<Integer>> list) {
		Collections.sort(list, new Comparator<List<Integer>>(){
	        @Override
	        public int compare(List<Integer> l1, List<Integer> l2) {
	        	Iterator<Integer> i1 = l1.iterator();
	        	Iterator<Integer> i2 = l2.iterator();
	        	while (i1.hasNext() && i2.hasNext()) {
	        		int v1 = i1.next();
	        		int v2 = i2.next();
	        		if (v1<v2) return -1;
	        		if (v1>v2) return 1;
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
