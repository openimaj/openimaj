/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.text.nlp.sentiment.model.classifier;

//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.openimaj.text.nlp.sentiment.model.SentimentModel;
//import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
//import org.openimaj.text.nlp.sentiment.type.DiscreteCountBipolarSentiment;
//import org.openimaj.text.nlp.sentiment.type.WeightedBipolarSentiment;
//import org.openimaj.text.nlp.stopwords.StopWords;
//import org.openimaj.util.pair.IndependentPair;
//
///**
// * @author Sina Samangooei (ss@ecs.soton.ac.uk)
// *
// */
//public class UniqueWordNaiveBayesSentimentModel implements SentimentModel<WeightedBipolarSentiment,UniqueWordNaiveBayesSentimentModel>{
//	
//	private static final float ZERO_PROB = 0f;
//	private static final float ASSUMED_WEIGHT = 1f;
//	private static final float ASSUMED_PROBABILITY = 1/3f;
//	private static final long DEFAULT_MINIMUM_SEEN = 50l;
//	Map<String,WeightedBipolarSentiment> wordSentimentWeights;
//	WeightedBipolarSentiment sentimentCount;
//	private double assumedWeight;
//	private double assumedProbability;
//	private HashMap<String, WeightedBipolarSentiment> totalWordCounts;
//	private boolean maxLike;
//	
//	/**
//	 * empty word/sentiment and overall sentiment counts
//	 */
//	public UniqueWordNaiveBayesSentimentModel() {
//		reset();
//		this.assumedWeight = ASSUMED_WEIGHT;
//		this.assumedProbability = ASSUMED_PROBABILITY;
//	}
//	
//	/**
//	 * Specify the assumed weight and probability for each class for unseen words (of that class).
//	 * i.e. if you've never seen a word for a class before, assume this probability
//	 * @param assumedWeight
//	 * @param assumedProbability
//	 */
//	public UniqueWordNaiveBayesSentimentModel(double assumedWeight, double assumedProbability) {
//		reset();
//		this.assumedWeight = assumedWeight;
//		this.assumedProbability = assumedProbability;
//		this.maxLike = true;
//	}
//
//	/**
//	 * @param assumedWeight
//	 * @param assumedProbability
//	 * @param maximumLiklihood whether maximum liklihood using log should be used
//	 */
//	public UniqueWordNaiveBayesSentimentModel(double assumedWeight, double assumedProbability, boolean maximumLiklihood) {
//		this(assumedWeight,assumedProbability);
//		this.maxLike = maximumLiklihood;
//	}
//
//	/**
//	 * reset this iteratively learning model
//	 */
//	public void reset() {
//		this.wordSentimentWeights = new HashMap<String,WeightedBipolarSentiment>();
//		this.totalWordCounts = new HashMap<String,WeightedBipolarSentiment>();
//		this.sentimentCount = new WeightedBipolarSentiment(0,0,0);
//	}
//	
//	/**
//	 * Estimate from a single instance
//	 * @param string
//	 * @param sentiment
//	 */
//	public void estimate(List<String> string, WeightedBipolarSentiment sentiment) {	
//		incrementTotalWordCounts(string,sentiment);
//		HashSet<String> words = getUniqueNonStopWords(string);
//		for (String word : words) {
//			WeightedBipolarSentiment currentCount = getWordWeights(word);
//			currentCount.addInplace(sentiment);
//		}
//		this.sentimentCount.addInplace(sentiment);
//	}
//	
//	@Override
//	public void estimate(List<? extends IndependentPair<List<String>, WeightedBipolarSentiment>> data) {
//		for (IndependentPair<List<String>, WeightedBipolarSentiment> independentPair : data) {
//			estimate(independentPair.firstObject(),independentPair.secondObject());
//		}
//	}
//	
//	
//
//	private void incrementTotalWordCounts(List<String> words, WeightedBipolarSentiment sentiment) {
//		for (String word : words) {
//			WeightedBipolarSentiment current = this.totalWordCounts.get(word);
//			if(current == null) current = new WeightedBipolarSentiment();
//			this.totalWordCounts.put(word, current.addInplace(sentiment));
//		}
//	}
//
//	private HashSet<String> getUniqueNonStopWords(List<String> words) {
//		HashSet<String> ret = new HashSet<String>();
//		for (String word : words) {
////			if(this.stopWords.isStopWord(word)) continue;
//			ret.add(word);
//		}
//		return ret;
//	}
//
//	private WeightedBipolarSentiment getWordWeights(String word) {
//		WeightedBipolarSentiment ret = this.wordSentimentWeights.get(word);
//		if(ret == null) this.wordSentimentWeights.put(word, ret = new WeightedBipolarSentiment(ZERO_PROB,ZERO_PROB,ZERO_PROB));
//		return ret;
//	}
//
//	/**
//	 * Find the sentiment of a document given sentiment, found u
//	 * @param data
//	 * @return nP(D | C) = SUM (P ( F | C ) )
//	 */
//	public WeightedBipolarSentiment docProb(List<String> data){
//		WeightedBipolarSentiment documentGivenSentiment = new WeightedBipolarSentiment(1f,1f,1f);
//		HashSet<String> words = getUniqueNonStopWords(data);
//		for (String word : words) {
//			WeightedBipolarSentiment word_sentiment = wordGivenSentiment(word); // == P ( F | C ) 
//			documentGivenSentiment.multiplyInplace(word_sentiment);
//		} // == SUM( log (P ( F | C ) ) )
//		return documentGivenSentiment;
//	}
//	
//	@Override
//	public WeightedBipolarSentiment predict(List<String> data) {
//		if(this.maxLike){
//			return maxliklihood(data);
//		}
//		else{
//			return probability(data);
//		}
//	}
//	
//	private WeightedBipolarSentiment maxliklihood(List<String> data) {
//		WeightedBipolarSentiment logDocumentGivenSentiment = logDocProb(data);
//		// Apply bayes here!
//		WeightedBipolarSentiment logSentimentGivenDocument = this.sentimentCount.divide(this.sentimentCount.total()).logInplace(); // sentiment = c/N(c)
//		logSentimentGivenDocument.addInplace(logDocumentGivenSentiment); // log(P(A | B)) ~= log(P(B | A)) + log(P(A))
//		
//		return logSentimentGivenDocument;
//	}
//
//	private WeightedBipolarSentiment logDocProb(List<String> data) {
//		WeightedBipolarSentiment logDocumentGivenSentiment = new WeightedBipolarSentiment(0f,0f,0f);
//		HashSet<String> words = getUniqueNonStopWords(data);
//		DiscreteCountBipolarSentiment seen = new DiscreteCountBipolarSentiment();
//		for (String word : words) {
//			WeightedBipolarSentiment word_sentiment = logWordGivenSentiment(word); // == log (P ( F | C ) )
//			if(word_sentiment.bipolar().positive())
//				seen.addInplace(DiscreteCountBipolarSentiment.POSITIVE);
//			else
//				seen.addInplace(DiscreteCountBipolarSentiment.NEGATIVE);
//			logDocumentGivenSentiment.addInplace(word_sentiment); 
//			
//		} // == SUM( log (P ( F | C ) ) )
//		return logDocumentGivenSentiment;
//	}
//
//	private WeightedBipolarSentiment probability(List<String> words){
//		WeightedBipolarSentiment documentGivenSentiment = docProb(words); // == MULTI( P ( F | C )  )
//		// Apply bayes here!
//		WeightedBipolarSentiment sentimentGivenDocument = this.sentimentCount.divide(this.sentimentCount.total()); // sentiment = c/N(c)
//		sentimentGivenDocument.multiplyInplace(documentGivenSentiment); // P(A | B) ~= P(B | A) * log(P(A)
//		
//		return sentimentGivenDocument;
//	}
//	
//	/**
//	 * Guarantees no word is ever 0 probability in any category
//	 * @param word
//	 * @return the probability of a word given a sentiment (P ( F | C )) weighted by prior assumptions
//	 */
//	public WeightedBipolarSentiment wordGivenSentiment(String word) {
//		return wordGivenSentiment(word,this.assumedWeight,this.assumedProbability);
//	}
//	
//	private WeightedBipolarSentiment wordGivenSentiment(String word, double weight, double assumedProbability) {
//		WeightedBipolarSentiment prob = wordProb(word);
//		double total = countWordAllCat(word);
//		prob = prob.multiplyInplace(total);
//		prob.addInplace(weight * assumedProbability);
//		prob.divideInplace(total+weight); // (weight * assumed + total * prob)/(total+weight)
//		return prob;
//	}
//	
//	private double countWordAllCat(String word) {
//		WeightedBipolarSentiment prob = this.wordSentimentWeights.get(word);
//		if(prob == null) {
//			return 0d;
//		}
//		else{
//			return prob.total();
//		}
//	}
//
//	public WeightedBipolarSentiment wordProb(String word) {
//		WeightedBipolarSentiment prob = this.wordSentimentWeights.get(word);
//		if(prob == null) {
//			prob = new WeightedBipolarSentiment(ZERO_PROB,ZERO_PROB,ZERO_PROB);
//		}
//		else{
//			prob = prob.clone();
//			prob.divideInplace(sentimentCount);
//			prob.correctNaN(0d);
//		}
//		return prob;
//	}
//
//	/**
//	 * Guarantees no word is ever 0 probability in any category
//	 * @param word
//	 * @return
//	 */
//	private WeightedBipolarSentiment logWordGivenSentiment(String word) {
//		return logWordGivenSentiment(word,this.assumedWeight,this.assumedProbability);
//	}
//
//	private WeightedBipolarSentiment logWordGivenSentiment(String word, double weight, double assumedProbability) {
//		return wordGivenSentiment(word,weight,assumedProbability).logInplace();
//	}
//
//	@Override
//	public boolean validate(IndependentPair<List<String>, WeightedBipolarSentiment> data) {
//		WeightedBipolarSentiment pred = this.predict(data.firstObject());
//		BipolarSentiment predBipolar = pred.bipolar();
//		BipolarSentiment valiBipolar = data.secondObject().bipolar();
//		return valiBipolar.equals(predBipolar);
//	}
//
//	@Override
//	public int numItemsToEstimate() {
//		return 1;
//	}
//
//	@Override
//	public double calculateError(List<? extends IndependentPair<List<String>, WeightedBipolarSentiment>> data) {
//		double total = data.size();
//		double correct = 0;
//		for (IndependentPair<List<String>, WeightedBipolarSentiment> independentPair : data) {
//			correct += validate(independentPair) ? 1 : 0;
//		}
//		return 1 - (correct/total);
//	}
//	
//	@Override
//	public UniqueWordNaiveBayesSentimentModel clone() {
//		UniqueWordNaiveBayesSentimentModel ret = new UniqueWordNaiveBayesSentimentModel();
//		ret.sentimentCount = this.sentimentCount.clone();
//		ret.wordSentimentWeights = new HashMap<String, WeightedBipolarSentiment>();
//		for (Entry<String, WeightedBipolarSentiment> wordSent: this.wordSentimentWeights.entrySet()) {
//			ret.wordSentimentWeights.put(wordSent.getKey(), wordSent.getValue().clone());
//		}
//		return ret;
//	}
//	
//	@Override
//	public String toString() {
//		String out = "Class counts:\n %s \n Wordcounts: \n %s";
//		return String.format(out, this.sentimentCount,this.wordSentimentWeights);
//	}
//
//	/**
//	 * @param word
//	 * @return the current count for this word (might be null if unseen)
//	 */
//	public WeightedBipolarSentiment wordCount(String word) {
//		return this.wordSentimentWeights.get(word);
//	}
//
//	
//
//}
