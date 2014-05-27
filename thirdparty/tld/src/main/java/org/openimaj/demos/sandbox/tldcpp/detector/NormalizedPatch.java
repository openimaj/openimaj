package org.openimaj.demos.sandbox.tldcpp.detector;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.MeanCenter;
import org.openimaj.image.processing.resize.ResizeFilterFunction;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.resize.filters.TriangleFilter;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Defines a intensity normalised patch extracted from an image. Allowances are
 * made for reuse of patches
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class NormalizedPatch {
	private final static MeanCenter msp = new MeanCenter();
	/**
	 * Normalised patch size
	 */
	public static final int TLD_PATCH_SIZE = 15;
	private static final ResizeFilterFunction filter = TriangleFilter.INSTANCE;

	/**
	 * The slut workspace gets around a little bit. Use the slut workspace but
	 * don't expect it to be yours for long.
	 */
	public static final FImage SLUT_WORKSPACE = new FImage(TLD_PATCH_SIZE, TLD_PATCH_SIZE);
	/**
	 * Is this patch positive, i.e. representative of the object
	 */
	public boolean positive;
	/**
	 * The image to extract this patch from
	 */
	public FImage source;
	/**
	 * The window to extract form the source
	 */
	public Rectangle window;
	/**
	 * The extracted patch, might be null, might be the SLUT_WORKSPACE.
	 */
	public FImage normalisedPatch;

	/**
	 * A function which uses
	 * {@link ResizeProcessor#zoom(FImage, Rectangle, FImage, Rectangle, ResizeFilterFunction)}
	 * on a to put {@link NormalizedPatch#window} form
	 * {@link NormalizedPatch#source} into normalisedPatch.
	 * 
	 * This is not a convenient function but it allows for very efficient
	 * resize/normalisation process (with minimal new stuff constructed)
	 * 
	 * @param holder
	 * @return the holder as a convenience
	 */
	protected FImage zoomAndNormaliseTo(FImage holder) {
		ResizeProcessor.zoom(source, window, holder, holder.getBounds(), filter);
		return holder.processInplace(msp);
	}

	/**
	 * calculate the variance, sets the valueImg if it is null
	 * 
	 * @return an inefficient way to calculate variance of this window, a new
	 *         image is constructed!
	 */
	public float calculateVariance() {
		prepareNormalisedPatch();
		final float[][] value = normalisedPatch.pixels;
		float temp = 0;
		final int n = normalisedPatch.width * normalisedPatch.height;
		for (int y = 0; y < normalisedPatch.height; y++) {
			for (int x = 0; x < normalisedPatch.width; x++) {
				temp += (value[y][x]) * (value[y][x]); // There are two implied
														// (- 0)'s here. these
														// values are MEAN
														// CENTERED
			}
		}
		return temp / n;
	}

	/**
	 * for construction of a new normalised patch
	 */
	public void prepareNormalisedPatch() {
		if (this.normalisedPatch == null) {
			this.normalisedPatch = new FImage(TLD_PATCH_SIZE, TLD_PATCH_SIZE);
			zoomAndNormaliseTo(normalisedPatch);
		}
	}
}
