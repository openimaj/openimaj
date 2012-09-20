package org.openimaj.text.nlp.textpipe.annotators;

import org.openimaj.text.nlp.textpipe.annotations.TextPipeAnnotation;

public abstract class AbstractTextPipeAnnotator<INPUT_ANNOTATION extends TextPipeAnnotation> {
	
	/**
	 * Annotates the given {@link TextPipeAnnotation}. The work is delegated to the abstract methods.
	 * @param annotation
	 * @throws MissingRequiredAnnotationException
	 */
	public void  annotate(INPUT_ANNOTATION annotation) throws MissingRequiredAnnotationException{
		checkForRequiredAnnotations(annotation);
		performAnnotation(annotation);
	}
	
	/**
	 * This is where the actual annotation needs to happen.
	 * @param annotation
	 * @throws MissingRequiredAnnotationException
	 */
	abstract void performAnnotation(INPUT_ANNOTATION annotation) throws MissingRequiredAnnotationException;
	
	/**
	 * This is where any additional checks for other required annotations can be done.
	 * @param annotation
	 * @throws MissingRequiredAnnotationException
	 */
	abstract void checkForRequiredAnnotations(INPUT_ANNOTATION annotation) throws MissingRequiredAnnotationException;

}
