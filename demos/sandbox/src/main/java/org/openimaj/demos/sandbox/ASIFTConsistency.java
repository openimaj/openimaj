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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.assignment.hard.ApproximateByteEuclideanAssigner;
import org.openimaj.ml.clustering.kmeans.fast.FastByteKMeans;

public class ASIFTConsistency {
	public static int NUM_HIRES_SIMULATIONS = 5; 
	
	public static void main(String [] args) throws IOException {
		for (int i=0; i<10; i++) {
			long t1 = System.currentTimeMillis();
			final FastByteKMeans cluster = IOUtils.read(new File("/Users/jsh2/mirflickr-sift-fastkmeans-1000000.voc"), FastByteKMeans.class);
			long t2 = System.currentTimeMillis();
			ApproximateByteEuclideanAssigner assigner = new ApproximateByteEuclideanAssigner(cluster);
			long t3 = System.currentTimeMillis();

			System.out.println("Loading took " + (t2-t1) + " ms");
			System.out.println("BTree creation took " + (t3-t2) + " ms");
		}
		
//		final FImage im1 = ImageUtilities.readF(new File("/Volumes/Raid/adige/images/20060318/ZS0103.jpg"));
//		final FImage im2 = ImageUtilities.readF(new File("/Volumes/Raid/adige/images/20060318/TN1205.jpg"));
//		
//		ASIFTEngine lowResEngine = new ASIFTEngine(false);
//		Map<AffineParams, LocalFeatureList<Keypoint>> res1 = lowResEngine.findKeypointsMapped(im1);
//		Map<AffineParams, LocalFeatureList<Keypoint>> res2 = lowResEngine.findKeypointsMapped(im2);
//		
//		final BasicQuantisedKeypointMatcher<QuantisedKeypoint> matcher = new BasicQuantisedKeypointMatcher<QuantisedKeypoint>(false);
//		
//		TObjectIntHashMap<Pair<AffineParams>> counts = new TObjectIntHashMap<Pair<AffineParams>>();
//		for (Entry<AffineParams, LocalFeatureList<Keypoint>> r1 : res1.entrySet()) {
//			for (Entry<AffineParams, LocalFeatureList<Keypoint>> r2 : res2.entrySet()) {
//				matcher.setModelFeatures(quantise(r1.getValue(), cluster));
//				matcher.findMatches(quantise(r2.getValue(), cluster));
//
//				counts.put(new Pair<AffineParams>(r1.getKey(), r2.getKey()), matcher.getMatches().size());
//			}
//		}
		
		
	}
	
	static List<QuantisedKeypoint> quantise(List<Keypoint> keys, ApproximateByteEuclideanAssigner cluster) {
		List<QuantisedKeypoint> qkeys = new ArrayList<QuantisedKeypoint>();
		
		for (Keypoint k : keys)
			qkeys.add(new QuantisedKeypoint(k.getLocation(), cluster.assign(k.ivec)));
		
		return qkeys;
	}
}
