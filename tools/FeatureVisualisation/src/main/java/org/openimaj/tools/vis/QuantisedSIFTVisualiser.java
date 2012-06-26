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
package org.openimaj.tools.vis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;

/**
 * A tool for visualising quantised sift features by drawing their sampling
 * boxes on the image they were extracted from
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class QuantisedSIFTVisualiser {
	@Option(name="--input-image", aliases="-ii", required=true, usage="input image file")
	File imageFile;
	
	@Option(name="--input-features", aliases="-if", required=true, usage="input quantised features file")
	File quantisedFeatureFile;

	@Option(name="--output", aliases="-o", required=true, usage="output image file")
	File outputImageFile;
	
	@Option(name="--colour", aliases="-c", required=false, usage="colour to draw boxes in")
	String colour = "BLUE";
	Float[] colourValue = null; 
	
	@Argument(required=false, usage="required term identifiers")
	List<Integer> requiredIds;

	void process() throws IOException {
		LocalFeatureList<QuantisedKeypoint> qkeys = MemoryLocalFeatureList.read(quantisedFeatureFile, QuantisedKeypoint.class);
		List<Keypoint> keys = new ArrayList<Keypoint>();
		
		MBFImage image = ImageUtilities.readMBF(imageFile);
		
		
		if (requiredIds == null || requiredIds.size() == 0) {
			for (QuantisedKeypoint kpt : qkeys) {
				keys.add(makeKeypoint(kpt));
			}
		} else {
			for (QuantisedKeypoint kpt : qkeys) {
				if (requiredIds.contains(kpt.id)) {
					keys.add(makeKeypoint(kpt));
				}
			}
		}
		
		KeypointVisualizer<Float[], MBFImage> viz = new KeypointVisualizer<Float[], MBFImage>(image, keys);
		MBFImage outimg = viz.drawPatches(colourValue, null);
		
		ImageUtilities.write(outimg, outputImageFile);
	}
	
	protected Keypoint makeKeypoint(QuantisedKeypoint kpt) {
		Keypoint key = new Keypoint();
		
		key.y = kpt.location.y;
		key.x = kpt.location.x;
		key.scale = kpt.location.scale;
		key.ori = kpt.location.orientation;
		
		return key; 
	}
	
	static QuantisedSIFTVisualiser load(String [] args) {
		QuantisedSIFTVisualiser options = new QuantisedSIFTVisualiser();
        CmdLineParser parser = new CmdLineParser( options );

        try {
	        parser.parseArgument( args );
	        
	        options.colourValue = RGBColour.fromString(options.colour);
        } catch( CmdLineException e ) {
	        System.err.println( e.getMessage() );
	        System.err.println( "java " + QuantisedSIFTVisualiser.class.getName() + " options...");
	        parser.printUsage( System.err );
	        System.exit(1);
        }

        return options;
	}
	
	/**
	 * The main method of the tool.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String [] args) throws IOException {
		QuantisedSIFTVisualiser extr = QuantisedSIFTVisualiser.load(args);
		extr.process();
	}
}
