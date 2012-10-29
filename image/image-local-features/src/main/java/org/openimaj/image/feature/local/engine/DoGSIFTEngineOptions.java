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
package org.openimaj.image.feature.local.engine;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveExtremaFinder;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Options for controlling SIFT feature localisation and extraction. Default
 * values are based on Lowe's papers.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            Type of image processed by the {@link Engine} associated with
 *            these options.
 */
public class DoGSIFTEngineOptions<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends GaussianPyramidOptions<IMAGE>
{
	/**
	 * The threshold on the ratio of the Eigenvalues of the Hessian matrix (Lowe
	 * IJCV, p.12)
	 */
	protected float eigenvalueRatio = BasicOctaveExtremaFinder.DEFAULT_EIGENVALUE_RATIO;

	/** Threshold on the magnitude of detected points (Lowe IJCV, p.11) */
	protected float magnitudeThreshold = BasicOctaveExtremaFinder.DEFAULT_MAGNITUDE_THRESHOLD;

	/**
	 * The magnification factor determining the size of a spatial SIFT bin
	 * relative to the scale. The overall sampling size is related to the number
	 * of spatial bins.
	 */
	protected float magnificationFactor = 3;

	/**
	 * Threshold for peak detection in the orientation histogram. A value of 1.0
	 * would result in only a single peak being detected.
	 */
	protected float peakThreshold = DominantOrientationExtractor.DEFAULT_PEAK_THRESHOLD;

	/**
	 * The number of orientation histogram bins for finding the dominant
	 * orientations; Lowe's IJCV paper (p.13) suggests 36 bins.
	 */
	protected int numOriHistBins = 36;

	/**
	 * The value for weighting the scaling Gaussian of the orientation histogram
	 * relative to the keypoint scale. Lowe's IJCV paper (p.13) suggests 1.5.
	 */
	protected float scaling = 1.5f;

	/**
	 * The number of iterations of the smoothing filter. The vlfeat SIFT
	 * implementation uses 6.
	 */
	protected int smoothingIterations = 6;

	/**
	 * The size of the sampling window relative to the sampling scale. Lowe's
	 * ICCV paper suggests 3;
	 */
	protected float samplingSize = 3.0f;

	/** The number of orientation bins (default 8) */
	protected int numOriBins = 8;

	/** The number of spatial bins in each direction (default 4) */
	protected int numSpatialBins = 4;

	/** Threshold for the maximum value allowed in the histogram (default 0.2) */
	protected float valueThreshold = 0.2f;

	/**
	 * The width of the Gaussian used for weighting samples, relative to the
	 * half-width of the sampling window (default 1.0).
	 */
	protected float gaussianSigma = 1.0f;

	/**
	 * Get the threshold on the ratio of the Eigenvalues of the Hessian matrix
	 * (Lowe IJCV, p.12)
	 * 
	 * @return the eigenvalue ratio threshold
	 */
	public float getEigenvalueRatio() {
		return eigenvalueRatio;
	}

	/**
	 * Set the threshold on the ratio of the Eigenvalues of the Hessian matrix
	 * (Lowe IJCV, p.12)
	 * 
	 * @param eigenvalueRatio
	 *            the eigenvalueRatio to set
	 */
	public void setEigenvalueRatio(float eigenvalueRatio) {
		this.eigenvalueRatio = eigenvalueRatio;
	}

	/**
	 * Get the threshold on the magnitude of detected points (Lowe IJCV, p.11)
	 * 
	 * @return the magnitude threshold
	 */
	public float getMagnitudeThreshold() {
		return magnitudeThreshold;
	}

	/**
	 * Set the threshold on the magnitude of detected points (Lowe IJCV, p.11)
	 * 
	 * @param magnitudeThreshold
	 *            the magnitude threshold to set
	 */
	public void setMagnitudeThreshold(float magnitudeThreshold) {
		this.magnitudeThreshold = magnitudeThreshold;
	}

	/**
	 * Get the magnification factor determining the size of a spatial SIFT bin
	 * relative to the scale. The overall sampling size is related to the number
	 * of spatial bins.
	 * 
	 * @return the magnification factor
	 */
	public float getMagnificationFactor() {
		return magnificationFactor;
	}

	/**
	 * Set the magnification factor determining the size of a spatial SIFT bin
	 * relative to the scale. The overall sampling size is related to the number
	 * of spatial bins.
	 * 
	 * @param magnificationFactor
	 *            the magnification factor to set
	 */
	public void setMagnificationFactor(float magnificationFactor) {
		this.magnificationFactor = magnificationFactor;
	}

	/**
	 * Get the threshold for peak detection in the orientation histogram. A
	 * value of 1.0 would result in only a single peak being detected.
	 * 
	 * @return the peak detection threshold
	 */
	public float getPeakThreshold() {
		return peakThreshold;
	}

