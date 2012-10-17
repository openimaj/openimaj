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
package org.openimaj.demos.sandbox.tld;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.io.FileUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.MBFImageFileBackedVideo;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

/**
 * Runs the TLD code, equivilant to run_TLD in the matlab
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TldMain {
	static String inputPath = "/Users/ss/Development/matlab/OpenTLD/_input";
	public static void main(String[] args) throws IOException {
		MBFImageFileBackedVideo video = readImageDirVideo();
//		VideoDisplay<MBFImage> disp = VideoDisplay.createVideoDisplay(video);
		Rectangle boundingBox = readBoundingBox();
		System.out.println(boundingBox);
		TLDOptions opt = new TLDOptions();
		final TLDTracker tracker = new TLDTracker(opt);
		MBFImage first = video.getCurrentFrame();
		tracker.init(first.flatten(), boundingBox);
		video.getNextFrame();
		VideoDisplay<MBFImage> disp = VideoDisplay.createVideoDisplay(video);
		disp.addVideoListener(new VideoDisplayListener<MBFImage>() {
			
			@Override
			public void beforeUpdate(MBFImage frame) {
				tracker.processFrame(frame.flatten());
				if(tracker.tracker == null) return;
				frame.drawShape(tracker.currentBoundingBox, 3, RGBColour.RED);
				frame.drawPoints(tracker.getTrackerPoints(), RGBColour.BLUE, 2);
			}
			
			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	private static Rectangle readBoundingBox() throws IOException {
		File bbFile = new File(inputPath,"init.txt");
		String bbStr = FileUtils.readall(bbFile);
		String[] bbParts = bbStr.split(",");
		int x1 = Integer.parseInt(bbParts[0]);
		int y1 = Integer.parseInt(bbParts[1]);
		int x2 = Integer.parseInt(bbParts[2]);
		int y2 = Integer.parseInt(bbParts[3].trim());
		return new Rectangle(x1,y1,(x2-x1),(y2-y1));
	}
	private static MBFImageFileBackedVideo readImageDirVideo() {
		File input = new File(inputPath);
		File[] fileList = input.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("png");
			}
		}) ;
		Arrays.sort(fileList, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				Integer o1Number = Integer.parseInt(o1.getName().split("[.]")[0]);
				Integer o2Number = Integer.parseInt(o2.getName().split("[.]")[0]);
				return o1Number.compareTo(o2Number);
			}
			
		});
		
		return new MBFImageFileBackedVideo(Arrays.asList(fileList));
	}
}
