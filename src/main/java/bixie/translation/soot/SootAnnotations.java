/**
 * 
 */
package bixie.translation.soot;

import java.util.LinkedList;
import java.util.List;

import soot.SootField;
import soot.SootMethod;
import soot.tagkit.AnnotationTag;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.SignatureTag;
import soot.tagkit.StringConstantValueTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import util.Log;

/**
 * @author schaef
 *
 */
public class SootAnnotations {

	public enum Annotation {
		NonNull;
	}
	

	public static LinkedList<SootAnnotations.Annotation> parseFieldTags(SootField sf) {
		List<Tag> tags = sf.getTags();
		LinkedList<SootAnnotations.Annotation> annot = null;
		for (Tag t : tags) {
			if (t instanceof VisibilityAnnotationTag) {
				if (annot!=null) {
					throw new RuntimeException("Bug in parseFieldTags");
				}
				annot = parseAnnotations((VisibilityAnnotationTag)t);
			} else if (t instanceof SignatureTag) {
				//TODO: do we want to do something with that?
			} else if (t instanceof StringConstantValueTag) {
				
			} else if (t instanceof IntegerConstantValueTag) {

			} else {
				Log.error("Unimplemented Tag found: "+t.getName());
			}
		}
		if (annot == null) annot = new LinkedList<SootAnnotations.Annotation>();
		return annot;
	}
	
	public static LinkedList<SootAnnotations.Annotation> parseAnnotations(VisibilityAnnotationTag vtag) {
		LinkedList<SootAnnotations.Annotation> annot = new LinkedList<SootAnnotations.Annotation>();
		if (vtag == null || vtag.getAnnotations()==null) {
			//no annotation
			return annot;
		}
		for (AnnotationTag at : vtag.getAnnotations()) {
			addTagToList(annot, at);
		}
		return annot;
	}
	
	public static LinkedList<LinkedList<SootAnnotations.Annotation>> parseParameterAnnotations(SootMethod m) {
		
		LinkedList<LinkedList<SootAnnotations.Annotation>> pannot = new LinkedList<LinkedList<SootAnnotations.Annotation>>();
		
		for (Tag t : m.getTags()) {
			if (t instanceof VisibilityParameterAnnotationTag) {
				VisibilityParameterAnnotationTag tag = (VisibilityParameterAnnotationTag)t;
				if (tag.getVisibilityAnnotations().size() != m.getParameterCount()) {
					throw new RuntimeException("number of tags does not match number of params ... I did not understand this part!");
				}				
				for (VisibilityAnnotationTag va : tag.getVisibilityAnnotations()) {					
					pannot.add(parseAnnotations(va));
				}
			} 
		}
		return pannot;

	}
	
	private static void addTagToList(LinkedList<SootAnnotations.Annotation> annot, AnnotationTag at) {
		if (at!=null) {
			if (at.getType().contains("Lorg/eclipse/jdt/annotation/NonNull")) {
				Log.info("@NonNull found" );
				annot.add(Annotation.NonNull);
			} else if (at.getType().contains("Lsun/reflect/CallerSensitive")) {
				Log.debug("Not sure what to do with this tag. Ignoring it: "+at.getType());
			} else if (at.getType().contains("Ljava/lang/SafeVarargs")) {
				//TODO: we could actually check that!
				Log.debug("Not sure what to do with this tag. Ignoring it: "+at.getType());
				
			} else {
//				Log.debug("Unhandled Annotation "+at);
			}
		} else {
			//no annotation
		}
	}
	
	
}
