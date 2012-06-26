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
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * 	Image processor that 
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @authorSina Samangooei <ss@ecs.soton.ac.uk>
 *	
 */
public class ResizeProcessor implements SinglebandImageProcessor<Float,FImage> 
{
	/**
	 * 	The resize mode to use.
	 * 
	 *  @author Sina Samangooei <ss@ecs.soton.ac.uk>, David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 4 Apr 2011
	 */
	public static enum Mode
	{
		/** Double the size of the image */
		DOUBLE,
		/** Halve the size of the image */
		HALF,
		/** The amount to scale the image by */
		SCALE,
		/** Resize the image preserving aspect ratio */
		ASPECT_RATIO,
		/** Resize the image to fit */
		FIT,
		/** Lazyness operator to allow the quick switching off of resize filters **/
		NONE
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
	private ResizeFilterFunction filterFunction = new BasicFilter();
	
	/**
	 * 	Constructor that takes the resize mode. Use this function
	 * 	if you only want to {@link Mode#DOUBLE double} or 
	 * 	{@link Mode#HALF halve} the image size.
	 * 
	 *  @param mode The resize mode.
	 */
	public ResizeProcessor( Mode mode )
	{
		this.mode = mode;
	}
	
	/**
	 * 	Constructor a resize processor that will rescale the image by a given
	 * 	scale factor using the given filter function.
	 * 
	 *  @param amount The amount to scale the image by
	 *  @param ff The resize filter function to use.
	 */
	public ResizeProcessor( float amount, ResizeFilterFunction ff )
	{
		this.mode = Mode.SCALE;
		this.amount = amount;
		this.filterFunction = ff;
	}
	
	/**
	 * 	Construct a resize processor that will rescale the image to the given
	 * 	width and height with the given filter function. By default, this
	 * 	method will retain the image's aspect ratio.
	 * 
	 *  @param newX The new width of the image.
	 *  @param newY The new height of the image.
	 *  @param ff The filter function to use.
	 */
	public ResizeProcessor( float newX, float newY, ResizeFilterFunction ff )
	{
		this.mode = Mode.ASPECT_RATIO;
		this.newX = newX;
		this.newY = newY;
		this.filterFunction = ff;
	}
	
	/**
	 * 	Constructor a resize processor that will rescale the image by a given
	 * 	scale factor using the default filter function.
	 * 
	 *  @param amount The amount to scale the image by
	 */
	public ResizeProcessor( float amount )
	{
		this( amount, null );
	}
	
	/**
	 * 	Construct a resize processor that will rescale the image to the given
	 * 	width and height with the default filter function. By default, this
	 * 	method will retain the image's aspect ratio which means that the
	 * 	resulting image may have dimensions less than those specified here.
	 * 
	 *  @param newX The new width of the image.
	 *  @param newY The new height of the image.
	 */
	public ResizeProcessor( float newX, float newY )
	{
		this( newX, newY, null );
	}
	
	/**
	 * 	Construct a resize processor that will rescale the image to the given
	 * 	width and height (optionally maintaining aspect ratio) 
	 * 	with the default filter function. If <code>aspectRatio</code> is false
	 * 	the image will be stretched to fit within the new width and height.
	 * 	If <code>aspectRatio</code> is set to true, the resulting images may
	 * 	have dimensions less than those specified here. 
	 * 
	 *  @param newX The new width of the image.
	 *  @param newY The new height of the image.
	 *  @param aspectRatio Whether to maintain the aspect ratio or not
	 */	
	public ResizeProcessor(int newX, int newY, boolean aspectRatio ) 
	{
		this( newX, newY, null );
		if( aspectRatio ) 
				this.mode = Mode.ASPECT_RATIO;
		else	this.mode = Mode.FIT;
	}

	/**
	 * 	{@inheritDoc}
	 * 	@see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage(FImage image)
	{
		switch( this.mode )
		{
			case DOUBLE: 		internalDoubleSize( image ); break;
			case HALF:   		internalHalfSize( image ); break;
			case FIT:			zoom( image, (int)newX, (int)newY ); break;
			case SCALE:			newX = image.width * amount;
								newY = image.height * amount;
			case ASPECT_RATIO: 	resample( image, (int)newX, (int)newY, true ); break;
			case NONE: return;
			default: 
				zoom( image, (int)newX, (int)newY, this.filterFunction, 
						this.filterFunction.getDefaultSupport() );
		}
	}

	/**
	 * 	Double the size of the image.
	 *  @param <I> the image type 
	 * 
	 *  @param image The image to double in size
	 *  @return a copy of the original image with twice the size
	 */
	public static <I extends Image<?,I> & SinglebandImageProcessor.Processable<Float,FImage,I>> I doubleSize(I image) {
		return image.process(new ResizeProcessor(Mode.DOUBLE));
	}
	
