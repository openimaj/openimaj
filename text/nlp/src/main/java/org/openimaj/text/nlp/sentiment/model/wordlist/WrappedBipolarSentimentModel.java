package org.openimaj.text.nlp.sentiment.model.wordlist;

import java.util.List;

import org.openimaj.text.nlp.sentiment.model.SentimentModel;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.BipolarSentimentProvider;
import org.openimaj.text.nlp.sentiment.type.Sentiment;
import org.openimaj.util.pair.IndependentPair;

/**
 * A {@link SentimentModel} which classifies sets of words to a {@link BipolarSentiment} using an
 * underlying {@link SentimentModel} which provides {@link Sentiment} instances which are also
 * {@link BipolarSentimentProvider} instances.
 * 
 * This is mainly an exercise in type shifting such that the {@link #validate(IndependentPair)}
 * functions are usable for the simple {@link BipolarSentiment}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> the type of sentiment to be translated from
 *
 */
public class WrappedBipolarSentimentModel 
		<T extends Sentiment & BipolarSentimentProvider> 
	implements 
		SentimentModel<BipolarSentiment,WrappedBipolarSentimentModel<T>>
{
	private SentimentModel<T, ?> innerSentimentModel;

	/**
	 * @param sentModel
	 */
	public  WrappedBipolarSentimentModel(SentimentModel<T,?> sentModel) {
		innerSentimentModel = sentModel;
	}

	@Override
	public void estimate(List<? extends IndependentPair<List<String>, BipolarSentiment>> data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean validate(IndependentPair<List<String>, BipolarSentiment> data) {
		BipolarSentiment sent = predict(data.firstObject());
		return sent.equals(data.secondObject());
	}

	@Override
	public BipolarSentiment predict(List<String> data) {
		T innerPredict = innerSentimentModel.predict(data);
		return innerPredict.bipolar();
	}

	@Override
	public int numItemsToEstimate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double calculateError(List<? extends IndependentPair<List<String>, BipolarSentiment>> data) {
		double total = data.size();
		double count = 0;
		for (IndependentPair<List<String>, BipolarSentiment> independentPair : data) {
			if(validate(independentPair)){
				count ++;
			}
		}
		return 1 - (count/total);
	}
	
	@Override
	public WrappedBipolarSentimentModel<T> clone() {
		return new WrappedBipolarSentimentModel<T>(this.innerSentimentModel);
	}

}
