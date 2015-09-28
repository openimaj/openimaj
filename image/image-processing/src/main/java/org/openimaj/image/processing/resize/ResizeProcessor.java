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
package org.openimaj.image.processing.resize;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.resize.filters.TriangleFilter;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Image processor and utility methods that can resize images.
 * <p>
 * Based on <code>filter_rcg.c</code> by Dale Schumacher and Ray Gardener from
 * Graphics Gems III, with improvements from TwelveMonkeys and ImageMagick,
 * which in-particular fix normalisation problems.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Incollection,
		author = { "Schumacher, Dale" },
		title = "Graphics Gems III",
		year = "1992",
		pages = { "8", "", "16" },
		chapter = "General Filtered Image Rescaling",
		url = "http://dl.acm.org/citation.cfm?id=130745.130747",
		editor = { "Kirk, David" },
		publisher = "Academic Press Professional, Inc.",
		customData = {
				"isbn", "0-12-409671-9",
				"numpages", "9",
				"acmid", "130747",
				"address", "San Diego, CA, USA"
		})
public class ResizeProcessor implements SinglebandImageProcessor<Float, FImage> {
	/**
	 * The resize mode to use.
	 *
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 * @created 4 Apr 2011
	 */
	public static enum Mode {
		/** Double the size of the image using bilinear interpolation */
		DOUBLE,
		/** Halve the size of the image, by sampling alternate pixels */
		HALF,
		/** Scale the image using the given factors */
		SCALE,
		/** Resize the image preserving aspect ratio */
		ASPECT_RATIO,
		/** Resize the image to fit */
		FIT,
		/**
		 * Resize to so that the longest side is at most the given maximum.
		 * Images smaller than the max size are unchanged.
		 */
		MAX,
		/**
		 * Resize to so that the area is at most the given maximum. Images with
		 * an area smaller than the max area are unchanged.
		 */
		MAX_AREA,
		/** Lazyness operator to allow the quick switching off of resize filters **/
		NONE,
	}

	/** The resize mode to use. */
	private Mode mode = null;

	/** The amount to scale the image by */
	private float amount = 0;

	/** The new width of the image */
	private float newX;

	/** The new height of the image */
	private float newY;

	/** The resize filter function to use */
	private ResizeFilterFunction filterFunction;

	/**
	 * The default {@link TriangleFilter} (bilinear-interpolation filter) used
	 * by instances of {@link ResizeProcessor}, unless otherwise specified.
	 */
	public static final ResizeFilterFunction DEFAULT_FILTER = TriangleFilter.INSTANCE;

	/**
	 * Constructor that takes the resize mode. Use this function if you only
	 * want to {@link Mode#DOUBLE double} or {@link Mode#HALF halve} the image
	 * size.
	 *
	 * @param mode
	 *            The resize mode.
	 */
	public ResizeProcessor(Mode mode) {
		this.mode = mode;
		this.filterFunction = DEFAULT_FILTER;
	}

	/**
	 * Constructor a resize processor that will rescale the image by a given
	 * scale factor using the given filter function.
	 *
	 * @param amount
	 *            The amount to scale the image by
	 * @param ff
	 *            The resize filter function to use.
	 */
	public ResizeProcessor(float amount, ResizeFilterFunction ff) {
		this.mode = Mode.SCALE;
		this.amount = amount;
		this.filterFunction = ff;
	}

	/**
	 * Construct a resize processor that will rescale the image to the given
	 * width and height with the given filter function. By default, this method
	 * will retain the image's aspect ratio.
	 *
	 * @param newX
	 *            The new width of the image.
	 * @param newY
	 *            The new height of the image.
	 * @param ff
	 *            The filter function to use.
	 */
	public ResizeProcessor(float newX, float newY, ResizeFilterFunction ff) {
		this.mode = Mode.ASPECT_RATIO;
		this.newX = newX;
		this.newY = newY;
		this.filterFunction = ff;
	}

	/**
	 * Constructor a resize processor that will rescale the image by a given
	 * scale factor using the default filter function.
	 *
	 * @param amount
	 *            The amount to scale the image by
	 */
	public ResizeProcessor(float amount) {
		this(amount, DEFAULT_FILTER);
	}

