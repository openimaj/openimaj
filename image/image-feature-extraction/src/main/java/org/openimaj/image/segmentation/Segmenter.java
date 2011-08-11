package org.openimaj.image.segmentation;

import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.pixel.ConnectedComponent;

/**
 * The Segmenter interface defines an object capable of segmenting
 * an image into {@link ConnectedComponent}s.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <I> The type of image
 */
public interface Segmenter<I extends Image<?,I>> {
	List<ConnectedComponent> segment(I image);
}
