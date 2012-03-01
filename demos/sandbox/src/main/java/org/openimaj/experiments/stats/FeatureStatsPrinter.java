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
package org.openimaj.experiments.stats;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MultipleMatchesMatcher;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.keypoints.Keypoint;

public enum FeatureStatsPrinter{
	FEATURE_COUNT {
		@Override
		public StatsOperation operation() {
			return new StatsOperation(){
				private int count = 0;

				@Override
				public void init(MBFImage image,List<Keypoint> siftFeatures) {
					this.count  = 0;
				}

				@Override
				public void gather(Keypoint k) {
					this.count ++;
				}

				@Override
				public void output(PrintStream writer) {
					writer.print(this.count);
				}
				
			};
		}
	},
	FEATURE_COUNT_NORM {
		@Override
		public StatsOperation operation() {
			return new StatsOperation(){

				private int count = 0;
				private float weight = 1.0f;

				@Override
				public void init(MBFImage image,List<Keypoint> siftFeatures) {
					this.count = 0;
					this.weight = image.getWidth() * image.getHeight();
				}

				@Override
				public void gather(Keypoint k) {
					this.count++;
				}

				@Override
				public void output(PrintStream outputStream) {
					outputStream.print(this.count / this.weight);
				}
				
			};
		}
	},
	MATCHING_FEATURES {
		@Override
		public StatsOperation operation() {
			return new StatsOperation(){
				
				private double nMatches;
				private double nFeatures;

				@Override
				public void init(MBFImage image,List<Keypoint> siftFeatures) {
					LocalFeatureMatcher<Keypoint> matcher = new FastBasicKeypointMatcher<Keypoint>(8);
					matcher.setModelFeatures(siftFeatures);
					List<Keypoint> pertubatedFeatures = Keypoint.addGaussianNoise(siftFeatures,1,2);
					matcher.findMatches(pertubatedFeatures);
					nMatches = matcher.getMatches().size();
					nFeatures = siftFeatures.size();
				}

				@Override
				public void gather(Keypoint k) {
					
				}

				@Override
				public void output(PrintStream printStream) {
					printStream.print(this.nMatches / this.nFeatures);
				}
				
			};
		}
	},
	SELF_SIMILAR_FEATURES{
		@Override
		public StatsOperation operation() {
			return new StatsOperation(){
				
				private double nMatches;
				private double nFeatures;
				private int multipleMatches;

				@Override
				public void init(MBFImage image,List<Keypoint> siftFeatures) {
					this.multipleMatches = (int) (siftFeatures.size() * 0.01);
					LocalFeatureMatcher<Keypoint> matcher = new MultipleMatchesMatcher<Keypoint>(multipleMatches,0.4);
					matcher.setModelFeatures(siftFeatures);
					List<Keypoint> pertubatedFeatures = Keypoint.addGaussianNoise(siftFeatures,1,2);
					matcher.findMatches(pertubatedFeatures);
					nMatches = matcher.getMatches().size();
					nFeatures = siftFeatures.size();
					
				}

				@Override
				public void gather(Keypoint k) {
					
				}

				@Override
				public void output(PrintStream printStream) {
					printStream.print((this.nMatches/multipleMatches) / this.nFeatures);
				}
				
			};
		}
	}
	;
	
	public static abstract class StatsOperation{
		public abstract void init(MBFImage image, List<Keypoint> siftFeatures);
		public abstract void gather(Keypoint k);
		public abstract void output(PrintStream printStream);
	}
	
	public abstract StatsOperation operation();

	public static List<StatsOperation> getOperations(List<FeatureStatsPrinter> statsList) {
		List<StatsOperation> operations = new ArrayList<StatsOperation>();
		for (FeatureStatsPrinter featureStatsPrinter : statsList) {
			operations.add(featureStatsPrinter.operation());
		}
		return operations;
	}

	public static void initOperations(List<StatsOperation> operations,MBFImage initImage, FileLocalFeatureList<Keypoint> siftFeatures) {
		for (StatsOperation statsOperation : operations) {
			statsOperation.init(initImage,siftFeatures);
		}
	}

	public static void gatherOperations(List<StatsOperation> operations,Keypoint keypoint) {
		for (StatsOperation statsOperation : operations) {
			statsOperation.gather(keypoint);
		}
	}

	public static void outputOperations(List<StatsOperation> operations, PrintStream out, String delim) {
		for (StatsOperation statsOperation : operations) {
			out.print(delim);
			statsOperation.output(out);
		}
	}
}
