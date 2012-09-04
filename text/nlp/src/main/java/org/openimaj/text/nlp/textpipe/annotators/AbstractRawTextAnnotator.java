package org.openimaj.text.nlp.textpipe.annotators;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;

public abstract class AbstractRawTextAnnotator{
	
	abstract RawTextAnnotation getRawTextAnnotation(String rawText);

}
