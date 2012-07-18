package org.openimaj.text.nlp.sentiment.model;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.Annotator;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.Sentiment;

/**
 * An annotator which can ascribe {@link Sentiment} to some {@link Object}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <OBJECT> The object to which sentiment can be ascribed
 * @param <EXTRACTOR> Extractor used to go from OBJECT to feature instances
 * @param <CLONETYPE> the type of model this model clones into
 */
public interface SentimentAnnotator<
		OBJECT,
		EXTRACTOR extends FeatureExtractor<?,OBJECT>,
		CLONETYPE extends SentimentAnnotator<OBJECT,EXTRACTOR,CLONETYPE>
	> 
	extends Annotator<OBJECT, BipolarSentiment, EXTRACTOR>{
}
