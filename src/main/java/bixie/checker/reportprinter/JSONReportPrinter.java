package bixie.checker.reportprinter;

import bixie.checker.report.Report;
import bixie.util.JSONTraceItem;
import bixie.util.JSONbug;
import bixie.util.Log;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.*;

public class JSONReportPrinter implements ReportPrinter {

	/*
	 * Sorted report is a map from severity (Integer) to a map from file name to
	 * list of reports (list of integer). This data structure is used to ensure
	 * that the printed report is always deterministic and not dependent on the
	 * order in which reports come in.
	 */
	private final Map<Integer, Map<String, List<List<Integer>>>> sortedReport = new HashMap<Integer, Map<String, List<List<Integer>>>>();

	int cirtical, errorhandling, unreachable;

	private final File reportDir, jsonFile;

	private final List<JSONbug> bugs;

	/**
	 *
	 */
	public JSONReportPrinter(File outDir) {
		reportDir = outDir;
		jsonFile = new File(reportDir.getAbsolutePath() + File.separator + "bixie.JSON");
		cirtical = 0;
		errorhandling = 0;
		unreachable = 0;
		bugs = new ArrayList<JSONbug>();
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
		return makeJsonString(bugs);
	}

	protected void consumeReport(Report r) {
		for (Entry<Integer, List<Report.FaultExplanation>> entry : r.getReports().entrySet()) {
			for (Report.FaultExplanation fe : entry.getValue()) {
				JSONbug bug = new JSONbug();
				bug.setBug_class("Inconsistent code found by Bixie");
				bug.setSeverity(entry.getKey().toString());
				bug.setFile(fe.fileName);
				List<JSONTraceItem> traceList = new LinkedList<JSONTraceItem>();
				List<Integer> nums = new LinkedList<>();
				//for (SourceLocation location : fe.locations) {nums.add(location.StartLine); System.console().printf(makeJsonString(location));}
				nums.addAll(fe.allLines);
				Collections.sort(nums);
				for (Integer line : fe.allLines) {
					JSONTraceItem tempTrace = new JSONTraceItem();
					tempTrace.setLine_number(line);
					tempTrace.setFilename(fe.fileName);
					List<Integer> conflicts = new LinkedList<>();
					conflicts.addAll(nums);
					conflicts.remove(line);
					tempTrace.setDescription("\t<--- Appears to violate an assumption by lines: " + conflicts);
					traceList.add(tempTrace);
				}
				if (nums.size() > 5) {
					bug.setQualifier("Inconsistent code found on lines: " + nums.subList(0, 5).toString().replace("]", ", ...]"));
				} else {
					bug.setQualifier("Inconsistent code found on lines: " + nums);
				}
				bug.setBug_trace(traceList);
				//TODO not finished here, ask DSN about how he wants it translated
				bugs.add(bug);
			}
		}
	}

	// https://stackoverflow.com/questions/15786129/converting-java-objects-to-json-with-jackson
	public static <T> String makeJsonString(T theObject) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(theObject);
		} catch (Exception e) {
			Log.error(theObject);
			return "Exception trying to get json for object: " + theObject;
		}
	}

	@Override
	public int countReports() {
		return this.cirtical + this.errorhandling + this.unreachable;
	}
}
