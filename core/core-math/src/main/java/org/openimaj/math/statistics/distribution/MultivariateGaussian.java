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
package org.openimaj.math.statistics.distribution;
import Jama.Matrix;


/**
 * A single multidimensional Gaussian
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MultivariateGaussian {
	Matrix covar;
	Matrix mean;
	int N;
	
	Matrix inv_covar;
	double pdf_const_factor;
	
	protected MultivariateGaussian(int ndims) {
		N = ndims;
		mean = new Matrix(1, ndims);
		covar = new Matrix(ndims, ndims);
	}
	
	/**
	 * Construct the Gaussian with the provided center and covariance
	 * @param mean centre of the Gaussian
	 * @param covar covariance of the Gaussian
	 */
	public MultivariateGaussian(Matrix mean, Matrix covar) {
		N = mean.getColumnDimension();
		this.mean = mean;
		this.covar = covar;
		cacheValues();
	}
	
	protected void cacheValues() {
		inv_covar = covar.inverse();
		pdf_const_factor = 1.0 / (Math.pow((2 * Math.PI), N) * Math.sqrt(covar.det()));
	}
	
	/**
	 * Estimate a multidimensional Gaussian from the data
	 * @param samples the data
	 * @return the Gaussian with the best fit to the data
	 */
	public static MultivariateGaussian estimate(float[][] samples) {
		int nsamples = samples.length;
		int ndims = samples[0].length; 
		
		MultivariateGaussian gauss = new MultivariateGaussian(ndims);
		
		//mean
		for (int j=0; j<nsamples; j++) {
			for (int i=0; i<ndims; i++) {
				gauss.mean.set(0, i, gauss.mean.get(0, i) + samples[j][i]);
			}
		}
		for (int i=0; i<ndims; i++) {
			gauss.mean.set(0, i, gauss.mean.get(0, i) / nsamples);
		}
		
		//covar
		for (int i=0; i<ndims; i++) {
			for (int j=0; j<ndims; j++) {
				double qij = 0;
				
				for (int k=0; k<nsamples; k++) {
					qij += (samples[k][i] - gauss.mean.get(0, i)) * (samples[k][j] - gauss.mean.get(0, j)); 
				}
				
				gauss.covar.set(i, j, qij / (nsamples - 1));
			}
		}
		
		gauss.cacheValues();
		
		return gauss;
	}
	
	/**
	 * Get the probability for a given point in space
	 * relative to the PDF represented by this Gaussian.
	 * 
	 * @param sample the point
	 * @return the probability
	 */
	public double estimateProbability(float[] sample) {
		Matrix xm = new Matrix(1, N);
		for (int i=0; i<N; i++) xm.set(0, i, sample[i] - mean.get(0, i));
		
		Matrix xmt = xm.transpose();
		
		double v = xm.times(inv_covar.times(xmt)).get(0, 0);
		
		return pdf_const_factor * Math.exp(-0.5 * v);
	}
	
	/**
	 * Get the probability for a given point in space
	 * relative to the PDF represented by this Gaussian.
	 * 
	 * @param sample the point
	 * @return the probability
	 */
	public double estimateProbability(Float[] sample) {
		Matrix xm = new Matrix(1, N);
		for (int i=0; i<N; i++) xm.set(0, i, sample[i] - mean.get(0, i));
		
		Matrix xmt = xm.transpose();
		
		double v = xm.times(inv_covar.times(xmt)).get(0, 0);
		
		return pdf_const_factor * Math.exp(-0.5 * v);
	}

	/**
	 * Get the covariance
	 * @return the covariance
	 */
	public Matrix getCovar() {
		return covar;
	}

	/**
	 * Get the mean
	 * @return the mean
	 */
	public Matrix getMean() {
		return mean;
	}

	/**
	 * Get the dimensionality
	 * @return number of dimensions
	 */
	public int getDims() {
		return N;
	}	
}
