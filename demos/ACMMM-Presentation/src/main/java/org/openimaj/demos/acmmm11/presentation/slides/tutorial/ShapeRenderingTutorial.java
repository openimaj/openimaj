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

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.video.Video;

/**
 * Slide showing shapes being drawn on a video
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ShapeRenderingTutorial extends TutorialPanel {
	private static final long serialVersionUID = 4894581289602770940L;
	
	private HaarCascadeDetector detector;

	/**
	 * Default constructor
	 * 
	 * @param capture
	 * @param width
	 * @param height
	 */
	public ShapeRenderingTutorial(Video<MBFImage> capture, int width, int height) {
		super("Drawing", capture, width, height);
		this.detector = new HaarCascadeDetector(20);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		MBFImageRenderer image = toDraw.createRenderer();
		
		List<DetectedFace> faces = this.detector.detectFaces(toDraw.flatten());
		for (DetectedFace detectedFace : faces) {
			float x = detectedFace.getBounds().x;
			float y = detectedFace.getBounds().y;
			float w = detectedFace.getBounds().width;
			float h = detectedFace.getBounds().height;
			renderBubbles(image,x-w/2,y,w,h);
		}
		
	}

	private void renderBubbles(MBFImageRenderer image, float x, float y, float width, float height) {
		float biggestW = width/3;
		float biggestH = height/4;
		image.drawShapeFilled(new Ellipse(x+biggestW*2, y+biggestH*3, biggestW/3, biggestW/3, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(x+biggestW*1.5, y+biggestH*2.5, biggestW/2.5, biggestH/2.5, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(x+biggestW, y+biggestH*1.75, biggestW/2, biggestH/2, 0f), RGBColour.WHITE);
		image.drawShapeFilled(new Ellipse(x, y, biggestW*1.5, biggestH*1.5 , 0f), RGBColour.WHITE);
		image.drawText("OpenIMAJ", (int)(x-biggestW), (int)y, HersheyFont. ASTROLOGY , (int)(biggestW/2.2), RGBColour.BLACK);
		image.drawText("is", (int)(x-biggestW/2), (int)(y+biggestH/2), HersheyFont. ASTROLOGY , (int)(biggestW/2.2), RGBColour.BLACK);
		image.drawText("Awesome", (int)(x-biggestW), (int)(y+biggestH), HersheyFont. ASTROLOGY , (int)(biggestW/2.2), RGBColour.BLACK);
	}
}