	/**
	 * Construct a resize processor that will rescale the image to the given
	 * width and height with the default filter function. By default, this
	 * method will retain the image's aspect ratio which means that the
	 * resulting image may have dimensions less than those specified here.
	 *
	 * @param newX
	 *            The new width of the image.
	 * @param newY
	 *            The new height of the image.
	 */
	public ResizeProcessor(float newX, float newY) {
		this(newX, newY, DEFAULT_FILTER);
	}

	/**
	 * Construct a resize processor that will rescale images that are taller or
	 * wider than the given size such that their biggest side is equal to the
	 * given size. Images that have both sides smaller than the given size will
	 * be unchanged.
	 *
	 * @param maxSize
	 *            The maximum allowable height or width
	 */
	public ResizeProcessor(int maxSize) {
		this.mode = Mode.MAX;
		this.newX = maxSize;
		this.newY = maxSize;
		this.filterFunction = DEFAULT_FILTER;
	}

	/**
	 * Construct a resize processor that will rescale images that are either
	 * bigger than a maximum area or are taller or wider than the given size
	 * such that their biggest side is equal to the given size. Images that have
	 * a smaller area or both sides smaller than the given size will be
	 * unchanged.
	 *
	 * @param maxSizeArea
	 *            The maximum allowable area, or height or width
	 * @param area
	 *            If true, then the limit is the area; false means limit is
	 *            longest side.
	 */
	public ResizeProcessor(int maxSizeArea, boolean area) {
		this.mode = area ? Mode.MAX_AREA : Mode.MAX;
		this.newX = maxSizeArea;
		this.newY = maxSizeArea;
	}

	/**
	 * Construct a resize processor that will rescale the image to the given
	 * width and height (optionally maintaining aspect ratio) with the default
	 * filter function. If <code>aspectRatio</code> is false the image will be
	 * stretched to fit within the new width and height. If
	 * <code>aspectRatio</code> is set to true, the resulting images may have
	 * dimensions less than those specified here.
	 *
	 * @param newX
	 *            The new width of the image.
	 * @param newY
	 *            The new height of the image.
	 * @param aspectRatio
	 *            Whether to maintain the aspect ratio or not
	 */
	public ResizeProcessor(int newX, int newY, boolean aspectRatio) {
		this(newX, newY, DEFAULT_FILTER);

		if (aspectRatio)
			this.mode = Mode.ASPECT_RATIO;
		else
			this.mode = Mode.FIT;
	}

