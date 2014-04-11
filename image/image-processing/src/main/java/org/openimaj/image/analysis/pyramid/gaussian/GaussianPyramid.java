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
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.pyramid.Pyramid;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * A Gaussian image pyramid consisting of a stack of octaves where the image
 * halves its size. The pyramid is of the style described in Lowe's SIFT paper.
 * 
 * Octaves are processed by an OctaveProcessor as they are created if the
 * processor is set in the options object.
 * 
 * The pyramid will only hold onto its octaves if either the keepOctaves option
 * is set to true, or if a PyramidProcessor is set in the options. The
 * PyramidProcessor will called after all the octaves are created.
 * 
 * Pyramids are Iterable for easy access to the octaves; however this will only
 * work if the pyramid has already been populated with the octaves retained.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            Type of underlying image
 */
public class GaussianPyramid<I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>>
		extends
		Pyramid<GaussianPyramidOptions<I>, GaussianOctave<I>, I>
		implements
		ImageAnalyser<I>, Iterable<GaussianOctave<I>>
{
	/**
	 * Construct a Pyramid with the given options.
	 * 
	 * @param options
	 *            the options
	 */
	public GaussianPyramid(GaussianPyramidOptions<I> options) {
		super(options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processing.pyramid.AbstractPyramid#process(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void process(I img) {
		if (img.getWidth() <= 1 || img.getHeight() <= 1)
			throw new IllegalArgumentException("Image is too small");

		// the octave image size: 1 means same as input, 0.5 is twice as big as
		// input, 2 is half input, 4 is quarter input, etc
		float octaveSize = 1.0f;

		// if doubleInitialImage is set, then the initial image should be scaled
		// to
		// twice its original size and the
		I image;
		if (options.doubleInitialImage) {
			image = ResizeProcessor.doubleSize(img);
			octaveSize *= 0.5;
		} else
			image = img.clone();

		// Lowe's IJCV paper (P.10) suggests that if you double the size of the
		// initial image then it has a sigma of 1.0; if the image is not doubled
		// the sigma is 0.5
		final float currentSigma = (options.doubleInitialImage ? 1.0f : 0.5f);
		if (options.initialSigma > currentSigma) {
			// we now need to bring the starting image to a sigma of
			// initialSigma
			// in order to start building the pyramid (every octave starts at
			// initialSigma sigmas).
			final float sigma = (float) Math.sqrt(options.initialSigma * options.initialSigma - currentSigma
					* currentSigma);
			image.processInplace(this.options.createGaussianBlur(sigma));
		}

		// the minimum size image in the pyramid must be bigger than
		// two pixels + whatever border is required by the options
		// (on both sides).
		final int minImageSize = 2 + (2 * options.getBorderPixels());

		while (image.getHeight() > minImageSize && image.getWidth() > minImageSize) {
			// construct empty octave
			final GaussianOctave<I> currentOctave = new GaussianOctave<I>(this, octaveSize);

			// populate the octave with images; once the octave
			// is complete any OctaveProcessor specified in the
			// options will be applied.
			currentOctave.process(image);

			// get the image with 2*sigma from the octave and
			// half its size ready for the next octave
			image = ResizeProcessor.halfSize(currentOctave.getNextOctaveImage());

			octaveSize *= 2.0; // the size of the octave increases by a factor
								// of two each iteration

			// if the octaves array is not null we want to retain each octave.
			if (octaves != null)
				octaves.add(currentOctave);
		}

		// if a PyramidProcessor was specified in the options it should
		// be applied now all the octaves are complete.
		if (options.getPyramidProcessor() != null) {
			options.getPyramidProcessor().process(this);
		}
	}
}
