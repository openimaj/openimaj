package org.openimaj.content.slideshow;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

/**
 * A slideshow that can be used in a Java Swing application. The slideshow
 * is created in a new window (JFrame).
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class SlideshowApplication extends Slideshow {
	protected FullscreenUtility fsutil;
	
	public SlideshowApplication(List<Slide> slides, int slideWidth, int slideHeight, BufferedImage background) throws IOException {
		super(new JFrame(), slides, slideWidth, slideHeight, background);
		fsutil = new FullscreenUtility((JFrame) container);
	}

	@Override
	protected void pack() {
		((JFrame)container).pack();
	}

	@Override
	public void setFullscreen(boolean fullscreen) {
		fsutil.setFullscreen(fullscreen);
	}

	@Override
	protected boolean isFullscreen() {
		return fsutil.fullscreen;
	}	
}
