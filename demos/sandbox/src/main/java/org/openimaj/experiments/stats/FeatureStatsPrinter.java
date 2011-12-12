package org.openimaj.experiments.stats;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
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
	SELF_SIMILAR_FEATURES {
		@Override
		public StatsOperation operation() {
			return new StatsOperation(){
				
				private double nMatches;
				private double nFeatures;

				@Override
				public void init(MBFImage image,List<Keypoint> siftFeatures) {
					LocalFeatureMatcher<Keypoint> matcher = new FastBasicKeypointMatcher<Keypoint>(8);
					matcher.setModelFeatures(siftFeatures);
					List<Keypoint> pertubatedFeatures = Keypoint.addGaussianNoise(siftFeatures,2,2);
					matcher.findMatches(pertubatedFeatures);
					MBFImage a = MatchingUtilities.drawMatches(image,image, matcher.getMatches(), RGBColour.RED);
					DisplayUtilities.displayName(a,"wang");
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
	};
	
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
