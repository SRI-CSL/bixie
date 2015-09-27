/**
 * 
 */
package bixie.checker.reportprinter;

import java.util.List;
import java.util.Map;

import bixie.checker.report.Report;

/**
 * @author schaef
 *
 */
public interface ReportPrinter {
	
	public void printReport(Report r);

	public String printSummary();
	
	public int countReports();
	
	public Map<Integer, Map<String, List<List<Integer>>>> getSortedReports();
	
}
