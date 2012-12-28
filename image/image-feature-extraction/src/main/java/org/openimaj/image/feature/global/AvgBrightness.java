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
package org.openimaj.image.feature.global;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.mask.AbstractMaskedObject;

/**
 * Extract the average brightness of an image. Brightness can be computed in a
 * number of different ways, depending on the chosen {@link Mode}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class AvgBrightness extends AbstractMaskedObject<FImage>
		implements
		ImageAnalyser<MBFImage>,
		FeatureVectorProvider<DoubleFV>
{
	/**
	 * Modes for computing brightness.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum Mode {
		/**
		 * Luminance using the NTSC weighting scheme (equivalent to the Y in the
		 * YUV colour space)
		 */
		NTSC_LUMINANCE {
			@Override
			public double computeBrightness(MBFImage image, FImage mask) {
				final FImage R = image.getBand(0);
				final FImage G = image.getBand(1);
				final FImage B = image.getBand(2);

				double brightness = 0;

				if (mask != null) {
					for (int y = 0; y < R.height; y++) {
						for (int x = 0; x < R.width; x++) {
							if (mask.pixels[y][x] == 1)
								brightness += (0.299f * R.pixels[y][x] + 0.587f * G.pixels[y][x] + 0.114f * B.pixels[y][x]);
						}
					}
				} else {
					for (int y = 0; y < R.height; y++)
						for (int x = 0; x < R.width; x++)
							brightness += (0.299f * R.pixels[y][x] + 0.587f * G.pixels[y][x] + 0.114f * B.pixels[y][x]);
				}

				return brightness / (R.height * R.width);
			}
		};

		/**
		 * Compute the average brightness of the given image (applying the mask
		 * if it's not <code>null</code>).
		 * 
		 * @param img
		 *            the image to extract the average brightness from
		 * @param mask
		 *            the mask
		 * @return the average brightness
		 */
		public abstract double computeBrightness(MBFImage img, FImage mask);
	}

	private Mode mode;
	private double brightness;

	/**
	 * Construct with the NTSC_LUMINANCE mode and no mask set
	 */
	public AvgBrightness() {
		this(Mode.NTSC_LUMINANCE, null);
	}

	/**
	 * Construct with the given mode and no mask set
	 * 
	 * @param mode
	 *            the {@link Mode}
	 */
	public AvgBrightness(Mode mode) {
		this(mode, null);
	}

	/**
	 * Construct with the given mode and a mask.
	 * 
	 * @param mode
	 *            the {@link Mode}
	 * @param mask
	 *            the mask.
	 */
	public AvgBrightness(Mode mode, FImage mask) {
		super(mask);

		this.mode = mode;
	}

	@Override
	public void analyseImage(MBFImage image) {
		brightness = mode.computeBrightness(image, mask);
	}

	/**
	 * Get the brightness.
	 * 
	 * @return the brightness
	 */
	public double getBrightness() {
		return brightness;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { brightness });
	}
}
