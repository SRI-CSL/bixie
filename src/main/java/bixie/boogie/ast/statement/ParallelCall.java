/**
 * 
 */
package bixie.boogie.ast.statement;

import java.util.List;

import bixie.boogie.ast.expression.Expression;
import bixie.boogie.ast.location.ILocation;

/**
 * @author schaef
 *
 */
public class ParallelCall extends Statement {

	protected Expression[] functionApplications;
	
	public ParallelCall(ILocation loc, Expression[] funapp) {
		super(loc);
		this.functionApplications = funapp;
	}
	
	public Expression[] getFunctionApplication() {
		return this.functionApplications;
	}

	public List<Object> getChildren() {
		List<Object> children = super.getChildren();
		for (Expression funapp : functionApplications) {
			children.add(funapp);
		}
		return children;
	}
	
}
