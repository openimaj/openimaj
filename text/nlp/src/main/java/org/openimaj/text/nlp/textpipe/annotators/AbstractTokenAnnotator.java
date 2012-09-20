package org.openimaj.text.nlp.textpipe.annotators;

import java.util.List;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public abstract class AbstractTokenAnnotator extends
		AbstractTextPipeAnnotator<RawTextAnnotation>{

	/**
	 * {@link AbstractTokenAnnotator} will annotate the
	 * {@link RawTextAnnotation} directly with {@link TokenAnnotation} unless
	 * there is already a {@link SentenceAnnotation}. Then it will annotate each
	 * {@link SentenceAnnotation}.
	 */
	@Override
	public void performAnnotation(RawTextAnnotation annotation) {
		if (annotation.getAnnotationKeyList()
				.contains(SentenceAnnotation.class)) {
			for (SentenceAnnotation sentence : annotation
					.getAnnotationsFor(SentenceAnnotation.class)) {
				sentence.addAllAnnotations(tokenise(sentence.text));
			}
		} else
			annotation.addAllAnnotations(tokenise(annotation.text));
	}
	
	abstract List<TokenAnnotation> tokenise(String text);

}
