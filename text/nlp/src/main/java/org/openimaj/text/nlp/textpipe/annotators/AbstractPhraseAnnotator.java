package org.openimaj.text.nlp.textpipe.annotators;

import java.util.List;

import org.openimaj.text.nlp.textpipe.annotations.AnnotationUtils;
import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public abstract class AbstractPhraseAnnotator extends
		AbstractTextPipeAnnotator<RawTextAnnotation> {

	@Override
	public void performAnnotation(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		if (!annotation.getAnnotationKeyList().contains(
				SentenceAnnotation.class))
			throw new MissingRequiredAnnotationException(
					"No SentenceAnnotation found");
		for (SentenceAnnotation sentence : annotation
				.getAnnotationsFor(SentenceAnnotation.class)) {
			if (sentence.getAnnotationKeyList().contains(TokenAnnotation.class)) {
				if (AnnotationUtils.allHaveAnnotation(
						sentence.getAnnotationsFor(TokenAnnotation.class),
						POSAnnotation.class)) {
					phraseChunk(sentence
							.getAnnotationsFor(TokenAnnotation.class));
				} else
					throw new MissingRequiredAnnotationException(
							"No POSAnnotation found on token");
			} else
				throw new MissingRequiredAnnotationException(
						"UnTokenized sentence found");
		}
	}

	protected abstract void phraseChunk(List<TokenAnnotation> tokens);

}
