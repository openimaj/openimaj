package org.openimaj.feature.local.filter;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.ByteFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.util.filter.Filter;

/**
 * Filter {@link LocalFeature}s typed on {@link ByteFV} by rejecting
 * those that have a low feature entropy. Such features are those
 * that tend to have little variation; for example, in the case of
 * SIFT features, the removed features are typically the ones that
 * mismatch easily.
 * <p>
 * This filter is an implementation of the approach 
 * described by Dong, Wang and Li; the default threshold is taken
 * from the paper, and will work with standard SIFT features, such 
 * as those produced by a {@link DoGSIFTEngine}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		author = { "Wei Dong", "Zhe Wang", "Kai Li" }, 
		title = "High-Confidence Near-Duplicate Image Detection", 
		type = ReferenceType.Inproceedings, 
		year = "2012",
		booktitle = "ACM International Conference on Multimedia Retrieval",
		customData = { "location", "Hong Kong, China" }
		)
public class ByteEntropyFilter implements Filter<LocalFeature<ByteFV>> {
	double threshold = 4.4;
	
	@Override
	public boolean accept(LocalFeature<ByteFV> object) {
		return entropy(object.getFeatureVector().values) > threshold;
	}

	/**
	 * Compute the entropy of the given byte vector.
	 * @param vector the vector.
	 * @return the entropy.
	 */
	public static double entropy(byte[] vector) {
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
