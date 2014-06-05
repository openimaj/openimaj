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
package org.openimaj.image.processing.convolution.filterbank;

import org.openimaj.image.FImage;

/**
 * Implementation of the MR8 filter bank described at:
 * http://www.robots.ox.ac.uk/~vgg/research/texclass/filters.html
 * 
 * This is the naive implementation and as such is quite slow.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MR8FilterBank extends RootFilterSetFilterBank {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		super.analyseImage(image);

		final FImage[] allresponses = responses;
		responses = new FImage[8];

		int allIndex = 0;
		int idx = 0;
		for (int type = 0; type < 2; type++) {
			for (int scale = 0; scale < SCALES.length; scale++) {
				responses[idx] = allresponses[allIndex];
				allIndex++;

				for (int orient = 1; orient < NUM_ORIENTATIONS; orient++) {
					for (int y = 0; y < image.height; y++) {
						for (int x = 0; x < image.width; x++) {
							responses[idx].pixels[y][x] = Math.max(responses[idx].pixels[y][x],
									allresponses[allIndex].pixels[y][x]);
						}
					}

					allIndex++;
				}

				idx++;
			}
		}

		responses[idx++] = allresponses[allIndex++];
		responses[idx++] = allresponses[allIndex++];
	}
}
