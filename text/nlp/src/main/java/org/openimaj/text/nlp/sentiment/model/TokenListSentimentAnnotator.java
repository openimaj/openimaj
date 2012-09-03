package org.openimaj.text.nlp.sentiment.model;

import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;

/**
 * A sentiment annotator which can deal with word tokens
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 * 
 * @param <EXTRACTOR> 
 * @param <CLONETYPE>
 */
public abstract class TokenListSentimentAnnotator <
		EXTRACTOR extends FeatureExtractor<?,List<String>>,
		CLONETYPE extends SentimentAnnotator<List<String>,EXTRACTOR,CLONETYPE> 
	> 
	extends AbstractAnnotator<List<String>, BipolarSentiment, EXTRACTOR>
	implements SentimentAnnotator< List<String>,EXTRACTOR, CLONETYPE>{
	
	/**
	* @param extractor the features extracted from lists of strings
	*/
	public TokenListSentimentAnnotator(EXTRACTOR extractor) {
		super(extractor);
	}
	
	@Override
	public Set<BipolarSentiment> getAnnotations() {
		return BipolarSentiment.listBipolarSentiment();
	}
}
