package org.openimaj.ml.clustering.spectral;


import org.apache.log4j.Logger;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.clustering.SimilarityClusterer;
import org.openimaj.ml.clustering.TrainingIndexClusters;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Wraps the functionality of a {@link SimilarityClusterer} around a dataset
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class NormalisedSimilarityDoubleClustererWrapper<T> extends SimilarityDoubleClustererWrapper<T> {
	
	private double eps;



	/**
	 * 
	 * @param data
	 * @param extractor
	 * @param dbscan
	 * @param eps
	 */
	public NormalisedSimilarityDoubleClustererWrapper(Dataset<T> data,
			FeatureExtractor<DoubleFV,T> extractor, SimilarityClusterer<? extends TrainingIndexClusters> dbscan, double eps) {
		super(data, extractor, dbscan);
		this.eps = eps;
	}

	Logger logger = Logger.getLogger(NormalisedSimilarityDoubleClustererWrapper.class);


	
	SparseMatrix similarity(double[][] testData) {
		final SparseMatrix mat = new SparseMatrix(testData.length,testData.length);
		final DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = dist.compare(testData[i], testData[j]);
				if(d>eps ) d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		SparseMatrix mat_norm = new SparseMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
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
