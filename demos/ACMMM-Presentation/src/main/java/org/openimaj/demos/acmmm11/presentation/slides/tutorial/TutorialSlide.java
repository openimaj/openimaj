package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.awt.Component;
import java.io.IOException;

import org.openimaj.demos.utils.slideshowframework.Slide;

public class TutorialSlide implements Slide {

	@Override
	public Component getComponent(int width, int height) throws IOException {
		return new OpenIMAJTutorials();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
