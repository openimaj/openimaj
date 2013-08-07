package org.openimaj.ml.clustering.spectral;


import org.apache.log4j.Logger;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.clustering.SimilarityClusterer;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Wraps the functionality of a {@link SimilarityClusterer} around a dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class NormalisedSimilarityDoubleClustererWrapper<T> extends DoubleFVSimilarityFunction<T> {
	
	private double eps;



	/**
	 * 
	 * @param extractor
	 * @param eps
	 */
	public NormalisedSimilarityDoubleClustererWrapper(FeatureExtractor<DoubleFV,T> extractor, double eps) {
		super(extractor);
		this.eps = eps;
	}

	Logger logger = Logger.getLogger(NormalisedSimilarityDoubleClustererWrapper.class);


	
	protected SparseMatrix similarity() {
		final SparseMatrix mat = new SparseMatrix(feats.length,feats.length);
		final DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < feats.length; i++) {
			for (int j = i; j < feats.length; j++) {
				double d = dist.compare(feats[i], feats[j]);
				if(d>eps ) 
					d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		SparseMatrix mat_norm = new SparseMatrix(feats.length,feats.length);
		for (int i = 0; i < feats.length; i++) {
			for (int j = i; j < feats.length; j++) {
				double d = mat.get(i, j);
				if(Double.isNaN(d)){
					continue;
				}
				else{
					d/=maxD;
				}
				mat_norm.put(i, j, 1-d);
				mat_norm.put(j, i, 1-d);
			}
		}
		return mat_norm;
	}
	
	

}
