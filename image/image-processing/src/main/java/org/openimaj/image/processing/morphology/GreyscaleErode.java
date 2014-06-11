package org.openimaj.image.processing.morphology;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.KernelProcessor;

/**
 * Morphological greyscale erosion of FImages. Only the positive part of the
 * {@link StructuringElement} is considered.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class GreyscaleErode implements KernelProcessor<Float, FImage> {
	protected StructuringElement element;
	protected int cx;
	protected int cy;
	protected int sw;
	protected int sh;

	/**
	 * Construct the dilate operator with the given structuring element
	 * 
	 * @param se
	 *            the structuring element
	 */
	public GreyscaleErode(StructuringElement se) {
		this.element = se;

		final int[] sz = se.size();
		sw = sz[0];
		sh = sz[1];
		cx = sw / 2;
		cy = sh / 2;
	}

	/**
	 * Construct the dilate operator with a BOX structuring element
	 */
	public GreyscaleErode() {
		this(StructuringElement.BOX);
	}

	@Override
	public int getKernelHeight() {
		return sh;
	}

	@Override
	public int getKernelWidth() {
		return sw;
	}

	@Override
	public Float processKernel(FImage patch) {
		float mv = Float.MAX_VALUE;
		for (final Pixel p : element.positive) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh) {
				mv = Math.min(patch.pixels[py][px], mv);
			}
		}
		return mv;
	}

	/**
	 * Apply erosion some number of times to an image with the default
	 * {@link StructuringElement#BOX} element
	 * 
	 * @param img
	 *            the image
	 * @param times
	 *            the number of times to apply the erosion
	 */
	public static void erode(FImage img, int times) {
		final GreyscaleErode e = new GreyscaleErode();
		for (int i = 0; i < times; i++)
			img.processInplace(e);
	}
}
