package org.openimaj.experiment.evaluation.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.Evaluator;
import org.openimaj.experiment.evaluation.cluster.analyser.ClusterAnalyser;
import org.openimaj.experiment.evaluation.cluster.processor.ClustererWrapper;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <D> The type of data which the internal clusterer can cluster lists of
 * @param <T> The type of results the
 */
public class ClusterEvaluator<D, T extends AnalysisResult> implements Evaluator<int[][],T>{

	private ClustererWrapper gen;
	private List<D> data;
	private int[][] correct;
	private ClusterAnalyser<T> analyser;

	/**
	 * @param gen
	 * @param data
	 * @param clusters
	 * @param analyser
	 */
	public ClusterEvaluator(ClustererWrapper gen, int[][] clusters, ClusterAnalyser<T> analyser) {
		this.gen = gen;
		this.data = data;
		this.correct = clusters;
		this.analyser = analyser;
	}
	/**
	 * @param gen
	 * @param analyser
	 * @param dataset
	 */
	public <A> ClusterEvaluator(ClustererWrapper gen, ClusterAnalyser<T> analyser, Map<A,? extends List<D>> dataset) {
		this.gen = gen;
		this.analyser = analyser;
		this.data = new ArrayList<D>();
		this.correct = new int[dataset.size()][];
		int j = 0;
		for (Entry<A, ? extends List<D>> es : dataset.entrySet()) {
			this.correct[j] = new int[es.getValue().size()];
			for (int i = 0; i < es.getValue().size(); i++) {
				this.correct[j][i] = this.data.size();
				this.data.add(es.getValue().get(i));
			}
			j++;
		}
	}

	@Override
	public int[][] evaluate() {
		return this.gen.cluster();
	}

	@Override
	public T analyse(int[][] estimated) {
		return this.analyser.analyse(correct, estimated);
	}

}
