/**
 * 
 */
package bixie.translation.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import bixie.translation.Options;
import bixie.util.Log;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.toolkits.pointer.RWSet;
import soot.jimple.toolkits.thread.AbstractRuntimeThread;
import soot.jimple.toolkits.thread.mhp.MhpTester;
import soot.jimple.toolkits.thread.mhp.MhpTransformer;

/**
 * @author schaef
 * Singleton class that collects information about which SootFields that are modified within a method
 * may be modified by other threads.
 */
public class MhpInfo {

	private HashMap<SootMethod, Set<SootField>> sharedFields = new HashMap<SootMethod, Set<SootField>>();
	
	private static MhpInfo instance = null;
	
	public static MhpInfo v() {
		if (instance==null) {
			if (!Options.v().useSoundThreads()) {
				throw new RuntimeException("You can only use MhpInfo if the SoundThread option is enabled");
			}
			
			instance = new MhpInfo();
		}
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;	
	}
	
	
	/**
	 * Returns the set of fields that are modified in "m" 
	 * and may be modified by other threads at the same time.
	 * @param m
	 * @return
	 */
	public Set<SootField> getSharedFields(SootMethod m) {
		if (this.sharedFields.containsKey(m)) {
			return this.sharedFields.get(m);
		}
		return new HashSet<SootField>();
	}
	
	/**
	 * 
	 */
	private MhpInfo() {
				
		MhpTester mhpt = MhpTransformer.v().getMhpTester();
		
		List<HashSet<SootField>> read_sets = new LinkedList<HashSet<SootField>>();
		List<HashSet<SootField>> write_sets = new LinkedList<HashSet<SootField>>();
		
		List<AbstractRuntimeThread> arts = mhpt.getThreads();
		if (arts == null) {
			return;
		}
		
		for (AbstractRuntimeThread art : arts) {
			
			HashSet<SootField> read_fields = new HashSet<SootField>();
			HashSet<SootField> write_fields = new HashSet<SootField>();
			for (int i=0;  i<art.methodCount(); i++) {
				Object o = art.getMethod(i);
				if (o instanceof SootMethod) {					
					SootMethod m = (SootMethod)o;					
					RWSet read_set = Scene.v().getSideEffectAnalysis().nonTransitiveReadSet(m);
					RWSet write_set = Scene.v().getSideEffectAnalysis().nonTransitiveWriteSet(m);
					read_fields.addAll(collectFields(read_set));
					write_fields.addAll(collectFields(write_set));
				} else {
					throw new RuntimeException("Not implemented "+o.getClass().toString());
				}
			}
			read_sets.add(read_fields);
			write_sets.add(write_fields);
		}
		
		for (int i=0; i<read_sets.size(); i++) {
			HashSet<SootField> shared_vars = new HashSet<SootField>();
			for (int j=0; j<read_sets.size(); j++) {
				//if (i==j) continue;
				HashSet<SootField> rw = new HashSet<SootField>(read_sets.get(i)); 
				rw.retainAll(write_sets.get(j));
				if (!rw.isEmpty()) {
					shared_vars.addAll(rw);
				}
				HashSet<SootField> ww = new HashSet<SootField>(write_sets.get(i)); 
				ww.retainAll(write_sets.get(j));
				if (!ww.isEmpty()) {
					shared_vars.addAll(ww);
				}				
			}	
			
			AbstractRuntimeThread art = arts.get(i); 
			for (int j=0;  j< art.methodCount(); j++) {
				Object o =  art.getMethod(j);
				
				if (o instanceof SootMethod) {
					SootMethod m = (SootMethod)o;
					if (!this.sharedFields.containsKey(m)) {
						this.sharedFields.put(m, new HashSet<SootField>());
					}
					this.sharedFields.get(m).addAll(shared_vars);
				} else {
					throw new RuntimeException("Not implemented.");
				}
			}
			
		}

	}
	
	private Set<SootField> collectFields(RWSet rwset) {
		HashSet<SootField> fields = new HashSet<SootField>();
		if (rwset!=null) {
			for (Object obj : rwset.getGlobals()) {
				if (obj instanceof SootField) {
					SootField sf = (SootField)obj;
					fields.add(sf);
				} else if (obj instanceof String) {
					Log.error("String case not implemented "+((String)obj));					
				} else {
					throw new RuntimeException("case not implemented "+obj.getClass().toString());
				}
			}
			for (Object obj : rwset.getFields()) {
				if (obj instanceof SootField) {
					SootField sf = (SootField)obj;
					fields.add(sf);
				} else if (obj instanceof String) {
					Log.error("String case not implemented "+((String)obj));
				} else {
					throw new RuntimeException("case not implemented "+obj.getClass().toString());
				}				
			}
		}
		return fields;
	}

	/**
	 * Debug print the set of shared variables for each SootMethod.
	 */
	public void printParallelFields() {
		StringBuilder sb = new StringBuilder();
		for (Entry<SootMethod, Set<SootField>> entry : this.sharedFields.entrySet()) {
			sb.append("Method " + entry.getKey().getBytecodeSignature()+" shares \n the following fields with other threads:\n");
			for (SootField sf : entry.getValue()) {
				sb.append("\t");
				sb.append(sf.getName());
				sb.append("\n");
			}
		}
		Log.error(sb.toString());
	}
}
