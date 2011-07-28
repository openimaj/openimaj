package org.openimaj.image.typography.hershey;

import org.openimaj.image.Image;
import org.openimaj.image.typography.FontStyle;

/**
 * Style parameters for Hershey vector fonts.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> pixel type of image
 */
public class HersheyFontStyle<T> extends FontStyle<HersheyFont, T> {
	public float italicSlant = 0.75f;
	
	/**
	 * Construct with the default parameters for the given image type
	 * @param image 
	 */
	protected HersheyFontStyle(HersheyFont font, Image<T, ?> image) {
		super(font, image);
	}
}
