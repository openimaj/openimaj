package org.openimaj.twitter.experiments.langid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.language.LanguageDetector;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;
import org.openimaj.twitter.collection.MemoryTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;
import org.openimaj.util.pair.IndependentPair;

public class TrueLanguageExperiment {
	String[] inputFiles = new String[]{
		"dev-out-r",
		"trn-out-r",
		"tst-out-r"
	};
	private ArrayList<TrueLanguageTwitterStatus> inputStatusLists;
	private HashMap<String, IndependentPair<Float, Long>> scores;
	
	public void prepareExperiment() throws IOException{
		this.inputStatusLists = new ArrayList<TrueLanguageTwitterStatus>();
		
		for (String input : this.inputFiles) {
			File inputFile = FileUtils.copyStreamToTemp(TrueLanguageExperiment.class.getResourceAsStream(input), input, ".txt");
			TwitterStatusList<TrueLanguageTwitterStatus> list = MemoryTwitterStatusList.read(inputFile,TrueLanguageTwitterStatus.class);
			this.inputStatusLists.addAll(list);
		}
	}
	
	public void doExperiment() throws IOException{
		this.scores = new HashMap<String,IndependentPair<Float,Long>>();
		IndependentPair<Float, Long> totalScore;
		this.scores.put("total",  totalScore = IndependentPair.pair(0f, 0l));
		LanguageDetector languageDetector = new LanguageDetector();
		for (TrueLanguageTwitterStatus status : inputStatusLists) {
			WeightedLocale detected = languageDetector.classify(status.text);
			IndependentPair<Float, Long> currentScore = this.scores.get(detected.language);
			if(currentScore == null){
				currentScore = IndependentPair.pair(0.0f, 0l);
				this.scores.put(detected.language, currentScore);
			}
			if(detected.language.equals(status.lang_true)){
				currentScore.setFirstObject(currentScore.firstObject() + 1);
				totalScore.setFirstObject(totalScore.firstObject() + 1);
			}
			currentScore.setSecondObject(currentScore.secondObject() + 1);
			totalScore.setSecondObject(totalScore.secondObject() + 1);
		}
		for (Entry<String, IndependentPair<Float, Long>> entry : this.scores.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue().firstObject() / entry.getValue().secondObject());
		}
		System.out.println("Total score:" + totalScore.firstObject() / totalScore.secondObject());
	}
	
	public static void main(String[] args) throws IOException {
		TrueLanguageExperiment exp = new TrueLanguageExperiment();
		exp.prepareExperiment();
		exp.doExperiment();
	}
}
