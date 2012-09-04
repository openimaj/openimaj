package org.openimaj.text.nlp.textpipe.annotators;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenListAnnotation;

public abstract class AbstractTokenAnnotator implements TextPipeAnnotatorInterface<RawTextAnnotation>{

	@Override
	public void annotate(RawTextAnnotation annotation) {
		annotation.addAnnotation(tokenise(annotation.text));
	}
	
	protected abstract TokenListAnnotation tokenise(String text);
	

}
