package org.openimaj.text.nlp.sentiment.model.classifier;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.text.nlp.sentiment.type.Sentiment;

/**
 * A {@link NaiveBayesAnnotator} for sentiment analysis.
 * @author 
 *
 * @param <SENTIMENT> {@link Sentiment} object that will be the annotation.
 */
public class NaiveBayesSentimentAnnotator<SENTIMENT extends Sentiment>
		extends
		NaiveBayesAnnotator<List<String>, SENTIMENT, GeneralSentimentFeatureExtractor> {

	public NaiveBayesSentimentAnnotator(
			org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator.Mode mode) {
		super(new GeneralSentimentFeatureExtractor(), mode);
	}

	@Override
	public void train(
			Iterable<? extends Annotated<List<String>, SENTIMENT>> data) {
		List<List<String>> rawTokens = new ArrayList<List<String>>();
		for(Annotated<List<String>, SENTIMENT> anno: data){
			rawTokens.add(anno.getObject());
		}
		this.extractor.initialize(rawTokens);
		super.train(data);
	}	

}
