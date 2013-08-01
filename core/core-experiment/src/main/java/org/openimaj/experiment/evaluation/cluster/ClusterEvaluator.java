package org.openimaj.experiment.evaluation.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.Evaluator;
import org.openimaj.experiment.evaluation.cluster.analyser.ClusterAnalyser;
import org.openimaj.experiment.evaluation.cluster.processor.SimpleClusterer;
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
	private SimpleClusterer<D> gen;
	private D data;

	/**
	 * @param gen
	 * @param data 
	 * @param clusters
	 * @param analyser
	 */
	public ClusterEvaluator(SimpleClusterer<D> gen, D data, int[][] clusters, ClusterAnalyser<T> analyser) {
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
	public <A,B> ClusterEvaluator(SimpleClusterer<D> gen, D data, Map<A,? extends List<B>> dataset, ClusterAnalyser<T> analyser) {
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
	 * @param analyser
	 * @param dataset
	 * @param transform turn a list of dataset items into the required type for clustering
	 */
	public <A,B> ClusterEvaluator(
		SimpleClusterer<D> gen, 
		ClusterAnalyser<T> analyser, 
		Map<A,? extends List<B>> dataset, 
		Function<List<B>,D> transform
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
		return this.gen.rawcluster(this.data);
	}

	@Override
	public T analyse(int[][] estimated) {
		return this.analyser.analyse(correct, estimated);
	}

}
