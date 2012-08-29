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
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.mask.AbstractMaskedObject;

/**
 * A two-valued summary representing mean hue (in radians) and variance of hue
 * respectively. Additionally, can produce a classification for black & white
 * versus colour versus sepia images based on hand-coded (and not well tested)
 * parameters.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class HueStats extends AbstractMaskedObject<FImage>
		implements
			ImageAnalyser<MBFImage>,
			FeatureVectorProvider<DoubleFV>
{
	double mean_x = 0;
	double m2_x = 0;
	double mean_y = 0;
	double m2_y = 0;
	int n = 0;

	/**
	 * Construct with no mask set
	 */
	public HueStats() {
		super();
	}

	/**
	 * Construct with a mask.
	 * 
	 * @param mask
	 *            the mask.
	 */
	public HueStats(FImage mask) {
		super(mask);
	}

	@Override
	public void analyseImage(MBFImage image) {
		// reset vars in case we're reused
		mean_x = 0;
		m2_x = 0;
		mean_y = 0;
		m2_y = 0;
		n = 0;

		final FImage hue = Transforms.calculateHue(image);

		for (int j = 0; j < hue.height; j++) {
			for (int i = 0; i < hue.width; i++) {
				if (mask != null && mask.pixels[j][i] == 0)
					continue;

				final double angle = hue.pixels[j][i];

				final double x = Math.cos(2 * Math.PI * angle);
				final double y = Math.sin(2 * Math.PI * angle);

				n++;
				final double delta_x = x - mean_x;
				final double delta_y = y - mean_y;
				mean_x += delta_x / n;
				mean_y += delta_y / n;

				m2_x += delta_x * (x - mean_x);
				m2_y += delta_y * (y - mean_y);
			}
		}
	}

	/**
	 * Get the mean hue value.
	 * 
	 * @return the mean hue value over all pixels.
	 */
	public double getMeanHue() {
		return Math.atan2(mean_y, mean_x);
	}

	/**
	 * Get the variance in hue value.
	 * 
	 * @return the variance of hue over all pixels.
	 */
	public double getHueVariance() {
		final double var_x = m2_x / n;
		final double var_y = m2_y / n;

		return var_y * var_x;
	}

	/**
	 * Tonal attributes for images based on the mean hue and variance.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum ToneAttr {
		/**
		 * Sepia toned image.
		 */
		SEPIA,
		/**
		 * Black and white image.
		 */
		BLACK_AND_WHITE,
		/**
		 * Colour image
		 */
		COLOR;

		/**
		 * Estimate the tone from the given mean and variance of the hue. This
		 * is hand-crafted and not well tested. A variance bigger than 5e-4 is
		 * taken to imply a colour image. If the variance is less than 5e-4 and
		 * the mean hue is between -0.1 and 0.1 radians, then it is assumed the
		 * image is back and white. If the variance is less than 5e-4 and the
		 * mean hue is between -0.6 and 0.8 radians, then it is assumed the
		 * image is sepia toned.
		 * 
		 * @param mean
		 *            the mean hue
		 * @param var
		 *            the variance in hue
		 * @return the estimated tone
		 */
		public static ToneAttr getAttr(double mean, double var) {
			if (var < 5e-4) {
				if (mean > -0.1 && mean < 0.1)
					return BLACK_AND_WHITE;
				if (mean > 0.6 && mean < 0.8)
					return SEPIA;
			}
			return COLOR;
		}
	}

	/**
	 * Estimate the tone of the image.
	 * 
	 * @see ToneAttr#getAttr(double, double)
	 * 
	 * @return the estimated tone
	 */
	public ToneAttr getTone() {
		return ToneAttr.getAttr(getMeanHue(), getHueVariance());
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { getMeanHue(), getHueVariance() });
	}
}
