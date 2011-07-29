package org.openimaj.image.typography;

import org.openimaj.image.Image;

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
	 * Get a font renderer suitable for rendering this font to the given image.  
	 * @param <T> type of pixel.
	 * @param <Q> type of {@link FontStyle}.
	 * @param image the target image.
	 * @return the FontRenderer
	 */
	public <T, Q extends FontStyle<?, T>> FontRenderer<T, Q> getRenderer(Image<T,?> image);
	
	/**
	 * Create an instance of a FontStyle with the default parameters for the given
	 * image.
	 * @param <T> type of pixel.
	 * @param image the image
	 * @return the FontStyle
	 */
	public <T> FontStyle<F, T> createStyle(Image<T, ?> image);
	
	/**
	 * Get the name of this font
	 * @return the name of the font
	 */
	public String getName();
}
