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

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.mask.AbstractMaskedObject;

/**
 * Implementation of the Naturalness index (CNI) defined by Huang, Qiao & Wu.
 * The CNI is a single valued summary of how natural the colours in an image
 * are. The index is a value between 0 and 1. Higher values indicate that the
 * colours in the image are more natural .
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Huang, Kai-Qi", "Wang, Qiao", "Wu, Zhen-Yang" },
		title = "Natural color image enhancement and evaluation algorithm based on human visual system",
		year = "2006",
		journal = "Comput. Vis. Image Underst.",
		pages = { "52", "", "63" },
		url = "http://dx.doi.org/10.1016/j.cviu.2006.02.007",
		month = "jul",
		number = "1",
		publisher = "Elsevier Science Inc.",
		volume = "103")
public class Naturalness extends AbstractMaskedObject<FImage>
		implements
		ImageAnalyser<MBFImage>,
		FeatureVectorProvider<DoubleFV>
{
	private final static double grassLower = 95.0 / 360.0;
	private final static double grassUpper = 135.0 / 360.0;
	private final static double skinLower = 25.0 / 360.0;
	private final static double skinUpper = 70.0 / 360.0;
	private final static double skyLower = 185.0 / 360.0;
	private final static double skyUpper = 260.0 / 360.0;

	private final static double satLower = 0.1;

	private final static double lightnessLower = 20.0 / 100.0;
	private final static double lightnessUpper = 80.0 / 100.0;

	private double skyMean = 0;
	private int skyN = 0;

	private double skinMean = 0;
	private int skinN = 0;

	private double grassMean = 0;
	private int grassN = 0;

	/**
	 * Construct with no mask set
	 */
	public Naturalness() {
		super();
	}

	/**
	 * Construct with a mask.
	 * 
	 * @param mask
	 *            the mask.
	 */
	public Naturalness(FImage mask) {
		super(mask);
	}

	@Override
	public void analyseImage(MBFImage image) {
		// reset vars in case we're reused
		skyMean = 0;
		skyN = 0;
		skinMean = 0;
		skinN = 0;
		grassMean = 0;
		grassN = 0;

		final MBFImage hsl = Transforms.RGB_TO_HSL(image);

		final FImage H = (hsl).getBand(0);
		final FImage S = (hsl).getBand(1);
		final FImage L = (hsl).getBand(2);

		for (int y = 0; y < H.height; y++) {
			for (int x = 0; x < H.width; x++) {
				if (mask != null && mask.pixels[y][x] == 0)
					continue;

				if (lightnessLower <= L.pixels[y][x] && L.pixels[y][x] <= lightnessUpper && S.pixels[y][x] > satLower) {
					final double hue = H.pixels[y][x];
					final double sat = S.pixels[y][x];

					if (skyLower <= hue && hue <= skyUpper) {
						skyMean += sat;
						skyN++;
					}

					if (skinLower <= hue && hue <= skinUpper) {
						skinMean += sat;
						skinN++;
					}

					if (grassLower <= hue && hue <= grassUpper) {
						grassMean += sat;
						grassN++;
					}
				}
			}
		}

		if (skyN != 0)
			skyMean /= skyN;
		if (skinN != 0)
			skinMean /= skinN;
		if (grassN != 0)
			grassMean /= grassN;
	}

	/**
	 * Get the naturalness value for the image previously analysed with
	 * {@link #analyseImage(MBFImage)}.
	 * 
	 * @return the naturalness value
	 */
	public double getNaturalness() {
		final double NSkin = Math.exp(-0.5 * Math.pow((skinMean - 0.76) / (0.52), 2));
		final double NGrass = Math.exp(-0.5 * Math.pow((grassMean - 0.81) / (0.53), 2));
		final double NSky = Math.exp(-0.5 * Math.pow((skyMean - 0.43) / (0.22), 2));

		final double nPixels = skinN + grassN + skyN;

		if (nPixels == 0)
			return 0;

		final double wSkin = skinN / nPixels;
		final double wGrass = grassN / nPixels;
		final double wSky = skyN / nPixels;

		return wSkin * NSkin + wGrass * NGrass + wSky * NSky;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { getNaturalness() });
	}
}
