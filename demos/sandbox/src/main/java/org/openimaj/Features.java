package org.openimaj;

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
