package org.openimaj.demos.acmmm11.presentation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.demos.acmmm11.presentation.slides.SIFTTrackerSlide;
import org.openimaj.demos.utils.slideshowframework.PictureSlide;
import org.openimaj.demos.utils.slideshowframework.Slide;
import org.openimaj.demos.utils.slideshowframework.Slideshow;
import org.openimaj.demos.utils.slideshowframework.VideoSlide;

public class OpenIMAJ_ACMMM2011 {
	public static void main(String[] args) throws MalformedURLException, IOException {
		List<Slide> slides = new ArrayList<Slide>();
		slides.add(new VideoSlide(new URL("file:///Users/jon/Movies/Pioneer.One.S01E01.720p.x264-VODO.mkv")));
		slides.add(new MovingPictureSlide(new URL("file:///Users/jon/Desktop/branding/imageterrier-icon.png")));
		slides.add(new PictureSlide(new URL("file:///Users/jon/Pictures/Pictures/2008_02_04/IMG_1921.JPG")));
		slides.add(new PictureSlide(new URL("file:///Users/jon/Pictures/Pictures/2008_02_07/IMG_2048.JPG")));
		slides.add(new SIFTTrackerSlide());
		
		new Slideshow(slides);
	}
}
