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

import static org.junit.Assert.assertTrue;
import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.roc.ROCAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.roc.ROCResult;
import org.openimaj.text.nlp.sentiment.model.wordlist.MPQATokenList;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestMPQAModel {
	private String[] positives;
	private String[] negatives;
	private String[] neutral;
	private MapBackedDataset<BipolarSentiment, ListDataset<List<String>>, List<String>> dataset;

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
		
		dataset = new MapBackedDataset<BipolarSentiment, ListDataset<List<String>>, List<String>>();
		Map<BipolarSentiment, ListDataset<List<String>>> map = dataset.getMap();
		
		prepare(map,BipolarSentiment.POSITIVE, positives);
		prepare(map,BipolarSentiment.NEGATIVE, negatives);
		prepare(map,BipolarSentiment.NEUTRAL, neutral);
		
		
		
	}
	
	private void prepare(Map<BipolarSentiment, ListDataset<List<String>>> map, BipolarSentiment sent,String[] examples) {
		ListBackedDataset<List<String>> dataset = new ListBackedDataset<List<String>>();
		for (String example : examples) {
			List<String> words = Arrays.asList(example.split(" "));
			dataset.add(words);
		}
		map.put(sent, dataset);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testPredict() throws Exception {
		MPQATokenList model = new MPQATokenList();
		ROCAnalyser<List<String>, BipolarSentiment> analyser = new ROCAnalyser<List<String>, BipolarSentiment>();
		ClassificationEvaluator<ROCResult<BipolarSentiment>, BipolarSentiment, List<String>> classEval = 
			new ClassificationEvaluator<ROCResult<BipolarSentiment>, BipolarSentiment, List<String>>(model, dataset, analyser);
		ROCResult<BipolarSentiment> result = classEval.analyse(classEval.evaluate());
		Map<BipolarSentiment, ReceiverOperatingCharacteristic> rocs = result.getROCData();
		System.out.println(rocs.get(BipolarSentiment.POSITIVE).computeStatistics().getOptimalThreshold().getClassifier().getThreshold());
		assertTrue(rocs.get(BipolarSentiment.POSITIVE).computeStatistics().getAreaUnderCurve() == 1);
		assertTrue(rocs.get(BipolarSentiment.NEGATIVE).computeStatistics().getAreaUnderCurve() == 1);
		assertTrue(rocs.get(BipolarSentiment.NEUTRAL).computeStatistics().getAreaUnderCurve() == 1);
//		assertTrue(bipolarModel.calculateError(negSamples) == 0);
//		assertTrue(bipolarModel.calculateError(neuSamples) == 0);
	}
}
