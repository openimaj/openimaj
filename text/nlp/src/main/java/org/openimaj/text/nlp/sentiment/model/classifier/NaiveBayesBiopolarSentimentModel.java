package org.openimaj.text.nlp.sentiment.model.classifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.text.nlp.sentiment.model.SentimentModel;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.WeightedBipolarSentiment;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class NaiveBayesBiopolarSentimentModel implements SentimentModel<WeightedBipolarSentiment,NaiveBayesBiopolarSentimentModel>{
	
	private static final float ZERO_PROB = 0f;
	private static final float ASSUMED_WEIGHT = 1f;
	private static final float ASSUMED_PROBABILITY = 1/3f;
	Map<String,WeightedBipolarSentiment> wordSentimentWeights;
	WeightedBipolarSentiment sentimentCount;
	private double assumedWeight;
	private double assumedProbability;
	
	/**
	 * empty word/sentiment and overall sentiment counts
	 */
	public NaiveBayesBiopolarSentimentModel() {
		reset();
		this.assumedWeight = ASSUMED_WEIGHT;
		this.assumedProbability = ASSUMED_PROBABILITY;
	}
	
	/**
	 * Specify the assumed weight and probability for each class for unseen words (of that class).
	 * i.e. if you've never seen a word for a class before, assume this probability
	 * @param assumedWeight
	 * @param assumedProbability
	 */
	public NaiveBayesBiopolarSentimentModel(double assumedWeight, double assumedProbability) {
		reset();
		this.assumedWeight = assumedWeight;
		this.assumedProbability = assumedProbability;
	}

	private void reset() {
		this.wordSentimentWeights = new HashMap<String,WeightedBipolarSentiment>();
		this.sentimentCount = new WeightedBipolarSentiment(0,0,0);
	}

	@Override
	public void estimate(List<? extends IndependentPair<List<String>, WeightedBipolarSentiment>> data) {
//		HashSet<String>
		for (IndependentPair<List<String>, WeightedBipolarSentiment> independentPair : data) {
			List<String> words = independentPair.firstObject();
			double nWords = words.size();
			
			for (String word : words) {
				WeightedBipolarSentiment currentCount = getWordWeights(word);
//				WeightedBipolarSentiment currentWeight = independentPair.secondObject().divide(nWords);
				WeightedBipolarSentiment currentWeight = independentPair.secondObject();
				currentCount.addInplace(currentWeight);
				this.sentimentCount.addInplace(currentWeight);
			}
		}
	}
	
	

	private WeightedBipolarSentiment getWordWeights(String word) {
		WeightedBipolarSentiment ret = this.wordSentimentWeights.get(word);
		if(ret == null) this.wordSentimentWeights.put(word, ret = new WeightedBipolarSentiment(ZERO_PROB,ZERO_PROB,ZERO_PROB));
		return ret;
	}

	@Override
	public WeightedBipolarSentiment predict(List<String> data) {
		WeightedBipolarSentiment logDocumentGivenSentiment = new WeightedBipolarSentiment(0f,0f,0f);
		for (String word : data) {
			WeightedBipolarSentiment word_sentiment = logWordGivenSentiment(word); // == log (P ( F | C ) )
			logDocumentGivenSentiment.addInplace(word_sentiment); 
			
		} // == SUM( log (P ( F | C ) ) )
		
		// Weight by length of document?
		logDocumentGivenSentiment.divideInplace((double)data.size());
		// Apply bayes here!
		WeightedBipolarSentiment logSentimentGivenDocument = this.sentimentCount.divide(this.sentimentCount.total()).logInplace(); // sentiment = c/N(c)
		logSentimentGivenDocument.addInplace(logDocumentGivenSentiment); // log(P(A | B)) ~= log(P(B | A)) + log(P(A))
		
		return logSentimentGivenDocument;
	}
	
	/**
	 * Guarantees no word is ever 0 probability in any category
	 * @param word
	 * @return
	 */
	private WeightedBipolarSentiment logWordGivenSentiment(String word) {
		return logWordGivenSentiment(word,this.assumedWeight,this.assumedProbability);
	}

	private WeightedBipolarSentiment logWordGivenSentiment(String word, double weight, double assumedProbability) {
		WeightedBipolarSentiment prob = this.wordSentimentWeights.get(word);
		double total = 0;
		if(prob == null) {
			prob = new WeightedBipolarSentiment(ZERO_PROB,ZERO_PROB,ZERO_PROB);
		}
		else{
			prob = prob.clone();
			total = prob.total();
			WeightedBipolarSentiment correctedCount = sentimentCount.clone();
			if(correctedCount.neutral() == 0) correctedCount.neutral(1);
			if(correctedCount.negative() == 0) correctedCount.negative(1);
			if(correctedCount.positive() == 0) correctedCount.positive(1);
			prob.divideInplace(correctedCount);
		}
		prob.times(total).add(weight * assumedProbability).divide(total+weight); // (weight * assumed + total * prob)/(total+weight)
		prob.logInplace();
		return prob;
	}

	@Override
	public boolean validate(IndependentPair<List<String>, WeightedBipolarSentiment> data) {
		WeightedBipolarSentiment pred = this.predict(data.firstObject()).timesInplace(-1d);
		BipolarSentiment predBipolar = pred.bipolar();
		BipolarSentiment valiBipolar = data.secondObject().bipolar();
		return valiBipolar.equals(predBipolar);
	}

	@Override
	public int numItemsToEstimate() {
		return 1;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<List<String>, WeightedBipolarSentiment>> data) {
		double total = data.size();
		double correct = 0;
		for (IndependentPair<List<String>, WeightedBipolarSentiment> independentPair : data) {
			correct += validate(independentPair) ? 1 : 0;
		}
		return 1 - (correct/total);
	}
	
	@Override
	public NaiveBayesBiopolarSentimentModel clone() {
		NaiveBayesBiopolarSentimentModel ret = new NaiveBayesBiopolarSentimentModel();
		ret.sentimentCount = this.sentimentCount.clone();
		ret.wordSentimentWeights = new HashMap<String, WeightedBipolarSentiment>();
		for (Entry<String, WeightedBipolarSentiment> wordSent: this.wordSentimentWeights.entrySet()) {
			ret.wordSentimentWeights.put(wordSent.getKey(), wordSent.getValue().clone());
		}
		return ret;
	}
	
	@Override
	public String toString() {
		String out = "Class counts:\n %s \n Wordcounts: \n %s";
		return String.format(out, this.sentimentCount,this.wordSentimentWeights);
	}

}
