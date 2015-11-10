package bixie.checker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import bixie.Options;
import bixie.boogie.ProgramFactory;
import bixie.boogie.controlflow.AbstractControlFlowFactory;
import bixie.boogie.controlflow.CfgProcedure;
import bixie.boogie.controlflow.DefaultControlFlowFactory;
import bixie.boogie.typechecker.TypeChecker;
import bixie.checker.inconsistency_checker.AbstractChecker;
import bixie.checker.inconsistency_checker.CdcChecker;
import bixie.checker.inconsistency_checker.CombinedChecker;
import bixie.checker.inconsistency_checker.GreedyCfgChecker;
import bixie.checker.report.Report;
import bixie.checker.reportprinter.ReportPrinter;
import bixie.util.Log;

/**
 * @author schaef
 * 
 */
public class ProgramAnalysis {

	private static long timeouts = 0L;

	public static void runFullProgramAnalysis(ProgramFactory pf,
			ReportPrinter rp) {

		GlobalsCache.v().setProgramFactory(pf);
		TypeChecker tc = new TypeChecker(pf.getASTRoot());
		// build the control-flow graphs
		DefaultControlFlowFactory cff = new DefaultControlFlowFactory(
				pf.getASTRoot(), tc);

		Long checkTime = System.currentTimeMillis();
		

		final long totalProcedures = (long)cff.getProcedureCFGs().size();
		long counter = 0L;
		
		
		for (CfgProcedure p : cff.getProcedureCFGs()) {
			counter += 1;
			updateProgress((double)counter/(double)totalProcedures);
			
			if (p.getRootNode() == null)
				continue;

			try {
				Report report = analyzeProcedure(p, cff);

				if (report != null ) {
					report.runFaultLocalization(); // do the interpolation based fault
										// localization here to avoid timeouts.
										// if (Options.v().stopTime) {
			
					rp.printReport(report);
				}

			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				Log.debug(sw.toString());
				break;
			}
		}

		checkTime = System.currentTimeMillis() - checkTime;
		
		Log.info("Total time: " + ((float) checkTime) / 1000f + "s");
		
		Log.info("Total Timeouts after " + bixie.Options.v().getTimeout() + "sec: "
				+ timeouts + " of "+ cff.getProcedureCFGs().size());

		GlobalsCache.resetInstance();		
	}
	
	  private static void updateProgress(double progressPercentage) {
		    final int width = 50; // progress bar width in chars

		    System.out.print("\r[");
		    int i = 0;
		    for (; i <= (int)(progressPercentage*width); i++) {
		      System.out.print(".");
		    }
		    for (; i < width; i++) {
		      System.out.print(" ");
		    }
		    System.out.print("]");
		  }

	
	
	private static AbstractChecker getChecker(AbstractControlFlowFactory cff,
			CfgProcedure p) {
		AbstractChecker checker = null;

		switch (Options.v().getSelectedChecker()) {
		case 1: {
			checker = new GreedyCfgChecker(cff, p);
			break;
		}
		case 2: {
			checker = new CdcChecker(cff, p);
			break;
		}
		default: {
			checker = new CombinedChecker(cff, p);
			break;
		}
		}
		return checker;
	}

	
	private static Report analyzeProcedure(CfgProcedure p,
			AbstractControlFlowFactory cff) {
		if (bixie.Options.v().getDebugMode()) {
			Log.info("Checking: " + p.getProcedureName());
		}		
		// create an executor to kill the verification with a timeout if
		// necessary
		ExecutorService executor = Executors.newSingleThreadExecutor();

		AbstractChecker checkerThread = getChecker(cff, p);
		
		final Future<?> future = executor.submit(checkerThread);

		boolean exception = false;

		try {
			// start thread and wait xx seconds. If timeout is set to 0, wait
			// until it terminates.
			if (bixie.Options.v().getTimeout() > 0) {
				future.get(bixie.Options.v().getTimeout(), TimeUnit.SECONDS);
			} else {
				future.get();
			}
			Log.debug("Finished method " + p.getProcedureName());
		} catch (TimeoutException e) {
			// set timeout to method info
			// methodInfo.setTimeout(true);
			timeouts++;
			Log.debug("Timeout reached for method " + p.getProcedureName());
			exception = true;
		} catch (OutOfMemoryError e) {
			Log.info("Out of memory for " + p.getProcedureName());
			exception = true;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Log.debug(sw.toString());
			exception = true;
		} finally {
			// cancel thread if not done
			if (!future.isDone()) {
				future.cancel(true);
			}

			// shutdown prover
			checkerThread.shutDownProver();

			// shutdown executor
			executor.shutdown();

		}

		Report report = checkerThread.getReport();
		if (exception)
			return null;
		return report;
	}

}
