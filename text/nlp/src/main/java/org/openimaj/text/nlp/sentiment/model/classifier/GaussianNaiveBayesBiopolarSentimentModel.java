package org.openimaj.text.nlp.sentiment.model.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.math.model.GaussianVectorNaiveBayesModel;
import org.openimaj.text.nlp.sentiment.model.SentimentModel;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.WeightedBipolarSentiment;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GaussianNaiveBayesBiopolarSentimentModel implements SentimentModel<WeightedBipolarSentiment,GaussianNaiveBayesBiopolarSentimentModel>{

	private GaussianVectorNaiveBayesModel<WeightedBipolarSentiment> gaussianVectorModel;
	private ArrayList<String> vocabList;

	public GaussianNaiveBayesBiopolarSentimentModel() {
		gaussianVectorModel = new GaussianVectorNaiveBayesModel<WeightedBipolarSentiment>();
	}
	
	@Override
	public GaussianNaiveBayesBiopolarSentimentModel clone(){
		return new GaussianNaiveBayesBiopolarSentimentModel();
	}
	
	@Override
	public void estimate(List<? extends IndependentPair<List<String>, WeightedBipolarSentiment>> data) {
		List<IndependentPair<double[], WeightedBipolarSentiment>> procData = new ArrayList<IndependentPair<double[],WeightedBipolarSentiment>>();
		HashMap<String,Integer> vocab = new HashMap<String,Integer>();
		for (IndependentPair<List<String>,WeightedBipolarSentiment> independentPair : data) {
			for (String s : independentPair.firstObject()) {
				Integer current = vocab.get(s);
				if(current == null) current = 0;
				vocab.put(s, current + 1);
				
			}
		}
		this.vocabList = new ArrayList<String>();
		for ( Entry<String, Integer>  entry : vocab.entrySet()) {
			if(entry.getValue() > 50){
				vocabList.add(entry.getKey());
			}
		}
		for (IndependentPair<List<String>,WeightedBipolarSentiment> independentPair : data) {
			procData.add(createDoubleArrSentiment(independentPair));
		}
		gaussianVectorModel.estimate(procData);
	}

	private IndependentPair<double[], WeightedBipolarSentiment> createDoubleArrSentiment(IndependentPair<List<String>, WeightedBipolarSentiment> independentPair) {
		double[] vect = createDoubleArr(independentPair.firstObject());
		IndependentPair<double[], WeightedBipolarSentiment> toAdd = IndependentPair.pair(vect, independentPair.secondObject());
		return toAdd;
	}

	private double[] createDoubleArr(List<String> strings) {
		double[] vect = new double[vocabList.size()];
		for (int i = 0; i < vect.length; i++) {
			vect[i]+=0.00001;
			
		}
		for (String s : strings) {
			int ind = vocabList.indexOf(s);
			if(ind >= 0) vect[ind] += 1;
		}
		return vect;
	}

	@Override
	public boolean validate(IndependentPair<List<String>, WeightedBipolarSentiment> data) {
		WeightedBipolarSentiment pred = this.predict(data.firstObject());
		BipolarSentiment predBipolar = pred.bipolar();
		BipolarSentiment valiBipolar = data.secondObject().bipolar();
		return valiBipolar.equals(predBipolar);
	}

	@Override
	public WeightedBipolarSentiment predict(List<String> data) {
		return gaussianVectorModel.predict(createDoubleArr(data));
	}

	@Override
	public int numItemsToEstimate() {
		return 0;
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
	
}
