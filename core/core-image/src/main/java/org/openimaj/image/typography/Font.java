package org.openimaj.image.typography;

import org.openimaj.image.renderer.ImageRenderer;

/**
 * Class representing a Font that can be rendered with
 * a {@link FontRenderer} with an associated {@link FontStyle}.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <F> the actual type of implementing class
 */
public interface Font<F extends Font<F>> {
	/**
	 * Get a font renderer suitable for rendering this font to the given image renderer.  
	 * @param <T> type of pixel.
	 * @param <Q> type of {@link FontStyle}.
	 * @param renderer the target image renderer.
	 * @return the FontRenderer
	 */
	public <T, Q extends FontStyle<F, T>> FontRenderer<T, Q> getRenderer(ImageRenderer<T,?> renderer);
	
	/**
	 * Create an instance of a FontStyle with the default parameters for the given
	 * image.
	 * @param <T> type of pixel.
	 * @param renderer the image renderer
	 * @return the FontStyle
	 */
	public <T> FontStyle<F, T> createStyle(ImageRenderer<T, ?> renderer);
	
	/**
	 * Get the name of this font
	 * @return the name of the font
	 */
	public String getName();
}