	/**
	 * Construct a resize processor that will rescale the image to the given
	 * width and height (optionally maintaining aspect ratio) with the given
	 * filter function. If <code>aspectRatio</code> is false the image will be
	 * stretched to fit within the new width and height. If
	 * <code>aspectRatio</code> is set to true, the resulting images may have
	 * dimensions less than those specified here.
	 *
	 * @param newX
	 *            The new width of the image.
	 * @param newY
	 *            The new height of the image.
	 * @param aspectRatio
	 *            Whether to maintain the aspect ratio or not
	 * @param filterf
	 *            The filter function
	 */
	public ResizeProcessor(int newX, int newY, boolean aspectRatio, ResizeFilterFunction filterf) {
		this(newX, newY, filterf);

		if (aspectRatio)
			this.mode = Mode.ASPECT_RATIO;
		else
			this.mode = Mode.FIT;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage(FImage image) {
		switch (this.mode) {
		case DOUBLE:
			internalDoubleSize(image);
			break;
		case HALF:
			internalHalfSize(image);
			break;
		case FIT:
			zoomInplace(image, (int) newX, (int) newY, filterFunction);
			break;
		case SCALE:
			newX = image.width * amount;
			newY = image.height * amount;
		case ASPECT_RATIO:
			resample(image, (int) newX, (int) newY, true, filterFunction);
			break;
		case MAX:
			resizeMax(image, (int) newX, filterFunction);
			break;
		case MAX_AREA:
			resizeMaxArea(image, (int) newX, filterFunction);
			break;
		case NONE:
			return;
		default:
			zoomInplace(image, (int) newX, (int) newY, this.filterFunction);
		}
	}

	/**
	 * Set the filter function used by the filter
	 *
	 * @param filterFunction
	 *            the filter function
	 */
	public void setFilterFunction(ResizeFilterFunction filterFunction) {
		this.filterFunction = filterFunction;
	}

	/**
	 * Resize an image such that its biggest size is at most as big as the given
	 * size. Images whose sides are smaller than the given size are untouched.
	 *
	 * @param image
	 *            the image to resize
	 * @param maxDim
	 *            the maximum allowable length for the longest side.
	 * @param filterf
	 *            The filter function
	 * @return the input image, appropriately resized.
	 */
	public static FImage resizeMax(FImage image, int maxDim, ResizeFilterFunction filterf) {
		final int width = image.width;
		final int height = image.height;

		int newWidth, newHeight;
		if (width < maxDim && height < maxDim) {
			return image;
		} else if (width < height) {
			newHeight = maxDim;
			final float resizeRatio = ((float) maxDim / (float) height);
			newWidth = (int) (width * resizeRatio);
		} else {
			newWidth = maxDim;
			final float resizeRatio = ((float) maxDim / (float) width);
			newHeight = (int) (height * resizeRatio);
		}

		zoomInplace(image, newWidth, newHeight, filterf);

		return image;
	}

	/**
	 * Resize an image such that its area size is at most as big as the given
	 * area. Images whose ares are smaller than the given area are untouched.
	 *
	 * @param image
	 *            the image to resize
	 * @param maxArea
	 *            the maximum allowable area.
	 * @param filterf
	 *            The filter function
	 * @return the input image, appropriately resized.
	 */
	public static FImage resizeMaxArea(FImage image, int maxArea, ResizeFilterFunction filterf) {
		final int width = image.width;
		final int height = image.height;
		final int area = width * height;

		if (area < maxArea) {
			return image;
		} else {
			final double whRatio = (double) width / (double) height;
			final int newWidth = (int) Math.sqrt(maxArea * whRatio);
			final int newHeight = (int) (newWidth / whRatio);

			zoomInplace(image, newWidth, newHeight, filterf);

			return image;
		}
	}

	/**
	 * Resize an image such that its biggest size is at most as big as the given
	 * size. Images whose sides are smaller than the given size are untouched.
	 *
	 * @param image
	 *            the image to resize
	 * @param maxDim
	 *            the maximum allowable length for the longest side.
	 * @return the input image, resized appropriately
	 */
	public static FImage resizeMax(FImage image, int maxDim) {
		final int width = image.width;
		final int height = image.height;

		int newWidth, newHeight;
		if (width < maxDim && height < maxDim) {
			return image;
		} else if (width < height) {
			newHeight = maxDim;
			final float resizeRatio = ((float) maxDim / (float) height);
			newWidth = (int) (width * resizeRatio);
		} else {
			newWidth = maxDim;
			final float resizeRatio = ((float) maxDim / (float) width);
			newHeight = (int) (height * resizeRatio);
		}

		zoomInplace(image, newWidth, newHeight);

		return image;
	}

	/**
	 * Resize an image such that its area size is at most as big as the given
	 * area. Images whose ares are smaller than the given area are untouched.
	 *
	 * @param image
	 *            the image to resize
	 * @param maxArea
	 *            the maximum allowable area.
	 * @return the input image, resized appropriately
	 */
	public static FImage resizeMaxArea(FImage image, int maxArea) {
		return resizeMaxArea(image, maxArea, DEFAULT_FILTER);
	}

	/**
	 * Double the size of the image.
	 *
	 * @param <I>
	 *            the image type
	 *
	 * @param image
	 *            The image to double in size
	 * @return a copy of the original image with twice the size
	 */
	public static <I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>> I doubleSize(I image) {
		return image.process(new ResizeProcessor(Mode.DOUBLE));
	}

	/**
	 * Double the size of the image.
	 *
	 * @param image
	 *            The image to double in size
	 * @return a copy of the original image with twice the size
	 */
	public static FImage doubleSize(FImage image) {
		int nheight, nwidth;
		float im[][], tmp[][];
		FImage newimage;

		nheight = 2 * image.height - 2;
		nwidth = 2 * image.width - 2;
		newimage = new FImage(nwidth, nheight);
		im = image.pixels;
		tmp = newimage.pixels;

		for (int y = 0; y < image.height - 1; y++) {
			for (int x = 0; x < image.width - 1; x++) {
				final int y2 = 2 * y;
				final int x2 = 2 * x;
				tmp[y2][x2] = im[y][x];
				tmp[y2 + 1][x2] = 0.5f * (im[y][x] + im[y + 1][x]);
				tmp[y2][x2 + 1] = 0.5f * (im[y][x] + im[y][x + 1]);
				tmp[y2 + 1][x2 + 1] = 0.25f * (im[y][x] + im[y + 1][x] + im[y][x + 1] + im[y + 1][x + 1]);
			}
		}
		return newimage;
	}

	protected static void internalDoubleSize(FImage image) {
		image.internalAssign(doubleSize(image));
	}

	/**
	 * Halve the size of the image.
	 *
	 * @param <I>
	 *
	 * @param image
	 *            The image halve in size
	 * @return a copy of the input image with half the size
	 */
	public static <I extends Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>> I halfSize(I image) {
		return image.process(new ResizeProcessor(Mode.HALF));
	}

	/**
	 * Halve the size of the image. Note that this method just samples every
	 * other pixel and will produce aliasing unless the image has been
	 * pre-filtered.
	 *
	 * @param image
	 *            The image halve in size
	 * @return a copy the the image with half the size
	 */
	public static FImage halfSize(FImage image) {
		int newheight, newwidth;
		float im[][], tmp[][];
		FImage newimage;

		newheight = image.height / 2;
		newwidth = image.width / 2;
		newimage = new FImage(newwidth, newheight);
		im = image.pixels;
		tmp = newimage.pixels;

		for (int y = 0, yi = 0; y < newheight; y++, yi += 2) {
			for (int x = 0, xi = 0; x < newwidth; x++, xi += 2) {
				tmp[y][x] = im[yi][xi];
			}
		}

		return newimage;
	}

	protected static void internalHalfSize(FImage image) {
		image.internalAssign(halfSize(image));
	}

	/**
	 * Returns a new image that is a resampled version of the given image.
	 *
	 * @param in
	 *            The source image
	 * @param newX
	 *            The new width of the image
	 * @param newY
	 *            The new height of the image
	 * @return A new {@link FImage}
	 */
	public static FImage resample(FImage in, int newX, int newY) {
		return resample(in.clone(), newX, newY, false);
	}

	/**
	 * Resamples the given image returning it as a reference. If
	 * <code>aspect</code> is true, the aspect ratio of the image will be
	 * retained, which means newX or newY could be smaller than given here. The
	 * dimensions of the new image will not be larger than newX or newY.
	 * Side-affects the given image.
	 *
	 * @param in
	 *            The source image
	 * @param newX
	 *            The new width of the image
	 * @param newY
	 *            The new height of the image
	 * @param aspect
	 *            Whether to maintain the aspect ratio
	 * @return the input image, resized appropriately
	 */
	public static FImage resample(FImage in, int newX, int newY, boolean aspect) {
		// Work out the size of the resampled image
		// if the aspect ratio is set to true
		int nx = newX;
		int ny = newY;
		if (aspect) {
			if (ny > nx)
				nx = (int) Math.round((in.width * ny) / (double) in.height);
			else
				ny = (int) Math.round((in.height * nx) / (double) in.width);
		}

		zoomInplace(in, nx, ny);
		return in;
	}

	/**
	 * Resamples the given image returning it as a reference. If
	 * <code>aspect</code> is true, the aspect ratio of the image will be
	 * retained, which means newX or newY could be smaller than given here. The
	 * dimensions of the new image will not be larger than newX or newY.
	 * Side-affects the given image.
	 *
	 * @param in
	 *            The source image
	 * @param newX
	 *            The new width of the image
	 * @param newY
	 *            The new height of the image
	 * @param aspect
	 *            Whether to maintain the aspect ratio
	 * @param filterf
	 *            The filter function
	 * @return the input image, resized appropriately
	 */
	public static FImage resample(FImage in, int newX, int newY, boolean aspect, ResizeFilterFunction filterf)
	{
		// Work out the size of the resampled image
		// if the aspect ratio is set to true
		int nx = newX;
		int ny = newY;
		if (aspect) {
			if (ny > nx)
				nx = (int) Math.round((in.width * ny) / (double) in.height);
			else
				ny = (int) Math.round((in.height * nx) / (double) in.width);
		}

		zoomInplace(in, nx, ny, filterf);
		return in;
	}

	/**
	 * For the port of the zoom function
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *
	 */
	static class PixelContribution {
		/** Index of the pixel */
		int pixel;

		double weight;
	}

	/**
	 * For the port of the zoom function
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *
	 */
	static class PixelContributions {
		int numberOfContributors;

		PixelContribution[] contributions;
	}

	/**
	 * Calculates the filter weights for a single target column. contribX->p
	 * must be freed afterwards.
	 *
	 * @param contribX
	 *            Receiver of contrib info
	 * @param xscale
	 *            Horizontal zooming scale
	 * @param fwidth
	 *            Filter sampling width
	 * @param dstwidth
	 *            Target bitmap width
	 * @param srcwidth
	 *            Source bitmap width
	 * @param filterf
	 *            Filter processor
	 * @param i
	 *            Pixel column in source bitmap being processed
	 *
	 * @returns -1 if error, 0 otherwise.
	 */
	private static void calc_x_contrib(PixelContributions contribX, double xscale, double fwidth, int dstwidth,
			int srcwidth, ResizeFilterFunction filterf, int i)
	{
		double width;
		double fscale;
		double center;
		double weight;

		if (xscale < 1.0) {
			/* Shrinking image */
			width = fwidth / xscale;
			fscale = 1.0 / xscale;

			if (width <= .5) {
				// Reduce to point sampling.
				width = .5 + 1.0e-6;
				fscale = 1.0;
			}

			contribX.numberOfContributors = 0;
			contribX.contributions = new PixelContribution[(int) (width * 2.0 + 1.0)];

			center = i / xscale;
			final int left = (int) Math.ceil(center - width);// Note: Assumes
			// width <= .5
			final int right = (int) Math.floor(center + width);

			double density = 0.0;

			for (int j = left; j <= right; j++) {
				weight = center - j;
				weight = filterf.filter(weight / fscale) / fscale;
				int n;
				if (j < 0) {
					n = -j;
				}
				else if (j >= srcwidth) {
					n = (srcwidth - j) + srcwidth - 1;
				}
				else {
					n = j;
				}

				/**/
				if (n >= srcwidth) {
					n = n % srcwidth;
				}
				else if (n < 0) {
					n = srcwidth - 1;
				}
				/**/

				final int k = contribX.numberOfContributors++;
				contribX.contributions[k] = new PixelContribution();
				contribX.contributions[k].pixel = n;
				contribX.contributions[k].weight = weight;

				density += weight;

			}

			if ((density != 0.0) && (density != 1.0)) {
				// Normalize.
				density = 1.0 / density;
				for (int k = 0; k < contribX.numberOfContributors; k++) {
					contribX.contributions[k].weight *= density;
				}
			}
		}
		else {
			/* Expanding image */
			contribX.numberOfContributors = 0;
			contribX.contributions = new PixelContribution[(int) (fwidth * 2.0 + 1.0)];

			center = i / xscale;
			final int left = (int) Math.ceil(center - fwidth);
			final int right = (int) Math.floor(center + fwidth);

			for (int j = left; j <= right; j++) {
				weight = center - j;
				weight = filterf.filter(weight);

				int n;
				if (j < 0) {
					n = -j;
				}
				else if (j >= srcwidth) {
					n = (srcwidth - j) + srcwidth - 1;
				}
				else {
					n = j;
				}

				/**/
				if (n >= srcwidth) {
					n = n % srcwidth;
				}
				else if (n < 0) {
					n = srcwidth - 1;
				}
				/**/

				final int k = contribX.numberOfContributors++;
				contribX.contributions[k] = new PixelContribution();
				contribX.contributions[k].pixel = n;
				contribX.contributions[k].weight = weight;
			}
		}
	}/* calcXContrib */

	/**
	 * Resizes an image.
	 *
	 * @param in
	 *            The source image
	 * @param newX
	 *            The desired width of the image
	 * @param newY
	 *            The desired height of the image
	 * @return the input image, resized appropriately
	 */
	public static FImage zoomInplace(FImage in, int newX, int newY) {
		final ResizeFilterFunction filter = DEFAULT_FILTER;
		return zoomInplace(in, newX, newY, filter);
	}

	/**
	 * Resizes an image.
	 *
	 * @param newX
	 *            New width of the image
	 * @param newY
	 *            New height of the image
	 * @param in
	 *            The source image
	 * @param filterf
	 *            The filter function
	 * @return the input image, resized appropriately
	 */
	public static FImage zoomInplace(FImage in, int newX, int newY, ResizeFilterFunction filterf) {
		final FImage dst = new FImage(newX, newY);
		zoom(in, dst, filterf);
		in.internalAssign(dst);
		return in;
	}

	/**
	 * Resizes bitmaps while resampling them.
	 *
	 * @param dst
	 *            Destination Image
	 * @param in
	 *            Source Image
	 * @param filterf
	 *            Filter to use
	 *
	 * @return the destination image
	 */
	public static FImage zoom(FImage in, FImage dst, ResizeFilterFunction filterf) {
		final int dstWidth = dst.getWidth();
		final int dstHeight = dst.getHeight();

		final int srcWidth = in.getWidth();
		final int srcHeight = in.getHeight();

		final double xscale = (double) dstWidth / (double) srcWidth;
		final double yscale = (double) dstHeight / (double) srcHeight;

		/* create intermediate column to hold horizontal dst column zoom */
		final float[] work = new float[in.height];

		final PixelContributions[] contribY = new PixelContributions[dstHeight];
		for (int i = 0; i < contribY.length; i++) {
			contribY[i] = new PixelContributions();
		}

		final float maxValue = in.max();

		// TODO: What to do when fwidth > srcHeight or dstHeight
		final double fwidth = filterf.getSupport();
		if (yscale < 1.0) {
			double width = fwidth / yscale;
			double fscale = 1.0 / yscale;

			if (width <= .5) {
				// Reduce to point sampling.
				width = .5 + 1.0e-6;
				fscale = 1.0;
			}

			for (int i = 0; i < dstHeight; i++) {
				contribY[i].contributions = new PixelContribution[(int) (width * 2.0 + 1)];
				contribY[i].numberOfContributors = 0;

				final double center = i / yscale;
				final int left = (int) Math.ceil(center - width);
				// final int right = (int) Math.floor(center + width);
				final int right = left + contribY[i].contributions.length - 1;

				double density = 0.0;
				for (int j = left; j <= right; j++) {
					double weight = center - j;
					weight = filterf.filter(weight / fscale) / fscale;
					int n;
					if (j < 0) {
						n = -j;
					}
					else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					}
					else {
						n = j;
					}

					/**/
					if (n >= srcHeight) {
						n = n % srcHeight;
					}
					else if (n < 0) {
						n = srcHeight - 1;
					}
					/**/

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;

					density += weight;
				}

				if ((density != 0.0) && (density != 1.0)) {
					// Normalize.
					density = 1.0 / density;
					for (int k = 0; k < contribY[i].numberOfContributors; k++) {
						contribY[i].contributions[k].weight *= density;
					}
				}
			}
		}
		else {
			for (int i = 0; i < dstHeight; ++i) {
				contribY[i].contributions = new PixelContribution[(int) (fwidth * 2 + 1)];
				contribY[i].numberOfContributors = 0;

				final double center = i / yscale;
				final double left = Math.ceil(center - fwidth);
				// final double right = Math.floor(center + fwidth);
				final double right = left + contribY[i].contributions.length - 1;
				for (int j = (int) left; j <= right; ++j) {
					double weight = center - j;
					weight = filterf.filter(weight);
					int n;
					if (j < 0) {
						n = -j;
					}
					else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					}
					else {
						n = j;
					}

					/**/
					if (n >= srcHeight) {
						n = n % srcHeight;
					}
					else if (n < 0) {
						n = srcHeight - 1;
					}
					/**/

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}

		for (int xx = 0; xx < dstWidth; xx++) {
			final PixelContributions contribX = new PixelContributions();
			calc_x_contrib(contribX, xscale, fwidth, dst.width, in.width, filterf, xx);

			/* Apply horiz filter to make dst column in tmp. */
			for (int k = 0; k < srcHeight; k++) {
				double weight = 0.0;
				boolean bPelDelta = false;
				// TODO: This line throws index out of bounds, if the image
				// is smaller than filter.support()
				final double pel = in.pixels[k][contribX.contributions[0].pixel];
				for (int j = 0; j < contribX.numberOfContributors; j++) {
					final double pel2 = j == 0 ? pel : in.pixels[k][contribX.contributions[j].pixel];
					if (pel2 != pel) {
						bPelDelta = true;
					}
					weight += pel2 * contribX.contributions[j].weight;
				}
				weight = bPelDelta ? Math.round(weight * 255) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				}
				else if (weight > maxValue) {
					weight = maxValue;
				}

				work[k] = (float) weight;
			}/* next row in temp column */

			/*
			 * The temp column has been built. Now stretch it vertically into
			 * dst column.
			 */
			for (int i = 0; i < dstHeight; i++) {
				double weight = 0.0;
				boolean bPelDelta = false;
				final double pel = work[contribY[i].contributions[0].pixel];

				for (int j = 0; j < contribY[i].numberOfContributors; j++) {
					// TODO: This line throws index out of bounds, if the
					// image is smaller than filter.support()
					final double pel2 = j == 0 ? pel : work[contribY[i].contributions[j].pixel];
					if (pel2 != pel) {
						bPelDelta = true;
					}
					weight += pel2 * contribY[i].contributions[j].weight;
				}
				weight = bPelDelta ? Math.round(weight * 255) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				}
				else if (weight > maxValue) {
					weight = maxValue;
				}

				dst.pixels[i][xx] = (float) weight;
			} /* next dst row */
		} /* next dst column */

