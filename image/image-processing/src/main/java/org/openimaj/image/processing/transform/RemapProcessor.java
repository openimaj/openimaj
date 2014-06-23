package org.openimaj.image.processing.transform;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.ImageInterpolation;
import org.openimaj.image.analysis.algorithm.ImageInterpolation.InterpolationType;
import org.openimaj.image.analysis.algorithm.ImageInterpolation.Interpolator;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * {@link ImageProcessor} and associated static utility methods for transforming
 * an image based on the pixel positions given by a distortion map:
 * <code>destination(x,y) = source(mapx(x,y), mapy(x,y))</code>. This allows
 * efficient implementation of highly non-linear mappings.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class RemapProcessor implements SinglebandImageProcessor<Float, FImage> {
	ImageInterpolation interpolation;
	FImage xords;
	FImage yords;

	/**
	 * Construct with the given distortion map, and use
	 * {@link InterpolationType#BILINEAR} interpolation.
	 * 
	 * @param xords
	 *            the x-ordinates
	 * @param yords
	 *            the y-ordinates
	 */
	public RemapProcessor(FImage xords, FImage yords) {
		this(xords, yords, ImageInterpolation.InterpolationType.BILINEAR);
	}

	/**
	 * Construct with the given distortion map and interpolator.
	 * 
	 * @param xords
	 *            the x-ordinates
	 * @param yords
	 *            the y-ordinates
	 * @param interpolator
	 *            the interpolator
	 */
	public RemapProcessor(FImage xords, FImage yords, Interpolator interpolator) {
		this.interpolation = new ImageInterpolation(interpolator);
		this.xords = xords;
		this.yords = yords;
	}

	@Override
	public void processImage(FImage image) {
		final FImage out = remap(image, xords, yords, interpolation);
		image.internalAssign(out);
	}

	/**
	 * Transform an image using the given parameters
	 * 
	 * @param in
	 *            the image to transform
	 * @param xords
	 *            the x-ordinates
	 * @param yords
	 *            the y-ordinates
	 * @param interpolator
	 *            the interpolator
	 * @return the transformed image
	 */
	public static FImage remap(FImage in, FImage xords, FImage yords, Interpolator interpolator) {
		return remap(in, xords, yords, new ImageInterpolation(interpolator));
	}

	/**
	 * Transform an image using the given parameters
	 * 
	 * @param in
	 *            the image to transform
	 * @param xords
	 *            the x-ordinates
	 * @param yords
	 *            the y-ordinates
	 * @param interpolation
	 *            the interpolation
	 * @return the transformed image
	 */
	public static FImage remap(FImage in, FImage xords, FImage yords, ImageInterpolation interpolation) {
		return remap(in, new FImage(xords.width, xords.height), xords, yords, interpolation);
	}

	/**
	 * Transform an image using the given parameters, and write te results into
	 * <code>out</code>
	 * 
	 * @param in
	 *            the image to transform
	 * @param out
	 *            the output image
	 * @param xords
	 *            the x-ordinates
	 * @param yords
	 *            the y-ordinates
	 * @param interpolation
	 *            the interpolation
	 * @return out
	 */
	public static FImage remap(FImage in, FImage out, FImage xords, FImage yords, ImageInterpolation interpolation) {
		final int width = Math.min(xords.width, out.width);
		final int height = Math.min(xords.height, out.height);

		interpolation.analyseImage(in);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				out.pixels[y][x] = interpolation.getPixelInterpolated(xords.pixels[y][x], yords.pixels[y][x]);
			}
		}
		return out;
	}

	/**
	 * Transform an image using the given parameters. Assume
	 * {@link InterpolationType#BILINEAR} interpolation.
	 * 
	 * @param in
	 *            the image to transform
	 * @param xords
	 *            the x-ordinates
	 * @param yords
	 *            the y-ordinates
	 * @return the transformed image
	 */
	public static FImage remap(FImage in, FImage xords, FImage yords) {
		return remap(in, xords, yords, new ImageInterpolation(ImageInterpolation.InterpolationType.BILINEAR));
	}

	/**
	 * Transform an image using the given parameters, and write the results into
	 * <code>out</code>. Assume {@link InterpolationType#BILINEAR}
	 * interpolation.
	 * 
	 * @param in
	 *            the image to transform
	 * @param out
	 *            the output image
	 * @param xords
	 *            the x-ordinates
	 * @param yords
	 *            the y-ordinates
	 * @return out
	 */
	public static FImage remap(FImage in, FImage out, FImage xords, FImage yords) {
		return remap(in, out, xords, yords, new ImageInterpolation(ImageInterpolation.InterpolationType.BILINEAR));
	}
}
