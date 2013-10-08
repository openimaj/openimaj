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
package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import gov.sandia.cognition.math.matrix.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.ml.linear.evaluation.BilinearEvaluator;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.Pair;

import com.google.common.collect.BiMap;

/**
 * Some stats of a model
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ModelStats {

	/**
	 * The score of the model estimating the the data before this round
	 */
	public double score;
	/**
	 * The sorted important words of the model after this round
	 */
	public Map<String, SortedImportantWords> importantWords;
	/**
	 * The current model
	 */
	public IncrementalBilinearSparseOnlineLearner learner;

	/**
	 * the min/max params of each task
	 */
	public Map<String,Pair<Double>> taskWordMinMax;
	/**
	 * The value of Y for this round of the model
	 */
	public Matrix correctY;
	/**
	 * The estimated value of Y for this round of the model
	 */
	public Matrix estimatedY;

	/**
	 * The bias from the model
	 */
	public Matrix bias;
	/**
	 * The min and max values for each user
	 */
	public Map<String, Pair<Double>> userMinMax;

	/**
	 * A new learner, no meaningful important words and a loss of 0
	 */
	public ModelStats() {
		this.score = 0;
		this.learner = null;
		this.importantWords = new HashMap<String, SortedImportantWords>();
		this.taskWordMinMax = new HashMap<String, Pair<Double>>();
	}

	/**
	 * The model and its associated loss
	 * @param eval
	 * @param learner
	 * @param in
	 */
	public ModelStats(BilinearEvaluator eval, IncrementalBilinearSparseOnlineLearner learner, Context in) {

//		IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> in = inaggr.getPayload();
		Map<String, Map<String, Double>> bagofwords = in.getTyped("bagofwords");
		Map<String, Double> averageticks = in.getTyped("averageticks");
		this.learner = learner;
		this.learner.updateUserValues(bagofwords, averageticks);
		BilinearSparseOnlineLearner bilinearLearner = this.learner.getBilinearLearner();

		// Evaluate the learner on the current data
		eval.setLearner(bilinearLearner);
		List<Pair<Matrix>> testList = new ArrayList<Pair<Matrix>>();
		Pair<Matrix> xy = this.learner.asMatrixPair(bagofwords,averageticks);
		testList.add(xy);
		this.score = eval.evaluate(testList);

		// Extract other statistics
		this.importantWords = importantWords();
		this.taskWordMinMax = minMaxWords();
		this.userMinMax = minMaxUsers();
		this.correctY = xy.secondObject();
		this.bias = bilinearLearner.getBias();
		this.estimatedY = bilinearLearner.predict(xy.firstObject());
	}

	private Map<String, Pair<Double>> minMaxUsers() {
		Map<String, Pair<Double>> ret = new HashMap<String, Pair<Double>>();
		if(this.learner == null) return ret;
		BiMap<String, Integer> depvals = this.learner.getDependantValues();
		BilinearSparseOnlineLearner bilearner = this.learner.getBilinearLearner();
		for (String task : depvals.keySet()) {
			Integer taskCol = this.learner.getDependantValues().get(task);
			ret.put(
				task,
				new Pair<Double>(
					CFMatrixUtils.min(bilearner.getU().getColumn(taskCol)),
					CFMatrixUtils.max(bilearner.getU().getColumn(taskCol))
				)
			);
		}

		return ret;
	}

	private Map<String, Pair<Double>> minMaxWords() {
		Map<String, Pair<Double>> ret = new HashMap<String, Pair<Double>>();
		if(this.learner == null) return ret;
		BiMap<String, Integer> depvals = this.learner.getDependantValues();
		BilinearSparseOnlineLearner bilearner = this.learner.getBilinearLearner();
		for (String task : depvals.keySet()) {
			Integer taskCol = this.learner.getDependantValues().get(task);
			ret.put(
				task,
				new Pair<Double>(
					CFMatrixUtils.min(bilearner.getW().getColumn(taskCol)),
					CFMatrixUtils.max(bilearner.getW().getColumn(taskCol))
				)
			);
		}

		return ret;
	}

	private Map<String, SortedImportantWords> importantWords() {
		Map<String, SortedImportantWords> ret = new HashMap<String, SortedImportantWords>();
		if(this.learner == null) return ret;
		BiMap<String, Integer> depvals = this.learner.getDependantValues();
		BilinearSparseOnlineLearner bilearner = this.learner.getBilinearLearner();
		for (String task : depvals.keySet()) {
			ret.put(
				task,
				new SortedImportantWords(task, learner, bilearner, 10)
			);
		}

		return ret;
	}

	public void printSummary() {
		if(learner == null){
			System.out.println("No loss!");
			return;
		}
		System.out.println("Loss: " + this.score);
		System.out.println("Important words: ");
		BilinearSparseOnlineLearner bilinearLearner = this.learner.getBilinearLearner();
		BiMap<Integer, String> inversewords = this.learner.getVocabulary().inverse();
		for (String task : this.importantWords.keySet()) {
			Pair<Double> minmax = this.taskWordMinMax.get(task);
			SortedImportantWords sortedImportantWords = this.importantWords.get(task);
			for (int wordIndex : sortedImportantWords.indexes) {
				System.out.println("Word: " + inversewords.get(wordIndex) + " index " + wordIndex);
				System.out.println(bilinearLearner.getW().getRow(wordIndex));
			}
			System.out.printf("... %s (%1.4f->%1.4f) %s\n",
					task,
					minmax.firstObject(),
					minmax.secondObject(),
					sortedImportantWords
					);
		}
		System.out.println("User importance: ");
		for (String task : this.importantWords.keySet()) {
			Pair<Double> minmax = this.userMinMax.get(task);
			System.out.printf("... %s (%1.4f->%1.4f)\n",
					task,
					minmax.firstObject(),
					minmax.secondObject()
					);
		}
		System.out.println("Model Bias: \n" + this.bias);
		System.out.println("Correct Y: \n" + this.correctY);
		System.out.println("Estimated Y: \n" + this.estimatedY);
	}

}
