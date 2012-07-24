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
package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;

/**
 * Slide showing face tracking
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FaceKETrackingTutorial extends TutorialPanel {
private static final long serialVersionUID = -5279460790389377219L;
	
	private FaceDetector<KEDetectedFace,FImage> detector;
	
	/**
	 * Default constructor
	 * 
	 * @param capture
	 * @param width
	 * @param height
	 */
	public FaceKETrackingTutorial(Video<MBFImage> capture, int width, int height){
		super("Face Finding", capture, width, height);
		
		this.detector = new FKEFaceDetector( new HaarCascadeDetector(height/3));
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		List<KEDetectedFace> faces = this.detector.detectFaces(toDraw.flatten());
		
		for (KEDetectedFace detectedFace : faces) {
			Rectangle b = detectedFace.getBounds();
			Point2dImpl bp = new Point2dImpl(b.x,b.y);
			toDraw.drawShape(b, RGBColour.RED);
			FacialKeypoint[] kpts = detectedFace.getKeypoints();
			List<Point2d> fpts = new ArrayList<Point2d>();
			for(FacialKeypoint kpt : kpts){
				Point2dImpl p = kpt.position;
				p.translate(bp);
				fpts.add(p);
			}
			toDraw.drawPoints(fpts, RGBColour.GREEN, 3);
		}
	}
}
