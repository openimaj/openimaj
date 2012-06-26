package org.openimaj.txt.nlp.sentiment;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.text.nlp.sentiment.model.wordlist.MPQA;
import org.openimaj.text.nlp.sentiment.model.wordlist.WrappedBipolarSentimentModel;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.DiscreteCountSentiment;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestMPQAModel {
	private String[] positives;
	private String[] negatives;
	private String[] neutral;
	private List<IndependentPair<List<String>, BipolarSentiment>> posSamples;
	private List<IndependentPair<List<String>, BipolarSentiment>> negSamples;
	private List<IndependentPair<List<String>, BipolarSentiment>> neuSamples;

	/**
	 * Create the model, prepare test statements
	 */
	@Before
	public void setup(){
		positives = new String[]{
			"I am happy about this"
		};
		
		negatives = new String[]{
			"This is making me sad"
		};
		
		neutral = new String[]{
			"I feel indifferent"
		};
		
		posSamples = prepare(positives, BipolarSentiment.POSITIVE);
		negSamples = prepare(negatives, BipolarSentiment.NEGATIVE);
		neuSamples = prepare(neutral, BipolarSentiment.NEUTRAL);
	}
	
	private List<IndependentPair<List<String>, BipolarSentiment>> prepare(String[] examples, BipolarSentiment sent) {
		List<IndependentPair<List<String>, BipolarSentiment>> ret = new ArrayList<IndependentPair<List<String>,BipolarSentiment>>();
		
		for (String example : examples) {
			List<String> words = Arrays.asList(example.split(" "));
			ret.add(IndependentPair.pair(words,sent));
		}
		return ret;
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testPredict() throws Exception {
		MPQA model = new MPQA();
		WrappedBipolarSentimentModel<DiscreteCountSentiment> bipolarModel = new WrappedBipolarSentimentModel<DiscreteCountSentiment>(model);
		assertTrue(bipolarModel.calculateError(posSamples) == 0);
		assertTrue(bipolarModel.calculateError(negSamples) == 0);
		assertTrue(bipolarModel.calculateError(neuSamples) == 0);
	}
}
