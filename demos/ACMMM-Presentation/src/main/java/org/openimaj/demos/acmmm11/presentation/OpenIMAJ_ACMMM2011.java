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
package org.openimaj.demos.acmmm11.presentation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.SlideshowApplication;
import org.openimaj.content.slideshow.VideoSlide;
import org.openimaj.demos.acmmm11.presentation.slides.AudioOutroSlide;
import org.openimaj.demos.acmmm11.presentation.slides.SIFTAltSIFTSlide;
import org.openimaj.demos.acmmm11.presentation.slides.SIFTTrackerSlide;
import org.openimaj.demos.acmmm11.presentation.slides.TutorialSlide;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay.EndAction;

/**
 * Presentation for ACM MM 2011.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class OpenIMAJ_ACMMM2011 {
	/**
	 * @return The slides for the presentation
	 * @throws IOException
	 */
	public static List<Slide> getSlides() throws IOException {
		final List<Slide> slides = new ArrayList<Slide>();

		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.001.png"))); // title
																								// slide
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.002.png"))); // What
																								// is
																								// OpenIMAJ
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.003.png"))); // What
																								// is
																								// ImageTerrier
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.005.png"))); // History
																								// of
																								// development
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.006.png"))); // All
																								// in
																								// java,
																								// isnt
																								// it
																								// slow?!
		slides.add(new SIFTTrackerSlide()); // live DoG/SIFT demo
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.007.png"))); // Why
																								// another
																								// set
																								// of
																								// libraries?
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.008.png"))); // Designed
																								// to
																								// be
																								// EXTENSIBLE
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.009.png"))); // Alt
																								// SIFT:
																								// DoG/SIFT
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.010.png"))); // Alt
																								// SIFT:
																								// highlight
																								// dom
																								// ori
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.011.png"))); // Alt
																								// SIFT:
																								// replace
																								// with
																								// Null
																								// ori
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.012.png"))); // Alt
																								// SIFT:
																								// highlight
																								// SIFT
																								// feature
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.013.png"))); // Alt
																								// SIFT:
																								// replace
																								// with
																								// Irregular
																								// binning
																								// SIFT
		slides.add(new SIFTAltSIFTSlide()); // Normal vs ALT demo
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.014.png"))); // Street
																								// View
																								// Cam
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.015.png")));
		slides.add(new VideoSlide(
				OpenIMAJ_ACMMM2011.class.getResource("kinect.m4v"), // video
				OpenIMAJ_ACMMM2011.class.getResource("slide.016.png"), // background
				TransformUtilities.translateMatrix(540, 550).times(TransformUtilities.scaleMatrix(1.0f, 1.0f)), // transform
				EndAction.LOOP
				)
				); // Student projects + Kinect video
		slides.add(new TutorialSlide()); // Student Tutorial demo
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.017.png"))); // Research
																								// applications
		slides.add(new VideoSlide(
				OpenIMAJ_ACMMM2011.class.getResource("guessthebuilding.mov"), // video
				OpenIMAJ_ACMMM2011.class.getResource("background.png"), // background
				EndAction.LOOP
				)
				); // Guess the Building demo
		slides.add(new VideoSlide(
				OpenIMAJ_ACMMM2011.class.getResource("stockphotofinder.mov"), // video
				OpenIMAJ_ACMMM2011.class.getResource("background.png"), // background
				EndAction.LOOP
				)
				); // Stock photo finder demo
		slides.add(new AudioOutroSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.018.png"))); // Questions
																								// +
																								// Audio
																								// strem
																								// viewer

		return slides;
	}

	/**
	 * Run the presentation
	 * 
	 * @param args
	 *            ignored
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		new SlideshowApplication(getSlides(), 1024, 768, ImageIO.read(OpenIMAJ_ACMMM2011.class
				.getResourceAsStream("background.png")));
	}
}
