/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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

/**
 * Perform an experiment, a set of tweets with a known language compared to the {@link LanguageDetector}'s response
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class TrueLanguageExperiment {
	String[] inputFiles = new String[]{
		"dev-out-r",
		"trn-out-r",
		"tst-out-r"
	};
	private ArrayList<TrueLanguageTwitterStatus> inputStatusLists;
	private HashMap<String, IndependentPair<Float, Long>> scores;
	
	/**
	 * Load the tweets needed for this experiment
	 * @throws IOException
	 */
	public void prepareExperiment() throws IOException{
		this.inputStatusLists = new ArrayList<TrueLanguageTwitterStatus>();
		
		for (String input : this.inputFiles) {
			File inputFile = FileUtils.copyStreamToFile(TrueLanguageExperiment.class.getResourceAsStream(input), File.createTempFile(input, ".txt"));
			TwitterStatusList<TrueLanguageTwitterStatus> list = MemoryTwitterStatusList.read(inputFile,TrueLanguageTwitterStatus.class);
			this.inputStatusLists.addAll(list);
			inputFile.delete();
		}
	}
	
	/**
	 * do the experiment
	 * @throws IOException
	 */
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
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		TrueLanguageExperiment exp = new TrueLanguageExperiment();
		exp.prepareExperiment();
		exp.doExperiment();
	}
}
