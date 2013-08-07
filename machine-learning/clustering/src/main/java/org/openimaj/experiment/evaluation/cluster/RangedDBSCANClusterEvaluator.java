package org.openimaj.experiment.evaluation.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.data.DoubleRange;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.Evaluator;
import org.openimaj.experiment.evaluation.cluster.analyser.ClusterAnalyser;
import org.openimaj.ml.clustering.dbscan.SparseMatrixDBSCAN;
import org.openimaj.util.function.Function;

import ch.akuhn.matrix.SparseMatrix;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <D> The type of data which the internal clusterer can cluster lists of
 * @param <T> The type of results the
 */
public class RangedDBSCANClusterEvaluator<D, T extends AnalysisResult> implements Evaluator<Map<Double,int[][]>,RangedAnalysisResult<Double,T>>{

	private int[][] correct;
	private ClusterAnalyser<T> analyser;
	private SparseMatrixDBSCAN gen;
	private SparseMatrix data;
	DoubleRange r;
	
	
	
	/**
	 * @param r the range of values for the {@link SparseMatrixDBSCAN} eps value
	 * @param gen
	 * @param data 
	 * @param clusters
	 * @param analyser
	 */
	public RangedDBSCANClusterEvaluator(DoubleRange r, SparseMatrixDBSCAN gen, SparseMatrix data, int[][] clusters, ClusterAnalyser<T> analyser) {
		this.gen = gen;
		this.correct = clusters;
		this.analyser = analyser;
		this.data = data;
	}
	
	/**
	 * @param r the range of values for the {@link SparseMatrixDBSCAN} eps value
	 * @param gen
	 * @param data 
	 * @param dataset extract the elements of this map "in order" and build a ground truth. very dangerous.
	 * @param analyser
	 */
	public <A,B> RangedDBSCANClusterEvaluator(DoubleRange r, SparseMatrixDBSCAN gen, SparseMatrix data, Map<A,? extends List<B>> dataset, ClusterAnalyser<T> analyser) {
		this.r = r;
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
	 * @param r the range of values for the {@link SparseMatrixDBSCAN} eps value
	 * @param gen
	 * @param data 
	 * @param indexFunc given a data instance, return its index
	 * @param dataset 
	 * @param analyser
	 */
	public <A,B> RangedDBSCANClusterEvaluator(
			DoubleRange r, 
			SparseMatrixDBSCAN gen, 
			SparseMatrix data,
			Function<B,Integer> indexFunc,
			Map<A,? extends List<B>> dataset, 
			ClusterAnalyser<T> analyser) {
		this.r = r;
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
	 * @param r the range of values for the {@link SparseMatrixDBSCAN} eps value
	 * @param gen
	 * @param dataset
	 * @param transform turn a list of dataset items into the required type for clustering
	 * @param analyser
	 */
	public <A,B> RangedDBSCANClusterEvaluator(
		DoubleRange r, 
		SparseMatrixDBSCAN gen, 
		Map<A,? extends List<B>> dataset, 
		Function<List<B>,SparseMatrix> transform, 
		ClusterAnalyser<T> analyser
	) {
		this.r = r;
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
	public Map<Double,int[][]> evaluate() {
		Map<Double, int[][]> ret = new HashMap<Double, int[][]>();
		for (Double eps : this.r) {
			this.gen.setEps(eps);
			ret.put(eps,new ClusterEvaluator<SparseMatrix, T>(gen, data, correct, analyser).evaluate());
		}
		return ret;
	}

	@Override
	public RangedAnalysisResult<Double, T> analyse(Map<Double,int[][]> estimated) {
		RangedAnalysisResult<Double, T> ret = new RangedAnalysisResult<Double, T>();
		for (Entry<Double, int[][]> ent : estimated.entrySet()) {
			ret.put(ent.getKey(), this.analyser.analyse(correct, ent.getValue()));
		}
		return ret;
	}

}
