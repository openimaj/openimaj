package org.openimaj.text.nlp.textpipe.annotators;

import java.util.List;

import org.openimaj.text.nlp.textpipe.annotations.AnnotationUtils;
import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public abstract class AbstractPOSAnnotator extends
		AbstractTextPipeAnnotator<RawTextAnnotation> {

	@Override
	public void performAnnotation(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		if (annotation.getAnnotationKeyList()
				.contains(SentenceAnnotation.class)) {
			for (SentenceAnnotation sentence : annotation
					.getAnnotationsFor(SentenceAnnotation.class)) {
				if (sentence.getAnnotationKeyList().contains(
						TokenAnnotation.class)) {
					List<TokenAnnotation> tokens = sentence.getAnnotationsFor(TokenAnnotation.class);
					List<POSAnnotation.PartOfSpeech> postags = pos(AnnotationUtils.getStringTokensFromTokenAnnotationList(tokens));
					for(int i =0; i<postags.size();i++){
						tokens.get(i).addAnnotation(new POSAnnotation(postags.get(i)));
					}
				} else
					throw new MissingRequiredAnnotationException(
							"Untokenised sentence found: Require TokenAnnotations.");
			}
		} else {
			if (annotation.getAnnotationKeyList().contains(
					TokenAnnotation.class)) {
				List<TokenAnnotation> tokens = annotation.getAnnotationsFor(TokenAnnotation.class);
				List<POSAnnotation.PartOfSpeech> postags = pos(AnnotationUtils.getStringTokensFromTokenAnnotationList(tokens));
				for(int i =0; i<postags.size();i++){
					tokens.get(i).addAnnotation(new POSAnnotation(postags.get(i)));
				}
			} else
				throw new MissingRequiredAnnotationException(
						"No TokenAnnotations found: Require TokenAnnotations.");
		}

	}

	protected abstract List<POSAnnotation.PartOfSpeech> pos(
			List<String> tokenList);

}
