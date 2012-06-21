package org.openimaj.txt.nlp.sentiment;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.sentiment.model.classifier.GaussianNaiveBayesBiopolarSentimentModel;
import org.openimaj.text.nlp.sentiment.model.classifier.NaiveBayesBiopolarSentimentModel;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.WeightedBipolarSentiment;
import org.openimaj.util.pair.IndependentPair;

public class TestNaiveBayesModel {
	private String[] positives;
	private String[] negatives;
	private String[] neutral;
	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> posSamples;
	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> negSamples;
	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> neuSamples;

	/**
	 * Create the model, prepare test statements
	 */
	@Before
	public void setup(){
		positives = new String[]{
			"happy happy very happy day",
			"very happy day",
		};
		
		negatives = new String[]{
			"sad sad sad day very sad",
			"very sad day"
		};
		
		neutral = new String[]{
			"very normal normal normal day day",
			"very normal",
		};
		
		posSamples = prepare(positives, new WeightedBipolarSentiment(1.0f, 0f, 0f));
		negSamples = prepare(negatives, new WeightedBipolarSentiment(0.0f, 1.0f, 0f));
		neuSamples = prepare(neutral, new WeightedBipolarSentiment(0.0f, 0f, 1.0f));
	}
	
	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> prepare(String[] examples, WeightedBipolarSentiment sent) {
		List<IndependentPair<List<String>, WeightedBipolarSentiment>> ret = new ArrayList<IndependentPair<List<String>,WeightedBipolarSentiment>>();
		
		for (String example : examples) {
			List<String> words = Arrays.asList(example.split(" "));
			ret.add(IndependentPair.pair(words,sent));
		}
		return ret;
	}
	
	/**
	 * Toy examples used for training/test
	 */
	@Test
	public void testSimple(){
		NaiveBayesBiopolarSentimentModel nbsent = new NaiveBayesBiopolarSentimentModel(1d,1d/3d);
		nbsent.estimate(posSamples.subList(0, 1));
		nbsent.estimate(negSamples.subList(0, 1));
		nbsent.estimate(neuSamples.subList(0, 1));
		
		System.out.println(nbsent);
		
		double posError = nbsent.calculateError(posSamples);
		double negError = nbsent.calculateError(negSamples);
		double neuError = nbsent.calculateError(neuSamples);
		assertTrue(posError == 0);
		assertTrue(negError == 0);
		assertTrue(neuError == 0);
	}
	
	@Test
	public void testIMDBReview() throws IOException{
		String negSource = "/org/openimaj/text/nlp/sentiment/imdbreview/neg.txt";
		String posSource = "/org/openimaj/text/nlp/sentiment/imdbreview/pos.txt";
		
		List<IndependentPair<List<String>, WeightedBipolarSentiment>> negExamples = loadIMDBSource(negSource,new WeightedBipolarSentiment(0f, 1.0f, 0f));
		List<IndependentPair<List<String>, WeightedBipolarSentiment>> posExamples = loadIMDBSource(posSource,new WeightedBipolarSentiment(1.0f, 0f, 0f));
		
		float TRAIN_PROP = 0.9f;
		int nTrainNeg = (int) (negExamples.size() * TRAIN_PROP);
		int nTrainPos = (int) (posExamples.size() * TRAIN_PROP);
		nTrainPos = nTrainNeg = Math.min(nTrainNeg, nTrainPos);
		NaiveBayesBiopolarSentimentModel nbsent = new NaiveBayesBiopolarSentimentModel(1d,1d/3d);
		nbsent.estimate(negExamples.subList(0, nTrainNeg));
		nbsent.estimate(posExamples.subList(0, nTrainPos));
		
		double negError = nbsent.calculateError(negExamples.subList(nTrainNeg, negExamples.size()));
		double posError = nbsent.calculateError(posExamples.subList(nTrainPos, posExamples.size()));
		
		System.out.println("Negative test error: " + negError);
		System.out.println("Positive test error: " + posError);
	}
	
	@Test
	public void testIMDBReviewSandia() throws IOException{
		String negSource = "/org/openimaj/text/nlp/sentiment/imdbreview/neg.txt";
		String posSource = "/org/openimaj/text/nlp/sentiment/imdbreview/pos.txt";
		
		List<IndependentPair<List<String>, WeightedBipolarSentiment>> negExamples = loadIMDBSource(negSource,new WeightedBipolarSentiment(0f, 1.0f, 0f));
		List<IndependentPair<List<String>, WeightedBipolarSentiment>> posExamples = loadIMDBSource(posSource,new WeightedBipolarSentiment(1.0f, 0f, 0f));
		
		float TRAIN_PROP = 0.01f;
		int nTrainNeg = (int) (negExamples.size() * TRAIN_PROP);
		int nTrainPos = (int) (posExamples.size() * TRAIN_PROP);
		nTrainPos = nTrainNeg = Math.min(nTrainNeg, nTrainPos);
		GaussianNaiveBayesBiopolarSentimentModel nbsent = new GaussianNaiveBayesBiopolarSentimentModel();
		System.out.println("Training...");
		List<IndependentPair<List<String>, WeightedBipolarSentiment>> trainList = negExamples.subList(0, nTrainNeg);
		trainList.addAll(posExamples.subList(0, nTrainPos));
		nbsent.estimate(trainList);
		System.out.println("Testing...");
		double negError = nbsent.calculateError(negExamples.subList(nTrainNeg, negExamples.size()));
		double posError = nbsent.calculateError(posExamples.subList(nTrainPos, posExamples.size()));
		
		System.out.println("Negative test error: " + negError);
		System.out.println("Positive test error: " + posError);
	}

	private List<IndependentPair<List<String>, WeightedBipolarSentiment>> loadIMDBSource(String sourceList, WeightedBipolarSentiment sentiment) throws IOException {
		String[] sources = FileUtils.readlines(TestNaiveBayesModel.class.getResourceAsStream(sourceList));
		System.out.println("Source List: " + sourceList);
		List<IndependentPair<List<String>, WeightedBipolarSentiment>> ret = new ArrayList<IndependentPair<List<String>, WeightedBipolarSentiment>>();
		int totalWords = 0;
		for (String source : sources) {
			List<String> wordList = Arrays.asList(FileUtils.readall(TestNaiveBayesModel.class.getResourceAsStream(source)).split("\\s+"));
			totalWords += wordList.size();
			IndependentPair<List<String>, WeightedBipolarSentiment> wordsentimentpair = IndependentPair.pair(wordList,sentiment);
			ret.add(wordsentimentpair);
		}
		System.out.println("... Total words: " + totalWords);
		return ret;
	}
}
