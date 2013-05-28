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
