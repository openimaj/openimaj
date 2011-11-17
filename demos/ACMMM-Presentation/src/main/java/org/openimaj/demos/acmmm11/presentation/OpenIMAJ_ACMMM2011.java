package org.openimaj.demos.acmmm11.presentation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.demos.acmmm11.presentation.slides.SIFTTrackerSlide;
import org.openimaj.demos.acmmm11.presentation.slides.TutorialSlide;
import org.openimaj.demos.utils.slideshowframework.PictureSlide;
import org.openimaj.demos.utils.slideshowframework.Slide;
import org.openimaj.demos.utils.slideshowframework.Slideshow;

public class OpenIMAJ_ACMMM2011 {
	public static void main(String[] args) throws MalformedURLException, IOException {
		List<Slide> slides = new ArrayList<Slide>();
		
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.001.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.002.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.003.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.004.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.005.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.006.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.007.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.008.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.009.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.010.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.011.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.012.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.013.png")));
		slides.add(new SIFTTrackerSlide());
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.015.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.016.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.017.png")));
		slides.add(new TutorialSlide());
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.018.png")));
		slides.add(new PictureSlide(OpenIMAJ_ACMMM2011.class.getResource("slide.019.png")));
		
//		slides.add(new VideoSlide(new URL("file:///Users/jon/Movies/Pioneer.One.S01E01.720p.x264-VODO.mkv")));
//		slides.add(new MovingPictureSlide(OpenIMAJ_ACMMM2011.class.getResource("imageterrier.png")));
//		slides.add(new PictureSlide(new URL("file:///Users/jon/Pictures/Pictures/2008_02_07/IMG_2048.JPG")));
		
		new Slideshow(slides, 1024, 768, ImageIO.read(OpenIMAJ_ACMMM2011.class.getResourceAsStream("background.png")));
	}
}
