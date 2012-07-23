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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.FileOutputStreamOptionHandler;
import org.kohsuke.args4j.MBFImageOptionHandler;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.FloodFill;
import org.openimaj.io.IOUtils;
import org.openimaj.tools.globalfeature.ShapeFeatures.ShapeFeaturesOp;


/**
 * A tool that computes features from the foreground
 * object in an image. The flood-fill algorithm is
 * used to segment the foreground/background based 
 * on a seed pixel and distance threshold.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SegmentingGlobalFeaturesTool {
	@Option(name="--feature-type", aliases="-f", handler=ProxyOptionHandler.class, usage="Feature type", required=false)
	private ShapeFeatures feature;
	private ShapeFeaturesOp featureOp;
	
	@Option(name = "--input", aliases="-i", required=true, usage="Set the input image", handler=MBFImageOptionHandler.class)
	private MBFImage input;
	
	@Option(name = "--output", aliases="-o", required=false, usage="Set the output file; if not set then output is to stdout", handler=FileOutputStreamOptionHandler.class)
	private OutputStream output = System.out;
	
	@Option(name = "--binary", aliases="-b", required=false, usage="Set output mode to binary")
	private boolean binary = false;
	
	@Option(name = "--mask-output", aliases="-mo", required=false, usage="Save the generated segmentation mask")
	private File maskoutput;
	
	@Option(name = "--px", aliases="-px", required=false, usage="x-position of the starting pixel")
	private int px = 0;
	
	@Option(name = "--py", aliases="-py", required=false, usage="y-position of the starting pixel")
	private int py = 0;
	
	@Option(name = "--thresh", aliases="-thresh", required=false, usage="threshold for flood-fill algorithm")
	private float thresh = 25F/255F;
	
	void execute() throws IOException {
		FImage mask = FloodFill.floodFill(input, px, py, thresh);
		
		if (maskoutput != null)
			ImageUtilities.write(mask, maskoutput.getName().substring(maskoutput.getName().lastIndexOf(".") + 1), maskoutput);
		
		if (feature != null) { 
			FeatureVector fv = featureOp.execute(input, mask);
		
			if (binary)
				IOUtils.writeBinary(output, fv);
			else
				IOUtils.writeASCII(output, fv);
		}
	}
	
	/**
	 * The main method for the tool.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		SegmentingGlobalFeaturesTool tool = new SegmentingGlobalFeaturesTool();
		
		CmdLineParser parser = new CmdLineParser(tool);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -cp GlobalFeaturesTool.jar uk.ac.soton.ecs.jsh2.SegmentingGlobalFeaturesTool [options...]");
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
		
		tool.execute();
	}
}
