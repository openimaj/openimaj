package org.openimaj.ml.clustering.spectral;


import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.log4j.Logger;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;

/**
 * Construct a similarity matrix using a Radial Basis Function 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class RBFSimilarityDoubleClustererWrapper<T> extends DoubleFVSimilarityFunction<T> {
	
	private double[] var;
	Logger logger = Logger.getLogger(RBFSimilarityDoubleClustererWrapper.class);

	/**
	 * @param extractor
	 */
	public RBFSimilarityDoubleClustererWrapper(FeatureExtractor<DoubleFV,T> extractor) {
		super(extractor);
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

	SparseMatrix similarity() {
		prepareVariance();
		int N = feats.length;
		SparseMatrix sim = new SparseMatrix(N,N);
		for (int i = 0; i < N; i++) {
			double[] di = feats[i];
			sim.put(i,i,1);
			for (int j = i+1; j < N; j++) {
				double[] dj = feats[j];
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
