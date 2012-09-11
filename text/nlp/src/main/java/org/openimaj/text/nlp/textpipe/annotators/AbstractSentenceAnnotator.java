package org.openimaj.text.nlp.textpipe.annotators;

import java.util.List;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;

public abstract class AbstractSentenceAnnotator extends AbstractTextPipeAnnotator<RawTextAnnotation> {

	@Override
	public void performAnnotation(RawTextAnnotation annotation) {
			annotation.addAllAnnotations(getSentenceAnnotations(annotation.text));
	}
	
	protected abstract List<SentenceAnnotation> getSentenceAnnotations(String text);

}
