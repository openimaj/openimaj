package org.openimaj.text.nlp.textpipe.annotators;

import java.util.List;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;
import org.openimaj.text.nlp.tokenisation.ReversableTokeniser;

public abstract class AbstractTokenAnnotator<TOKENISER extends ReversableTokeniser<TOKENISER,TokenAnnotation<TOKENISER>>> extends
		AbstractTextPipeAnnotator<RawTextAnnotation> implements ReversableTokeniser<TOKENISER,TokenAnnotation<TOKENISER>> {

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

}
