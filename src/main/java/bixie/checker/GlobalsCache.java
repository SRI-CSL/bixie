/**
 * 
 */
package bixie.checker;

import bixie.boogie.ProgramFactory;

/**
 * @author schaef
 *
 */
public class GlobalsCache {
	
	private static GlobalsCache instance = null;
	
	public static GlobalsCache v() {
		if (instance==null) {
			instance = new GlobalsCache();
		}
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;		
	}
	
	private GlobalsCache() {
		
	}
	
	/**
	 * @return the programFactory
	 */
	public ProgramFactory getProgramFactory() {
		return programFactory;
	}

	/**
	 * @param programFactory the programFactory to set
	 */
	public void setProgramFactory(ProgramFactory programFactory) {
		this.programFactory = programFactory;
	}

	private ProgramFactory programFactory;
	
}
