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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

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
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;

/**
 * Tool to extract prototypical quantised SIFT patches from a set of images and their
 * corresponding quantised SIFT features.
 * 
 * Patches are extracted by blurring the image to the correct scale and then extracting
 * the patch using its geometry. It is assumed that the patch size is 2 * 2 * 3 * scale
 * as per the original geometry suggested in Lowe's SIFT paper.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class QuantisedSIFTPatchExtractor {
	@Option(name="--path", aliases="-p", required=true, usage="Path to the directory structure containing the quantised SIFT features", metaVar="path")
	File featurePath;
	
	@Option(name="--feature-matches", aliases="-fm", required=true, usage="regex to test whether a file is a feature")
	String featureMatchesRegex;
	
	@Option(name="--image-match", aliases="-im", required=true, usage="regex to apply to feature paths to select required parts")
	String imageMatchRegex;
	
	@Option(name="--image-replace", aliases="-ir", required=true, usage="regex to build image path from feature path using the --image-match")
	String imageReplaceRegex;
	
	@Option(name="--output", aliases="-o", required=false, usage="output directory")
	File outputDir;
	
	@Option(name="--output-file", aliases="-of", required=false, usage="output file for aggregated image")
	File outputImageFile;
	
	@Argument(required=false, usage="required term identifiers")
	List<Integer> requiredIds;
	TIntArrayList requiredIdsList;
	
	TIntHashSet foundTerms = new TIntHashSet();
	
	FImage outputImage;
	
	void process() throws IOException {
		if (outputDir != null) outputDir.mkdirs();
		
		if (outputImageFile != null && requiredIdsList != null) {
			outputImage = new FImage(128*requiredIdsList.size(), 128);
		}
		
		process(featurePath);
	}
	
	protected boolean process(File file) throws IOException {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				boolean ret = process(f);
				if (ret) return true;
			}
		} else {
			if (file.getAbsolutePath().matches(featureMatchesRegex)) {
				return processFeatureFile(file);
			}
		}
		return false;
	}
	
	protected FImage loadCorrespondingImage(File feature) throws IOException {
		//get image name/path
		File imagePath = new File(feature.getAbsolutePath().replaceAll(imageMatchRegex, imageReplaceRegex));
		System.out.println(feature + " -> " + imagePath);
		
		return ImageUtilities.readF(imagePath);
	}
	
	protected boolean processFeatureFile(File featureFile) throws IOException {
		LocalFeatureList<QuantisedKeypoint> qkeys = MemoryLocalFeatureList.read(featureFile, QuantisedKeypoint.class);
		FImage image = null;
		
		for (QuantisedKeypoint kpt : qkeys) {
			if ((requiredIdsList == null || requiredIdsList.contains(kpt.id)) && !foundTerms.contains(kpt.id)) {
				if (image == null) image = loadCorrespondingImage(featureFile);
				
				Keypoint key = new Keypoint();
				key.y = kpt.location.y;
				key.x = kpt.location.x;
				key.scale = kpt.location.scale;
				key.ori = kpt.location.orientation;

				List<Keypoint> keys = new ArrayList<Keypoint>();
				keys.add(key);

				KeypointVisualizer<Float, FImage> viz = new KeypointVisualizer<Float, FImage>(image, keys);
				FImage patch = viz.getPatches(128).get(key);
				
				if (outputDir != null) {
					File patchout = new File(outputDir, "patch-" + kpt.id + ".png");
					ImageUtilities.write(patch, "png", patchout);
				}
				
				if (outputImage != null) {
					System.out.println("term: " + kpt.id + " " + requiredIdsList.indexOf(kpt.id));
					
					outputImage.createRenderer().drawImage(patch, 128 * requiredIdsList.indexOf(kpt.id), 0);
					ImageUtilities.write(outputImage, outputImageFile);
				}
				
				foundTerms.add(kpt.id);
				
				if (requiredIdsList != null && foundTerms.size() == requiredIdsList.size()) {
					return true;
				}
			}
		}
		
		return false; 
	}
	
	static QuantisedSIFTPatchExtractor load(String [] args) {
		QuantisedSIFTPatchExtractor options = new QuantisedSIFTPatchExtractor();
        CmdLineParser parser = new CmdLineParser( options );

        try {
	        parser.parseArgument( args );
	        if (options.requiredIds != null && options.requiredIds.size() > 0) {
	        	options.requiredIdsList = new TIntArrayList();
	        	for (int i : options.requiredIds) options.requiredIdsList.add(i);
	        }
        } catch( CmdLineException e ) {
	        System.err.println( e.getMessage() );
	        System.err.println( "java " + QuantisedSIFTPatchExtractor.class.getName() + " options...");
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
		QuantisedSIFTPatchExtractor extr = QuantisedSIFTPatchExtractor.load(args);
		extr.process();
	}
}
