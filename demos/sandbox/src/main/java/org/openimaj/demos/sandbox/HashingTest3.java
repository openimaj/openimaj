package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.ByteFVComparison;
import org.openimaj.feature.IntFVComparison;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class HashingTest3 {
	public static void main(String[] args) throws IOException {
		FImage im1 = ImageUtilities.readF(new File("/Users/jon/Data/ukbench/full/ukbench00001.jpg"));
		FImage im2 = ImageUtilities.readF(new File("/Users/jon/Data/ukbench/full/ukbench00002.jpg"));

		im1 = ResizeProcessor.resizeMax(im1, 300);
		im2 = ResizeProcessor.resizeMax(im2, 300);

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		final LocalFeatureList<Keypoint> f1 = engine.findFeatures(im1);
		final LocalFeatureList<Keypoint> f2 = engine.findFeatures(im2);

		final HashingTest t = new HashingTest();
		for (final Keypoint k1 : f1) {
			final int[] s1 = t.createSketch(k1.ivec);
			for (final Keypoint k2 : f2) {
				final int[] s2 = t.createSketch(k2.ivec);

				final double e = ByteFVComparison.EUCLIDEAN.compare(k1.ivec, k2.ivec);
				final double h = IntFVComparison.PACKED_HAMMING.compare(s1, s2);

				System.out.println(e + "\t" + h);
			}
		}
	}
}
