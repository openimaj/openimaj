package org.openimaj.demos.sandbox;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.local.matcher.FastEuclideanKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;

public class SIFTEntropy {
	public static void main(String[] args) throws IOException {
		FImage image1 = ImageUtilities.readF(new File("/Users/jsh2/Downloads/ukbench/full/ukbench00000.jpg"));
		FImage image2 = ImageUtilities.readF(new File("/Users/jsh2/Downloads/ukbench/full/ukbench00001.jpg"));
		
		DoGSIFTEngine engine = new DoGSIFTEngine();
		
		LocalFeatureList<Keypoint> keys1 = engine.findFeatures(image1);
		LocalFeatureList<Keypoint> keys2 = engine.findFeatures(image2);
		
		LocalFeatureList<Keypoint> keys1f = new MemoryLocalFeatureList<Keypoint>();
		LocalFeatureList<Keypoint> keys2f = new MemoryLocalFeatureList<Keypoint>();
		
		double thresh = 5.4;
		for (Keypoint k : keys1) if (entropy(k.ivec) > thresh) keys1f.add(k);
		for (Keypoint k : keys2) if (entropy(k.ivec) > thresh) keys2f.add(k);
		
		System.out.println(keys1.size() +" " + keys1f.size());
		System.out.println(keys2.size() +" " + keys2f.size());
		
		FastEuclideanKeypointMatcher<Keypoint> matcher = new FastEuclideanKeypointMatcher<Keypoint>(20000);
		matcher.setModelFeatures(keys1);
		matcher.findMatches(keys2);
		System.out.println(matcher.getMatches().size());
		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1F));
		
		DisplayUtilities.display(KeypointVisualizer.drawPatchesInplace(image1, keys1f, 1f, null));
		
		matcher.setModelFeatures(keys1f);
		matcher.findMatches(keys2f);
		System.out.println(matcher.getMatches().size());
		DisplayUtilities.display(MatchingUtilities.drawMatches(image1, image2, matcher.getMatches(), 1F));
	}
	
	
	static double entropy(byte[] vector) {
		final int[] counts = new int[256];
		for (int i=0; i<vector.length; i++) {
			counts[vector[i] + 128]++;
		}
		
		final double log2 = Math.log(2);
		double entropy = 0;
		for (int b=0; b<counts.length; b++) {
			double p = (double)counts[b] / (double)vector.length;
			
			entropy -= (p == 0 ? 0 : p * Math.log(p)/log2);
		}
		return entropy;
	}
}
