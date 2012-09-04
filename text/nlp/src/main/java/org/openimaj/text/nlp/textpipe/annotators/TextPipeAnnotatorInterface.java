package org.openimaj.text.nlp.textpipe.annotators;

import org.openimaj.text.nlp.textpipe.annotations.TextPipeAnnotation;

public interface TextPipeAnnotatorInterface<T extends TextPipeAnnotation> {
	
	public void  annotate(T annotation);

}
