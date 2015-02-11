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

	@Override
	protected SparseMatrix similarity() {
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
