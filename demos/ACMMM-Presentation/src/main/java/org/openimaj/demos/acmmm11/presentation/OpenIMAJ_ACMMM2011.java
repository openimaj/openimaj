package org.openimaj.demos.acmmm11.presentation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.content.slideshow.PictureSlide;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.content.slideshow.Slideshow;
import org.openimaj.content.slideshow.VideoSlide;
import org.openimaj.demos.acmmm11.presentation.slides.AudioOutroSlide;
import org.openimaj.demos.acmmm11.presentation.slides.SIFTAltSIFTSlide;
import org.openimaj.demos.acmmm11.presentation.slides.SIFTTrackerSlide;
import org.openimaj.demos.acmmm11.presentation.slides.TutorialSlide;
import org.openimaj.math.geometry.transforms.TransformUtilities;

public class OpenIMAJ_ACMMM2011 {
	public static void main(String[] args) throws MalformedURLException, IOException {
		List<Slide> slides = new ArrayList<Slide>();
		
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.001.png"))); // title slide
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.002.png"))); // What is OpenIMAJ
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.003.png"))); // What is ImageTerrier
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.005.png"))); // History of development
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.006.png"))); // All in java, isnt it slow?!
		slides.add(new SIFTTrackerSlide()); // live DoG/SIFT demo
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.007.png"))); // Why another set of libraries?
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.008.png"))); // Designed to be EXTENSIBLE
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.009.png"))); // Alt SIFT: DoG/SIFT
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.010.png"))); // Alt SIFT: highlight dom ori
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.011.png"))); // Alt SIFT: replace with Null ori
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.012.png"))); // Alt SIFT: highlight SIFT feature
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.013.png"))); // Alt SIFT: replace with Irregular binning SIFT
		slides.add(new SIFTAltSIFTSlide()); // Normal vs ALT demo
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.014.png"))); // Street View Cam
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.015.png")));
		slides.add(new VideoSlide(
				OpenIMAJ_ACMMM2011.class.getResource("kinect.m4v"), // video
				OpenIMAJ_ACMMM2011.class.getResource("slide.016.png"), // background
				TransformUtilities.translateMatrix(540, 550).times(TransformUtilities.scaleMatrix(1.0f, 1.0f)) // transform
			)
		); // Student projects + Kinect video
//		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.017.png")));
		slides.add(new TutorialSlide()); // Student Tutorial demo
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.017.png"))); // Research applications
		slides.add(new VideoSlide(
				OpenIMAJ_ACMMM2011.class.getResource("guessthebuilding.mov"), // video
				OpenIMAJ_ACMMM2011.class.getResource("background.png") // background
			)
		); // Guess the Building demo
		slides.add(new VideoSlide(
				OpenIMAJ_ACMMM2011.class.getResource("stockphotofinder.mov"), // video
				OpenIMAJ_ACMMM2011.class.getResource("background.png") // background
			)
		); // Stock photo finder demo
		slides.add(new AudioOutroSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.018.png"))); // Questions + Audio strem viewer
//		slides.add(new VideoSlide(new URL("file:///Users/jon/Movies/Pioneer.One.S01E01.720p.x264-VODO.mkv")));
//		slides.add(new MovingPictureSlide(OpenIMAJ_ACMMM2011.class.getResource("imageterrier.png")));
//		slides.add(new PictureSlide(new URL("file:///Users/jon/Pictures/Pictures/2008_02_07/IMG_2048.JPG")));
		
		new Slideshow(slides, 1024,768, ImageIO.read(OpenIMAJ_ACMMM2011.class.getResourceAsStream("background.png")));
	}
}
