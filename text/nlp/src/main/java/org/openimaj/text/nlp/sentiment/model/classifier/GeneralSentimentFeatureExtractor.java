package org.openimaj.text.nlp.sentiment.model.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;

/**
 * {@link FeatureExtractor} that is suitable for {@link NaiveBayesAnnotator}.
 * Should be initialized with training corpus of the machine learning
 * {@link AbstractAnnotator} you are using.
 * 
 * @author laurence
 * 
 */
public class GeneralSentimentFeatureExtractor implements
		FeatureExtractor<DoubleFV, List<String>> {

	private ArrayList<String> vocabList;
	private int wordOccuranceThresh = 50;

	/**
	 * Construct with the training set. This is required to build a vocabulary.
	 * @param list of tokenised corpus documents.
	 */
	public GeneralSentimentFeatureExtractor(
			List<List<String>> domainVocabularyCorpus) {
		initialize(domainVocabularyCorpus);
	}
	
	
	/**
	 * Blank constructor. Will require initialize to be called at a later stage.
	 */
	public GeneralSentimentFeatureExtractor(){
		
	}

	/**
	 * Allows a new vocabulary to be constructed from a new corpus.
	 * @param list of tokenised corpus documents.
	 */
	public void initialize(List<List<String>> domainVocabularyCorpus) {
		HashMap<String, Integer> vocab = new HashMap<String, Integer>();
		for (List<String> doc : domainVocabularyCorpus) {
			for (String s : doc) {
				Integer current = vocab.get(s);
				if (current == null)
					current = 0;
				vocab.put(s, current + 1);

			}
		}
		this.vocabList = new ArrayList<String>();
		for (Entry<String, Integer> entry : vocab.entrySet()) {
			if (entry.getValue() > wordOccuranceThresh) {
				vocabList.add(entry.getKey());
			}
		}
	}

	@Override
	public DoubleFV extractFeature(List<String> tokens) {
		double[] vect = new double[vocabList.size()];
		for (int i = 0; i < vect.length; i++) {
			vect[i] += 0.00001;
		}
		for (String s : tokens) {
			int ind = vocabList.indexOf(s);
			if (ind >= 0)
				vect[ind] += 1;
		}
		double[] vectNorm = new double[vocabList.size()];
		for (int i = 0; i < vect.length; i++) {
			vectNorm[i]=vect[i]/tokens.size();			
		}
		return new DoubleFV(vect);
	}

}
