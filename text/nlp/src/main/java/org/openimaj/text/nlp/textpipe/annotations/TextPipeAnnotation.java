package org.openimaj.text.nlp.textpipe.annotations;

import java.util.HashMap;

/**
 * Classes that extend {@link TextPipeAnnotation} are annotations and
 * annotatable. Annotations are a set based on the class of the annotation,
 * adding a new annotation T extends {@link TextPipeAnnotation} will replace
 * older annotations of that class.
 * 
 * @author laurence
 * 
 */
public abstract class TextPipeAnnotation {

	private HashMap<Class<? extends TextPipeAnnotation>, TextPipeAnnotation> annotations;

	public TextPipeAnnotation() {
		annotations = new HashMap<Class<? extends TextPipeAnnotation>, TextPipeAnnotation>();
	}

	public <T extends TextPipeAnnotation> void addAnnotation(T annotation) {
		annotations.put(annotation.getClass(), annotation);
	}

	@SuppressWarnings("unchecked")
	public <T extends TextPipeAnnotation> T getAnnotation(Class<T> key) {
		if (annotations.containsKey(key)) {
			return (T) annotations.get(key);
		}
		return null;
	}

}
