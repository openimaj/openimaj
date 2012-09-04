package org.openimaj.text.nlp.textpipe.annotators;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;

public class DefaultRawTextAnnotator extends AbstractRawTextAnnotator{

	@Override
	RawTextAnnotation getRawTextAnnotation(String rawText) {
		return new RawTextAnnotation(rawText);
	}

}
