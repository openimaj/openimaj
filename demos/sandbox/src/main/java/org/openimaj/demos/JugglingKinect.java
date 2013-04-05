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

import java.io.IOException;

import org.openimaj.hardware.kinect.KinectException;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.HoughCircles;
import org.openimaj.image.analysis.algorithm.HoughCircles.WeightedCircle;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class JugglingKinect {
	public static void main(String[] args) throws KinectException, IOException {
//		KinectController c = new KinectController();
//		VideoDisplay<MBFImage> vid = (VideoDisplay<MBFImage>) VideoDisplay.createVideoDisplay(c.videoStream);
		VideoDisplay<MBFImage> vid = VideoDisplay.createVideoDisplay(new VideoCapture(320,240));
		vid.addVideoListener(new VideoDisplayListener<MBFImage>() {
			int frames = 0;
			private HoughCircles circles;
			@Override
			public void beforeUpdate(MBFImage frame) {
				if(frame==null)return;
				FImage gframe = frame.flatten();
				frames ++;
//				FImage hband = trans.getBand(1).normalise();
//				frame = frame.process(new Disk(20));
				CannyEdgeDetector d = new CannyEdgeDetector();
				ResizeProcessor resize = new ResizeProcessor(0.3f);
				FImage resized = gframe.process(resize);
//				FImage canny = resized.process(new FSobelMagnitude()).threshold(0.8f);
				FImage canny = resized.process(d);
				if(this.circles == null)
					this.circles = new HoughCircles(canny.width/15,canny.width/4,5,360);
				canny.analyseWith(circles);
//				if(frames % 2 == 0){
//					f = f.process(circles);
//					f.drawPoints(circles.accum, 1.f, 10);
//					f.drawShape(new Circle(10,10,10), 1f);
//				}
				MBFImage colResized = new MBFImage(resized.clone(),resized.clone(),resized.clone());
				for (WeightedCircle circ : circles.getBest(5)) {
					System.out.println(circ.weight);
					colResized.drawShape(circ, new Float[]{circ.weight,0f,0f});
				}

				DisplayUtilities.displayName(canny,"circles");
				DisplayUtilities.displayName(colResized,"wang");
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
