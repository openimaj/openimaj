package org.openimaj.ml.clustering.spectral;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.clustering.SimilarityClusterer;
import org.openimaj.util.function.Function;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Wraps the functionality of a {@link SimilarityClusterer} around a dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> 
 *
 */
public abstract class DoubleFVSimilarityFunction<T> implements Function<List<T>,SparseMatrix>{
	Logger logger = Logger.getLogger(DoubleFVSimilarityFunction.class);
	double[][] feats = null;
	private FeatureExtractor<DoubleFV, T> extractor;
	private List<T> data;
	/**
	 * @param extractor
	 *
	 */
	public DoubleFVSimilarityFunction(FeatureExtractor<DoubleFV, T> extractor) {
		this.extractor = extractor;
	}
	
	public SparseMatrix apply(List<T> in) {
		this.data = in;
		this.prepareFeats();
		return this.similarity();
	};
	
	protected void prepareFeats() {
		if(feats!=null)return;
		int numInstances = data.size();
		feats = new double[numInstances][];
		int index = 0;
		for (T d : this.data) {
			feats[index++] = this.extractor.extractFeature(d).values;
		}
	}
	
	abstract SparseMatrix similarity();
}