	/**
	 * 	Double the size of the image.
	 * 
	 *  @param image The image to double in size
	 *  @return a copy of the original image with twice the size
	 */
	public static FImage doubleSize(FImage image) {
		int nheight, nwidth;
		float im[][], tmp[][];
		FImage newimage;

		nheight = 2 * image.height - 2;
		nwidth = 2 * image.width - 2;
		newimage = new FImage( nwidth, nheight );
		im = image.pixels;
		tmp = newimage.pixels;

		for( int y = 0; y < image.height - 1; y++ ) {
			for( int x = 0; x < image.width - 1; x++ )
			{
				int y2 = 2 * y;
				int x2 = 2 * x;
				tmp[y2][x2] = im[y][x];
				tmp[y2 + 1][x2] = 0.5f * (im[y][x] + im[y + 1][x]);
				tmp[y2][x2 + 1] = 0.5f * (im[y][x] + im[y][x + 1]);
				tmp[y2 + 1][x2 + 1] = 0.25f * (im[y][x] + im[y + 1][x] + im[y][x + 1] + im[y + 1][x + 1]);
			}
		}
		return newimage;
	}
	
	protected static void internalDoubleSize(FImage image)
	{
		image.internalAssign(doubleSize(image));
	}
	
	/**
	 * 	Halve the size of the image.
	 *  @param <I> 
	 * 
	 *  @param image The image halve in size
	 *  @return a copy of the input image with half the size 
	 */
	public static <I extends Image<?,I> & SinglebandImageProcessor.Processable<Float,FImage,I>> I halfSize(I image) {
		return image.process(new ResizeProcessor(Mode.HALF));
	}
	
	/**
	 * 	Halve the size of the image.
	 * 
	 *  @param image The image halve in size
	 *  @return a copy the the image with half the size
	 */
	public static FImage halfSize(FImage image) {
		int newheight, newwidth;
		float im[][], tmp[][];
		FImage newimage;

		newheight = image.height / 2;
		newwidth = image.width / 2;
		newimage = new FImage( newwidth, newheight );
		im = image.pixels;
		tmp = newimage.pixels;

		for( int y = 0, yi = 0; y < newheight; y++, yi += 2 ) {
			for( int x = 0, xi = 0; x < newwidth; x++, xi += 2 ) {
				tmp[y][x] = im[yi][xi];
			}
		}
		
		return newimage;
	}
		
	protected static void internalHalfSize(FImage image) {
		image.internalAssign(halfSize(image));
	}
	
	/**
	 * 	Returns a new image that is a resampled version of this image.
	 *  @param in The source image
	 *  @param newX The new width of the image
	 *  @param newY The new height of the image
	 *  @return A new {@link FImage}
	 */
	public static FImage resample( FImage in, int newX, int newY )
	{
		return resample(in.clone(), newX, newY, false );
	}

	/**
	 * Resamples the given image returning it as a reference. If
	 * <code>aspect</code> is true, the aspect ratio of the image will be
	 * retained, which means newX or newY could be smaller than given here. The
	 * dimensions of the new image will not be larger than newX or newY.
	 * Side-affects the given image.
	 * 
	 * @param in The source image
	 * @param newX The new width of the image
	 * @param newY The new height of the image
	 * @param aspect Whether to maintain the aspect ratio
	 * @return A new resampled image
	 */
	public static FImage resample( FImage in, int newX, int newY, boolean aspect )
	{
		// Work out the size of the resampled image
		// if the aspect ratio is set to true
		int nx = newX;
		int ny = newY;
		if( aspect )
		{
			if( ny > nx )
				nx = (int) ((in.width * ((double)ny/(double)in.height)));
			else
				ny = (int) ((in.height * ((double)nx/(double)in.width)));
		}
		
		zoom( in, nx, ny );
		return in;
	}

	/**
	 * For the port of the zoom function
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * 
	 */
	private static class PixelContribution
	{
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
	private static class PixelContributions
	{
		int numberOfContributors;

		PixelContribution[] contributions;
	}

	/**
	 * Ensures that the return result is between h and l.
	 * 
	 * @param v The value to clamp
	 * @param l The minimum value
	 * @param h The maximum value
	 * @return h < v < l
	 */
	private static float clamp( float v, float l, float h )
	{
		return ((v) < (l) ? (l) : (v) > (h) ? (h) : v);
	}

