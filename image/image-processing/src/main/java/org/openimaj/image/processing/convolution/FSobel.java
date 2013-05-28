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
package org.openimaj.image.processing.convolution;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * Helper {@link ImageAnalyser} that computes the X and Y image gradients using
 * Sobel filters. Optionally, the input image can be blurred first using a
 * Gaussian.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FSobel implements ImageAnalyser<FImage> {
	private float sigma;

	/**
	 * The X gradients
	 */
	public FImage dx;

	/**
	 * The Y gradients
	 */
	public FImage dy;

	/**
	 * Construct with no Gaussian blurring
	 */
	public FSobel() {
		this(0);
	}

	/**
	 * Construct with an initial Gaussian blurring of the given standard
	 * deviation.
	 * 
	 * @param sigma
	 *            the standard deviation of the Gaussian blur
	 */
	public FSobel(float sigma) {
		this.sigma = sigma;
	}

	@Override
	public void analyseImage(FImage image) {
		final FImage tmp = sigma == 0 ? image : image.process(new FGaussianConvolve(sigma));
		dx = tmp.process(new FSobelX());
		dy = tmp.process(new FSobelY());
	}
}
