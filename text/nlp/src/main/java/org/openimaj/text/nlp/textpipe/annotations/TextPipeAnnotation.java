package org.openimaj.text.nlp.textpipe.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Classes that extend {@link TextPipeAnnotation} are annotations and
 * annotatable. Annotations are a HashMap keyed on the class of the annotation,
 * adding a new annotation T extends {@link TextPipeAnnotation} will be added to
 * the the list of Annotations of that class already added.
 * 
 * @author laurence
 * 
 */
public abstract class TextPipeAnnotation {

	private HashMap<Class<? extends TextPipeAnnotation>, List<TextPipeAnnotation>> annotations;

	public TextPipeAnnotation() {
		annotations = new HashMap<Class<? extends TextPipeAnnotation>, List<TextPipeAnnotation>>();
	}

	public <T extends TextPipeAnnotation> void addAnnotation(T annotation) {
		if (annotations.containsKey(annotation.getClass())) {
			annotations.get(annotation.getClass()).add(annotation);
		} else {
			ArrayList<TextPipeAnnotation> annos = new ArrayList<TextPipeAnnotation>();
			annos.add(annotation);
			annotations.put(annotation.getClass(), annos);
		}
	}

	public <T extends TextPipeAnnotation> void addAllAnnotations(
			Collection<T> annotationCollection) {
		if (annotationCollection != null && annotationCollection.size() > 0) {
			Class key = annotationCollection.iterator().next().getClass();
			if (annotations.containsKey(key)) {
				annotations.get(key).addAll(annotationCollection);
			} else {
				ArrayList<TextPipeAnnotation> annos = new ArrayList<TextPipeAnnotation>();
				annos.addAll(annotationCollection);
				annotations.put(key, annos);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends TextPipeAnnotation> List<T> getAnnotationsFor(Class<T> key) {
		if (annotations.containsKey(key)) {
			return (List<T>) annotations.get(key);
		}
		return null;
	}

	public Set<Class<? extends TextPipeAnnotation>> getAnnotationKeyList() {
		return annotations.keySet();
	}

}
