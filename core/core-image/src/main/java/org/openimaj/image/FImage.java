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
 * Class representing a single-band floating-point image; that is an image where
 * each pixel is represented by a floating-point number.
 * <p>
 * {@link FImage}s can be created from PGM files or from pixel arrays. If you
 * wish to read other types of files then use the {@link ImageUtilities} class
 * that provides read/write functions for {@link Image} objects.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
	 * Create an {@link FImage} from an array of floating point values with the
	 * given width and height. The length of the array must equal the width
	 * multiplied by the height.
	 *
	 * @param array
	 *            An array of floating point values.
	 * @param width
	 *            The width of the resulting image.
	 * @param height
	 *            The height of th resulting image.
	 */
	public FImage(final float[] array, final int width, final int height)
	{
		assert (array.length == width * height);

		this.pixels = new float[height][width];
		this.height = height;
		this.width = width;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				this.pixels[y][x] = array[y * width + x];
	}

	/**
	 * Create an {@link FImage} from an array of double values with the given
	 * width and height. The length of the array must equal the width multiplied
	 * by the height. The values will be downcast to floats.
	 *
	 * @param array
	 *            An array of floating point values.
	 * @param width
	 *            The width of the resulting image.
	 * @param height
	 *            The height of th resulting image.
	 */
	public FImage(final double[] array, final int width, final int height)
	{
		assert (array.length == width * height);

		this.pixels = new float[height][width];
		this.height = height;
		this.width = width;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				this.pixels[y][x] = (float) array[y * width + x];
	}

	/**
	 * Create an {@link FImage} from an array of double values with the given
	 * width and height. The length of the array must equal the width multiplied
	 * by the height. The values will be downcast to floats.
	 *
	 * @param array
	 *            An array of floating point values.
	 * @param width
	 *            The width of the resulting image.
	 * @param height
	 *            The height of the resulting image.
	 * @param offset
	 *            The offset in the array to begin reading from
	 */
	public FImage(final double[] array, final int width, final int height, int offset)
	{
		assert (array.length == width * height);

		this.pixels = new float[height][width];
		this.height = height;
		this.width = width;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				this.pixels[y][x] = (float) array[offset + y * width + x];
	}

	/**
	 * Create an {@link FImage} from an array of floating point values.
	 *
	 * @param array
	 *            the array representing pixel values to copy data from.
	 */
	public FImage(final float[][] array)
	{
		this.pixels = array;
		this.height = array.length;
		this.width = array[0].length;
	}

	/**
	 * Create an empty {@link FImage} of the given size.
	 *
	 * @param width
	 *            image width (number of columns)
	 * @param height
	 *            image height (number of rows)
	 */
	public FImage(final int width, final int height) {
		this.pixels = new float[height][width];

		this.height = height;
		this.width = width;
	}

	/**
	 * Construct an {@link FImage} from an array of packed ARGB integers.
	 *
	 * @param data
	 *            array of packed ARGB pixels
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 */
	public FImage(final int[] data, final int width, final int height) {
		this.internalAssign(data, width, height);
	}

	/**
	 * Construct an {@link FImage} from an array of packed ARGB integers using
	 * the specified plane.
	 *
	 * @param data
	 *            array of packed ARGB pixels
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 * @param plane
	 *            The {@link ARGBPlane} to copy data from
	 */
	public FImage(final int[] data, final int width, final int height, final ARGBPlane plane) {
		this.width = width;
		this.height = height;
		this.pixels = new float[height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int rgb = data[x + y * width];

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
				default:
					break;
				}

				this.pixels[y][x] = colour;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#abs()
	 */
	@Override
	public FImage abs() {
		for (int r = 0; r < this.height; r++)
			for (int c = 0; c < this.width; c++)
				this.pixels[r][c] = Math.abs(this.pixels[r][c]);
		return this;
	}

	/**
	 * Adds the pixel values of the given {@link FImage} to the pixels of this
	 * image. Returns a new {@link FImage} and does not affect this image or the
	 * given image. This is a version of {@link Image#add(Image)} which takes an
	 * {@link FImage}. This method directly accesses the underlying float[][]
	 * and is therefore fast. This function returns a new {@link FImage}.
	 *
	 * @see org.openimaj.image.Image#add(Image)
	 * @param im
	 *            {@link FImage} to add into this one.
	 * @return A new {@link FImage}
	 */
	public FImage add(final FImage im)
	{
		if (!ImageUtilities.checkSameSize(this, im))
			throw new AssertionError("images must be the same size");

		final FImage newImage = new FImage(im.width, im.height);

		for (int r = 0; r < im.height; r++)
			for (int c = 0; c < im.width; c++)
				newImage.pixels[r][c] = this.pixels[r][c] + im.pixels[r][c];

		return newImage;
	}

	/**
	 * Returns a new {@link FImage} that contains the pixels of this image
	 * increased by the given value. {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#add(java.lang.Object)
	 */
	@Override
	public FImage add(final Float num)
	{
		final FImage newImage = new FImage(this.width, this.height);
		final float fnum = num;

		for (int r = 0; r < this.height; r++)
			for (int c = 0; c < this.width; c++)
				newImage.pixels[r][c] = this.pixels[r][c] + fnum;

		return newImage;
	}

	/**
	 * {@inheritDoc} This method throws an {@link UnsupportedOperationException}
	 * if the given image is not an {@link FImage}.
	 *
	 * @see org.openimaj.image.Image#add(org.openimaj.image.Image)
	 * @exception UnsupportedOperationException
	 *                if an unsupported type is added
	 * @return a reference to this {@link FImage}
	 */
	@Override
	public FImage add(final Image<?, ?> im)
	{
		if (im instanceof FImage)
			return this.add((FImage) im);
		else
			throw new UnsupportedOperationException("Unsupported Type");
	}

	/***
	 * Adds the given image pixel values to the pixel values of this image.
	 * Version of {@link Image#addInplace(Image)} which takes an {@link FImage}.
	 * This directly accesses the underlying float[][] and is therefore fast.
	 * This function side-affects the pixels in this {@link FImage}.
	 *
	 * @see Image#addInplace(Image)
	 * @param im
	 *            the FImage to add
	 * @return a reference to this
	 */
	public FImage addInplace(final FImage im)
	{
		if (!ImageUtilities.checkSameSize(this, im))
			throw new AssertionError("images must be the same size");

		for (int r = 0; r < im.height; r++)
			for (int c = 0; c < im.width; c++)
				this.pixels[r][c] += im.pixels[r][c];

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#addInplace(java.lang.Object)
	 */
	@Override
	public FImage addInplace(final Float num)
	{
		final float fnum = num;
		for (int r = 0; r < this.height; r++)
			for (int c = 0; c < this.width; c++)
				this.pixels[r][c] += fnum;

		return this;
	}

	/**
	 * {@inheritDoc} This method throws an {@link UnsupportedOperationException}
	 * if the given image is not an {@link FImage}.
	 *
	 * @see org.openimaj.image.Image#addInplace(org.openimaj.image.Image)
	 * @exception UnsupportedOperationException
	 *                if an unsupported type is added
	 * @return a reference to this {@link FImage}
	 */
	@Override
	public FImage addInplace(final Image<?, ?> im)
	{
		if (im instanceof FImage)
			return this.addInplace((FImage) im);
		else
			throw new UnsupportedOperationException("Unsupported Type");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#clip(java.lang.Object, java.lang.Object)
	 */
	@Override
	public FImage clip(final Float min, final Float max)
	{
		int r, c;

		for (r = 0; r < this.height; r++)
		{
			for (c = 0; c < this.width; c++)
			{
				if (this.pixels[r][c] < min)
					this.pixels[r][c] = 0;
				if (this.pixels[r][c] > max)
					this.pixels[r][c] = 1;
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#clipMax(java.lang.Object)
	 */
	@Override
	public FImage clipMax(final Float thresh)
	{
		final float fthresh = thresh;
		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				if (this.pixels[r][c] > fthresh)
					this.pixels[r][c] = 1;
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#clipMin(java.lang.Object)
	 */
	@Override
	public FImage clipMin(final Float thresh)
	{
		final float fthresh = thresh;
		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				if (this.pixels[r][c] < fthresh)
					this.pixels[r][c] = 0;
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.SingleBandImage#clone()
	 */
	@Override
	public FImage clone()
	{
		final FImage cpy = new FImage(this.width, this.height);
		int r;

		for (r = 0; r < this.height; r++)
			System.arraycopy(this.pixels[r], 0, cpy.pixels[r], 0, this.width);

		return cpy;
	}

	@Override
	public FImageRenderer createRenderer() {
		return new FImageRenderer(this);
	}

	@Override
	public FImageRenderer createRenderer(final RenderHints options) {
		return new FImageRenderer(this, options);
	}

	/**
	 * Divides the pixels values of this image with the values from the given
	 * image. This is a version of {@link Image#divide(Image)} which takes an
	 * {@link FImage}. This directly accesses the underlying float[][] and is
	 * therefore fast. This function returns a new {@link FImage}.
	 *
	 * @see Image#divide(Image)
	 * @param im
	 *            the {@link FImage} to be the denominator.
	 * @return A new {@link FImage}
	 */
	public FImage divide(final FImage im)
	{
		if (!ImageUtilities.checkSameSize(this, im))
			throw new AssertionError("images must be the same size");

		final FImage newImage = new FImage(im.width, im.height);
		int r, c;

		for (r = 0; r < im.height; r++)
			for (c = 0; c < im.width; c++)
				newImage.pixels[r][c] = this.pixels[r][c] / im.pixels[r][c];

		return newImage;
	}

	/**
	 * Divides the pixel values of this image with the values from the given
	 * image. This is a version of {@link Image#divideInplace(Image)} which
	 * takes an {@link FImage}. This directly accesses the underlying float[][]
	 * and is therefore fast. This function side-affects this image.
	 *
	 * @see Image#divideInplace(Image)
	 * @param im
	 *            the {@link FImage} to be the denominator
	 * @return a reference to this {@link FImage}
	 */
	public FImage divideInplace(final FImage im)
	{
		if (!ImageUtilities.checkSameSize(this, im))
			throw new AssertionError("images must be the same size");

		for (int y = 0; y < this.height; y++)
		{
			for (int x = 0; x < this.width; x++)
			{
				this.pixels[y][x] /= im.pixels[y][x];
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#divideInplace(java.lang.Object)
	 */
	@Override
	public FImage divideInplace(final Float val)
	{
		final float fval = val;

		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < this.width; x++)
				this.pixels[y][x] /= fval;

		return this;
	}

	/**
	 * Divide all pixels by a given value
	 *
	 * @param fval
	 *            the value
	 * @return this image
	 * @see org.openimaj.image.Image#divideInplace(java.lang.Object)
	 */
	public FImage divideInplace(final float fval)
	{
		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < this.width; x++)
				this.pixels[y][x] /= fval;

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#divideInplace(org.openimaj.image.Image)
	 */
	@Override
	public FImage divideInplace(final Image<?, ?> im)
	{
		if (im instanceof FImage)
			return this.divideInplace((FImage) im);
		else
			throw new UnsupportedOperationException("Unsupported Type");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#extractROI(int, int,
	 *      org.openimaj.image.Image)
	 */
	@Override
	public FImage extractROI(final int x, final int y, final FImage out)
	{
		for (int r = y, rr = 0; rr < out.height; r++, rr++)
		{
			for (int c = x, cc = 0; cc < out.width; c++, cc++)
			{
				if (r < 0 || r >= this.height || c < 0 || c >= this.width)
					(out).pixels[rr][cc] = 0;
				else
					(out).pixels[rr][cc] = this.pixels[r][c];
			}
		}

		return out;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#extractROI(int, int, int, int)
	 */
	@Override
	public FImage extractROI(final int x, final int y, final int w, final int h)
	{
		final FImage out = new FImage(w, h);

		for (int r = y, rr = 0; rr < h; r++, rr++)
		{
			for (int c = x, cc = 0; cc < w; c++, cc++)
			{
				if (r < 0 || r >= this.height || c < 0 || c >= this.width)
					out.pixels[rr][cc] = 0;
				else
					out.pixels[rr][cc] = this.pixels[r][c];
			}
		}

		return out;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.SingleBandImage#fill(java.lang.Comparable)
	 */
	@Override
	public FImage fill(final Float colour)
	{
		for (int r = 0; r < this.height; r++)
			for (int c = 0; c < this.width; c++)
				this.pixels[r][c] = colour;

		return this;
	}

	/**
	 * Fill an image with the given colour
	 *
	 * @param colour
	 *            the colour
	 * @return the image
	 * @see org.openimaj.image.SingleBandImage#fill(java.lang.Comparable)
	 */
	public FImage fill(final float colour)
	{
		for (int r = 0; r < this.height; r++)
			for (int c = 0; c < this.width; c++)
				this.pixels[r][c] = colour;

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getContentArea()
	 */
	@Override
	public Rectangle getContentArea() {
		int minc = this.width, maxc = 0, minr = this.height, maxr = 0;

		for (int r = 0; r < this.height; r++) {
			for (int c = 0; c < this.width; c++) {
				if (this.pixels[r][c] > 0) {
					if (c < minc)
						minc = c;
					if (c > maxc)
						maxc = c;
					if (r < minr)
						minr = r;
					if (r > maxr)
						maxr = r;
				}
			}
		}

		return new Rectangle(minc, minr, maxc - minc, maxr - minr);
	}

	/**
	 * Returns the pixels of the image as a vector (array) of doubles.
	 *
	 * @return the pixels of the image as a vector (array) of doubles.
	 */
	public double[] getDoublePixelVector()
	{
		final double f[] = new double[this.height * this.width];
		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < this.width; x++)
				f[x + y * this.width] = this.pixels[y][x];

		return f;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getField(org.openimaj.image.Image.Field)
	 */
	@Override
	public FImage getField(final Field f)
	{
		final FImage img = new FImage(this.width, this.height / 2);

		int r, r2, c;
		final int init = (f.equals(Field.ODD) ? 1 : 0);
		for (r = init, r2 = 0; r < this.height && r2 < this.height / 2; r += 2, r2++)
		{
			for (c = 0; c < this.width; c++)
			{
				img.pixels[r2][c] = this.pixels[r][c];
			}
		}

		return img;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getFieldCopy(org.openimaj.image.Image.Field)
	 */
	@Override
	public FImage getFieldCopy(final Field f)
	{
		final FImage img = new FImage(this.width, this.height);

		int r, c;
		for (r = 0; r < this.height; r += 2)
		{
			for (c = 0; c < this.width; c++)
			{
				if (f.equals(Field.EVEN))
				{
					img.pixels[r][c] = this.pixels[r][c];
					img.pixels[r + 1][c] = this.pixels[r][c];
				}
				else
				{
					img.pixels[r][c] = this.pixels[r + 1][c];
					img.pixels[r + 1][c] = this.pixels[r + 1][c];
				}
			}
		}

		return img;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getFieldInterpolate(org.openimaj.image.Image.Field)
	 */
	@Override
	public FImage getFieldInterpolate(final Field f)
	{
		final FImage img = new FImage(this.width, this.height);

		int r, c;
		for (r = 0; r < this.height; r += 2)
		{
			for (c = 0; c < this.width; c++)
			{
				if (f.equals(Field.EVEN))
				{
					img.pixels[r][c] = this.pixels[r][c];

					if (r + 2 == this.height)
					{
						img.pixels[r + 1][c] = this.pixels[r][c];
					}
					else
					{
						img.pixels[r + 1][c] = 0.5F * (this.pixels[r][c] + this.pixels[r + 2][c]);
					}
				}
				else
				{
					img.pixels[r + 1][c] = this.pixels[r + 1][c];

					if (r == 0)
					{
						img.pixels[r][c] = this.pixels[r + 1][c];
					}
					else
					{
						img.pixels[r][c] = 0.5F * (this.pixels[r - 1][c] + this.pixels[r + 1][c]);
					}
				}
			}
		}

		return img;
	}

	/**
	 * Returns the pixels of the image as a vector (array) of floats.
	 *
	 * @return the pixels of the image as a vector (array) of floats.
	 */
	public float[] getFloatPixelVector()
	{
		final float f[] = new float[this.height * this.width];
		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < this.width; x++)
				f[x + y * this.width] = this.pixels[y][x];

		return f;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getPixel(int, int)
	 */
	@Override
	public Float getPixel(final int x, final int y)
	{
		return this.pixels[y][x];
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getPixelComparator()
	 */
	@Override
	public Comparator<? super Float> getPixelComparator() {
		return new Comparator<Float>() {

			@Override
			public int compare(final Float o1, final Float o2) {
				return o1.compareTo(o2);
			}

		};
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getPixelInterp(double, double)
	 * @see Interpolation#bilerp(double, double, double, double, double, double)
	 */
	@Override
	public Float getPixelInterp(final double x, final double y)
	{
		int x0 = (int) Math.floor(x);
		int x1 = x0 + 1;
		int y0 = (int) Math.floor(y);
		int y1 = y0 + 1;

		if (x0 < 0)
			x0 = 0;
		if (x0 >= this.width)
			x0 = this.width - 1;
		if (y0 < 0)
			y0 = 0;
		if (y0 >= this.height)
			y0 = this.height - 1;

		if (x1 < 0)
			x1 = 0;
		if (x1 >= this.width)
			x1 = this.width - 1;
		if (y1 < 0)
			y1 = 0;
		if (y1 >= this.height)
			y1 = this.height - 1;

		final float f00 = this.pixels[y0][x0];
		final float f01 = this.pixels[y1][x0];
		final float f10 = this.pixels[y0][x1];
		final float f11 = this.pixels[y1][x1];
		float dx = (float) (x - x0);
		float dy = (float) (y - y0);
		if (dx < 0)
			dx = 1 + dx;
		if (dy < 0)
			dy = 1 + dy;

		return Interpolation.bilerp(dx, dy, f00, f01, f10, f11);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getPixelInterp(double, double)
	 * @see Interpolation#bilerp(double, double, double, double, double, double)
	 */
	@Override
	public Float getPixelInterp(final double x, final double y, final Float background)
	{
		final int x0 = (int) Math.floor(x);
		final int x1 = x0 + 1;
		final int y0 = (int) Math.floor(y);
		final int y1 = y0 + 1;

		boolean tx0, tx1, ty0, ty1;
		tx0 = ty0 = tx1 = ty1 = true;
		if (x0 < 0)
			tx0 = false;
		if (x0 >= this.width)
			tx0 = false;
		if (y0 < 0)
			ty0 = false;
		if (y0 >= this.height)
			ty0 = false;

		if (x1 < 0)
			tx1 = false;
		if (x1 >= this.width)
			tx1 = false;
		if (y1 < 0)
			ty1 = false;
		if (y1 >= this.height)
			ty1 = false;

		final double f00 = (ty0 && tx0 ? this.pixels[y0][x0] : background.floatValue()); // this.pixels[y0][x0];
		final double f01 = (ty1 && tx0 ? this.pixels[y1][x0] : background.floatValue()); // this.pixels[y1][x0];
		final double f10 = (ty0 && tx1 ? this.pixels[y0][x1] : background.floatValue()); // this.pixels[y0][x1];
		final double f11 = (ty1 && tx1 ? this.pixels[y1][x1] : background.floatValue()); // this.pixels[y1][x1];

		double dx = x - x0;
		double dy = y - y0;
		if (dx < 0)
			dx = 1 + dx;
		if (dy < 0)
			dy = 1 + dy;

		final double interpVal = Interpolation.bilerp(dx, dy, f00, f01, f10, f11);
		return (float) interpVal;
	}

	/**
	 * Interpolate the value of a pixel at the given coordinates
	 *
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 * @param background
	 *            the background colour
	 * @return the interpolated pixel value
	 * @see org.openimaj.image.Image#getPixelInterp(double, double)
	 * @see Interpolation#bilerp(double, double, double, double, double, double)
	 */
	public float getPixelInterpNative(final float x, final float y, final float background)
	{
		final int x0 = (int) Math.floor(x);
		final int x1 = x0 + 1;
		final int y0 = (int) Math.floor(y);
		final int y1 = y0 + 1;

		boolean tx0, tx1, ty0, ty1;
		tx0 = ty0 = tx1 = ty1 = true;
		if (x0 < 0)
			tx0 = false;
		if (x0 >= this.width)
			tx0 = false;
		if (y0 < 0)
			ty0 = false;
		if (y0 >= this.height)
			ty0 = false;

		if (x1 < 0)
			tx1 = false;
		if (x1 >= this.width)
			tx1 = false;
		if (y1 < 0)
			ty1 = false;
		if (y1 >= this.height)
			ty1 = false;

		final float f00 = (ty0 && tx0 ? this.pixels[y0][x0] : background); // this.pixels[y0][x0];
		final float f01 = (ty1 && tx0 ? this.pixels[y1][x0] : background); // this.pixels[y1][x0];
		final float f10 = (ty0 && tx1 ? this.pixels[y0][x1] : background); // this.pixels[y0][x1];
		final float f11 = (ty1 && tx1 ? this.pixels[y1][x1] : background); // this.pixels[y1][x1];

		float dx = x - x0;
		float dy = y - y0;
		if (dx < 0)
			dx = 1 + dx;
		if (dy < 0)
			dy = 1 + dy;

		final float interpVal = Interpolation.bilerpf(dx, dy, f00, f01, f10, f11);
		return interpVal;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@Override
	public FImage internalCopy(final FImage im)
	{
		final int h = im.height;
		final int w = im.width;
		final float[][] impixels = im.pixels;

		for (int r = 0; r < h; r++)
			System.arraycopy(impixels[r], 0, this.pixels[r], 0, w);

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@Override
	public FImage internalAssign(final FImage im)
	{
		this.pixels = im.pixels;
		this.height = im.height;
		this.width = im.width;

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#internalAssign(int [] data, int width, int
	 *      height)
	 */
	@Override
	public FImage internalAssign(final int[] data, final int width, final int height) {
		if (this.height != height || this.width != width) {
			this.height = height;
			this.width = width;
			this.pixels = new float[height][width];
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int rgb = data[x + width * y];

				final int red = ((rgb >> 16) & 0xff);
				final int green = ((rgb >> 8) & 0xff);
				final int blue = ((rgb) & 0xff);

				// NTSC colour conversion:
				// This improves keypoint detection for some reason!
				final float fpix = 0.299f * red + 0.587f * green + 0.114f * blue;

				this.pixels[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(int) fpix];
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#inverse()
	 */
	@Override
	public FImage inverse()
	{
		int r, c;
		final float max = this.max();

		for (r = 0; r < this.height; r++)
			for (c = 0; c < this.width; c++)
				this.pixels[r][c] = max - this.pixels[r][c];

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#max()
	 */
	@Override
	public Float max()
	{
		int r, c;
		float max = Float.MIN_VALUE;

		for (r = 0; r < this.height; r++)
			for (c = 0; c < this.width; c++)
				if (max < this.pixels[r][c])
					max = this.pixels[r][c];

		return max;
	}

	/**
	 * Get the pixel with the maximum value. Returns an {@link FValuePixel}
	 * which contains the location and value of the pixel. If there are multiple
	 * pixels with the same value then the first is returned. Note that this
	 * method assumes all pixel values are greater than 0.
	 *
	 * @return the maximum pixel as an {@link FValuePixel}.
	 */
	public FValuePixel maxPixel()
	{
		final FValuePixel max = new FValuePixel(-1, -1);
		max.value = -Float.MAX_VALUE;

		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				if (max.value < this.pixels[y][x]) {
					max.value = this.pixels[y][x];
					max.x = x;
					max.y = y;
				}
			}
		}

		return max;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#min()
	 */
	@Override
	public Float min()
	{
		int r, c;
		float min = Float.MAX_VALUE;

		for (r = 0; r < this.height; r++)
			for (c = 0; c < this.width; c++)
				if (min > this.pixels[r][c])
					min = this.pixels[r][c];

		return min;
	}

	/**
	 * Get the pixel with the minimum value. Returns an {@link FValuePixel}
	 * which contains the location and value of the pixel. If there are multiple
	 * pixels with the same value then the first is returned. Note that this
	 * method assumes all pixel values are greater than 0.
	 *
	 * @return The minimum pixel as an {@link FValuePixel}.
	 */
	public FValuePixel minPixel()
	{
		final FValuePixel min = new FValuePixel(-1, -1);
		min.value = Float.MAX_VALUE;

		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < this.width; x++)
				if (min.value > this.pixels[y][x]) {
					min.value = this.pixels[y][x];
					min.x = x;
					min.y = y;
				}

		return min;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#multiply(java.lang.Object)
	 */
	@Override
	public FImage multiply(final Float num)
	{
		return super.multiply(num);
	}

	/**
	 * Multiplies this image's pixel values by the corresponding pixel values in
	 * the given image side-affecting this image. This is a version of
	 * {@link Image#multiplyInplace(Image)} which takes an {@link FImage}. This
	 * directly accesses the underlying float[][] and is therefore fast. This
	 * function works inplace.
	 *
	 * @see Image#multiplyInplace(Image)
	 * @param im
	 *            the {@link FImage} to multiply with this image
	 * @return a reference to this image
	 */
	public FImage multiplyInplace(final FImage im)
	{
		if (!ImageUtilities.checkSameSize(this, im))
			throw new AssertionError("images must be the same size");

		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				this.pixels[r][c] *= im.pixels[r][c];
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#multiplyInplace(java.lang.Object)
	 */
	@Override
	public FImage multiplyInplace(final Float num)
	{
		final float fnum = num;
		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				this.pixels[r][c] *= fnum;
			}
		}

		return this;
	}

	/**
	 * Multiply all pixel values by the given value
	 *
	 * @param fnum
	 *            the value
	 * @return this image
	 * @see org.openimaj.image.Image#multiplyInplace(java.lang.Object)
	 */
	public FImage multiplyInplace(final float fnum)
	{
		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				this.pixels[r][c] *= fnum;
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc} This method will throw an
	 * {@link UnsupportedOperationException} if the input input is not an
	 * {@link FImage}.
	 *
	 * @see org.openimaj.image.Image#multiplyInplace(org.openimaj.image.Image)
	 * @throws UnsupportedOperationException
	 *             if the given image is not an {@link FImage}
	 */
	@Override
	public FImage multiplyInplace(final Image<?, ?> im)
	{
		if (im instanceof FImage)
			return this.multiplyInplace((FImage) im);
		else
			throw new UnsupportedOperationException("Unsupported Type");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return A new {@link FImage}
	 * @see org.openimaj.image.Image#newInstance(int, int)
	 */
	@Override
	public FImage newInstance(final int width, final int height)
	{
		return new FImage(width, height);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#normalise()
	 */
	@Override
	public FImage normalise()
	{
		final float min = this.min();
		final float max = this.max();

		if (max == min)
			return this;

		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				this.pixels[r][c] = (this.pixels[r][c] - min) / (max - min);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.SingleBandImage#process(org.openimaj.image.processor.KernelProcessor)
	 */
	@Override
	public FImage process(final KernelProcessor<Float, FImage> p) {
		return this.process(p, false);
	}

	/**
	 * {@inheritDoc} This method has been overridden in {@link FImage} for
	 * performance.
	 *
	 * @see org.openimaj.image.SingleBandImage#process(org.openimaj.image.processor.KernelProcessor,
	 *      boolean)
	 */
	@Override
	public FImage process(final KernelProcessor<Float, FImage> p, final boolean pad)
	{
		final FImage newImage = new FImage(this.width, this.height);
		final int kh = p.getKernelHeight();
		final int kw = p.getKernelWidth();

		final FImage tmp = new FImage(kw, kh);

		final int hh = kh / 2;
		final int hw = kw / 2;

		if (!pad) {
			for (int y = hh; y < this.height - (kh - hh); y++) {
				for (int x = hw; x < this.width - (kw - hw); x++) {
					newImage.pixels[y][x] = p.processKernel(this.extractROI(x - hw, y - hh, tmp));
				}
			}
		} else {
			for (int y = 0; y < this.height; y++) {
				for (int x = 0; x < this.width; x++) {
					newImage.pixels[y][x] = p.processKernel(this.extractROI(x - hw, y - hh, tmp));
				}
			}
		}

		return newImage;
	}

	/**
	 * {@inheritDoc} This method has been overridden in {@link FImage} for
	 * performance.
	 *
	 * @see org.openimaj.image.Image#processInplace(org.openimaj.image.processor.PixelProcessor)
	 */
	@Override
	public FImage processInplace(final PixelProcessor<Float> p)
	{
		for (int y = 0; y < this.height; y++)
		{
			for (int x = 0; x < this.width; x++)
			{
				this.pixels[y][x] = p.processPixel(this.pixels[y][x]);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc} This method has been overridden in {@link FImage} for
	 * performance.
	 *
	 * @see org.openimaj.image.Image#analyseWith(org.openimaj.image.analyser.PixelAnalyser)
	 */
	@Override
	public void analyseWith(final PixelAnalyser<Float> p)
	{
		p.reset();

		for (int y = 0; y < this.height; y++)
		{
			for (int x = 0; x < this.width; x++)
			{
				p.analysePixel(this.pixels[y][x]);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#setPixel(int, int, java.lang.Object)
	 */
	@Override
	public void setPixel(final int x, final int y, final Float val) {
		if (x >= 0 && x < this.width && y >= 0 && y < this.height)
			this.pixels[y][x] = val;
	}

	/**
	 * Subtracts the given {@link FImage} from this image returning a new image
	 * containing the result.
	 *
	 * @param im
	 *            The image to subtract from this image.
	 * @return A new image containing the result.
	 */
	public FImage subtract(final FImage im)
	{
		if (!ImageUtilities.checkSameSize(this, im))
			throw new AssertionError("images must be the same size");

		final FImage newImage = new FImage(im.width, im.height);
		int r, c;

		for (r = 0; r < im.height; r++)
			for (c = 0; c < im.width; c++)
				newImage.pixels[r][c] = this.pixels[r][c] - im.pixels[r][c];
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#subtract(java.lang.Object)
	 */
	@Override
	public FImage subtract(final Float num)
	{
		final FImage newImage = new FImage(this.width, this.height);

		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				newImage.pixels[r][c] = this.pixels[r][c] - num;
			}
		}
		return newImage;
	}

	/**
	 * {@inheritDoc} Throws an {@link UnsupportedOperationException} if the
	 * given image is not an {@link FImage}.
	 *
	 * @see org.openimaj.image.Image#subtract(org.openimaj.image.Image)
	 * @throws UnsupportedOperationException
	 *             if the given image is not an {@link FImage}.
	 */
	@Override
	public FImage subtract(final Image<?, ?> input)
	{
		if (input instanceof FImage)
			return this.subtract((FImage) input);
		else
			throw new UnsupportedOperationException("Unsupported Type");
	}

	/**
	 * Subtracts (pixel-by-pixel) the given {@link FImage} from this image.
	 * Side-affects this image.
	 *
	 * @param im
	 *            The {@link FImage} to subtract from this image.
	 * @return A reference to this image containing the result.
	 */
	public FImage subtractInplace(final FImage im)
	{
		if (!ImageUtilities.checkSameSize(this, im))
			throw new AssertionError("images must be the same size");

		float pix1[][], pix2[][];
		int r, c;

		pix1 = this.pixels;
		pix2 = im.pixels;

		for (r = 0; r < this.height; r++)
			for (c = 0; c < this.width; c++)
				pix1[r][c] -= pix2[r][c];

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#subtractInplace(java.lang.Object)
	 */
	@Override
	public FImage subtractInplace(final Float num)
	{
		final float fnum = num;
		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				this.pixels[r][c] -= fnum;
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#subtractInplace(org.openimaj.image.Image)
	 */
	@Override
	public FImage subtractInplace(final Image<?, ?> im)
	{
		if (im instanceof FImage)
			return this.subtractInplace((FImage) im);
		else
			throw new UnsupportedOperationException("Unsupported Type");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#threshold(java.lang.Object)
	 */
	@Override
	public FImage threshold(final Float thresh)
	{
		final float fthresh = thresh;
		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				if (this.pixels[r][c] <= fthresh)
					this.pixels[r][c] = 0;
				else
					this.pixels[r][c] = 1;
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#toByteImage()
	 */
	@Override
	public byte[] toByteImage()
	{
		final byte[] pgmData = new byte[this.height * this.width];

		for (int j = 0; j < this.height; j++)
		{
			for (int i = 0; i < this.width; i++)
			{
				int v = (int) (255.0f * this.pixels[j][i]);

				v = Math.max(0, Math.min(255, v));

				pgmData[i + j * this.width] = (byte) (v & 0xFF);
			}
		}
		return pgmData;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#toPackedARGBPixels()
	 */
	@Override
	public int[] toPackedARGBPixels()
	{
		final int[] bimg = new int[this.width * this.height];

		for (int r = 0; r < this.height; r++) {
			for (int c = 0; c < this.width; c++) {
				final int v = (Math.max(0, Math.min(255, (int) (this.pixels[r][c] * 255))));

				final int rgb = 0xff << 24 | v << 16 | v << 8 | v;
				bimg[c + this.width * r] = rgb;
			}
		}

		return bimg;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String imageString = "";
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				imageString += String.format("%+.3f ", this.pixels[y][x]);
				if (x == 16) {
					if (this.width - 16 <= x)
						continue;
					imageString += "... ";
					x = this.width - 16;
				}
			}
			imageString += "\n";
			if (y == 16) {
				if (this.height - 16 <= y)
					continue;
				y = this.height - 16;
				imageString += "... \n";
			}

		}
		return imageString;
	}

	/**
	 * Returns a string representation of every pixel in this image using the
	 * format string (see {@link String#format(String, Object...)}) to format
	 * each pixel value.
	 *
	 * @param format
	 *            The format string to use for each pixel output
	 * @return A string representation of the image
	 * @see String#format(String, Object...)
	 */
	public String toString(final String format) {
		String imageString = "";
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				imageString += String.format(format, this.pixels[y][x]);
			}
			imageString += "\n";
		}
		return imageString;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#transform(Jama.Matrix)
	 */
	@Override
	public FImage transform(final Matrix transform) {
		return super.transform(transform);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#zero()
	 */
	@Override
	public FImage zero()
	{
		for (int r = 0; r < this.height; r++)
		{
			for (int c = 0; c < this.width; c++)
			{
				this.pixels[r][c] = 0;
			}
		}
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof FImage)) {
			return false;
		}
		return this.equalsThresh((FImage) o, 0);
	}

	/**
	 * Compare this image against another using a threshold on the absolute
	 * difference between pixel values in order to determine equality.
	 *
	 * @param o
	 *            the image to compare against
	 * @param thresh
	 *            the threshold for determining equality
	 * @return true images are the same size and if all pixel values have a
	 *         difference less than threshold; false otherwise.
	 */
	public boolean equalsThresh(final FImage o, final float thresh) {
		final FImage that = o;
		if (that.height != this.height || that.width != this.width)
			return false;
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				if (Math.abs(that.pixels[i][j] - this.pixels[i][j]) > thresh) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get the value of the pixel at coordinate p
	 *
	 * @param p
	 *            The coordinate to get
	 *
	 * @return The pixel value at (x, y)
	 */
	public float getPixelNative(final Pixel p) {
		return this.getPixelNative(p.x, p.y);
	}

	/**
	 * Get the value of the pixel at coordinate <code>(x, y)</code>.
	 *
	 * @param x
	 *            The x-coordinate to get
	 * @param y
	 *            The y-coordinate to get
	 *
	 * @return The pixel value at (x, y)
	 */
	public float getPixelNative(final int x, final int y) {
		return this.pixels[y][x];
	}

	/**
	 * Returns the pixels in this image as a vector (an array of the pixel
	 * type).
	 *
	 * @param f
	 *            The array into which to place the data
	 * @return The pixels in the image as a vector (a reference to the given
	 *         array).
	 */
	public float[] getPixelVectorNative(final float[] f)
	{
		for (int y = 0; y < this.getHeight(); y++)
			for (int x = 0; x < this.getWidth(); x++)
				f[x + y * this.getWidth()] = this.pixels[y][x];

		return f;
	}

	/**
	 * Sets the pixel at <code>(x,y)</code> to the given value. Side-affects
	 * this image.
	 *
	 * @param x
	 *            The x-coordinate of the pixel to set
	 * @param y
	 *            The y-coordinate of the pixel to set
	 * @param val
	 *            The value to set the pixel to.
	 */
	public void setPixelNative(final int x, final int y, final float val) {
		this.pixels[y][x] = val;
	}

	/**
	 * Convenience method to initialise an array of FImages
	 *
	 * @param num
	 *            array length
	 * @param width
	 *            width of images
	 * @param height
	 *            height of images
	 * @return array of newly initialised images
	 */
	public static FImage[] createArray(final int num, final int width, final int height) {
		final FImage[] array = new FImage[num];

		for (int i = 0; i < num; i++) {
			array[i] = new FImage(width, height);
		}

		return array;
	}

	/**
	 * @return The sum of all the pixels in the image
	 */
	public float sum() {
		float sum = 0;
		for (final float[] row : this.pixels) {
			for (int i = 0; i < row.length; i++) {
				sum += row[i];
			}
		}
		return sum;
	}

	/**
	 * Convert this {@link FImage} to an RGB {@link MBFImage}.
	 *
	 * @return a new RGB colour image.
	 */
	public MBFImage toRGB() {
		return new MBFImage(ColourSpace.RGB, this.clone(), this.clone(), this.clone());
	}

	@Override
	public FImage flipX() {
		final int hwidth = this.width / 2;

		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < hwidth; x++) {
				final int xx = this.width - x - 1;

				final float tmp = this.pixels[y][x];

				this.pixels[y][x] = this.pixels[y][xx];
				this.pixels[y][xx] = tmp;
			}
		}
		return this;
	}

	@Override
	public FImage flipY() {
		final int hheight = this.height / 2;

		for (int y = 0; y < hheight; y++) {
			final int yy = this.height - y - 1;

			for (int x = 0; x < this.width; x++) {
				final float tmp = this.pixels[y][x];

				this.pixels[y][x] = this.pixels[yy][x];
				this.pixels[yy][x] = tmp;
			}
		}

		return this;
	}

	/**
	 * Overlay the given image on this image with the given alpha channel at the
	 * given location.
	 *
	 * @param img
	 *            The image to overlay
	 * @param alpha
	 *            The alpha channel to use
	 * @param x
	 *            The location to draw the image
	 * @param y
	 *            The location to draw the image
	 * @return This image with the overlay on it
	 */
	public FImage overlayInplace(final FImage img, final FImage alpha, final int x, final int y)
	{
		final int sx = Math.max(x, 0);
		final int sy = Math.max(y, 0);
		final int ex = Math.min(this.width, x + img.getWidth());
		final int ey = Math.min(this.height, y + img.getHeight());

		for (int yc = sy; yc < ey; yc++)
		{
			for (int xc = sx; xc < ex; xc++)
			{
				final float a = alpha.pixels[yc - sy][xc - sx];
				this.pixels[yc][xc] = (a * img.pixels[yc - sy][xc - sx] +
						(1 - a) * this.pixels[yc][xc]);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method will overlay the given image at the given location with full
	 * opacity.
	 *
	 * @see org.openimaj.image.Image#overlayInplace(org.openimaj.image.Image,
	 *      int, int)
	 */
	@Override
	public FImage overlayInplace(final FImage image, final int x, final int y)
	{
		return this.overlayInplace(image, this.clone().fill(1f), x, y);
	}

	/**
	 * Create a random image of the given size.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the image
	 */
	public static FImage randomImage(final int width, final int height) {
		final FImage img = new FImage(width, height);

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				img.pixels[y][x] = (float) Math.random();

		return img;
	}

	@Override
	public FImage replace(Float target, Float replacement) {
		return replace((float) target, (float) replacement);
	}

	/**
	 * Replace pixels of a certain colour with another colour. Side-affects this
	 * image.
	 *
	 * @param target
	 *            the colour to fill the image with
	 * @param replacement
	 *            the colour to fill the image with
	 * @return A reference to this image.
	 */
	public FImage replace(float target, float replacement) {
		for (int r = 0; r < this.height; r++)
			for (int c = 0; c < this.width; c++)
				if (this.pixels[r][c] == target)
					this.pixels[r][c] = replacement;

		return this;
	}

	@Override
	public FImage extractCentreSubPix(float cx, float cy, FImage out) {
		final int width = out.width;
		final int height = out.height;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float ix = (float) (x + cx - (width - 1) * 0.5);
				final float iy = (float) (y + cy - (height - 1) * 0.5);
				out.pixels[y][x] = this.getPixelInterpNative(ix, iy, 0f);
			}
		}
		return out;
	}
}