		return dst;
	}

	/**
	 * Draws one portion of an image into another, resampling as necessary using
	 * the default filter function.
	 *
	 * @param dst
	 *            Destination Image
	 * @param in
	 *            Source Image
	 * @param inRect
	 *            the location of pixels in the source image
	 * @param dstRect
	 *            the destination of pixels in the destination image
	 * @return the destination image
	 */
	public static FImage zoom(FImage in, Rectangle inRect, FImage dst, Rectangle dstRect) {
		return zoom(in, inRect, dst, dstRect, DEFAULT_FILTER);
	}

	/**
	 * Draws one portion of an image into another, resampling as necessary.
	 *
	 * @param dst
	 *            Destination Image
	 * @param in
	 *            Source Image
	 * @param inRect
	 *            the location of pixels in the source image
	 * @param dstRect
	 *            the destination of pixels in the destination image
	 * @param filterf
	 *            Filter to use
	 *
	 * @return the destination image
	 */
	public static FImage zoom(FImage in, Rectangle inRect, FImage dst, Rectangle dstRect, ResizeFilterFunction filterf)
	{
		// First some sanity checking!
		if (!in.getBounds().isInside(inRect) || !dst.getBounds().isInside(dstRect))
			throw new IllegalArgumentException("Bad bounds");

		double xscale, yscale; /* zoom scale factors */
		int n; /* pixel number */
		double center, left, right; /* filter calculation variables */
		double width, fscale;
		double weight; /* filter calculation variables */
		boolean bPelDelta;
		float pel, pel2;
		PixelContributions contribX;

		// This is a convenience
		final FImage src = in;
		final int srcX = (int) inRect.x;
		final int srcY = (int) inRect.y;
		final int srcWidth = (int) inRect.width;
		final int srcHeight = (int) inRect.height;

		final int dstX = (int) dstRect.x;
		final int dstY = (int) dstRect.y;
		final int dstWidth = (int) dstRect.width;
		final int dstHeight = (int) dstRect.height;

		final float maxValue = in.max();

		/* create intermediate column to hold horizontal dst column zoom */
		final Float[] work = new Float[srcHeight];

		xscale = (double) dstWidth / (double) srcWidth;

		/* Build y weights */
		/* pre-calculate filter contributions for a column */
		final PixelContributions[] contribY = new PixelContributions[dstHeight];

		yscale = (double) dstHeight / (double) srcHeight;
		final double fwidth = filterf.getSupport();

		if (yscale < 1.0) {
			width = fwidth / yscale;
			fscale = 1.0 / yscale;
			double density = 0;
			for (int i = 0; i < dstHeight; ++i) {
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round(width * 2 + 1)];

				center = i / yscale;
				left = Math.ceil(center - width);
				right = Math.floor(center + width);
				for (int j = (int) left; j <= right; ++j) {
					weight = center - j;
					weight = filterf.filter(weight / fscale) / fscale;

					if (j < 0) {
						n = -j;
					} else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					} else {
						n = j;
					}

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
					density += weight;
				}

				if ((density != 0.0) && (density != 1.0)) {
					// Normalize.
					density = 1.0 / density;
					for (int k = 0; k < contribY[i].numberOfContributors; k++) {
						contribY[i].contributions[k].weight *= density;
					}
				}
			}
		} else {
			for (int i = 0; i < dstHeight; ++i) {
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round(fwidth * 2 + 1)];

				center = i / yscale;
				left = Math.ceil(center - fwidth);
				right = Math.floor(center + fwidth);
				for (int j = (int) left; j <= right; ++j) {
					weight = center - j;
					weight = filterf.filter(weight);

					if (j < 0) {
						n = -j;
					} else if (j >= srcHeight) {
						n = (srcHeight - j) + srcHeight - 1;
					} else {
						n = j;
					}

					final int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}

		for (int xx = 0; xx < dstWidth; xx++) {
			contribX = new PixelContributions();
			calc_x_contrib(contribX, xscale, fwidth, dstWidth, srcWidth, filterf, xx);

			/* Apply horz filter to make dst column in tmp. */
			for (int k = 0; k < srcHeight; ++k) {
				weight = 0.0;
				bPelDelta = false;

				pel = src.pixels[k + srcY][contribX.contributions[0].pixel + srcX];

				for (int j = 0; j < contribX.numberOfContributors; ++j) {
					pel2 = src.pixels[k + srcY][contribX.contributions[j].pixel + srcX];
					if (pel2 != pel)
						bPelDelta = true;
					weight += pel2 * contribX.contributions[j].weight;
				}
				weight = bPelDelta ? Math.round(weight * 255f) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				}
				else if (weight > maxValue) {
					weight = maxValue;
				}

				work[k] = (float) weight;
			} /* next row in temp column */

			/*
			 * The temp column has been built. Now stretch it vertically into
			 * dst column.
			 */
			for (int i = 0; i < dstHeight; ++i) {
				weight = 0.0;
				bPelDelta = false;
				pel = work[contribY[i].contributions[0].pixel];

				for (int j = 0; j < contribY[i].numberOfContributors; ++j) {
					pel2 = work[contribY[i].contributions[j].pixel];
					if (pel2 != pel)
						bPelDelta = true;
					weight += pel2 * contribY[i].contributions[j].weight;
				}

				weight = bPelDelta ? Math.round(weight * 255f) / 255f : pel;

				if (weight < 0) {
					weight = 0;
				}
				else if (weight > maxValue) {
					weight = maxValue;
				}

				dst.pixels[i + dstY][xx + dstX] = (float) weight;

			} /* next dst row */
		} /* next dst column */

		return dst;
	} /* zoom */
}
