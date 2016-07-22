
package bixie.boogie.typechecker;

import bixie.boogie.ast.declaration.Declaration;
import bixie.boogie.type.BoogieType;

public class VariableInfo {
	private final boolean rigid;
	private final Declaration declaration;
	private final String name;
	private final BoogieType type;

	public boolean isRigid() {
		return rigid;
	}

	public String getName() {
		return name;
	}

	public BoogieType getType() {
		return type;
	}

	public Declaration getDeclaration() {
		return declaration;
	}

	public VariableInfo(boolean rigid, Declaration declaration, String name,
			BoogieType type) {
		super();
		this.rigid = rigid;
		this.declaration = declaration;
		this.name = name;
		this.type = type;
	}

	public String toString() {
		return name + ":" + type;
	}
}
