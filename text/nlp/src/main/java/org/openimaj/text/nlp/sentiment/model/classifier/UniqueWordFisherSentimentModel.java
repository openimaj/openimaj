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
//public class UniqueWordFisherSentimentModel implements SentimentModel<WeightedBipolarSentiment,UniqueWordFisherSentimentModel>{
//	
//	private static final float ZERO_PROB = 0f;
//	private static final float ASSUMED_WEIGHT = 1f;
//	private static final float ASSUMED_PROBABILITY = 1/3f;
//	Map<String,WeightedBipolarSentiment> wordSentimentWeights;
//	WeightedBipolarSentiment sentimentCount;
//	private double assumedWeight;
//	private double assumedProbability;
//	
//	/**
//	 * empty word/sentiment and overall sentiment counts
//	 */
//	public UniqueWordFisherSentimentModel() {
//		this(ASSUMED_WEIGHT,ASSUMED_PROBABILITY);
//	}
//	
//	/**
//	 * Specify the assumed weight and probability for each class for unseen words (of that class).
//	 * i.e. if you've never seen a word for a class before, assume this probability
//	 * @param assumedWeight
//	 * @param assumedProbability
//	 */
//	public UniqueWordFisherSentimentModel(double assumedWeight, double assumedProbability) {
//		reset();
//		this.assumedWeight = assumedWeight;
//		this.assumedProbability = assumedProbability;
//	}
//
//	/**
//	 * reset this iteratively learning model
//	 */
//	public void reset() {
//		this.wordSentimentWeights = new HashMap<String,WeightedBipolarSentiment>();
//		this.sentimentCount = new WeightedBipolarSentiment(0,0,0);
//	}
//	
//	/**
//	 * Estimate from a single instance
//	 * @param string
//	 * @param sentiment
//	 */
//	public void estimate(List<String> string, WeightedBipolarSentiment sentiment) {	
//		HashSet<String> words = getUniqueWords(string);
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
//
//	private HashSet<String> getUniqueWords(List<String> words) {
//		HashSet<String> ret = new HashSet<String>();
//		for (String word : words) {
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
//	@Override
//	public WeightedBipolarSentiment predict(List<String> data) {
//		WeightedBipolarSentiment sent = new WeightedBipolarSentiment(ZERO_PROB,ZERO_PROB,ZERO_PROB);
//		HashSet<String> words = getUniqueWords(data);
//		for (String word : words) {
//			sent.addInplace(weightedSentimentProb(word).logInplace());
//		}
//		sent.multiplyInplace(-2d);
//		
//		return inverseChi2(sent,data.size()*2);
//	}
//	
//	private WeightedBipolarSentiment inverseChi2(WeightedBipolarSentiment sent,int df) {
//		WeightedBipolarSentiment m = sent.divide(2d);
//		WeightedBipolarSentiment sum = m.multiply(-1d).expInplace();
//		WeightedBipolarSentiment term = sum.clone();
//		for (double i = 1; i < df/2; i++) {
//			term.multiplyInplace(m.divide(i));
//			sum.addInplace(term);
//		}
//		return sum.clipMaxInplace(1d);
//	}
//
//	private WeightedBipolarSentiment weightedSentimentProb(String word) {
//		return weightedSentimentProb(word,this.assumedWeight,this.assumedProbability);
//	}
//
//	private WeightedBipolarSentiment weightedSentimentProb(String word,double weight, double assumedProb) {
//		WeightedBipolarSentiment prob = sentimentProb(word);
//		double total = countWordAllCat(word);
//		prob = prob.multiplyInplace(total);
//		prob.addInplace(weight * assumedProb);
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
//	/**
//	 * @param word
//	 * @return probability of each sentiment given a word
//	 */
//	public WeightedBipolarSentiment sentimentProb(String word){
//		WeightedBipolarSentiment  clf = wordProb(word);
//		double sum = clf.total();
//		if(sum == 0) return clf.clone();
//		return clf.divide(sum); // P (f | C) / SUM(P(F|C))
//	}
//	
//	/**
//	 * The probability a word given each sentiment 
//	 * @param word
//	 * @return P(F|C)
//	 */
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
//	public UniqueWordFisherSentimentModel clone() {
//		UniqueWordFisherSentimentModel ret = new UniqueWordFisherSentimentModel();
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
