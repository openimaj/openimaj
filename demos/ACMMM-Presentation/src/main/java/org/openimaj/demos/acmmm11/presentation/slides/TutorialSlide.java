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
package org.openimaj.demos.acmmm11.presentation.slides;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.CameraSelector;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.CannyVideoTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.ColourHistogramGrid;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.FaceTrackingTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.SIFTFeatureTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.SegmentationTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.ShapeRenderingTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.TutorialPanel;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

/**
 * Slide showing the results from following the tutorial.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samagooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TutorialSlide implements Slide {
	private VideoCapture capture;
	private List<VideoDisplay<MBFImage>> displays;

	@Override
	public Component getComponent(int slideWidth, int slideHeight) throws IOException {
		final JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridLayout(2, 3));

		final int videoWidth = 320;
		final int videoHeight = 240;

		capture = CameraSelector.getPreferredVideoCapture(videoWidth, videoHeight);
		displays = new ArrayList<VideoDisplay<MBFImage>>();

		final TutorialPanel[] tutorials = new TutorialPanel[] {
				new FaceTrackingTutorial(capture, videoWidth, videoHeight),
				new SIFTFeatureTutorial(capture, videoWidth, videoHeight),
				new SegmentationTutorial(capture, videoWidth, videoHeight),
				new CannyVideoTutorial(capture, videoWidth, videoHeight),
				new ShapeRenderingTutorial(capture, videoWidth, videoHeight),
				new ColourHistogramGrid(capture, videoWidth, videoHeight)
		};

		for (final TutorialPanel innerPanel : tutorials) {
			panel.add(innerPanel);

			final VideoDisplay<MBFImage> vd = VideoDisplay.createOffscreenVideoDisplay(capture);
			vd.addVideoListener(innerPanel);
			displays.add(vd);
		}

		panel.validate();

		return panel;
	}

	@Override
	public void close() {
		for (final VideoDisplay<MBFImage> vd : displays)
			vd.close();
		displays.clear();
		capture.stopCapture();
	}
}
