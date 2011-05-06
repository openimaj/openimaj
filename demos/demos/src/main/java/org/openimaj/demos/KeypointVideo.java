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
package org.openimaj.demos;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.MBFImageFileBackedVideo;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

public class KeypointVideo {
	static File frameLocation = new File("/Users/ss/Development/data/demo/keypoint-video/frames");
	static File queryLocation = new File("/Users/ss/Development/data/demo/keypoint-video/query.jpg");
	static String fileFilter = ".*[.]png";
	
	static Video<MBFImage>loadVideo(File location, String filter){
		File[] allFiles = location.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File f, String name) {
				return name.matches(fileFilter);
			}
			
		});
		Arrays.sort(allFiles,new Comparator<File>(){
			@Override
			public int compare(File f1, File f2) {
				Integer if1 = new Integer(f1.getName().split("[.]")[0]);
				Integer if2 = new Integer(f2.getName().split("[.]")[0]);
				return if1.compareTo(if2);
			}
			
		});
		List<File> frames = Arrays.asList(allFiles);
		return new MBFImageFileBackedVideo(frames,60);
	}
	
	public static void main(String args[]) throws IOException{
		Video<MBFImage> v = loadVideo(frameLocation,fileFilter);
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(v);
		final DoGSIFTEngine  engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		final FImage queryImage = ImageUtilities.readF(queryLocation);
		final LocalFeatureList<Keypoint> queryKPL = engine.findFeatures(queryImage);
//		final HomographyModel model = new HomographyModel(30.0f);
//		final RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 10, new RANSAC.BestFitStoppingCondition(), true);
//		final ConsistentKeypointMatcher<Keypoint> matcher = new ConsistentKeypointMatcher<Keypoint>(8,0);
//		matcher.setFittingModel(ransac);
		final BasicMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(8);
		matcher.setModelFeatures(queryKPL);
		final JFrame matchFrame = DisplayUtilities.makeFrame("Matches");
		display.addVideoListener(new VideoDisplayListener<MBFImage>(){

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}

			@Override
			public void beforeUpdate(MBFImage display) {
//				display.drawShape(new Rectangle(30,30,30,30), RGBColour.RED);
				FImage fdisplay = display.flatten();
				LocalFeatureList<Keypoint> kpl = engine.findFeatures(fdisplay);
				matcher.findMatches(kpl);
				List<Pair<Keypoint>> matches = matcher.getMatches();
				for(Keypoint k : kpl){
					display.drawPoint(k, RGBColour.RED, 2);
					display.drawShape(new Circle(k.x,k.y,k.scale), 2, RGBColour.GREEN);
				}
				DisplayUtilities.display(MatchingUtilities.drawMatches(queryImage, fdisplay, matches, 0f), matchFrame);
			}
			
		});
	}
		
}
