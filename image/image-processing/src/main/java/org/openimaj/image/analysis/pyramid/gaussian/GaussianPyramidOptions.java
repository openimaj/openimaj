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
package org.openimaj.image.analysis.pyramid.gaussian;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.PyramidOptions;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Options for constructing a Gaussian pyramid in the style of Lowe's SIFT
 * paper.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            type of underlying image.
 */
public class GaussianPyramidOptions<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends
		PyramidOptions<GaussianOctave<IMAGE>, IMAGE>
{
	/**
	 * Number of pixels of border for processors to ignore. Also used in
	 * calculating the minimum image size for the last octave.
	 */
	protected int borderPixels = 5;

	/**
	 * Should the starting image of the pyramid be stretched to twice its size?
	 */
	protected boolean doubleInitialImage = true;

	/**
	 * The number of extra scale steps taken beyond scales.
	 */
	protected int extraScaleSteps = 2; // number of extra steps to take beyond
										// doubling sigma

	/**
	 * Assumed initial scale of the first image in each octave. For SIFT, Lowe
	 * suggested 1.6 (for optimal repeatability; see Lowe's IJCV paper, P.10).
	 */
	protected float initialSigma = 1.6f;

	/**
	 * The number of scales in this octave minus extraScaleSteps. Levels are
	 * constructed so that level[scales] has twice the sigma of level[0].
	 */
	protected int scales = 3;

	/**
	 * Default constructor.
	 */
	public GaussianPyramidOptions() {

	}

	/**
	 * Construct the pyramid options by copying the non-processor options from
	 * the given options object.
	 * 
	 * @param options
	 *            options to copy from
	 */
	public GaussianPyramidOptions(GaussianPyramidOptions<?> options) {
		this.borderPixels = options.borderPixels;
		this.doubleInitialImage = options.doubleInitialImage;
		this.extraScaleSteps = options.extraScaleSteps;
		this.initialSigma = options.initialSigma;
		this.keepOctaves = options.keepOctaves;
		this.scales = options.scales;
	}

	/**
	 * Get the number of pixels used for a border that processors shouldn't
	 * touch.
	 * 
	 * @return number of border pixels.
	 */
	public int getBorderPixels() {
		return borderPixels;
	}

	/**
	 * Get the number of extra scale steps taken beyond scales.
	 * 
	 * @see #getScales()
	 * 
	 * @return the extraScaleSteps
	 */
	public int getExtraScaleSteps() {
		return extraScaleSteps;
	}

	/**
	 * Get the assumed initial scale of the first image in each octave.
	 * 
	 * @return the initialSigma
	 */
	public float getInitialSigma() {
		return initialSigma;
	}

	/**
	 * Get the number of scales in this octave minus extraScaleSteps. Levels of
	 * each octave are constructed so that level[scales] has twice the sigma of
	 * level[0].
	 * 
	 * @return the scales
	 */
	public int getScales() {
		return scales;
	}

	/**
	 * Should the starting image of the pyramid be stretched to twice its size?
	 * 
	 * @return the doubleInitialImage
	 */
	public boolean isDoubleInitialImage() {
		return doubleInitialImage;
	}

	/**
	 * Set the number of pixels used for a border that processors shouldn't
	 * touch. Also affects the minimum image size for the last octave, which
	 * must be at least 2 + 2*borderPixels.
	 * 
	 * @param borderPixels
	 *            number of pixels to leave as border
	 */
	public void setBorderPixels(int borderPixels) {
		if (borderPixels < 2)
			throw new IllegalArgumentException("BorderDistance must be >= 2");
		this.borderPixels = borderPixels;
	}

	/**
	 * Set whether starting image of the pyramid be stretched to twice its size?
	 * 
	 * @param doubleInitialImage
	 *            the doubleInitialImage to set
	 */
	public void setDoubleInitialImage(boolean doubleInitialImage) {
		this.doubleInitialImage = doubleInitialImage;
	}

	/**
	 * Set the number of extra scale steps taken beyond scales.
	 * 
	 * @see #setScales(int)
	 * 
	 * @param extraScaleSteps
	 *            the extraScaleSteps to set
	 */
	public void setExtraScaleSteps(int extraScaleSteps) {
		this.extraScaleSteps = extraScaleSteps;
	}

	/**
	 * Set the assumed initial scale of the first image in each octave. For
	 * SIFT, Lowe suggested 1.6 (for optimal repeatability; see Lowe's IJCV
	 * paper, P.10).
	 * 
	 * @param initialSigma
	 *            the initialSigma to set
	 */
	public void setInitialSigma(float initialSigma) {
		this.initialSigma = initialSigma;
	}

	/**
	 * Set the number of scales in this octave minus extraScaleSteps. Levels of
	 * each octave are constructed so that level[scales] has twice the sigma of
	 * level[0].
	 * 
	 * @param scales
	 *            the scales to set
	 */
	public void setScales(int scales) {
		this.scales = scales;
	}

	/**
	 * Create a {@link SinglebandImageProcessor} that performs a Gaussian
	 * blurring with a standard deviation given by sigma. This method is used by
	 * the {@link GaussianOctave} and {@link GaussianPyramid} to create filters
	 * for performing the blurring. By overriding in subclasses, you can control
	 * the exact filter implementation (i.e. for speed).
	 * 
	 * @param sigma
	 *            the gaussian standard deviation
	 * @return the image processor to apply the blur
	 */
	public SinglebandImageProcessor<Float, FImage> createGaussianBlur(float sigma) {
		return new FGaussianConvolve(sigma);
	}
}