	/**
	 * Calculates the filter weights for a single target column. contribX->p
	 * must be freed afterwards.
	 * 
	 * @param contribX Receiver of contrib info
	 * @param xscale Horizontal zooming scale
	 * @param fwidth Filter sampling width
	 * @param dstwidth Target bitmap width
	 * @param srcwidth Source bitmap width
	 * @param filterf Filter processor
	 * @param i Pixel column in source bitmap being processed
	 * 
	 * @returns -1 if error, 0 otherwise.
	 */
	private static int calc_x_contrib( PixelContributions contribX, double xscale, double fwidth, int dstwidth, int srcwidth, ResizeFilterFunction filterf, int i )
	{
		double width;
		double fscale;
		double center, left, right;
		double weight;
		int j, k, n;

		if( xscale < 1.0 )
		{
			/* Shrinking image */
			width = fwidth / xscale;
			fscale = 1.0 / xscale;

			contribX.numberOfContributors = 0;
			contribX.contributions = new PixelContribution[(int) Math.round( width * 2 + 1 )];

			center = i / xscale;
			left = Math.ceil( center - width );
			right = Math.floor( center + width );
			for( j = (int) left; j <= right; ++j )
			{
				weight = center - j;
				weight = filterf.filter( weight / fscale ) / fscale;
				if( j < 0 )
					n = -j;
				else if( j >= srcwidth )
					n = (srcwidth - j) + srcwidth - 1;
				else
					n = j;

				k = contribX.numberOfContributors++;
				contribX.contributions[k] = new PixelContribution();
				contribX.contributions[k].pixel = n;
				contribX.contributions[k].weight = weight;
			}

		}
		else
		{
			/* Expanding image */
			contribX.numberOfContributors = 0;
			contribX.contributions = new PixelContribution[(int) Math.round( fwidth * 2 + 1 )];

			center = i / xscale;
			left = Math.ceil( center - fwidth );
			right = Math.floor( center + fwidth );

			for( j = (int) left; j <= right; ++j )
			{
				weight = center - j;
				weight = filterf.filter( weight );

				if( j < 0 )
				{
					n = -j;
				}
				else if( j >= srcwidth )
				{
					n = (srcwidth - j) + srcwidth - 1;
				}
				else
				{
					n = j;
				}

				k = contribX.numberOfContributors++;
				contribX.contributions[k] = new PixelContribution();
				contribX.contributions[k].pixel = n;
				contribX.contributions[k].weight = weight;
			}
		}
		return 0;
	} /* calc_x_contrib */

	/**
	 * Simple zoom function that resizes image while resampling.
	 * @param in The source image
	 * @param newX The desired width of the image
	 * @param newY The desired height of the image
	 * @return -1 if error, 0 if success
	 */
	public static int zoom( FImage in, int newX, int newY )
	{
		ResizeFilterFunction filter = new BasicFilter();
		return zoom( in, newX, newY, filter, filter.getDefaultSupport() );
	}

	/**
	 * Resizes bitmaps while resampling them.
	 * 
	 * @param newX New width of the image
	 * @param newY New height of the image
	 * @param in The source image
	 * @param filterf The filter function
	 * @param fwidth The width of the filter
	 * @return -1 if error, 0 if success
	 */
	public static int zoom( FImage in, int newX, int newY, ResizeFilterFunction filterf, double fwidth )
	{
		FImage dst = new FImage( newX, newY );
		int val = zoom(in, dst, filterf, fwidth );
		if( val != -1 ) in.internalAssign( dst );
		return val;
	}

