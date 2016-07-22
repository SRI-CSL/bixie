
package bixie.boogie.ast;

import java.util.LinkedList;
import java.util.List;

import bixie.boogie.ast.location.BoogieLocation;
import bixie.boogie.ast.location.ILocation;

public abstract class ASTNode {

	BoogieLocation location = null;

	public ASTNode(ILocation location) {

		if (location instanceof BoogieLocation) {
			this.location = ((BoogieLocation) location);
		}
	}

	public List<Object> getChildren() {
		return new LinkedList<Object>();
	}

	// public Payload getPayload() {
	// if (payload == null) {
	// //payload = new Payload(null, this.getClass().getName().toUpperCase());
	// }
	// return null;
	// }

	public ILocation getLocation() {
		return this.location;
	}
}
