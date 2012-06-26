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
import org.openimaj.image.analyser.PixelAnalyser;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.renderer.FImageRenderer;
import org.openimaj.image.renderer.RenderHints;
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
 * 	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FImage extends SingleBandImage<Float, FImage>
{
	private static final long serialVersionUID = 1L;
	
	/** The logging class */
	protected static Logger logger = Logger.getLogger(FImage.class);
	
	/**
	 * The default number of sigmas at which the Gaussian function is truncated
	 * when building a kernel
	 */
	protected static final float DEFAULT_GAUSS_TRUNCATE = 4.0f;

	/** The underlying pixels */
	public float pixels[][];
	
	/**
	 * 	Create an {@link FImage} from an array of floating point
	 * 	values with the given width and height. The length of the array
	 * 	must equal the width multiplied by the height.
	 * 
	 *  @param array An array of floating point values.
	 *  @param width The width of the resulting image.
	 *  @param height The height of th resulting image.
	 */
	public FImage( float[] array, int width, int height )
	{
		assert( array.length == width*height );
		
		pixels = new float[height][width];
		this.height = height;
		this.width = width;

		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				pixels[y][x] = array[y*width+x];
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
	 * Version of {@link Image#addInplace(Image)} which takes an {@link FImage}. 
	 * This directly accesses the underlying float[][] 
	 * and is therefore fast. This function side-affects the pixels in this
	 * {@link FImage}.
	 * 
	 * @see Image#addInplace(Image)
	 * @param im the FImage to add
	 * @return a reference to this
	 */
	public FImage addInplace( FImage im )
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
	 * 	@see org.openimaj.image.Image#addInplace(java.lang.Object)
	 */
	@Override
	public FImage addInplace( Float num )
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
	 * 	@see org.openimaj.image.Image#addInplace(org.openimaj.image.Image)
	 * 	@exception UnsupportedOperationException if an unsupported type is added
	 * 	@return a reference to this {@link FImage}
	 */
	@Override
	public FImage addInplace( Image<?,?> im )
	{
		if( im instanceof FImage )
			return addInplace( (FImage) im );
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
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.SingleBandImage#clone()
	 */
	@Override
	public FImage clone()
	{
		FImage cpy = new FImage( width, height );
		int r;

		for( r = 0; r < height; r++ )
			System.arraycopy(pixels[r], 0, cpy.pixels[r], 0, width);

		return cpy;
	}
	
	@Override
	public FImageRenderer createRenderer() {
		return new FImageRenderer(this);
	}

	@Override
	public FImageRenderer createRenderer(RenderHints options) {
		return new FImageRenderer(this, options);
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
	 * image. This is a version of {@link Image#divideInplace(Image)} which 
	 * takes an {@link FImage}. This directly accesses the underlying float[][] 
	 * and is therefore fast. This function side-affects this image.
	 * 
	 * @see Image#divideInplace(Image)
	 * @param im the {@link FImage} to be the denominator
	 * @return a reference to this {@link FImage}
	 */
	public FImage divideInplace( FImage im )
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
	 * 	@see org.openimaj.image.Image#divideInplace(java.lang.Object)
	 */
	@Override
	public FImage divideInplace( Float val )
	{
		float fval = val;

		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				pixels[y][x] /= fval;

		return this;
	}
	
	/**
	 * Divide all pixels by a given value
	 * @param fval the value 
	 * @return this image
	 * @see org.openimaj.image.Image#divideInplace(java.lang.Object)
	 */
	public FImage divideInplace( float fval )
	{
		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				pixels[y][x] /= fval;

		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#divideInplace(org.openimaj.image.Image)
	 */
	@Override
	public FImage divideInplace( Image<?,?> im )
	{
		if( im instanceof FImage )
			return divideInplace( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
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
	 * Fill an image with the given colour
	 * @param colour the colour
	 * @return the image
	 * @see org.openimaj.image.SingleBandImage#fill(java.lang.Comparable)
	 */
	public FImage fill( float colour )
	{
		for( int r = 0; r < height; r++ )
			for( int c = 0; c < width; c++ )
				pixels[r][c] = colour;
		
		return this;
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
	 * 	Returns the pixels of the image as a vector (array) of doubles.
	 * 
	 *  @return the pixels of the image as a vector (array) of doubles.
	 */
	public float[] getFloatPixelVector()
	{
		float f[] = new float[height*width];
		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width; x++ )
				f[x+y*width] = pixels[y][x];
		
		return f;
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

		float f00 = this.pixels[y0][x0];
		float f01 = this.pixels[y1][x0];
		float f10 = this.pixels[y0][x1];
		float f11 = this.pixels[y1][x1];
		float dx = (float) (x - x0);
		float dy = (float) (y - y0);
		if(dx < 0) dx = 1 + dx;
		if(dy < 0) dy = 1 + dy;
		
		return Interpolation.bilerp( dx, dy, f00, f01, f10, f11 );
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
	 * Interpolate the value of a pixel at the given coordinates
	 * @param x the x-ordinate
	 * @param y the y-ordinate
	 * @param background the background colour
	 * @return the interpolated pixel value
	 * @see org.openimaj.image.Image#getPixelInterp(double, double)
	 * @see Interpolation#bilerp(double, double, double, double, double, double)
	 */
	public float getPixelInterpNative( float x, float y , float background)
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

		float f00 = (ty0 && tx0 ? this.pixels[y0][x0] : background); // this.pixels[y0][x0];
		float f01 = (ty1 && tx0 ? this.pixels[y1][x0] : background); // this.pixels[y1][x0];
		float f10 = (ty0 && tx1 ? this.pixels[y0][x1] : background); // this.pixels[y0][x1];
		float f11 = (ty1 && tx1 ? this.pixels[y1][x1] : background); // this.pixels[y1][x1];
		
		
		float dx = x - x0;
		float dy = y - y0;
		if(dx < 0) dx = 1 + dx;
		if(dy < 0) dy = 1 + dy;

		float interpVal = Interpolation.bilerpf( dx, dy, f00, f01, f10, f11 );
		return (float) interpVal;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@Override
	public FImage internalCopy( FImage im )
	{
		final int h = im.height;
		final int w = im.width;
		final float [][] impixels = im.pixels;
		
		for (int r=0; r<h; r++)
			System.arraycopy(impixels[r], 0, pixels[r], 0, w);

		return this;
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

				pixels[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(int)fpix];
			}
		}
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
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#multiply(java.lang.Object)
	 */
	@Override
	public FImage multiply( Float num )
	{
		return super.multiply( num );
	}

	/**
	 * Multiplies this image's pixel values by the corresponding pixel values
	 * in the given image side-affecting this image. This is a
	 * version of {@link Image#multiplyInplace(Image)} which takes an {@link FImage}. 
	 * This directly accesses the underlying float[][] 
	 * and is therefore fast. This function works inplace.
	 * 
	 * @see Image#multiplyInplace(Image)
	 * @param im the {@link FImage} to multiply with this image
	 * @return a reference to this image
	 */
	public FImage multiplyInplace( FImage im )
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
	 * 	@see org.openimaj.image.Image#multiplyInplace(java.lang.Object)
	 */
	@Override
	public FImage multiplyInplace( Float num )
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
	 * Multiply all pixel values by the given value
	 * @param fnum the value
	 * @return this image
	 * @see org.openimaj.image.Image#multiplyInplace(java.lang.Object)
	 */
	public FImage multiplyInplace( float fnum )
	{
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
	 *	This method will throw an {@link UnsupportedOperationException} if the
	 *	input input is not an {@link FImage}.
	 * 	@see org.openimaj.image.Image#multiplyInplace(org.openimaj.image.Image)
	 * 	@throws UnsupportedOperationException if the given image is not an {@link FImage}
	 */
	@Override
	public FImage multiplyInplace( Image<?,?> im )
	{
		if( im instanceof FImage )
			return multiplyInplace( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
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
	 *  @see org.openimaj.image.Image#processInplace(org.openimaj.image.processor.PixelProcessor)
	 */
	@Override
	public FImage processInplace( PixelProcessor<Float> p )
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
	 *  @see org.openimaj.image.Image#analyseWith(org.openimaj.image.analyser.PixelAnalyser)
	 */
	@Override
	public void analyseWith( PixelAnalyser<Float> p )
	{
		p.reset();
		
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				p.analysePixel( pixels[y][x] );
			}
		}
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
	public FImage subtractInplace( FImage im )
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
	 *  @see org.openimaj.image.Image#subtractInplace(java.lang.Object)
	 */
	@Override
	public FImage subtractInplace( Float num )
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
	 *  @see org.openimaj.image.Image#subtractInplace(org.openimaj.image.Image)
	 */
	@Override
	public FImage subtractInplace( Image<?,?> im )
	{
		if( im instanceof FImage )
			return subtractInplace( (FImage) im );
		else
			throw new UnsupportedOperationException( "Unsupported Type" );
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
	 *  @see org.openimaj.image.Image#transform(Jama.Matrix)
	 */
	@Override
	public FImage transform(Matrix transform){
		return super.transform(transform);
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
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof FImage)){return false;}
		return equalsThresh((FImage)o, 0);
	}
	
	/**
	 * Compare this image against another using a threshold
	 * on the absolute difference between pixel values in
	 * order to determine equality.
	 * @param o the image to compare against
	 * @param thresh the threshold for determining equality
	 * @return true images are the same size and if all pixel values 
	 * 			have a difference less than threshold; false otherwise.
	 */
	public boolean equalsThresh(FImage o, float thresh) {
		FImage that = (FImage) o;
		if(that.height!= this.height || that.width != this.width) return false;
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				if(Math.abs(that.pixels[i][j] - this.pixels[i][j]) > thresh){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get the value of the pixel at coordinate p
	 * 
	 * @param p The coordinate to get
	 * 
	 * @return The pixel value at (x, y)
	 */
	public float getPixelNative(Pixel p) {
		return getPixelNative(p.x, p.y);
	}
	
	/**
	 * Get the value of the pixel at coordinate <code>(x, y)</code>.
	 * 
	 * @param x The x-coordinate to get
	 * @param y The y-coordinate to get
	 * 
	 * @return The pixel value at (x, y)
	 */
	public float getPixelNative(int x, int y) {
		return pixels[y][x];
	}
	
	/**
	 * 	Returns the pixels in this image as a vector (an array of the pixel
	 * 	type).
	 *  
	 *  @param f The array into which to place the data
	 *  @return The pixels in the image as a vector (a reference to the given array).
	 */
	public float[] getPixelVectorNative( float[] f )
	{
		for( int y = 0; y < getHeight(); y++ )
			for( int x = 0; x < getWidth(); x++ )
				f[x+y*getWidth()] = pixels[y][x];
		
		return f;
	}
	
	/**
	 * Sets the pixel at <code>(x,y)</code> to the given value. Side-affects
	 * this image.
	 * 
	 * @param x The x-coordinate of the pixel to set
	 * @param y The y-coordinate of the pixel to set
	 * @param val The value to set the pixel to.
	 */
	public void setPixelNative(int x, int y, float val) {
		pixels[y][x] = val;
	}
		
	@Override
	public FImage shiftLeftInplace(int n) {
		if (n<0) return shiftRightInplace(-n);
		for( int y = 0; y < height; y++ )
			for( int x = 0; x < width-n; x++ )
				pixels[y][x] = pixels[y][x+n];
		
		for( int y = 0; y < height; y++ )
			for( int x = width-n; x < width; x++ )
				pixels[y][x] = 0;
		
		return this;
	}
	
	@Override
	public FImage shiftRightInplace(int n) {
		if (n<0) return shiftLeftInplace(-n);
		for( int y = 0; y < height; y++ )
			for( int x = width-1; x >= n; x-- )
				pixels[y][x] = pixels[y][x-n];
		
		for( int y = 0; y < height; y++ )
			for( int x = 0; x < n; x++ )
				pixels[y][x] = 0;
		
		return this;
	}

	/**
	 * Convenience method to initialise an array of FImages 
	 * @param num array length
	 * @param width width of images
	 * @param height height of images
	 * @return array of newly initialised images
	 */
	public static FImage[] createArray(final int num, final int width, final int height) {
		FImage [] array = new FImage[num];
		
		for (int i=0; i<num; i++) {
			array[i] = new FImage(width, height);
		}
		
		return array;
	}

	/**
	 * @return The sum of all the pixels in the image
	 */
	public float sum() {
		float sum = 0;
		for (float[] row : this.pixels) {
			for (int i = 0; i < row.length; i++) {
				sum += row[i];
			}
		}
		return sum;
	}

	/**
	 * Convert this {@link FImage} to an RGB {@link MBFImage}.
	 * @return a new RGB colour image.
	 */
	public MBFImage toRGB() {
		return new MBFImage(ColourSpace.RGB, this.clone(), this.clone(), this.clone());
	}

	@Override
	public FImage flipX() {
		final int hwidth = width / 2;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<hwidth; x++) {
				int xx = width - x -1;
				
				float tmp = pixels[y][x];
				
				pixels[y][x] = pixels[y][xx];
				pixels[y][xx] = tmp;
			}
		}
		return this;
	}

	@Override
	public FImage flipY() {
		final int hheight = height / 2;
		
		for (int y=0; y<hheight; y++) {
			int yy = height - y - 1;
			
			for (int x=0; x<width; x++) {
				float tmp = pixels[y][x];
				
				pixels[y][x] = pixels[yy][x];
				pixels[yy][x] = tmp;
			}
		}
		
		return this;
	}
}
