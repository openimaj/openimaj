package org.openimaj.demos.sandbox;

import org.openimaj.OpenIMAJ;
import org.openimaj.aop.classloader.ClassLoaderTransform;
import org.openimaj.citation.ReferenceListener;
import org.openimaj.citation.ReferencesClassTransformer;
import org.openimaj.citation.annotation.output.StandardFormatters;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;

public class SIFTDemoRefs {
	public static void main(String[] args) throws Throwable {
		if (ClassLoaderTransform.run(SIFTDemoRefs.class, args, new ReferencesClassTransformer()))
			return;

		final FImage image = ImageUtilities.readF(OpenIMAJ.getLogoAsStream());

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		for (final Keypoint keypoint : engine.findFeatures(image)) {
			System.out.println(keypoint);
		}

		System.out.println(StandardFormatters.STRING.format(ReferenceListener.getReferences()));
	}
}
