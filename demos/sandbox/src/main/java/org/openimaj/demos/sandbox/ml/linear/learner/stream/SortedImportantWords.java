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

import gov.sandia.cognition.math.matrix.Vector;

import java.util.Comparator;
import java.util.Iterator;

import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.IncrementalBilinearSparseOnlineLearner;
import org.openimaj.util.queue.BoundedPriorityQueue;

import com.google.common.collect.BiMap;

/**
 * Iterate over words in terms of their importance
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SortedImportantWords implements Iterable<String>{
	BiMap<String,Integer> words;
	int[] indexes;
	private int taskIndex;
	private Vector wordWeights;


	/**
	 * @param task The task for which word importance is to be measured
	 * @param learner The incremental learner (giving access to the words)
	 * @param bilearner The source of the word parameters
	 * @param size The number of words
	 */
	public SortedImportantWords(
			String task,
			IncrementalBilinearSparseOnlineLearner learner,
			BilinearSparseOnlineLearner bilearner,
			int size
	) {
		this.words = learner.getVocabulary();
		this.taskIndex = learner.getDependantValues().get(task);
		this.wordWeights = bilearner.getW().getColumn(taskIndex);
		BoundedPriorityQueue<Integer> queue = new BoundedPriorityQueue<Integer>(size, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				Double weighto1 = wordWeights.getElement(o1);
				Double weighto2 = wordWeights.getElement(o2);
				return -weighto1.compareTo(weighto2);
			}
		});
		for (int i = 0; i < wordWeights.getDimensionality(); i++) {
			queue.add(i);
		}
		this.indexes = new int[size];
		int i = 0;
		while(!queue.isEmpty()){
			this.indexes[i++] = queue.pollTail();
		}
	}


	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			int index = 0;
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String next() {
				return words.inverse().get(indexes[index++]);
			}

			@Override
			public boolean hasNext() {
				return index<indexes.length;
			}
		};
	}

	@Override
	public String toString() {
		String ret = "[";
		for (String word : this) {
			double wordWeight = this.wordWeights.getElement(this.words.get(word));
			ret += String.format("%s (%1.4f)",word,wordWeight) + ", ";
		}
		ret += "]";
		return ret;
	}

}
