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
package org.openimaj.experiment.evaluation.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.Evaluator;
import org.openimaj.experiment.evaluation.cluster.analyser.ClusterAnalyser;
import org.openimaj.experiment.evaluation.cluster.processor.Clusterer;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <D> The type of data which the internal clusterer can cluster lists of
 * @param <T> The type of results the
 */
public class ClusterEvaluator<D, T extends AnalysisResult> implements Evaluator<int[][],T>{

	private int[][] correct;
	private ClusterAnalyser<T> analyser;
	private Clusterer<D> gen;
	private D data;

	/**
	 * @param gen
	 * @param data 
	 * @param clusters
	 * @param analyser
	 */
	public ClusterEvaluator(Clusterer<D> gen, D data, int[][] clusters, ClusterAnalyser<T> analyser) {
		this.gen = gen;
		this.correct = clusters;
		this.analyser = analyser;
		this.data = data;
	}
	
	/**
	 * @param gen
	 * @param data 
	 * @param dataset extract the elements of this map "in order" and build a ground truth. very dangerous.
	 * @param analyser
	 */
	public <A,B> ClusterEvaluator(Clusterer<D> gen, D data, Map<A,? extends List<B>> dataset, ClusterAnalyser<T> analyser) {
		this.gen = gen;
		this.correct = new int[dataset.size()][];
		int j = 0;
		int k = 0;
		for (Entry<A, ? extends List<B>> es : dataset.entrySet()) {
			this.correct[j] = new int[es.getValue().size()];
			int i = 0;
			List<B> value = es.getValue();
			for (int l = 0; l < value.size(); l++) {
				this.correct[j][i++] = k;
				k++;
			}
			j++;
		}
		this.analyser = analyser;
		this.data = data;
	}
	
	/**
	 * @param gen
	 * @param data 
	 * @param indexFunc given a data instance, return its index
	 * @param dataset 
	 * @param analyser
	 */
	public <A,B> ClusterEvaluator(
			Clusterer<D> gen, 
			D data, 
			Function<B,Integer> indexFunc,
			Map<A,? extends List<B>> dataset, 
			ClusterAnalyser<T> analyser) {
		this.gen = gen;
		this.correct = new int[dataset.size()][];
		int j = 0;
		for (Entry<A, ? extends List<B>> es : dataset.entrySet()) {
			this.correct[j] = new int[es.getValue().size()];
			int i = 0;
			List<B> value = es.getValue();
			for (B b : value) {
				this.correct[j][i++] = indexFunc.apply(b);
			}
			j++;
		}
		this.analyser = analyser;
		this.data = data;
	}
	
	/**
	 * @param gen
	 * @param dataset
	 * @param transform turn a list of dataset items into the required type for clustering
	 * @param analyser
	 */
	public <A,B> ClusterEvaluator(
		Clusterer<D> gen, 
		Map<A,? extends List<B>> dataset, 
		Function<List<B>,D> transform, 
		ClusterAnalyser<T> analyser
	) {
		this.gen = gen;
		this.analyser = analyser;
		this.correct = new int[dataset.size()][];
		int j = 0;
		List<B> flattened = new ArrayList<B>();
		for (Entry<A, ? extends List<B>> es : dataset.entrySet()) {
			this.correct[j] = new int[es.getValue().size()];
			int i = 0;
			for (B b : es.getValue()) {
				this.correct[j][i++] = flattened.size();
				flattened.add(b);
			}
			j++;
		}
		this.data = transform.apply(flattened);
	}
	
	
	
	

	@Override
	public int[][] evaluate() {
		return this.gen.performClustering(this.data);
	}

	@Override
	public T analyse(int[][] estimated) {
		return this.analyser.analyse(correct, estimated);
	}

}
