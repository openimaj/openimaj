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
package org.openimaj.experiment.gmm.retrieval;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.ml.gmm.GaussianMixtureModelEM;
import org.openimaj.ml.gmm.GaussianMixtureModelEM.CovarianceType;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.function.Function;

import Jama.Matrix;

/**
 * This function turns a list of features to a gaussian mixture model
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class GMMFromFeatures implements Function<
			LocalFeatureList<? extends LocalFeature<?,? extends FeatureVector>>, 
			MixtureOfGaussians
		>
{
	
	/**
	 * default number of guassians to train agains
	 */
	public static final int DEFAULT_COMPONENTS = 10;
	/**
	 * default covariance type
	 */
	public static final CovarianceType DEFAULT_COVARIANCE = GaussianMixtureModelEM.CovarianceType.Spherical;
	
	private GaussianMixtureModelEM gmm;
	/**
	 * Defaults to {@link #DEFAULT_COMPONENTS} and 
	 */
	public GMMFromFeatures() {
		this.gmm = new GaussianMixtureModelEM(DEFAULT_COMPONENTS, DEFAULT_COVARIANCE);
	}
	
	/**
	 * @param type
	 */
	public GMMFromFeatures(CovarianceType type) {
		this.gmm = new GaussianMixtureModelEM(DEFAULT_COMPONENTS, type);
	}
	
	/**
	 * @param nComps
	 */
	public GMMFromFeatures(int nComps) {
		this.gmm = new GaussianMixtureModelEM(nComps, DEFAULT_COVARIANCE);
	}
	
	/**
	 * @param nComps
	 * @param type
	 */
	public GMMFromFeatures(int nComps,CovarianceType type) {
		this.gmm = new GaussianMixtureModelEM(nComps, type);
	}
	
	@Override
	public MixtureOfGaussians apply(LocalFeatureList<? extends LocalFeature<?,? extends FeatureVector>> features) {
		System.out.println("Creating double array...");
		double[][] doubleFeatures = new double[features.size()][];
		int i = 0;
		for (LocalFeature<?,?> localFeature : features) {			
			doubleFeatures[i] = ArrayUtils.divide(localFeature.getFeatureVector().asDoubleVector(), 128);
			i++;
		}
		System.out.println(String.format("Launching EM with double array: %d x %d",doubleFeatures.length,doubleFeatures[0].length));
		return this.gmm.estimate(new Matrix(doubleFeatures));
	}



}
