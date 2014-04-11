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

import java.lang.reflect.Array;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.Octave;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * This class represents a Gaussian octave in the style of Lowe's SIFT paper.
 * 
 * The size of the image stack is controlled by the parameters scales and
 * extraScaleSteps. The stack is constructed such that images[0] is the initial
 * image, and images[scales] has twice the blur of the initial image. The sigma
 * of the initial image is the parameter initialSigma.
 * 
 * Octaves are Iterable for easy access to each of the images in turn.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            Type of underlying image
 */
public class GaussianOctave<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends
		Octave<GaussianPyramidOptions<IMAGE>, GaussianPyramid<IMAGE>, IMAGE>
{

	/**
	 * Construct a Gaussian octave with the provided parent Pyramid and
	 * octaveSize. The octaveSize parameter is the size of the octave's images
	 * compared to the original image used to construct the pyramid. An
	 * octaveSize of 1 means the same size as the original, 2 means half size, 4
	 * means quarter size, etc.
	 * 
	 * @param parent
	 *            the pyramid that this octave belongs to
	 * @param octaveSize
	 *            the size of the octave relative to the original image.
	 */
	public GaussianOctave(GaussianPyramid<IMAGE> parent, float octaveSize) {
		super(parent, octaveSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processing.pyramid.AbstractOctave#process(org.openimaj
	 * .image.Image)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void process(IMAGE image) {
		images = (IMAGE[]) Array.newInstance(image.getClass(), options.scales + options.extraScaleSteps + 1);

		// we want to each level to be separated by a constant factor
		// k=2^(1/scales)
		final float k = (float) Math.pow(2.0, 1.0 / options.scales);

		// image[0] of the octave is the input image
		images[0] = image;

		// the intial (input) image is considered to have sigma initialSigma.
		float prevSigma = options.initialSigma;

		for (int i = 1; i < options.scales + options.extraScaleSteps + 1; i++) {
			images[i] = images[i - 1].clone();

			// compute the amount to increase from prevSigma to prevSigma*k
			final float increase = prevSigma * (float) Math.sqrt(k * k - 1.0);

			images[i].processInplace(options.createGaussianBlur(increase));

			prevSigma *= k;
		}

		// if a processor is defined, apply it
		if (options.getOctaveProcessor() != null)
			options.getOctaveProcessor().process(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processing.pyramid.AbstractOctave#getNextOctaveImage()
	 */
	@Override
	public IMAGE getNextOctaveImage() {
		return images[options.scales];
	}
}
