package org.openimaj.demos.sandbox;

import java.io.IOException;

import org.openimaj.OpenIMAJ;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;

public class SIFTDemo {
	public static void main(String[] args) throws IOException {
		final FImage image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		for (final Keypoint keypoint : engine.findFeatures(image)) {
			System.out.println(keypoint);
		}
	}
}
