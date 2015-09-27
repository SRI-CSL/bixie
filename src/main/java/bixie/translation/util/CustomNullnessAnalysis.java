/**
 * 
 */
package bixie.translation.util;

import soot.Value;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.toolkits.annotation.nullcheck.NullnessAnalysis;
import soot.toolkits.graph.UnitGraph;

/**
 * @author schaef
 *
 */
public class CustomNullnessAnalysis extends NullnessAnalysis {

	public CustomNullnessAnalysis(UnitGraph graph) {
		super(graph);
	}

	@Override
	protected boolean isAlwaysNonNull(Value v) {	
		if (v instanceof CaughtExceptionRef) {
			return true;
		}
		return false;
	}
	
	
}
