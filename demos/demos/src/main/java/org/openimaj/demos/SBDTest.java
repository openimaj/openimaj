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
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.xuggle.XuggleVideo;

public class SBDTest {
	public static void main(String[] args) {
		Video<MBFImage> vid = new XuggleVideo(new File("/Users/jsh2/Desktop/07211859-rttr-16k-news2-rttr-16k.mpg"));
		System.out.println(vid.countFrames());
		
		
		VideoShotDetector<MBFImage> vsd = new VideoShotDetector<MBFImage>(vid);
		vsd.setStoreAllDifferentials(false);
		vsd.setFindKeyframes(false);
		vsd.process();
		
		List<ShotBoundary> boundaries = vsd.getShotBoundaries();
		List<MBFImage> keyframes = new ArrayList<MBFImage>(boundaries.size());
		ShotBoundary prev = boundaries.get(0);
		vid.setCurrentFrameIndex(0);
		
		
		boundaries.add(new ShotBoundary(new HrsMinSecFrameTimecode(vid.countFrames(), vid.getFPS())));
		
		for (int i=1; i<boundaries.size(); i++) {
			ShotBoundary next = boundaries.get(i);
			
			System.out.println(next);
			System.out.println(next.getTimecode().getFrameNumber());
			
			long pframe = prev.getTimecode().getFrameNumber();
			long nframe = next.getTimecode().getFrameNumber();
			long mframe = pframe + ((nframe - pframe) / 2);
			
			vid.setCurrentFrameIndex(mframe);
			MBFImage frame = vid.getCurrentFrame().clone();
			keyframes.add(frame);
			DisplayUtilities.display(frame, ""+mframe);
			
			prev = next;
		}
	}
}