	/**
	 * Set the threshold for peak detection in the orientation histogram. A
	 * value of 1.0 would result in only a single peak being detected.
	 * 
	 * @param peakThreshold
	 *            the peak detection threshold to set
	 */
	public void setPeakThreshold(float peakThreshold) {
		this.peakThreshold = peakThreshold;
	}

	/**
	 * Get the number of orientation histogram bins for finding the dominant
	 * orientations; Lowe's IJCV paper (p.13) suggests 36 bins.
	 * 
	 * @return the number of orientation histogram bins
	 */
	public int getNumOriHistBins() {
		return numOriHistBins;
	}

	/**
	 * Set the number of orientation histogram bins for finding the dominant
	 * orientations; Lowe's IJCV paper (p.13) suggests 36 bins.
	 * 
	 * @param numOriHistBins
	 *            the number of orientation histogram bins to set
	 */
	public void setNumOriHistBins(int numOriHistBins) {
		this.numOriHistBins = numOriHistBins;
	}

	/**
	 * Get the value for weighting the scaling Gaussian of the orientation
	 * histogram relative to the keypoint scale. Lowe's IJCV paper (p.13)
	 * suggests 1.5.
	 * 
	 * @return the scaling amount
	 */
	public float getScaling() {
		return scaling;
	}

	/**
	 * Set the value for weighting the scaling Gaussian of the orientation
	 * histogram relative to the keypoint scale. Lowe's IJCV paper (p.13)
	 * suggests 1.5.
	 * 
	 * @param scaling
	 *            the scaling amount to set
	 */
	public void setScaling(float scaling) {
		this.scaling = scaling;
	}

	/**
	 * Get the number of iterations of the smoothing filter. The vlfeat SIFT
	 * implementation uses 6.
	 * 
	 * @return the number of smoothing iterations
	 */
	public int getSmoothingIterations() {
		return smoothingIterations;
	}

	/**
	 * Set the number of iterations of the smoothing filter. The vlfeat SIFT
	 * implementation uses 6.
	 * 
	 * @param smoothingIterations
	 *            the number of smoothing iterations to set
	 */
	public void setSmoothingIterations(int smoothingIterations) {
		this.smoothingIterations = smoothingIterations;
	}

	/**
	 * Get the size of the sampling window relative to the sampling scale.
	 * Lowe's ICCV paper suggests 3;
	 * 
	 * @return the sampling size
	 */
	public float getSamplingSize() {
		return samplingSize;
	}

	/**
	 * Set the size of the sampling window relative to the sampling scale.
	 * Lowe's ICCV paper suggests 3;
	 * 
	 * @param samplingSize
	 *            the sampling size to set
	 */
	public void setSamplingSize(float samplingSize) {
		this.samplingSize = samplingSize;
	}

	/**
	 * Get the number of orientation bins (default 8) in the SIFT feature
	 * 
	 * @return the number of orientation bins
	 */
	public int getNumOriBins() {
		return numOriBins;
	}

	/**
	 * Set the number of orientation bins (default 8) in the SIFT feature
	 * 
	 * @param numOriBins
	 *            the number of orientation bins to set
	 */
	public void setNumOriBins(int numOriBins) {
		this.numOriBins = numOriBins;
	}

	/**
	 * Get the number of spatial bins in each direction (default 4) in the SIFT
	 * feature
	 * 
	 * @return the number of spatial bins
	 */
	public int getNumSpatialBins() {
		return numSpatialBins;
	}

	/**
	 * Set the number of spatial bins in each direction (default 4) in the SIFT
	 * feature
	 * 
	 * @param numSpatialBins
	 *            the number of spatial bins to set
	 */
	public void setNumSpatialBins(int numSpatialBins) {
		this.numSpatialBins = numSpatialBins;
	}

	/**
	 * Get the threshold for the maximum value allowed in the histogram (default
	 * 0.2)
	 * 
	 * @return the threshold
	 */
	public float getValueThreshold() {
		return valueThreshold;
	}

	/**
	 * Set the threshold for the maximum value allowed in the histogram (default
	 * 0.2)
	 * 
	 * @param valueThreshold
	 *            the threshold to set
	 */
	public void setValueThreshold(float valueThreshold) {
		this.valueThreshold = valueThreshold;
	}

	/**
	 * Get the width of the Gaussian used for weighting samples, relative to the
	 * half-width of the sampling window (default 1.0).
	 * 
	 * @return the Gaussian width
	 */
	public float getGaussianSigma() {
		return gaussianSigma;
	}

	/**
	 * Get the width of the Gaussian used for weighting samples, relative to the
	 * half-width of the sampling window (default 1.0).
	 * 
	 * @param gaussianSigma
	 *            the Gaussian width to set
	 */
	public void setGaussianSigma(float gaussianSigma) {
		this.gaussianSigma = gaussianSigma;
	}
}
