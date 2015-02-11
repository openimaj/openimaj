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
package org.openimaj.image.feature.local.affine;

import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.engine.DoGColourSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngineOptions;
import org.openimaj.image.feature.local.engine.Engine;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Colour Affine-simulated SIFT (CASIFT). A {@link DoGColourSIFTEngine} is used
 * internally to find the features. Specifically, the
 * {@link DoGColourSIFTEngine} creates a luminance image from which to apply the
 * difference-of-Gaussian interest point detection algorithm, but extracts the
 * actual SIFT features from the bands of the input image directly. This means
 * that the type of Colour-SIFT feature is controlled directly by the
 * colour-space of the input image; for example if an RGB image is given as
 * input, then the feature will be standard RGB-SIFT.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ColourASIFT extends ASIFT<MBFImage, Float[]> {
	/**
	 * Construct with the given options for the internal {@link DoGSIFTEngine}.
	 *
	 * @param opts
	 */
	public ColourASIFT(DoGSIFTEngineOptions<MBFImage> opts) {
		super(opts);
	}

	/**
	 * Construct the ASIFT extractor using the default parameters for the
	 * {@link DoGSIFTEngine}, with the exception of the option to double the
	 * size of the initial image which can be overridden.
	 *
	 * @see DoGSIFTEngineOptions#setDoubleInitialImage(boolean)
	 *
	 * @param hires
	 *            if true, then the input image is doubled in size before the
	 *            SIFT features are extracted.
	 */
	public ColourASIFT(boolean hires) {
		super(hires);
	}

	@Override
	public Engine<Keypoint, MBFImage> constructEngine(DoGSIFTEngineOptions<MBFImage> opts) {
		return new DoGColourSIFTEngine(opts);
	}
}