	/**
	 * Resizes bitmaps while resampling them.
	 * 
	 * Added by David Dupplaw, ported from Graphics Gems III
	 * 
	 * @param dst Destination Image
	 * @param in Source Image
	 * @param filterf Filter to use
	 * @param fwidth Filter width
	 * 
	 * @return -1 if error, 0 if success.
	 */
	public static int zoom(FImage in, FImage dst, ResizeFilterFunction filterf, double fwidth )
	{
		double xscale, yscale; /* zoom scale factors */
		int n; /* pixel number */
		double center, left, right; /* filter calculation variables */
		double width, fscale;
		double weight; /* filter calculation variables */
		boolean bPelDelta;
		float pel, pel2;
		PixelContributions contribX;
		int nRet = -1;

		// This is a convenience
		FImage src = in;

		/* create intermediate column to hold horizontal dst column zoom */
		Float[] tmp = new Float[src.height];

		xscale = (double) dst.width / (double) src.width;

		/* Build y weights */
		/* pre-calculate filter contributions for a column */
		PixelContributions[] contribY = new PixelContributions[dst.height];

		yscale = (double) dst.height / (double) src.height;

		if( yscale < 1.0 )
		{
			width = fwidth / yscale;
			fscale = 1.0 / yscale;
			for( int i = 0; i < dst.height; ++i )
			{
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round( width * 2 + 1 )];

				center = i / yscale;
				left = Math.ceil( center - width );
				right = Math.floor( center + width );
				for( int j = (int) left; j <= right; ++j )
				{
					weight = center - j;
					weight = filterf.filter( weight / fscale ) / fscale;

					if( j < 0 )
					{
						n = -j;
					}
					else if( j >= src.height )
					{
						n = (src.height - j) + src.height - 1;
					}
					else
					{
						n = j;
					}

					int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}
		else
		{
			for( int i = 0; i < dst.height; ++i )
			{
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round( fwidth * 2 + 1 )];

				center = i / yscale;
				left = Math.ceil( center - fwidth );
				right = Math.floor( center + fwidth );
				for( int j = (int) left; j <= right; ++j )
				{
					weight = center - j;
					weight = filterf.filter( weight );

					if( j < 0 )
					{
						n = -j;
					}
					else if( j >= src.height )
					{
						n = (src.height - j) + src.height - 1;
					}
					else
					{
						n = j;
					}

					int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}

		for( int xx = 0; xx < dst.width; xx++ )
		{
			contribX = new PixelContributions();
			if( 0 != calc_x_contrib( contribX, xscale, fwidth, dst.width, src.width, filterf, xx ) ) return 0;

			/* Apply horz filter to make dst column in tmp. */
			for( int k = 0; k < src.height; ++k )
			{
				weight = 0.0;
				bPelDelta = false;

				pel = src.pixels[k][contribX.contributions[0].pixel];

				for( int j = 0; j < contribX.numberOfContributors; ++j )
				{
					pel2 = src.pixels[k][contribX.contributions[j].pixel];
					if( pel2 != pel ) bPelDelta = true;
					weight += pel2 * contribX.contributions[j].weight;
				}
				weight = bPelDelta ? Math.round( weight * 255f ) / 255f : pel;

				// 0 is black, 1 is white
				tmp[k] = clamp( (float) weight, 0f, 1f );
			} /* next row in temp column */

			/*
			 * The temp column has been built. Now stretch it vertically into
			 * dst column.
			 */
			for( int i = 0; i < dst.height; ++i )
			{
				weight = 0.0;
				bPelDelta = false;
				pel = tmp[contribY[i].contributions[0].pixel];

				for( int j = 0; j < contribY[i].numberOfContributors; ++j )
				{
					pel2 = tmp[contribY[i].contributions[j].pixel];
					if( pel2 != pel ) bPelDelta = true;
					weight += pel2 * contribY[i].contributions[j].weight;
				}

				weight = bPelDelta ? Math.round( weight * 255f ) / 255f : pel;

				// 0 is black, 1 is white
				dst.pixels[i][xx] = clamp( (float) weight, 0f, 1f );

			} /* next dst row */
		} /* next dst column */

		nRet = 0; /* success */
		return nRet;

	} /* zoom */
	
	
	/**
	 * calls {@link #zoom(FImage, Rectangle, FImage, Rectangle, ResizeFilterFunction, double)} with the 
	 * {@link BasicFilter} 
	 * @param in
	 * @param inRect
	 * @param dst
	 * @param dstRect
	 * @return result of {@link #zoom(FImage, Rectangle, FImage, Rectangle, ResizeFilterFunction, double)}
	 */
	public static int zoom(FImage in, Rectangle inRect, FImage dst, Rectangle dstRect){
		BasicFilter filter = new BasicFilter();
		return zoom(in,inRect,dst,dstRect,filter,filter.getDefaultSupport());
	}
	
	/**
	 * Resizes bitmaps while resampling them. A literal port of {@link #zoom(FImage, int, int, ResizeFilterFunction, double)} 
	 * using a rectangle in the destination and source images
	 * 
	 * Added by David Dupplaw, ported from Graphics Gems III
	 * 
	 * @param dst Destination Image
	 * @param in Source Image
	 * @param inRect the location of pixels in the source image
	 * @param dstRect the destination of pixels in the destination image
	 * @param filterf Filter to use
	 * @param fwidth Filter width
	 * 
	 * @return -1 if error, 0 if success.
	 */
	public static int zoom(FImage in, Rectangle inRect, FImage dst, Rectangle dstRect, ResizeFilterFunction filterf, double fwidth )
	{
		
		// First some sanity checking!
		if(!in.getBounds().isInside(inRect) || !dst.getBounds().isInside(dstRect)) return -1;
		double xscale, yscale; /* zoom scale factors */
		int n; /* pixel number */
		double center, left, right; /* filter calculation variables */
		double width, fscale;
		double weight; /* filter calculation variables */
		boolean bPelDelta;
		float pel, pel2;
		PixelContributions contribX;
		int nRet = -1;

		// This is a convenience
		FImage src = in;
		int srcX = (int)inRect.x;
		int srcY = (int)inRect.y;
		int srcWidth = (int) inRect.width;
		int srcHeight = (int) inRect.height;
		
		int dstX = (int)dstRect.x;
		int dstY = (int)dstRect.y;
		int dstWidth = (int) dstRect.width;
		int dstHeight = (int) dstRect.height;

		/* create intermediate column to hold horizontal dst column zoom */
		Float[] tmp = new Float[srcHeight];

		xscale = (double) dstWidth / (double) srcWidth;

		/* Build y weights */
		/* pre-calculate filter contributions for a column */
		PixelContributions[] contribY = new PixelContributions[dstHeight];

		yscale = (double) dstHeight / (double) srcHeight;

		if( yscale < 1.0 )
		{
			width = fwidth / yscale;
			fscale = 1.0 / yscale;
			for( int i = 0; i < dstHeight; ++i )
			{
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round( width * 2 + 1 )];

				center = i / yscale;
				left = Math.ceil( center - width );
				right = Math.floor( center + width );
				for( int j = (int) left; j <= right; ++j )
				{
					weight = center - j;
					weight = filterf.filter( weight / fscale ) / fscale;

					if( j < 0 )
					{
						n = -j;
					}
					else if( j >= srcHeight )
					{
						n = (srcHeight - j) + srcHeight - 1;
					}
					else
					{
						n = j;
					}

					int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}
		else
		{
			for( int i = 0; i < dstHeight; ++i )
			{
				contribY[i] = new PixelContributions();
				contribY[i].numberOfContributors = 0;
				contribY[i].contributions = new PixelContribution[(int) Math.round( fwidth * 2 + 1 )];

				center = i / yscale;
				left = Math.ceil( center - fwidth );
				right = Math.floor( center + fwidth );
				for( int j = (int) left; j <= right; ++j )
				{
					weight = center - j;
					weight = filterf.filter( weight );

					if( j < 0 )
					{
						n = -j;
					}
					else if( j >= srcHeight )
					{
						n = (srcHeight - j) + srcHeight - 1;
					}
					else
					{
						n = j;
					}

					int k = contribY[i].numberOfContributors++;
					contribY[i].contributions[k] = new PixelContribution();
					contribY[i].contributions[k].pixel = n;
					contribY[i].contributions[k].weight = weight;
				}
			}
		}

		for( int xx = 0; xx < dstWidth; xx++ )
		{
			contribX = new PixelContributions();
			if( 0 != calc_x_contrib( contribX, xscale, fwidth, dstWidth, srcWidth, filterf, xx ) ) return 0;

			/* Apply horz filter to make dst column in tmp. */
			for( int k = 0; k < srcHeight; ++k )
			{
				weight = 0.0;
				bPelDelta = false;

				pel = src.pixels[k + srcY][contribX.contributions[0].pixel + srcX];

				for( int j = 0; j < contribX.numberOfContributors; ++j )
				{
					pel2 = src.pixels[k + srcY][contribX.contributions[j].pixel + srcX];
					if( pel2 != pel ) bPelDelta = true;
					weight += pel2 * contribX.contributions[j].weight;
				}
				weight = bPelDelta ? Math.round( weight * 255f ) / 255f : pel;

				// 0 is black, 1 is white
				tmp[k] = clamp( (float) weight, 0f, 1f );
			} /* next row in temp column */

			/*
			 * The temp column has been built. Now stretch it vertically into
			 * dst column.
			 */
			for( int i = 0; i < dstHeight; ++i )
			{
				weight = 0.0;
				bPelDelta = false;
				pel = tmp[contribY[i].contributions[0].pixel];

				for( int j = 0; j < contribY[i].numberOfContributors; ++j )
				{
					pel2 = tmp[contribY[i].contributions[j].pixel];
					if( pel2 != pel ) bPelDelta = true;
					weight += pel2 * contribY[i].contributions[j].weight;
				}

				weight = bPelDelta ? Math.round( weight * 255f ) / 255f : pel;

				// 0 is black, 1 is white
				dst.pixels[i + dstY][xx + dstX] = clamp( (float) weight, 0f, 1f );

			} /* next dst row */
		} /* next dst column */

		nRet = 0; /* success */
		return nRet;

	} /* zoom */
	
}
