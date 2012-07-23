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
package org.openimaj.tools.globalfeature;

import java.util.Arrays;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.MBFImageOptionHandler;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.ByteFV;
import org.openimaj.feature.ByteFVComparison;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FVComparator;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.IntFV;
import org.openimaj.feature.IntFVComparison;
import org.openimaj.feature.ShortFV;
import org.openimaj.feature.ShortFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.FloodFill;
import org.openimaj.tools.globalfeature.ShapeFeatures.ShapeFeaturesOp;

/**
 * A tool for computing the similarities/distances between two images based
 * on a feature from the foreground object in the image. The flood-fill algorithm is
 * used to segment the foreground/background based 
 * on a seed pixel and distance threshold.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SegmentingPairWiseComparisonTool {
	@Option(name="--image-1", aliases="-im1", usage="first image", handler=MBFImageOptionHandler.class, required=true)
	private MBFImage im1;
	
	@Option(name="--image-2", aliases="-im2", usage="second image", handler=MBFImageOptionHandler.class, required=true)
	private MBFImage im2;
	
	@Option(name="--metric", aliases="-m", usage="comparison metric", required=true)
	private FeatureComparison compare;
	
	@Option(name="--feature-type", aliases="-f", handler=ProxyOptionHandler.class, usage="Feature type", required=true)
	private ShapeFeatures feature;
	private ShapeFeaturesOp featureOp;
	
	@Option(name = "--px1", aliases="-px1", required=false, usage="x-position of the starting pixel in image 1")
	private int px1 = 0;
	
	@Option(name = "--py1", aliases="-py1", required=false, usage="y-position of the starting pixel in image 1")
	private int py1 = 0;
	
	@Option(name = "--thresh1", aliases="-thresh1", required=false, usage="threshold for flood-fill algorithm in image 1")
	private float thresh1 = 25F/255F;
	
	@Option(name = "--px2", aliases="-px2", required=false, usage="x-position of the starting pixel in image 2")
	private int px2 = 0;
	
	@Option(name = "--py2", aliases="-py2", required=false, usage="y-position of the starting pixel in image 2")
	private int py2 = 0;
	
	@Option(name = "--thresh2", aliases="-thresh2", required=false, usage="threshold for flood-fill algorithm in image 2")
	private float thresh2 = 25F/255F;
	
	
	@SuppressWarnings("unchecked")
	protected <T extends FeatureVector> FVComparator<T> getComp(T fv, FeatureComparison type) {
		if (fv instanceof ByteFV) return (FVComparator<T>) ByteFVComparison.valueOf(type.name());
		if (fv instanceof ShortFV) return (FVComparator<T>) ShortFVComparison.valueOf(type.name());
		if (fv instanceof IntFV) return (FVComparator<T>) IntFVComparison.valueOf(type.name());
		if (fv instanceof FloatFV) return (FVComparator<T>) FloatFVComparison.valueOf(type.name());
		if (fv instanceof DoubleFV) return (FVComparator<T>) DoubleFVComparison.valueOf(type.name());
		return null;
	}
	
	double execute() {
		FImage mask1 = FloodFill.floodFill(im1, px1, py1, thresh1);
		FImage mask2 = FloodFill.floodFill(im2, px2, py2, thresh2);
		
		FeatureVector fv1 = featureOp.execute(im1, mask1);
		FeatureVector fv2 = featureOp.execute(im2, mask2);
		
		if (compare == FeatureComparison.EQUALS) {
			if (Arrays.equals(fv1.asDoubleVector(), fv2.asDoubleVector()))
				return 1;
			return 0;
		} else {
			return getComp(fv1, compare).compare(fv1, fv2);
		}
	}
	
	/**
	 * The main method of the tool.
	 * @param args
	 */
	public static void main(String [] args) {
		SegmentingPairWiseComparisonTool tool = new SegmentingPairWiseComparisonTool();
		
		CmdLineParser parser = new CmdLineParser(tool);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: pairwisecomp [options...]");
			parser.printUsage(System.err);
			
			if (tool.feature == null) {
				for (GlobalFeatureType m : GlobalFeatureType.values()) {
					System.err.println();
					System.err.println(m + " options: ");
					new CmdLineParser(m.getOptions()).printUsage(System.err);
				}
			}
			return;
		}
		
		System.out.println(tool.execute());
	}
}
