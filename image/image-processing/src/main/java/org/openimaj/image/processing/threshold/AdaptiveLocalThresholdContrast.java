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
package org.openimaj.image.processing.threshold;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.FilterSupport;
import org.openimaj.image.processing.algorithm.MinMaxAnalyser;

/**
 * Adaptive local thresholding using the local contrast. Pixels are set to 1 if
 * they are closer to the local maximum rather than the local minimum.
 *
 * @see <a
 *      href="http://fiji.sc/wiki/index.php/Auto_Local_Threshold">http://fiji.sc/wiki/index.php/Auto_Local_Threshold</a>
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class AdaptiveLocalThresholdContrast extends AbstractLocalThreshold {
	/**
	 * Construct the thresholding operator with the given patch size (assumed
	 * square)
	 *
	 * @param size
	 *            size of the local image patch
	 */
	public AdaptiveLocalThresholdContrast(int size) {
		super(size);
	}

	/**
	 * Construct the thresholding operator with the given patch size
	 *
	 * @param size_x
	 *            width of patch
	 * @param size_y
	 *            height of patch
	 */
	public AdaptiveLocalThresholdContrast(int size_x, int size_y) {
		super(size_x, size_y);
	}

	@Override
	public void processImage(FImage image) {
		final MinMaxAnalyser minimax = new MinMaxAnalyser(FilterSupport.createBlockSupport(sizeX, sizeY));
		minimax.analyseImage(image);

		final float[][] minpix = minimax.min.pixels;
		final float[][] maxpix = minimax.max.pixels;
		final float[][] ipix = image.pixels;

		for (int y = 0; y < image.height; y++)
			for (int x = 0; x < image.width; x++)
				ipix[y][x] = (ipix[y][x] - minpix[y][x]) > (maxpix[y][x] - ipix[y][x]) ? 1 : 0;
	}
}
