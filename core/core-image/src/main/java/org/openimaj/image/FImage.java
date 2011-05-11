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
package org.openimaj.image;

import java.util.Comparator;

import org.apache.log4j.Logger;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.util.Interpolation;

import Jama.Matrix;

/**
 * 	Class representing a single-band floating-point image; that is an image
 * 	where each pixel is represented by a floating-point number.
 * 	<p>
 * 	{@link FImage}s can be created from PGM files or from pixel arrays.
 * 	If you wish to read other types of files then use the {@link ImageUtilities}
 * 	class that provides read/write functions for {@link Image} objects.
 * 
 * 	@author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class FImage extends SingleBandImage<Float, FImage>
{
	/** The logging class */
	protected Logger logger = Logger.getLogger(FImage.class);
	
	/**
	 * The default number of sigmas at which the Gaussian function is truncated
	 * when building a kernel
	 */
	protected static final float DEFAULT_GAUSS_TRUNCATE = 4.0f;

	/** The underlying pixels */
	public float pixels[][];
	
	/**
	 * Construct an {@link FImage} from an array of packed ARGB integers.
	 * 
	 * @param data array of packed ARGB pixels
	 * @param width the image width
	 * @param height the image height
	 */
	public FImage(int [] data, int width, int height) {
		internalAssign( data, width, height );
	}

	/**
	 * Construct an {@link FImage} from an array of packed ARGB integers using the 
	 * specified plane.
	 * 
	 * @param data array of packed ARGB pixels
	 * @param width the image width
	 * @param height the image height
	 * @param plane The {@link ARGBPlane} to copy data from
	 */
	public FImage( int [] data, int width, int height, ARGBPlane plane ) {
		this.width = width;
		this.height = height;
		pixels = new float[height][width];

		for( int y = 0; y < height; y++ ) {
			for( int x = 0; x < width; x++ ) {
				int rgb = data[x + y*width];

				int colour = 0;
				switch (plane)
				{
				case RED:
					colour = ((rgb >> 16) & 0xff);
					break;
				case GREEN:
					colour = ((rgb >> 8) & 0xff);
					break;
				case BLUE:
					colour = ((rgb) & 0xff);
					break;
				}

				pixels[y][x] = colour;
			}
		}
	}

	/**
	 * Create an {@link FImage} from an array of floating point values.
	 * 
	 * @param array the array representing pixel values to copy data from.
	 */
	public FImage( float[][] array )
	{
		pixels = array;
		height = array.length;
		width = array[0].length;
	}

	/**
	 * Create an empty {@link FImage} of the given size.
	 * @param width image width (number of columns)
	 * @param height image height (number of rows)
	 */
	public FImage( int width, int height ) {
		pixels = new float[height][width];
		this.height = height;
		this.width = width;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#internalAssign(int [] data, int width, int height)
	 */
	@Override
	public FImage internalAssign(int [] data, int width, int height) {
		if (this.height != height || this.width != width) {
			this.height = height;
			this.width = width;
			pixels = new float[height][width];
		}
		
		for( int y = 0; y < height; y++ ) {
			for( int x = 0; x < width; x++ ) {
				int rgb = data[ x + width*y ];

				int red = ((rgb >> 16) & 0xff);
				int green = ((rgb >> 8) & 0xff);
				int blue = ((rgb) & 0xff);

				// NTSC colour conversion:
				// This improves keypoint detection for some reason!
				float fpix = 0.299f * red + 0.587f * green + 0.114f * blue;

				pixels[y][x] = fpix / 255.0F;
			}
		}
		return this;
	}

	/**
	 * Adds the pixel values of the given {@link FImage} to the pixels of this image.
	 * Returns a new {@link FImage} and does not affect this image or the given
	 * image. This is a version of {@link Image#add(Image)} which takes an {@link FImage}. 
	 * This method directly accesses the underlying float[][] 
	 * and is therefore fast. This function returns a new {@link FImage}.
	 * 
	 * @see org.openimaj.image.Image#add(Image)
	 * @param im {@link FImage} to add into this one. 
	 * @return A new {@link FImage}
	 */
	public FImage add( FImage im )
	{
		if (!ImageUtilities.checkSameSize( this, im ))
			throw new AssertionError("images must be the same size");
		
		FImage newImage = new FImage( im.width, im.height );

		for( int r = 0; r < im.height; r++ )
			for( int c = 0; c < im.width; c++ )
				newImage.pixels[r][c] = pixels[r][c] + im.pixels[r][c];

		return newImage;
	}

	/**
	 * 	Returns a new {@link FImage} that contains the pixels of this image
	 * 	increased by the given value.
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#add(java.lang.Object)
	 */
	@Override
	public FImage add( Float num )
	{
		FImage newImage = new FImage( width, height );
		float fnum = num;

		for( int r = 0; r < height; r++ )
			for( int c = 0; c < width; c++ )
				newImage.pixels[r][c] = pixels[r][c] + fnum;

		return newImage;
	}

	/**
	 *	{@inheritDoc}
	 * 	This method throws an {@link UnsupportedOperationException} if the given
	 * 	image is not an {@link FImage}.
	 * 	@see org.openimaj.image.Image#add(org.openimaj.image.Image)
	 * 	@exception UnsupportedOperationException if an unsupported type is added
	 * 	@return a reference to this {@link FImage}
	 */
	@Override
	public FImage add( Image<?,?> im )
	{
		if( im instanceof FImage )
			return add( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
	}

	/***
	 * Adds the given image pixel values to the pixel values of this image.
	 * Version of {@link Image#addInline(Image)} which takes an {@link FImage}. 
	 * This directly accesses the underlying float[][] 
	 * and is therefore fast. This function side-affects the pixels in this
	 * {@link FImage}.
	 * 
	 * @see Image#addInline(Image)
	 * @param im the FImage to add
	 * @return a reference to this
	 */
	public FImage addInline( FImage im )
	{
		if (!ImageUtilities.checkSameSize( this, im ))
			throw new AssertionError("images must be the same size");

		for( int r = 0; r < im.height; r++ )
			for( int c = 0; c < im.width; c++ )
				pixels[r][c] += im.pixels[r][c];

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#addInline(java.lang.Object)
	 */
	@Override
	public FImage addInline( Float num )
	{
		float fnum = num;
		for( int r = 0; r < height; r++ )
			for( int c = 0; c < width; c++ )
				pixels[r][c] = +fnum;

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	This method throws an {@link UnsupportedOperationException} if the given
	 * 	image is not an {@link FImage}.
	 * 	@see org.openimaj.image.Image#addInline(org.openimaj.image.Image)
	 * 	@exception UnsupportedOperationException if an unsupported type is added
	 * 	@return a reference to this {@link FImage}
	 */
	@Override
	public FImage addInline( Image<?,?> im )
	{
		if( im instanceof FImage )
			return addInline( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#clip(java.lang.Object, java.lang.Object)
	 */
	@Override
	public FImage clip( Float min, Float max )
	{
		int r, c;

		for( r = 0; r < height; r++ )
		{
			for( c = 0; c < width; c++ )
			{
				if( pixels[r][c] < min ) pixels[r][c] = min;
				if( pixels[r][c] > max ) pixels[r][c] = max;
			}
		}

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.SingleBandImage#clone()
	 */
	@Override
	public FImage clone()
	{
		FImage cpy = new FImage( width, height );
		int r, c;

		for( r = 0; r < height; r++ )
			for( c = 0; c < width; c++ )
				cpy.pixels[r][c] = pixels[r][c];

		return cpy;
	}

	/**
	 * Divides the pixels values of this image with the values from the
	 * given image. This is a version of {@link Image#divide(Image)} 
	 * which takes an {@link FImage}. 
	 * This directly accesses the underlying float[][] 
	 * and is therefore fast. This function returns a new {@link FImage}.
	 * @see Image#divide(Image)
	 * @param im the {@link FImage} to be the denominator.
	 * @return A new {@link FImage}
	 */
	public FImage divide( FImage im )
	{
		if (!ImageUtilities.checkSameSize( this, im ))
			throw new AssertionError("images must be the same size");

		FImage newImage = new FImage( im.width, im.height );
		int r, c;

		for( r = 0; r < im.height; r++ )
			for( c = 0; c < im.width; c++ )
				newImage.pixels[r][c] = pixels[r][c] / im.pixels[r][c];

		return newImage;
	}

	/**
	 * Divides the pixel values of this image with the values from the given
	 * image. This is a version of {@link Image#divideInline(Image)} which 
	 * takes an {@link FImage}. This directly accesses the underlying float[][] 
	 * and is therefore fast. This function side-affects this image.
	 * 
	 * @see Image#divideInline(Image)
	 * @param im the {@link FImage} to be the denominator
	 * @return a reference to this {@link FImage}
	 */
	public FImage divideInline( FImage im )
	{
		if (!ImageUtilities.checkSameSize( this, im ))
			throw new AssertionError("images must be the same size");

		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				pixels[y][x] /= im.pixels[y][x];
			}
		}

		return this;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#divideInline(java.lang.Object)
	 */
	@Override
	public FImage divideInline( Float val )
	{
		float fval = val;

		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				pixels[y][x] /= fval;

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#divideInline(org.openimaj.image.Image)
	 */
	@Override
	public FImage divideInline( Image<?,?> im )
	{
		if( im instanceof FImage )
			return divideInline( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#extractROI(int, int, int, int)
	 */
	@Override
	public FImage extractROI( int x, int y, int w, int h )
	{
		FImage out = new FImage( w, h );

		for( int r = y, rr = 0; rr < h; r++, rr++ )
		{
			for( int c = x, cc = 0; cc < w; c++, cc++ )
			{
				if( r < 0 || r >= height || c < 0 || c >= width )
					out.pixels[rr][cc] = 0;
				else
					out.pixels[rr][cc] = pixels[r][c];
			}
		}

		return out;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#getField(org.openimaj.image.Image.Field)
	 */
	@Override
	public FImage getField( Field f )
	{
		FImage img = new FImage( width, height / 2 );

		int r, r2, c, init = (f.equals( Field.ODD ) ? 1 : 0);
		for( r = init, r2 = 0; r < height && r2 < height / 2; r += 2, r2++ )
		{
			for( c = 0; c < width; c++ )
			{
				img.pixels[r2][c] = pixels[r][c];
			}
		}

		return img;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#getFieldCopy(org.openimaj.image.Image.Field)
	 */
	@Override
	public FImage getFieldCopy( Field f )
	{
		FImage img = new FImage( width, height );

		int r, c;
		for( r = 0; r < height; r += 2 )
		{
			for( c = 0; c < width; c++ )
			{
				if( f.equals( Field.EVEN ) )
				{
					img.pixels[r][c] = pixels[r][c];
					img.pixels[r + 1][c] = pixels[r][c];
				}
				else
				{
					img.pixels[r][c] = pixels[r + 1][c];
					img.pixels[r + 1][c] = pixels[r + 1][c];
				}
			}
		}

		return img;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#getFieldInterpolate(org.openimaj.image.Image.Field)
	 */
	@Override
	public FImage getFieldInterpolate( Field f )
	{
		FImage img = new FImage( width, height );

		int r, c;
		for( r = 0; r < height; r += 2 )
		{
			for( c = 0; c < width; c++ )
			{
				if( f.equals( Field.EVEN ) )
				{
					img.pixels[r][c] = pixels[r][c];

					if( r + 2 == height )
					{
						img.pixels[r + 1][c] = pixels[r][c];
					}
					else
					{
						img.pixels[r + 1][c] = 0.5F * (pixels[r][c] + pixels[r + 2][c]);
					}
				}
				else
				{
					img.pixels[r + 1][c] = pixels[r + 1][c];

					if( r == 0 )
					{
						img.pixels[r][c] = pixels[r + 1][c];
					}
					else
					{
						img.pixels[r][c] = 0.5F * (pixels[r - 1][c] + pixels[r + 1][c]);
					}
				}
			}
		}

		return img;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#getPixel(int, int)
	 */
	@Override
	public Float getPixel( int x, int y )
	{
		return pixels[y][x];
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@Override
	public FImage internalAssign( FImage im )
	{
		pixels = im.pixels;
		height = im.height;
		width = im.width;

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#inverse()
	 */
	@Override
	public FImage inverse()
	{
		int r, c;
		float max = max();

		for( r = 0; r < height; r++ )
			for( c = 0; c < width; c++ )
				pixels[r][c] = max - pixels[r][c];

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#max()
	 */
	@Override
	public Float max()
	{
		int r, c;
		float max = Float.MIN_VALUE;

		for( r = 0; r < height; r++ )
			for( c = 0; c < width; c++ )
				if( max < pixels[r][c] ) max = pixels[r][c];

		return max;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#min()
	 */
	@Override
	public Float min()
	{
		int r, c;
		float min = Float.MAX_VALUE;

		for( r = 0; r < height; r++ )
			for( c = 0; c < width; c++ )
				if( min > pixels[r][c] ) min = pixels[r][c];

		return min;
	}

	/**
	 * Get the pixel with the minimum value. Returns an {@link FValuePixel} which
	 * contains the location and value of the pixel. If there are multiple
	 * pixels with the same value then the first is returned. Note that this
	 * method assumes all pixel values are greater than 0.
	 * 
	 * @return The minimum pixel as an {@link FValuePixel}.
	 */
	public FValuePixel maxPixel()
	{
		FValuePixel max = new FValuePixel(-1,-1);
		
		for( int y = 0; y < height; y++ ) {
			for( int x = 0; x < width; x++ ) {
				if( max.value < pixels[y][x] ) {
					max.value = pixels[y][x];
					max.x = x;
					max.y = y;
				}
			}
		}

		return max;
	}

	/**
	 * Get the pixel with the maximum value. Returns an {@link FValuePixel} which
	 * contains the location and value of the pixel. If there are multiple
	 * pixels with the same value then the first is returned. Note that this
	 * method assumes all pixel values are greater than 0.
	 * 
	 * @return the maximum pixel as an {@link FValuePixel}.
	 */
	public FValuePixel minPixel()
	{
		FValuePixel min = new FValuePixel(-1,-1);
		min.value = Float.MAX_VALUE;

		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				if( min.value > pixels[y][x] ) {
					min.value = pixels[y][x];
					min.x = x;
					min.y = y;
				}

		return min;
	}
	
	/**
	 * Multiplies this image's pixel values by the corresponding pixel values
	 * in the given image side-affecting this image. This is a
	 * version of {@link Image#multiplyInline(Image)} which takes an {@link FImage}. 
	 * This directly accesses the underlying float[][] 
	 * and is therefore fast. This function works inline.
	 * 
	 * @see Image#multiplyInline(Image)
	 * @param im the {@link FImage} to multiply with this image
	 * @return a reference to this image
	 */
	public FImage multiplyInline( FImage im )
	{
		if (!ImageUtilities.checkSameSize( this, im ))
			throw new AssertionError("images must be the same size");

		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				pixels[r][c] *= im.pixels[r][c];
			}
		}

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#multiplyInline(java.lang.Object)
	 */
	@Override
	public FImage multiplyInline( Float num )
	{
		float fnum = num;
		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				pixels[r][c] *= fnum;
			}
		}

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#multiply(java.lang.Object)
	 */
	@Override
	public FImage multiply( Float num )
	{
		return super.multiply( num );
	}

	/**
	 *	{@inheritDoc}
	 *	This method will throw an {@link UnsupportedOperationException} if the
	 *	input input is not an {@link FImage}.
	 * 	@see org.openimaj.image.Image#multiplyInline(org.openimaj.image.Image)
	 * 	@throws UnsupportedOperationException if the given image is not an {@link FImage}
	 */
	@Override
	public FImage multiplyInline( Image<?,?> im )
	{
		if( im instanceof FImage )
			return multiplyInline( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#normalise()
	 */
	@Override
	public FImage normalise()
	{
		float min = min();
		float max = max();

		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				pixels[r][c] = (pixels[r][c] - min) / (max - min);
			}
		}

		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#setPixel(int, int, java.lang.Object)
	 */
	@Override
	public void setPixel( int x, int y, Float val ) {
		if (x>=0 && x<width && y>=0 && y<height)
			pixels[y][x] = val;
	}

	/**
	 * 	Subtracts the given {@link FImage} from this image returning a new
	 * 	image containing the result.
	 * 
	 *  @param im The image to subtract from this image.
	 *  @return A new image containing the result.
	 */
	public FImage subtract( FImage im )
	{
		if (!ImageUtilities.checkSameSize( this, im ))
			throw new AssertionError("images must be the same size");

		FImage newImage = new FImage( im.width, im.height );
		int r, c;

		for( r = 0; r < im.height; r++ )
			for( c = 0; c < im.width; c++ )
				newImage.pixels[r][c] = pixels[r][c] - im.pixels[r][c];
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#subtract(java.lang.Object)
	 */
	@Override
	public FImage subtract( Float num )
	{
		FImage newImage = new FImage( width, height );

		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				newImage.pixels[r][c] = pixels[r][c] - num;
			}
		}
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  Throws an {@link UnsupportedOperationException} if the given image
	 *  is not an {@link FImage}.
	 *  @see org.openimaj.image.Image#subtract(org.openimaj.image.Image)
	 *  @throws UnsupportedOperationException if the given image is not an {@link FImage}.
	 */
	@Override
	public FImage subtract( Image<?,?> input )
	{
		if( input instanceof FImage )
			return subtract( (FImage) input );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
	}

	/**
	 * 	Subtracts (pixel-by-pixel) the given {@link FImage} from this image.
	 * 	Side-affects this image.
	 * 
	 *  @param im The {@link FImage} to subtract from this image.
	 *  @return A reference to this image containing the result.
	 */
	public FImage subtractInline( FImage im )
	{
		if (!ImageUtilities.checkSameSize( this, im ))
			throw new AssertionError("images must be the same size");

		float pix1[][], pix2[][];
		int r, c;

		pix1 = pixels;
		pix2 = im.pixels;

		for( r = 0; r < height; r++ )
			for( c = 0; c < width; c++ )
				pix1[r][c] -= pix2[r][c];

		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#subtractInline(java.lang.Object)
	 */
	@Override
	public FImage subtractInline( Float num )
	{
		float fnum = num;
		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				pixels[r][c] -= fnum;
			}
		}

		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#subtractInline(org.openimaj.image.Image)
	 */
	@Override
	public FImage subtractInline( Image<?,?> im )
	{
		if( im instanceof FImage )
			return subtractInline( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#clipMax(java.lang.Object)
	 */
	@Override
	public FImage clipMax( Float thresh )
	{
		float fthresh = thresh;
		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				if( pixels[r][c] > fthresh ) pixels[r][c] = 0;
			}
		}
		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#clipMin(java.lang.Object)
	 */
	@Override
	public FImage clipMin( Float thresh )
	{
		float fthresh = thresh;
		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				if( pixels[r][c] < fthresh ) pixels[r][c] = 0;
			}
		}
		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#threshold(java.lang.Object)
	 */
	@Override
	public FImage threshold( Float thresh )
	{
		float fthresh = thresh;
		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				if( pixels[r][c] <= fthresh )
					pixels[r][c] = 0;
				else
					pixels[r][c] = 1;
			}
		}
		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#toPackedARGBPixels()
	 */
	@Override
	public int [] toPackedARGBPixels()
	{
		int [] bimg = new int[width * height];
		
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				int v = (int) (255.0f * pixels[r][c]);
				int rgb = 0xff << 24 | v << 16 | v << 8 | v;
				bimg[c + width * r] = rgb;
			}
		}

		return bimg;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#toByteImage()
	 */
	@Override
	public byte[] toByteImage()
	{
		byte[] pgmData = new byte[height * width];

		for( int j = 0; j < height; j++ )
		{
			for( int i = 0; i < width; i++ )
			{
				int v = (int) (255.0f * pixels[j][i]);

				v = Math.max( 0, Math.min( 255, v ) );

				pgmData[i + j * width] = (byte) (v & 0xFF);
			}
		}
		return pgmData;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#zero()
	 */
	@Override
	public FImage zero()
	{
		for( int r = 0; r < height; r++ )
		{
			for( int c = 0; c < width; c++ )
			{
				pixels[r][c] = 0;
			}
		}
		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  This method uses Bresenham's line algorithm.
	 *  @see org.openimaj.image.Image#drawLine(int, int, int, int, int, java.lang.Object)
	 */
	@Override
	public void drawLine( int x0, int y0, int x1, int y1, int thickness, Float grey )
	{
		int offset = thickness / 2;
		int extra = thickness % 2; 

		// implementation of Bresenham's algorithm from Wikipedia.
		int Dx = x1 - x0;
		int Dy = y1 - y0;
		boolean steep = (Math.abs( Dy ) >= Math.abs( Dx ));
		if( steep )
		{
			int tmp;
			// SWAP(x0, y0);
			tmp = x0;
			x0 = y0;
			y0 = tmp;
			// SWAP(x1, y1);
			tmp = x1;
			x1 = y1;
			y1 = tmp;

			// recompute Dx, Dy after swap
			Dx = x1 - x0;
			Dy = y1 - y0;
		}
		int xstep = 1;
		if( Dx < 0 )
		{
			xstep = -1;
			Dx = -Dx;
		}
		int ystep = 1;
		if( Dy < 0 )
		{
			ystep = -1;
			Dy = -Dy;
		}
		int TwoDy = 2 * Dy;
		int TwoDyTwoDx = TwoDy - 2 * Dx; // 2*Dy - 2*Dx
		int E = TwoDy - Dx; // 2*Dy - Dx
		int y = y0;
		int xDraw, yDraw;
		for( int x = x0; x != x1; x += xstep )
		{
			if( steep )
			{
				xDraw = y;
				yDraw = x;
			}
			else
			{
				xDraw = x;
				yDraw = y;
			}
			// plot
			if( xDraw > 0 && xDraw < width && yDraw > 0 && yDraw < height ) {
				if (thickness == 1 ) {
					pixels[yDraw][xDraw] = grey;
				} else if (thickness > 1) {
					for (int yy=yDraw-offset; yy<yDraw+offset+extra; yy++)
						for (int xx=xDraw-offset; xx<xDraw+offset+extra; xx++)
							if (xx >= 0 && yy >= 0 && xx < width && yy< height) pixels[yy][xx] = grey;
				}
			}

			// next
			if( E > 0 )
			{
				E += TwoDyTwoDx; // E += 2*Dy - 2*Dx;
				y = y + ystep;
			}
			else
			{
				E += TwoDy; // E += 2*Dy;
			}
		}
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#drawLine(int, int, double, int, int, java.lang.Object)
	 */
	@Override
	public void drawLine( int x1, int y1, double theta, int length, int thickness, Float grey )
	{
		int x2 = x1 + (int) Math.round( Math.cos( theta ) * length );
		int y2 = y1 + (int) Math.round( Math.sin( theta ) * length );

		drawLine( x1, y1, x2, y2, thickness, grey );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#drawPolygon(org.openimaj.math.geometry.shape.Polygon, int, java.lang.Object)
	 */
	@Override
	public void drawPolygon( Polygon p, int thickness, Float grey )
	{
		if( p.nVertices() < 2 ) return;

		Point2d p1, p2;
		for( int i = 0; i < p.nVertices() - 1; i++ )
		{
			p1 = p.getVertices().get( i );
			p2 = p.getVertices().get( i + 1 );
			drawLine( Math.round( p1.getX() ), Math.round( p1.getY() ), Math.round( p2.getX() ), Math.round( p2.getY() ), thickness, grey );
		}

		p1 = p.getVertices().get( p.nVertices() - 1 );
		p2 = p.getVertices().get( 0 );
		drawLine( Math.round( p1.getX() ), Math.round( p1.getY() ), Math.round( p2.getX() ), Math.round( p2.getY() ), thickness, grey );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#drawPoint(org.openimaj.math.geometry.point.Point2d, java.lang.Object, int)
	 */
	@Override
	public void drawPoint( Point2d p, Float grey, int size )
	{
		if(!this.getBounds().isInside(p)) return;
		int x = Math.round( p.getX() );
		int y = Math.round( p.getY() );

		if( x > width || y > height ) return;

		for( int j = y; j < Math.min( y + size, height ); j++ )
		{
			for( int i = x; i < Math.min( x + size, width ); i++ )
			{
				pixels[j][i] = grey;
			}
		}
	}

	/**
	 *  {@inheritDoc}
	 *  @return A new {@link FImage}
	 *  @see org.openimaj.image.Image#newInstance(int, int)
	 */
	@Override
	public FImage newInstance( int width, int height )
	{
		return new FImage( width, height );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#extractROI(int, int, org.openimaj.image.Image)
	 */
	@Override
	public FImage extractROI( int x, int y, FImage out )
	{
		for( int r = y, rr = 0; rr < out.height; r++, rr++ )
		{
			for( int c = x, cc = 0; cc < out.width; c++, cc++ )
			{
				if( r < 0 || r >= height || c < 0 || c >= width )
					(out).pixels[rr][cc] = 0;
				else
					(out).pixels[rr][cc] = pixels[r][c];
			}
		}

		return out;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.SingleBandImage#process(org.openimaj.image.processor.KernelProcessor)
	 */
	@Override
	public FImage process( KernelProcessor<Float, FImage> p) {
		return process(p, false);
	}

	/**
	 *  {@inheritDoc}
	 *  This method has been overridden in {@link FImage} for performance.
	 *  @see org.openimaj.image.SingleBandImage#process(org.openimaj.image.processor.KernelProcessor, boolean)
	 */
	@Override
	public FImage process( KernelProcessor<Float, FImage> p, boolean pad )
	{
		FImage newImage = new FImage( width, height );
		int kh = p.getKernelHeight();
		int kw = p.getKernelWidth();

		FImage tmp = new FImage( kw, kh );

		int hh = kh / 2;
		int hw = kw / 2;

		if (!pad) {
			for( int y = hh; y < height - (kh - hh); y++ ) {
				for( int x = hw; x < width - (kw - hw); x++ ) {
					newImage.pixels[y][x] = p.processKernel( this.extractROI( x - hw, y - hh, tmp ) );
				}
			}
		} else {
			for( int y = 0; y < height; y++ ) {
				for( int x = 0; x < width; x++ ) {
					newImage.pixels[y][x] = p.processKernel( this.extractROI( x - hw, y - hh, tmp ) );
				}
			}
		}

		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  This method has been overridden in {@link FImage} for performance.
	 *  @see org.openimaj.image.Image#processInline(org.openimaj.image.processor.PixelProcessor)
	 */
	@Override
	public FImage processInline( PixelProcessor<Float> p )
	{
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				pixels[y][x] = p.processPixel( pixels[y][x] );
			}
		}

		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  This method has been overridden in {@link FImage} for performance.
	 *  @see org.openimaj.image.Image#processInline(PixelProcessor p, Image... images)
	 */
	@Override
	public FImage processInline( PixelProcessor<Float> p, Image<?,?>... images )
	{
		Number[] otherpixels = new Number[images.length];
		for( int y = 0; y < getHeight(); y++ )
		{
			for( int x = 0; x < getWidth(); x++ )
			{
				for( int i = 0; i < images.length; i++ )
					otherpixels[i] = (Number) images[i].getPixel( x, y );
				pixels[y][x] = p.processPixel( pixels[y][x], otherpixels );
			}
		}
		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getPixelInterp(double, double)
	 *  @see Interpolation#bilerp(double, double, double, double, double, double)
	 */
	@Override
	public Float getPixelInterp( double x, double y )
	{
		int x0 = (int) Math.floor(x);
		int x1 = x0 + 1;
		int y0 = (int) Math.floor(y);
		int y1 = y0 + 1;

		if( x0 < 0 ) x0 = 0;
		if( x0 >= this.width ) x0 = this.width - 1;
		if( y0 < 0 ) y0 = 0;
		if( y0 >= this.height ) y0 = this.height - 1;

		if( x1 < 0 ) x1 = 0;
		if( x1 >= this.width ) x1 = this.width - 1;
		if( y1 < 0 ) y1 = 0;
		if( y1 >= this.height ) y1 = this.height - 1;

		double f00 = this.pixels[y0][x0];
		double f01 = this.pixels[y1][x0];
		double f10 = this.pixels[y0][x1];
		double f11 = this.pixels[y1][x1];
		double dx = x - x0;
		double dy = y - y0;
		if(dx < 0) dx = 1 + dx;
		if(dy < 0) dy = 1 + dy;
		
		return (float) Interpolation.bilerp( dx, dy, f00, f01, f10, f11 );
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getPixelInterp(double, double)
	 *  @see Interpolation#bilerp(double, double, double, double, double, double)
	 */
	@Override
	public Float getPixelInterp( double x, double y , Float background)
	{
		int x0 = (int) Math.floor(x);
		int x1 = x0 + 1;
		int y0 = (int) Math.floor(y);
		int y1 = y0 + 1;

		boolean tx0,tx1,ty0,ty1;
		tx0 = ty0 = tx1 = ty1 = true;
		if( x0 < 0 ) tx0 = false;
		if( x0 >= this.width ) tx0 = false;
		if( y0 < 0 ) ty0 = false;
		if( y0 >= this.height ) ty0 = false;

		if( x1 < 0 ) tx1 = false;
		if( x1 >= this.width ) tx1 = false;
		if( y1 < 0 ) ty1 = false;
		if( y1 >= this.height ) ty1 = false;

		double f00 = (ty0 && tx0 ? this.pixels[y0][x0] : background.floatValue()); // this.pixels[y0][x0];
		double f01 = (ty1 && tx0 ? this.pixels[y1][x0] : background.floatValue()); // this.pixels[y1][x0];
		double f10 = (ty0 && tx1 ? this.pixels[y0][x1] : background.floatValue()); // this.pixels[y0][x1];
		double f11 = (ty1 && tx1 ? this.pixels[y1][x1] : background.floatValue()); // this.pixels[y1][x1];
		
		
		double dx = x - x0;
		double dy = y - y0;
		if(dx < 0) dx = 1 + dx;
		if(dy < 0) dy = 1 + dy;

		double interpVal = Interpolation.bilerp( dx, dy, f00, f01, f10, f11 );
		return (float) interpVal;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.SingleBandImage#fill(java.lang.Comparable)
	 */
	@Override
	public FImage fill( Float colour )
	{
		for( int r = 0; r < height; r++ )
			for( int c = 0; c < width; c++ )
				pixels[r][c] = colour;
		
		return this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#abs()
	 */
	@Override
	public FImage abs() {
		for( int r = 0; r < height; r++ )
			for( int c = 0; c < width; c++ )
				pixels[r][c] = Math.abs(pixels[r][c]);
		return this;
	}

	/**
	 *  A transform guaranteed to result in an image of the same size as the current image. Edge pixels are filled with the values of the closest edge.
	 *  @param transform Transform {@link Matrix} to apply.
	 *  @param midX
	 *  @param midY
	 *  @return A new image containing the result.
	 */
	public FImage funkyTransform(Matrix transform, int midX, int midY){
		FImage newImage = this.newInstance(this.getWidth(),getHeight());
		for(int x = 0 ; x < this.getWidth(); x ++ ){
			for(int y = 0; y < this.getHeight(); y++){
				int cx = x-midX;
				int cy = y-midY;
				double px = transform.get(0, 0) * cx + transform.get(0, 1) * cy;
				double py = transform.get(1, 0) * cx + transform.get(1, 1) * cy;

				px += midX;
				py += midY;

				int xfloor = (int) Math.floor(px);
				int yfloor = (int) Math.floor(py);

				double xplus = px - xfloor;
				double yplus = py - yfloor;

				double w1 = Math.abs((1 - xplus) * (1 - yplus));
				double w2 = Math.abs((    xplus) * (1 - yplus));
				double w3 = Math.abs((1 - xplus) * (    yplus));
				double w4 = Math.abs((    xplus) * (    yplus));

				int x1 = (xfloor < 0 ? 0 : xfloor >= this.getWidth() ? this.getWidth()-1 : xfloor);
				int x2 = (xfloor+1 < 0 ? 0 : xfloor+1 >= this.getWidth() ? this.getWidth()-1 : xfloor+1);
				int x3 = (xfloor < 0 ? 0 : xfloor >= this.getWidth() ? this.getWidth()-1 : xfloor);
				int x4 = (xfloor+1 < 0 ? 0 : xfloor+1 >= this.getWidth() ? this.getWidth()-1 : xfloor+1);

				int y1 = (yfloor < 0 ? 0 : yfloor >= this.getHeight() ? this.getHeight()-1 : yfloor);
				int y2 = (yfloor < 0 ? 0 : yfloor >= this.getHeight() ? this.getHeight()-1 : yfloor);
				int y3 = (yfloor+1 < 0 ? 0 : yfloor+1 >= this.getHeight() ? this.getHeight()-1 : yfloor+1);
				int y4 = (yfloor+1 < 0 ? 0 : yfloor+1 >= this.getHeight() ? this.getHeight()-1 : yfloor+1);

				newImage.pixels[y][x] += pixels[y1][x1] * w1;
				newImage.pixels[y][x] += pixels[y2][x2] * w2;
				newImage.pixels[y][x] += pixels[y3][x3] * w3;
				newImage.pixels[y][x] += pixels[y4][x4] * w4;
			}
		}
		return newImage;
	}

	/**
	 * 
	 *  @param transform Transform {@link Matrix} to apply.
	 *  @param midX
	 *  @param midY
	 *  @param boundColour
	 *  @return A new image containing the result.
	 */
	// TODO: Complete Javadocs
	public FImage funkyTransform(Matrix transform,int midX,int midY, float boundColour){
		//		List<Point2d> allPoints = new ArrayList<Point2d>();
		FImage newImage = this.newInstance(this.getWidth(),getHeight());
		for(int x = 0 ; x < this.getWidth(); x ++ ){
			for(int y = 0; y < this.getHeight(); y++){
				int cx = x-midX;
				int cy = y-midY;
				double px = transform.get(0, 0) * cx + transform.get(0, 1) * cy;
				double py = transform.get(1, 0) * cx + transform.get(1, 1) * cy;

				px += midX;
				py += midY;

				int xfloor = (int) Math.floor(px);
				int yfloor = (int) Math.floor(py);

				double xplus = px - xfloor;
				double yplus = py - yfloor;

				double w1 = Math.abs((1 - xplus) * (1 - yplus));
				double w2 = Math.abs((    xplus) * (1 - yplus));
				double w3 = Math.abs((1 - xplus) * (    yplus));
				double w4 = Math.abs((    xplus) * (    yplus));

				int x1 = (xfloor < 0 ? -1 : xfloor >= this.getWidth() ? this.getWidth() : xfloor);
				int x2 = (xfloor+1 < 0 ? -1 : xfloor+1 >= this.getWidth() ? this.getWidth() : xfloor+1);
				int x3 = (xfloor < 0 ? -1 : xfloor >= this.getWidth() ? this.getWidth() : xfloor);
				int x4 = (xfloor+1 < 0 ? -1 : xfloor+1 >= this.getWidth() ? this.getWidth() : xfloor+1);

				int y1 = (yfloor < 0 ? -1 : yfloor >= this.getHeight() ? this.getHeight() : yfloor);
				int y2 = (yfloor < 0 ? -1 : yfloor >= this.getHeight() ? this.getHeight() : yfloor);
				int y3 = (yfloor+1 < 0 ? -1 : yfloor+1 >= this.getHeight() ? this.getHeight() : yfloor+1);
				int y4 = (yfloor+1 < 0 ? -1 : yfloor+1 >= this.getHeight() ? this.getHeight() : yfloor+1);

				// if ANY are out of bounds then set a hard value of bound colour
				if(x1 < 0 || x2 < 0 || x3 < 0 || x4 < 0 || y1 < 0 || y2 < 0 || y3 < 0 || y4 < 0 ||
						x1 >= this.getWidth() || x2 >= this.getWidth() || x3 >= this.getWidth() || x4 >= this.getWidth() || 
						y1 >= this.getHeight() || y2 >= this.getHeight() || y3>= this.getHeight() || y4 >= this.getHeight())
				{
					newImage.pixels[y][x] = boundColour;
				}
				else{
					newImage.pixels[y][x] += pixels[y1][x1] * w1;
					newImage.pixels[y][x] += pixels[y2][x2] * w2;
					newImage.pixels[y][x] += pixels[y3][x3] * w3;
					newImage.pixels[y][x] += pixels[y4][x4] * w4;
				}
			}
		}
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		String imageString = "";
		for(int y = 0; y < this.height;y++){
			for(int x = 0; x < this.width; x++){
				imageString += String.format("%+.3f ", this.pixels[y][x]);
				if(x == 16){
					if(this.width - 16 <= x) continue;
					imageString += "... ";
					x = this.width - 16;
				}
			}
			imageString += "\n";
			if(y == 16) {
				if(this.height - 16 <= y) continue;
				y = this.height - 16;
				imageString += "... \n";
			}

		}
		return imageString;
	}

	/**
	 *	Returns a string representation of every pixel in this image
	 *	using the format string (see {@link String#format(String, Object...)})
	 *	to format each pixel value.
	 * 
	 *  @param format The format string to use for each pixel output
	 *  @return A string representation of the image
	 *  @see String#format(String, Object...)
	 */
	public String toString(String format) {
		String imageString = "";
		for(int y = 0; y < this.height;y++) {
			for(int x = 0; x < this.width; x++) {
				imageString += String.format(format, this.pixels[y][x]);
			}
			imageString += "\n";
		}
		return imageString;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getContentArea()
	 */
	@Override
	public Rectangle getContentArea() {
		int minc=width, maxc=0, minr=height, maxr=0;

		for (int r=0; r<height; r++) {
			for (int c=0; c<width; c++) {
				if (pixels[r][c] > 0) {
					if (c < minc) minc = c;
					if (c > maxc) maxc = c;
					if (r < minr) minr = r;
					if (r > maxr) maxr = r;					
				}
			}
		}

		return new Rectangle(minc, minr, maxc-minc, maxr-minr);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#transform(Jama.Matrix)
	 */
	@Override
	public FImage transform(Matrix transform){
		return super.transform(transform);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getPixelComparator()
	 */
	@Override
	public Comparator<? super Float> getPixelComparator() {
		return new Comparator<Float>(){

			@Override
			public int compare(Float o1, Float o2) {
				return o1.compareTo(o2);
			}
			
		};
	}
	
	/**
	 * 	Returns the pixels of the image as a vector (array) of doubles.
	 * 
	 *  @return the pixels of the image as a vector (array) of doubles.
	 */
	public double[] getDoublePixelVector()
	{
		double f[] = new double[height*width];
		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				f[x+y*width] = pixels[y][x];
		
		return f;
	}
}
