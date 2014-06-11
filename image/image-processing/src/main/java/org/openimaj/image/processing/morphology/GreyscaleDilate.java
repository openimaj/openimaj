package org.openimaj.image.processing.morphology;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.KernelProcessor;

/**
 * Morphological greyscale dilation of FImages. Only the positive part of the
 * {@link StructuringElement} is considered.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class GreyscaleDilate implements KernelProcessor<Float, FImage> {
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
	public GreyscaleDilate(StructuringElement se) {
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
	public GreyscaleDilate() {
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
		float mv = -Float.MAX_VALUE;
		for (final Pixel p : element.positive) {
			final int px = cx - p.x;
			final int py = cy - p.y;
			if (px >= 0 && py >= 0 && px < sw && py < sh) {
				mv = Math.max(patch.pixels[py][px], mv);
			}
		}
		return mv;
	}

	/**
	 * Apply dilation some number of times to an image with the default
	 * {@link StructuringElement#BOX} element
	 * 
	 * @param img
	 *            the image
	 * @param times
	 *            the number of times to apply the dilation
	 */
	public static void dilate(FImage img, int times) {
		final GreyscaleDilate d = new GreyscaleDilate();
		for (int i = 0; i < times; i++)
			img.processInplace(d);
	}
}
