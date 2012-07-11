package org.openimaj.text.nlp.sentiment.model;

import java.util.List;

import org.openimaj.math.model.Model;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.Annotator;
import org.openimaj.ml.annotation.FeatureExtractor;
import org.openimaj.text.nlp.sentiment.type.Sentiment;

import edu.stanford.nlp.pipeline.AnnotatorPool;

/**
 * An annotator and model which can ascribe {@link Sentiment} to some list of tokenised strings.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <SENTIMENT> The type of sentiment this model returns
 * @param <CLONETYPE> the type of model this model clones into
 */
public abstract class SentimentModel<
		SENTIMENT extends Sentiment,
		CLONETYPE extends SentimentModel<SENTIMENT,CLONETYPE> 
	> 
	extends AbstractAnnotator<List<String>, SENTIMENT, FeatureExtractor<?,List<String>>>
	implements Model<List<String>,SENTIMENT>{

	/**
	 * To model sentiment, features must be extractable from lists of words
	 * @param extractor
	 */
	public SentimentModel(FeatureExtractor<?, List<String>> extractor) {
		super(extractor);
	}
	
	
	@Override
	public abstract CLONETYPE clone();
}
