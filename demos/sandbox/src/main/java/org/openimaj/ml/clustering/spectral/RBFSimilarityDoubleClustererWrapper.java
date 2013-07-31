package org.openimaj.ml.clustering.spectral;


import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.log4j.Logger;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.clustering.SimilarityClusterer;
import org.openimaj.ml.clustering.TrainingIndexClusters;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;

/**
 * Construct a similarity matrix using a Radial Basis Function 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class RBFSimilarityDoubleClustererWrapper<T> extends SimilarityDoubleClustererWrapper<T> {
	
	private double[] var;
	Logger logger = Logger.getLogger(RBFSimilarityDoubleClustererWrapper.class);

	/**
	 * 
	 * @param data
	 * @param extractor
	 * @param dbscan
	 */
	public RBFSimilarityDoubleClustererWrapper(Dataset<T> data,FeatureExtractor<DoubleFV,T> extractor, SimilarityClusterer<? extends TrainingIndexClusters> dbscan) {
		super(data, extractor, dbscan);
		prepareFeats();
		prepareVariance();
	}
	
	private void prepareVariance() {
		this.var = new double[this.feats[0].length];
		Matrix m = new DenseMatrix(feats);
		double[] colArr = new double[this.feats.length];
		Variance v = new Variance();
		for (int i = 0; i < this.var.length; i++) {
			m.column(i).storeOn(colArr, 0);
			this.var[i] = v.evaluate(colArr);
		}
	}

	SparseMatrix similarity(double[][] testData) {
		int N = testData.length;
		SparseMatrix sim = new SparseMatrix(N,N);
		for (int i = 0; i < N; i++) {
			double[] di = testData[i];
			for (int j = i+1; j < N; j++) {
				double[] dj = testData[j];
				double expInner = 0;
				// -1*sum((data(i,:)-data(j,:)).^2./(2*my_var))
				for (int k = 0; k < dj.length; k++) {
					double kv = di[k] - dj[k];
					expInner += (kv * kv) / (2 * this.var[k]);
				}
				
				double v = Math.exp(-1 * expInner);
				sim.put(i, j, v);
				sim.put(j, i, v);
			}
		}
		return sim;
	}
	
	

}
