package org.openimaj.image.processing.restoration.inpainting;

import java.util.Collection;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;

/**
 * Abstract base for {@link Inpainter} implementations that consume a mask image
 * (rather than connected components or pixel sets). Provides the necessary
 * methods to build the mask image for all the various calls to
 * <code>setMask</code>.
 * <p>
 * All <code>setMask</code> implementations call {@link #initMask()}, which
 * subclasses should implement to perform any required initialisation.
 * {@link #processImage(Image)} performs checks on the image dimensions and then
 * calls {@link #performInpainting(Image)}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of image that this processor can process
 */
@SuppressWarnings("javadoc")
public abstract class AbstractImageMaskInpainter<IMAGE extends Image<?, IMAGE>>
		implements
		Inpainter<IMAGE>
{
	/**
	 * The mask image
	 */
	protected FImage mask;

	@Override
	public void setMask(FImage mask) {
		this.mask = mask;
		initMask();
	}

	@Override
	public void setMask(int width, int height, Collection<? extends Iterable<Pixel>> mask) {
		this.mask = new FImage(width, height);

		for (final Iterable<Pixel> ps : mask) {
			for (final Pixel p : ps) {
				if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height)
					this.mask.pixels[p.y][p.x] = 1;
			}
		}
		initMask();
	}

	@Override
	public void setMask(int width, int height, PixelSet... mask) {
		this.mask = new FImage(width, height);

		for (final Iterable<Pixel> ps : mask) {
			for (final Pixel p : ps) {
				if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height)
					this.mask.pixels[p.y][p.x] = 1;
			}
		}
		initMask();
	}

	/**
	 * Perform any initialisation once the mask has been set
	 */
	protected abstract void initMask();

	@Override
	public final void processImage(IMAGE image) {
		if (mask == null)
			throw new IllegalArgumentException("Mask has not been set");

		if (image.getWidth() != mask.getWidth() || image.getHeight() != mask.getHeight())
			throw new IllegalArgumentException("Image and mask size do not match");

		performInpainting(image);
	}

	/**
	 * Perform the inpainting of the given image
	 * 
	 * @param image
	 *            the image to inpaint
	 */
	protected abstract void performInpainting(IMAGE image);
}
