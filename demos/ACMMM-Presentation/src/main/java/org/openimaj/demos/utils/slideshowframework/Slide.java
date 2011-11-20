package org.openimaj.demos.utils.slideshowframework;

import java.awt.Component;
import java.io.IOException;

/**
 * A slide that can be displayed by a {@link Slideshow}.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public interface Slide {
	/**
	 * Get the component to draw.
	 * 
	 * @param width the slide width
	 * @param height the slide height
	 * @return the component
	 * @throws IOException
	 */
	public abstract Component getComponent(int width, int height) throws IOException;
	
	/**
	 * Close the current slide. Called by {@link Slideshow} when the
	 * slide is removed from display.
	 */
	public abstract void close();
}
