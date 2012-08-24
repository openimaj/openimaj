package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.ByteFVComparison;
import org.openimaj.feature.IntFVComparison;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.util.pair.Pair;

public class HashingTest2 {
	public static void main(String[] args) throws IOException {
		FImage im1 = ImageUtilities.readF(new File("/Users/jon/Data/ukbench/full/ukbench00001.jpg"));
		FImage im2 = ImageUtilities.readF(new File("/Users/jon/Data/ukbench/full/ukbench00002.jpg"));

		im1 = ResizeProcessor.resizeMax(im1, 300);
		im2 = ResizeProcessor.resizeMax(im2, 300);

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		final LocalFeatureList<Keypoint> f1 = engine.findFeatures(im1);
		final LocalFeatureList<Keypoint> f2 = engine.findFeatures(im2);

		final BasicMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(8);

		matcher.setModelFeatures(f1);
		matcher.findMatches(f2);
		final List<Pair<Keypoint>> matches = matcher.getMatches();

		// DisplayUtilities.display(MatchingUtilities.drawMatches(im1, im2,
		// matches, 1f));

		Pair<Keypoint> minPair = null;
		double minDist = 1000;
		for (final Pair<Keypoint> p : matches) {
			final double dist = ByteFVComparison.EUCLIDEAN.compare(p.firstObject().ivec, p.secondObject().ivec);

			if (dist < minDist) {
				minDist = dist;
				minPair = p;
			}
		}

		// System.out.println(minDist);
		// System.out.println(Arrays.toString(minPair.firstObject().ivec));
		// System.out.println(Arrays.toString(minPair.secondObject().ivec));
		//
		// System.out.println(Arrays.toString(HashingTest.logScale(minPair.firstObject().ivec,
		// 0.001F)));
		// System.out.println(Arrays.toString(HashingTest.logScale(minPair.secondObject().ivec,
		// 0.001F)));
		final HashingTest t = new HashingTest();
		final int[] h1 = t.createSketch(minPair.firstObject().ivec);
		final int[] h2 = t.createSketch(minPair.secondObject().ivec);

		printBits(h1);
		printBits(h2);

		System.out.println(IntFVComparison.PACKED_HAMMING.compare(h1, h2));

		printBits(t.createSketch(matches.get(0).firstObject().ivec));
		printBits(t.createSketch(matches.get(0).secondObject().ivec));

		System.out.println(IntFVComparison.PACKED_HAMMING.compare(t.createSketch(matches.get(0).firstObject().ivec),
				t.createSketch(matches.get(0).secondObject().ivec)));

		printBits(t.createSketch(matches.get(1).firstObject().ivec));
		printBits(t.createSketch(matches.get(1).secondObject().ivec));
		System.out.println(IntFVComparison.PACKED_HAMMING.compare(t.createSketch(matches.get(0).firstObject().ivec),
				t.createSketch(matches.get(0).secondObject().ivec)));

		System.out.println(IntFVComparison.PACKED_HAMMING.compare(t.createSketch(matches.get(0).firstObject().ivec),
				t.createSketch(matches.get(1).secondObject().ivec)));

	}

	static void printBits(int[] h) {
		for (final int i : h) {
			final String str = Integer.toBinaryString(i);
			String z = "";
			for (int j = 0; j < 32 - str.length(); j++)
				z += "0";
			System.out.print(z + str + " ");
		}
		System.out.println();
	}
}
