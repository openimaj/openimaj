package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastEuclideanKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.util.filter.FilterUtils;

public class SIFTEntropy {
	public static void main(String[] args) throws IOException {
		final FImage image1 = ImageUtilities.readF(new File("/Users/jsh2/Data/ukbench/full/ukbench00000.jpg"));
		final FImage image2 = ImageUtilities.readF(new File("/Users/jsh2/Data/ukbench/full/ukbench00001.jpg"));

		final DoGSIFTEngine engine = new DoGSIFTEngine();

		final LocalFeatureList<Keypoint> keys1 = engine.findFeatures(ResizeProcessor.resizeMax(image1, 150));
		final LocalFeatureList<Keypoint> keys2 = engine.findFeatures(ResizeProcessor.resizeMax(image2, 150));

		final List<Keypoint> keys1f = FilterUtils.filter(keys1, new ByteEntropyFilter());
		final List<Keypoint> keys2f = FilterUtils.filter(keys2, new ByteEntropyFilter());

		System.out.println(keys1.size() + " " + keys1f.size());
		System.out.println(keys2.size() + " " + keys2f.size());

		final FastEuclideanKeypointMatcher<Keypoint> matcher = new FastEuclideanKeypointMatcher<Keypoint>(8000);
		matcher.setModelFeatures(keys1);
		matcher.findMatches(keys2);
		System.out.println(matcher.getMatches().size());
		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1F));

		matcher.setModelFeatures(keys1f);
		matcher.findMatches(keys2f);
		System.out.println(matcher.getMatches().size());
		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1F));
	}
}
