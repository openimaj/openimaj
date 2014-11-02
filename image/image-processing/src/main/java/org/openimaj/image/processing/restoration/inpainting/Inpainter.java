package org.openimaj.image.processing.restoration.inpainting;

import java.util.Collection;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Interface defining an implementation of an inpainting algorithm. Inpainting
 * algorithms are {@link ImageProcessor}s, but it is expected that a mask
 * showing which pixels need to be painted is provided before the
 * {@link #processImage(Image)} call.
 * <p>
 * {@link Inpainter}s are necessarily not thread safe, but implementations are
 * expected to be reusable once the mask has been reset. <strong>It is expected
 * that a call to one of the <code>setMask</code> methods is made before every
 * call to {@link #processImage(Image)}.</strong>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of image that this processor can process
 */
public interface Inpainter<IMAGE extends Image<?, IMAGE>>
		extends
		ImageProcessor<IMAGE>
{
	/**
	 * Set the mask. The mask configures which pixels need to be painted.
	 * 
	 * @param mask
	 *            the mask image; should be binary, with 1 values where painting
	 *            needs to occur.
	 */
	public void setMask(FImage mask);

	/**
	 * Set the mask. The mask configures which pixels need to be painted.
	 * 
	 * @param width
	 *            the mask width
	 * @param height
	 *            the mask height
	 * @param mask
	 *            the mask pixels
	 */
	public void setMask(int width, int height, Collection<? extends Iterable<Pixel>> mask);

	/**
	 * Set the mask. The mask configures which pixels need to be painted.
	 * 
	 * @param width
	 *            the mask width
	 * @param height
	 *            the mask height
	 * @param mask
	 *            the mask pixels
	 */
	public void setMask(int width, int height, PixelSet... mask);

	/**
	 * Inpaint the given image, painting all the mask pixels set by a prior call
	 * to {@link #setMask(int,int,Collection)}, {@link #setMask(FImage)} or
	 * {@link #setMask(int,int,PixelSet...)}
	 * 
	 * @param image
	 *            the image to perform inpainting on
	 */
	@Override
	public void processImage(IMAGE image);
}
