/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
