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

import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor.PixelContribution;
import org.openimaj.image.processing.resize.ResizeProcessor.PixelContributions;
import org.openimaj.image.processing.resize.filters.TriangleFilter;
import org.openimaj.image.processor.SinglebandImageProcessor;


/**
 * A copy of the {@link ResizeProcessor} which speeds up the resize operation
 * between images of a given size to another fixed size by caching the contribution
 * calculations
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FixedResizeProcessor implements SinglebandImageProcessor<Float, FImage> {
	
	private float newX;
	private float newY;
	private ResizeFilterFunction filterFunction;
	private float srcX;
	private float srcY;
	private ImageContributions ic;
	final float[] work;
	
	/**
	 * The default {@link TriangleFilter} (bilinear-interpolation filter) used
	 * by instances of {@link ResizeProcessor}, unless otherwise specified.
	 */
	public static final ResizeFilterFunction DEFAULT_FILTER = TriangleFilter.INSTANCE;
	
	/**
	 * Construct a fixed resize processor that will rescale the image to the given
	 * width and height with the given filter function. By default, this method
	 * will retain the image's aspect ratio.
	 * @param srcX 
	 * 			The expected width of input images
	 * @param srcY 
	 * 			The expected with of output images
	 * @param newX
	 *            The new width of the image.
	 * @param newY
	 *            The new height of the image.
	 * @param ff
	 *            The filter function to use.
	 */
	public FixedResizeProcessor(float srcX, float srcY, float newX, float newY, ResizeFilterFunction ff) {
		this.srcX = srcX;
		this.srcY = srcY;
		this.newX = newX;
		this.newY = newY;
		this.filterFunction = ff;
		prepareResample(true);
		this.work = new float[(int)newY];
	}

	/**
	 * Construct a fixed resize processor that will rescale the image to the given
	 * width and height with the default filter function. By default, this
	 * method will retain the image's aspect ratio which means that the
	 * resulting image may have dimensions less than those specified here.
	 * @param srcX 
	 * 			  The expected width of input images
	 * @param srcY
	 * 			  The expected height of input images
	 * @param newX
	 *            The new width of the image.
	 * @param newY
	 *            The new height of the image.
	 */
	public FixedResizeProcessor(float srcX, float srcY, float newX, float newY) {
		this(srcX,srcY,newX, newY, DEFAULT_FILTER);
	}
	
	/**
	 * @param image
	 * 			The expected width and height of input images
	 * @param newX
	 * 			The new width of the image
	 * @param newY
	 * 			The new height of the image
	 */
	public FixedResizeProcessor(FImage image, int newX, int newY) {
		this(image.width,image.height,newX,newY);
	}

	private void prepareResample(boolean aspect) {
		// Work out the size of the resampled image
		// if the aspect ratio is set to true
		int nx = (int)newX;
		int ny = (int)newY;
		if (aspect) {
			if (ny > nx)
				nx = (int) Math.round((this.srcX * ny) / (double) this.srcY);
			else
				ny = (int) Math.round((this.srcY * nx) / (double) this.srcX);
		}
		this.newX = nx;
		this.newY = ny;

		this.ic = FixedResizeProcessor.prepareZoom((int)srcX,(int)srcY,(int)newX,(int)newY,this.filterFunction);
	}
	static class ImageContributions{
		PixelContributions[] xContributions;
		PixelContributions[] yContributions;
	}
	private static ImageContributions prepareZoom(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ResizeFilterFunction filterf) {
		final double xscale = (double) dstWidth / (double) srcWidth;
		final double yscale = (double) dstHeight / (double) srcHeight;
		
		final PixelContributions[] contribY = new PixelContributions[dstHeight];
		for (int i = 0; i < contribY.length; i++) {
			contribY[i] = new PixelContributions();
		}
		
		final PixelContributions[] contribX = new PixelContributions[dstWidth];
		for (int i = 0; i < contribX.length; i++) {
			contribX[i] = new PixelContributions();
		}
		
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
				final int right = (int) Math.floor(center + width);

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
				final double right = Math.floor(center + fwidth);
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
		
		if (xscale < 1.0) {
			for (int i = 0; i < dstWidth; ++i) {
				/* Shrinking image */
				double width = fwidth / xscale;
				double fscale = 1.0 / xscale;
	
				if (width <= .5) {
					// Reduce to point sampling.
					width = .5 + 1.0e-6;
					fscale = 1.0;
				}
	
				contribX[i].numberOfContributors = 0;
				contribX[i].contributions = new PixelContribution[(int) (width * 2.0 + 1.0)];
	
				double center = i / xscale;
				final int left = (int) Math.ceil(center - width);// Note: Assumes
																	// width <= .5
				final int right = (int) Math.floor(center + width);
	
				double density = 0.0;
	
				for (int j = left; j <= right; j++) {
					double weight = center - j;
					weight = filterf.filter(weight / fscale) / fscale;
					int n;
					if (j < 0) {
						n = -j;
					}
					else if (j >= srcWidth) {
						n = (srcWidth - j) + srcWidth - 1;
					}
					else {
						n = j;
					}
	
					/**/
					if (n >= srcWidth) {
						n = n % srcWidth;
					}
					else if (n < 0) {
						n = srcWidth - 1;
					}
					/**/
	
					final int k = contribX[i].numberOfContributors++;
					contribX[i].contributions[k] = new PixelContribution();
					contribX[i].contributions[k].pixel = n;
					contribX[i].contributions[k].weight = weight;
	
					density += weight;
	
				}
	
				if ((density != 0.0) && (density != 1.0)) {
					// Normalize.
					density = 1.0 / density;
					for (int k = 0; k < contribX[i].numberOfContributors; k++) {
						contribX[i].contributions[k].weight *= density;
					}
				}
			}
		}
		else {
			for (int i = 0; i < dstWidth; ++i) {
				/* Expanding image */
				contribX[i].numberOfContributors = 0;
				contribX[i].contributions = new PixelContribution[(int) (fwidth * 2.0 + 1.0)];
	
				double center = i / xscale;
				final int left = (int) Math.ceil(center - fwidth);
				final int right = (int) Math.floor(center + fwidth);
	
				for (int j = left; j <= right; j++) {
					double weight = center - j;
					weight = filterf.filter(weight);
	
					int n;
					if (j < 0) {
						n = -j;
					}
					else if (j >= srcWidth) {
						n = (srcWidth - j) + srcWidth - 1;
					}
					else {
						n = j;
					}
	
					/**/
					if (n >= srcWidth) {
						n = n % srcWidth;
					}
					else if (n < 0) {
						n = srcWidth - 1;
					}
					/**/
	
					final int k = contribX[i].numberOfContributors++;
					contribX[i].contributions[k] = new PixelContribution();
					contribX[i].contributions[k].pixel = n;
					contribX[i].contributions[k].weight = weight;
				}
			}
		}
		
		ImageContributions ic = new ImageContributions();
		ic.xContributions = contribX;
		ic.yContributions = contribY;
		
		return ic;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage(FImage in) {
		if(in.width != this.srcX || in.height != srcY){
			throw new RuntimeException("Incompatible image type used with FixedResizeProcessor, try the normal ResizeProcessor");
		}
		/* create intermediate column to hold horizontal dst column zoom */
		
		FImage dst = new FImage((int)this.newX,(int)this.newY);
		final float maxValue = in.max();
		for (int xx = 0; xx < dst.width; xx++) {
			final PixelContributions contribX = this.ic.xContributions[xx];

			/* Apply horiz filter to make dst column in tmp. */
			for (int k = 0; k < in.height; k++) {
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
			for (int i = 0; i < dst.height; i++) {
				double weight = 0.0;
				boolean bPelDelta = false;
				final double pel = work[ic.yContributions[i].contributions[0].pixel];

				for (int j = 0; j < ic.yContributions[i].numberOfContributors; j++) {
					// TODO: This line throws index out of bounds, if the
					// image is smaller than filter.support()
					final double pel2 = j == 0 ? pel : work[ic.yContributions[i].contributions[j].pixel];
					if (pel2 != pel) {
						bPelDelta = true;
					}
					weight += pel2 * ic.yContributions[i].contributions[j].weight;
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
		
		in.internalAssign(dst);
	}

}
