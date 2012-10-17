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
package org.openimaj.txt.nlp.sentiment;

//import static org.junit.Assert.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.openimaj.io.FileUtils;
//import org.openimaj.text.nlp.sentiment.model.classifier.GaussianNaiveBayesBiopolarSentimentModel;
//import org.openimaj.text.nlp.sentiment.model.classifier.UniqueWordFisherSentimentModel;
//import org.openimaj.text.nlp.sentiment.model.classifier.UniqueWordNaiveBayesSentimentModel;
//import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
//import org.openimaj.text.nlp.sentiment.type.DiscreteCountBipolarSentiment;
//import org.openimaj.text.nlp.sentiment.type.WeightedBipolarSentiment;
//import org.openimaj.util.pair.IndependentPair;
//
//public class TestNaiveBayesModel {
//	private String[] positives;
//	private String[] negatives;
//	private String[] neutral;
//	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> posSamples;
//	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> negSamples;
//	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> neuSamples;
//
//	/**
//	 * Create the model, prepare test statements
//	 */
//	@Before
//	public void setup(){
//		positives = new String[]{
//			"happy happy very happy day",
//			"very happy day",
//		};
//		
//		negatives = new String[]{
//			"sad sad sad day very sad",
//			"very sad day"
//		};
//		
//		neutral = new String[]{
//			"very normal normal normal day day",
//			"very normal",
//		};
//		
//		posSamples = prepare(positives, new WeightedBipolarSentiment(1.0f, 0f, 0f));
//		negSamples = prepare(negatives, new WeightedBipolarSentiment(0.0f, 1.0f, 0f));
//		neuSamples = prepare(neutral, new WeightedBipolarSentiment(0.0f, 0f, 1.0f));
//	}
//	
//	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> prepare(String[] examples, WeightedBipolarSentiment sent) {
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> ret = new ArrayList<IndependentPair<List<String>,WeightedBipolarSentiment>>();
//		
//		for (String example : examples) {
//			List<String> words = tok(example);
//			ret.add(IndependentPair.pair(words,sent));
//		}
//		return ret;
//	}
//	
//	public List<String> tok(String line){
//		return Arrays.asList(line.split(" "));
//	}
//	
//	/**
//	 * Toy examples used for training/test
//	 */
//	@Test
//	public void testSimple(){
//		UniqueWordNaiveBayesSentimentModel nbsent = new UniqueWordNaiveBayesSentimentModel(1d,1d/3d);
//		nbsent.estimate(posSamples.subList(0, 1));
//		nbsent.estimate(negSamples.subList(0, 1));
//		nbsent.estimate(neuSamples.subList(0, 1));
//		
//		System.out.println(nbsent);
//		
//		double posError = nbsent.calculateError(posSamples);
//		double negError = nbsent.calculateError(negSamples);
//		double neuError = nbsent.calculateError(neuSamples);
//		assertTrue(posError == 0);
//		assertTrue(negError == 0);
//		assertTrue(neuError == 0);
//	}
//	
//	/**
//	 * Tests from the collective intelligence book
//	 */
//	@Test
//	public void testCollectiveIntelligence(){
//		UniqueWordNaiveBayesSentimentModel nbsent = new UniqueWordNaiveBayesSentimentModel(1d,1d/3d,false);
//		WeightedBipolarSentiment good = DiscreteCountBipolarSentiment.POSITIVE.weightedBipolar();
//		WeightedBipolarSentiment bad = DiscreteCountBipolarSentiment.NEGATIVE.weightedBipolar();
//		nbsent.estimate(tok("The quick brown fox jumps over the lazy dog"),good);
//		nbsent.estimate(tok("Make quick money in the online casino"),bad);
//		
//		WeightedBipolarSentiment quickProb = nbsent.wordCount("quick");
//		assertTrue(quickProb.equals(new WeightedBipolarSentiment(1,1,0)));
//		nbsent = new UniqueWordNaiveBayesSentimentModel(1d,0.5,false);
//		retrain(nbsent);
//		quickProb = nbsent.wordProb("quick");
//		assertTrue(quickProb.positive() == 2d/3d);
//		WeightedBipolarSentiment moneyWeighted = nbsent.wordGivenSentiment("money");
//		assertTrue(moneyWeighted.positive() == 0.25);
//		retrain(nbsent);
//		moneyWeighted = nbsent.wordGivenSentiment("money");
//		assertTrue(moneyWeighted.positive() == 1/6d);
//		nbsent.reset();
//		retrain(nbsent);
//		WeightedBipolarSentiment qrabbit = nbsent.predict(tok("quick rabbit"));
//		assertEquals(qrabbit.positive(),0.15624,0.0001);
//		assertEquals(qrabbit.negative(),0.05,0.0001);
//		
//	}
//	
//	@Test
//	public void testCollectiveIntelligenceFisher(){
//		UniqueWordFisherSentimentModel nbsent = new UniqueWordFisherSentimentModel(1d,0.5d);
//		retrain(nbsent);
//		WeightedBipolarSentiment quickFisher = nbsent.sentimentProb("quick");
//		assertEquals(quickFisher.positive(),0.57142,0.00001);
//		WeightedBipolarSentiment quickRabbit = nbsent.predict(tok("quick rabbit"));
//		assertEquals(quickRabbit.positive(),0.78013,0.00001);
//		
//	}
//	
//	private void retrain(UniqueWordNaiveBayesSentimentModel nbsent) {
//		WeightedBipolarSentiment good = DiscreteCountBipolarSentiment.POSITIVE.weightedBipolar();
//		WeightedBipolarSentiment bad = DiscreteCountBipolarSentiment.NEGATIVE.weightedBipolar();
//		nbsent.estimate(tok("Nobody owns the water"),good);
//		nbsent.estimate(tok("the quick rabbit jumps fences"),good);
//		nbsent.estimate(tok("buy pharmaceuticals now"),bad);
//		nbsent.estimate(tok("make quick money at the online casino"),bad);
//		nbsent.estimate(tok("The quick brown fox jumps"),good);
//	}
//	
//	private void retrain(UniqueWordFisherSentimentModel nbsent) {
//		WeightedBipolarSentiment good = DiscreteCountBipolarSentiment.POSITIVE.weightedBipolar();
//		WeightedBipolarSentiment bad = DiscreteCountBipolarSentiment.NEGATIVE.weightedBipolar();
//		nbsent.estimate(tok("Nobody owns the water"),good);
//		nbsent.estimate(tok("the quick rabbit jumps fences"),good);
//		nbsent.estimate(tok("buy pharmaceuticals now"),bad);
//		nbsent.estimate(tok("make quick money at the online casino"),bad);
//		nbsent.estimate(tok("The quick brown fox jumps"),good);
//	}
//
//	@Test
//	public void testIMDBReview() throws IOException{
//		String negSource = "/org/openimaj/text/nlp/sentiment/imdbreview/neg.txt";
//		String posSource = "/org/openimaj/text/nlp/sentiment/imdbreview/pos.txt";
//		
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> negExamples = loadIMDBSource(negSource,new WeightedBipolarSentiment(0f, 1.0f, 0f));
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> posExamples = loadIMDBSource(posSource,new WeightedBipolarSentiment(1.0f, 0f, 0f));
//		
//		float TRAIN_PROP = 0.9f;
//		int nTrainNeg = (int) (negExamples.size() * TRAIN_PROP);
//		int nTrainPos = (int) (posExamples.size() * TRAIN_PROP);
//		nTrainPos = nTrainNeg = Math.min(nTrainNeg, nTrainPos);
//		UniqueWordNaiveBayesSentimentModel nbsent = new UniqueWordNaiveBayesSentimentModel(50d,0.5,false);
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> trainList = new ArrayList<IndependentPair<List<String>,WeightedBipolarSentiment>>();
//		trainList.addAll(negExamples.subList(0, nTrainNeg));
//		trainList.addAll(posExamples.subList(0, nTrainPos));
//		nbsent.estimate(trainList);
//		
//		double negError = nbsent.calculateError(negExamples.subList(nTrainNeg, negExamples.size()));
//		double posError = nbsent.calculateError(posExamples.subList(nTrainPos, posExamples.size()));
//		
//		System.out.println("Negative test error: " + negError);
//		System.out.println("Positive test error: " + posError);
//	}
//	
//	@Test
//	public void testIMDBReviewFisher() throws IOException{
//		String negSource = "/org/openimaj/text/nlp/sentiment/imdbreview/neg.txt";
//		String posSource = "/org/openimaj/text/nlp/sentiment/imdbreview/pos.txt";
//		
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> negExamples = loadIMDBSource(negSource,new WeightedBipolarSentiment(0f, 1.0f, 0f));
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> posExamples = loadIMDBSource(posSource,new WeightedBipolarSentiment(1.0f, 0f, 0f));
//		
//		float TRAIN_PROP = 0.9f;
//		int nTrainNeg = (int) (negExamples.size() * TRAIN_PROP);
//		int nTrainPos = (int) (posExamples.size() * TRAIN_PROP);
//		nTrainPos = nTrainNeg = Math.min(nTrainNeg, nTrainPos);
//		UniqueWordFisherSentimentModel nbsent = new UniqueWordFisherSentimentModel(1d,1/3d);
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> trainList = new ArrayList<IndependentPair<List<String>,WeightedBipolarSentiment>>();
//		trainList.addAll(negExamples.subList(0, nTrainNeg));
//		trainList.addAll(posExamples.subList(0, nTrainPos));
//		nbsent.estimate(trainList);
//		
//		double negError = nbsent.calculateError(negExamples.subList(nTrainNeg, negExamples.size()));
//		double posError = nbsent.calculateError(posExamples.subList(nTrainPos, posExamples.size()));
//		
//		System.out.println("Negative test error: " + negError);
//		System.out.println("Positive test error: " + posError);
//	}
//	
//	@Test
//	public void testIMDBReviewSandia() throws IOException{
//		String negSource = "/org/openimaj/text/nlp/sentiment/imdbreview/neg.txt";
//		String posSource = "/org/openimaj/text/nlp/sentiment/imdbreview/pos.txt";
//		
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> negExamples = loadIMDBSource(negSource,new WeightedBipolarSentiment(0f, 1.0f, 0f));
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> posExamples = loadIMDBSource(posSource,new WeightedBipolarSentiment(1.0f, 0f, 0f));
//		
//		float TRAIN_PROP = 0.9f;
//		int nTrainNeg = (int) (negExamples.size() * TRAIN_PROP);
//		int nTrainPos = (int) (posExamples.size() * TRAIN_PROP);
//		nTrainPos = nTrainNeg = Math.min(nTrainNeg, nTrainPos);
//		GaussianNaiveBayesBiopolarSentimentModel nbsent = new GaussianNaiveBayesBiopolarSentimentModel();
//		System.out.println("Training...");
//		
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> trainList = new ArrayList<IndependentPair<List<String>,WeightedBipolarSentiment>>();
//		trainList.addAll(negExamples.subList(0, nTrainNeg));
//		trainList.addAll(posExamples.subList(0, nTrainPos));
//		nbsent.estimate(trainList);
//		
//		
//		System.out.println("Testing...");
//		double negError = nbsent.calculateError(negExamples.subList(nTrainNeg, negExamples.size()));
//		double posError = nbsent.calculateError(posExamples.subList(nTrainPos, posExamples.size()));
//		
//		System.out.println("Negative test error: " + negError);
//		System.out.println("Positive test error: " + posError);
//	}
//
//	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> loadIMDBSource(String sourceList, WeightedBipolarSentiment sentiment) throws IOException {
//		String[] sources = FileUtils.readlines(TestNaiveBayesModel.class.getResourceAsStream(sourceList));
//		System.out.println("Source List: " + sourceList);
//		List<IndependentPair<List<String>, WeightedBipolarSentiment>> ret = new ArrayList<IndependentPair<List<String>, WeightedBipolarSentiment>>();
//		int totalWords = 0;
//		for (String source : sources) {
//			List<String> wordList = Arrays.asList(FileUtils.readall(TestNaiveBayesModel.class.getResourceAsStream(source)).split("\\s+"));
//			totalWords += wordList.size();
//			IndependentPair<List<String>, WeightedBipolarSentiment> wordsentimentpair = IndependentPair.pair(wordList,sentiment);
//			ret.add(wordsentimentpair);
//		}
//		System.out.println("... Total words: " + totalWords);
//		return ret;
//	}
//}
