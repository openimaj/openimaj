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
import org.openimaj.image.MBFImage;

/**
 * A tool for computing the similarities/distances between two images based
 * on a global feature.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class PairWiseComparisonTool {
	@Option(name="--image-1", aliases="-im1", usage="first image", handler=MBFImageOptionHandler.class, required=true)
	private MBFImage im1;
	
	@Option(name="--image-2", aliases="-im2", usage="second image", handler=MBFImageOptionHandler.class, required=true)
	private MBFImage im2;
	
	@Option(name="--metric", aliases="-m", usage="comparison metric", required=true)
	private FeatureComparison compare;
	
	@Option(name="--feature-type", aliases="-f", handler=ProxyOptionHandler.class, usage="Feature type", required=true)
	private GlobalFeatureType feature;
	private GlobalFeatureExtractor featureOp;
	
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
		FeatureVector fv1 = featureOp.extract(im1);
		FeatureVector fv2 = featureOp.extract(im2);
		
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
		PairWiseComparisonTool tool = new PairWiseComparisonTool();
		
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
