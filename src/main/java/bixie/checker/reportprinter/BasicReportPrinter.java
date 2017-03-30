/**
 * 
 */
package bixie.checker.reportprinter;

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
public class BasicReportPrinter implements ReportPrinter {

	/* Sorted report is a map from severity (Integer) to
	 * a map from file name to list of reports (list of integer). 
	 * This data structure is used to ensure that the printed report
	 * is always deterministic and not dependent on the order in which
	 * reports come in. 
	*/
	Map<Integer, Map<String, List<List<Integer>>>> sortedReport = new HashMap<Integer, Map<String, List<List<Integer>>>>();
	
	int cirtical, errorhandling, unreachable;
	
	/**
	 * 
	 */
	public BasicReportPrinter() {
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
		for (String fname : knownFiles) {
			//TODO: don't hard code the keys.
			sb.append("In File: " + fname+"\n");
			cirtical+= printReportForFileBySeverity(sb, fname, 0, "** Critical **");
			if (bixie.Options.v().serverityLimit>0){
				unreachable += printReportForFileBySeverity(sb, fname, 1, " - Unreachable -");
			}
		}
		
		
		sb.append("Summary: fwd=");
		sb.append(cirtical);
		sb.append("\tbwd=");
		sb.append(unreachable);
		sb.append("\n");
		
		return sb.toString();
	}
	
	private int printReportForFileBySeverity(StringBuilder sb, String fname, Integer severity, String severityText) {
		int lineCount = 0;
		if (sortedReport.containsKey(severity) && sortedReport.get(severity).containsKey(fname)) {
			sb.append(severityText+"\n");			
			for (List<Integer> lines : sortedReport.get(severity).get(fname) ) {
				lineCount++;
				sb.append("\tLines: ");
				String comma = "";
				for (Integer i : lines) {
					sb.append(comma);
					sb.append(i);
					comma = ", ";
				}
				sb.append("\n");
			}	
		}
		return lineCount;
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
