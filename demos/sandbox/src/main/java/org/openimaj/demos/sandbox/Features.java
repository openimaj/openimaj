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
package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.AvgBrightness;
import org.openimaj.image.feature.global.Colorfulness;
import org.openimaj.image.feature.global.Naturalness;
import org.openimaj.image.feature.global.RGBRMSContrast;
import org.openimaj.image.feature.global.RMSContrast;
import org.openimaj.image.feature.global.Saturation;
import org.openimaj.image.feature.global.SaturationVariation;
import org.openimaj.image.feature.global.Sharpness;
import org.openimaj.image.feature.global.SharpnessVariation;

public class Features {
	public static void main(String[] args) throws IOException {
		final AvgBrightness brightness = new AvgBrightness();
		final Colorfulness colorfulness = new Colorfulness();
		final Naturalness naturalness = new Naturalness();
		final RMSContrast contrast = new RMSContrast();
		final RGBRMSContrast rgbcontrast = new RGBRMSContrast();
		final Sharpness sharpness = new Sharpness();
		final SharpnessVariation sharpnessVariation = new SharpnessVariation();
		final Saturation saturation = new Saturation();
		final SaturationVariation saturationVariation = new SaturationVariation();

		final File[] files = new File("/Users/jon/Dropbox/features/images").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg");
			}
		});

		for (final File f : files) {
			final MBFImage image = ImageUtilities.readMBF(f);
			final FImage fimage = Transforms.calculateIntensity(image);

			brightness.analyseImage(image);
			System.out.println(f.getName() + "\tbrightness\t" + brightness.getBrightness());

			image.analyseWith(colorfulness);
			System.out.println(f.getName() + "\tcolorfulness\t" + colorfulness.getColorfulness());

			naturalness.analyseImage(image);
			System.out.println(f.getName() + "\tnaturalness\t" + naturalness.getNaturalness());

			contrast.analyseImage(fimage);
			System.out.println(f.getName() + "\trms-contrast\t" + contrast.getContrast());

			rgbcontrast.analyseImage(image);
			System.out.println(f.getName() + "\trgb-rms-contrast\t" + rgbcontrast.getContrast());

			sharpness.analyseImage(fimage);
			System.out.println(f.getName() + "\tsharpness\t" + sharpness.getSharpness());

			sharpnessVariation.analyseImage(fimage);
			System.out.println(f.getName() + "\tsharpness-variation\t" + sharpnessVariation.getSharpnessVariation());

			saturation.analyseImage(image);
			System.out.println(f.getName() + "\tsaturation\t" + saturation.getSaturation());

			saturationVariation.analyseImage(image);
			System.out.println(f.getName() + "\tsaturation-variation\t" + saturationVariation.getSaturationVariation());
		}
	}
}
