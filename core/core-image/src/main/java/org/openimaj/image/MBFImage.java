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

import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;

/**
 * A multiband floating-point image.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MBFImage extends MultiBandImage<Float, MBFImage, FImage> {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct an empty MBFImage with a the default RGB colourspace
	 */
	public MBFImage() {
		super(ColourSpace.RGB);
	}

	/**
	 * Construct an MBFImage from single band images. The given images are used
	 * directly as the bands and are not cloned.
	 *
	 * @param colourSpace
	 *            the colourspace
	 * @param images
	 *            the bands
	 */
	public MBFImage(final ColourSpace colourSpace, final FImage... images) {
		super(colourSpace, images);
	}

	/**
	 * Construct an MBFImage from single band images with the default RGB
	 * colourspace if there are three images, RGBA if there are 4 images, or
	 * CUSTOM otherwise. The given images are used directly as the bands and are
	 * not cloned; if you want to create an RGB {@link MBFImage} from a single
	 * {@link FImage}, you would need to clone the {@link FImage} at least
	 * twice.
	 *
	 * @param images
	 *            the bands
	 */
	public MBFImage(final FImage... images) {
		super(images.length == 3 ? ColourSpace.RGB : images.length == 4 ? ColourSpace.RGBA : ColourSpace.CUSTOM, images);
	}

	/**
	 * Construct an empty RGB image (3 bands)
	 *
	 * @param width
	 *            Width of image
	 * @param height
	 *            Height of image
	 */
	public MBFImage(final int width, final int height) {
		this(width, height, ColourSpace.RGB);
	}

	/**
	 * Construct an empty image
	 *
	 * @param width
	 *            Width of image
	 * @param height
	 *            Height of image
	 * @param colourSpace
	 *            the colourspace
	 */
	public MBFImage(final int width, final int height, final ColourSpace colourSpace) {
		this.colourSpace = colourSpace;

		for (int i = 0; i < colourSpace.getNumBands(); i++) {
			this.bands.add(new FImage(width, height));
		}
	}

	/**
	 * Construct an empty image. If the number of bands is 3, RGB is assumed, if
	 * the number is 4, then RGBA is assumed, otherwise the colourspace is set
	 * to CUSTOM.
	 *
	 * @param width
	 *            Width of image
	 * @param height
	 *            Height of image
	 * @param nbands
	 *            number of bands
	 */
	public MBFImage(final int width, final int height, final int nbands) {
		if (nbands == 3)
			this.colourSpace = ColourSpace.RGB;
		else if (nbands == 4)
			this.colourSpace = ColourSpace.RGBA;

		for (int i = 0; i < nbands; i++) {
			this.bands.add(new FImage(width, height));
		}
	}

	/**
	 * Create an image from a BufferedImage object. Resultant image have RGB
	 * bands in the 0-1 range.
	 *
	 * @param data
	 *            array of packed ARGB pixels
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 */
	public MBFImage(final int[] data, final int width, final int height) {
		this(data, width, height, false);
	}

	/**
	 * Create an image from a int[] object. Resultant image will be in the 0-1
	 * range. If alpha is true, bands will be RGBA, otherwise RGB
	 *
	 * @param data
	 *            array of packed ARGB pixels
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 * @param alpha
	 *            should we load the alpha channel
	 */
	public MBFImage(final int[] data, final int width, final int height, final boolean alpha) {
		this(width, height, alpha ? 4 : 3);
		this.internalAssign(data, width, height);
	}

	/**
	 * Create an MBFImage from an array of double values with the given width
	 * and height. The length of the array must equal the width multiplied by
	 * the height by the number of bands. The values will be downcast to floats.
	 * The data can either be interlaced (rgbrgb...) or band at a time (rrrr...
	 * gggg... bbbb...).
	 *
	 * @param ds
	 *            An array of floating point values.
	 * @param width
	 *            The width of the resulting image.
	 * @param height
	 *            The height of the resulting image.
	 * @param nbands
	 *            the number of bands
	 * @param interlaced
	 *            if true the data in the array is interlaced
	 */
	public MBFImage(double[] ds, int width, int height, int nbands, boolean interlaced) {
		if (interlaced) {
			for (int i = 0; i < nbands; i++) {
				bands.add(new FImage(width, height));
			}
			for (int y = 0, c = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					for (int i = 0; i < nbands; i++, c++) {
						bands.get(i).pixels[y][x] = (float) ds[c];
					}
				}
			}
		} else {
			for (int i = 0; i < nbands; i++) {
				bands.add(new FImage(ds, width, height, i * width * height));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.soton.ecs.jsh2.image.MultiBandImage#flattenMax()
	 */
	@Override
	public FImage flattenMax() {
		final int width = this.getWidth();
		final int height = this.getHeight();

		final FImage out = new FImage(width, height);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float max = (this.bands.get(0)).pixels[y][x];

				for (int i = 1; i < this.numBands(); i++)
					if (max > (this.bands.get(i)).pixels[y][x])
						max = (this.bands.get(i)).pixels[y][x];

				out.pixels[y][x] = max;
			}
		}

		return out;
	}

	@Override
	public FImage flatten() {
		// overly optimised flatten

		final int width = this.getWidth();
		final int height = this.getHeight();

		final FImage out = new FImage(width, height);
		final float[][] outp = out.pixels;
		final int nb = this.numBands();

		for (int i = 1; i < nb; i++) {
			final float[][] bnd = this.bands.get(i).pixels;

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					outp[y][x] += bnd[y][x];

				}
			}
		}

		final float norm = 1f / nb;
		final float[][] bnd = this.bands.get(0).pixels;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				outp[y][x] = (outp[y][x] + bnd[y][x]) * norm;
			}
		}

		return out;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.soton.ecs.jsh2.image.Image#getPixel(int, int)
	 */
	@Override
	public Float[] getPixel(final int x, final int y) {
		final Float[] pixels = new Float[this.bands.size()];

		for (int i = 0; i < this.bands.size(); i++) {
			pixels[i] = this.bands.get(i).getPixel(x, y);
		}

		return pixels;
	}

	@Override
	public Comparator<? super Float[]> getPixelComparator() {
		return new Comparator<Float[]>() {

			@Override
			public int compare(final Float[] o1, final Float[] o2) {
				int sumDiff = 0;
				boolean anyDiff = false;
				for (int i = 0; i < o1.length; i++) {
					sumDiff += o1[i] - o2[i];
					anyDiff = sumDiff != 0 || anyDiff;
				}
				if (anyDiff) {
					if (sumDiff > 0)
						return 1;
					else
						return -1;
				} else
					return 0;
			}

		};
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.soton.ecs.jsh2.image.Image#getPixelInterp(double, double)
	 */
	@Override
	public Float[] getPixelInterp(final double x, final double y) {
		final Float[] result = new Float[this.bands.size()];

		for (int i = 0; i < this.bands.size(); i++) {
			result[i] = this.bands.get(i).getPixelInterp(x, y);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.soton.ecs.jsh2.image.Image#getPixelInterp(double,
	 * double,Float[])
	 */
	@Override
	public Float[] getPixelInterp(final double x, final double y, final Float[] b) {
		final Float[] result = new Float[this.bands.size()];

		for (int i = 0; i < this.bands.size(); i++) {
			result[i] = this.bands.get(i).getPixelInterp(x, y, b[i]);
		}

		return result;
	}

	/**
	 * Assign planar RGB bytes (R1G1B1R2G2B2...) to this image.
	 *
	 * @param bytes
	 *            the byte array
	 * @param width
	 *            the width of the byte image
	 * @param height
	 *            the height of the byte image
	 * @return this
	 */
	public MBFImage internalAssign(final byte[] bytes, final int width, final int height) {
		if (this.getWidth() != width || this.getHeight() != height)
			this.internalAssign(this.newInstance(width, height));

		final float[][] br = this.bands.get(0).pixels;
		final float[][] bg = this.bands.get(1).pixels;
		final float[][] bb = this.bands.get(2).pixels;

		for (int i = 0, y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int blue = bytes[i++] & 0xff;
				final int green = (bytes[i++]) & 0xff;
				final int red = (bytes[i++]) & 0xff;
				br[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[red];
				bg[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[green];
				bb[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[blue];
			}
		}

		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openimaj.image.Image#internalAssign(int[], int, int)
	 */
	@Override
	public MBFImage internalAssign(final int[] data, final int width, final int height) {
		if (this.getWidth() != width || this.getHeight() != height)
			this.internalAssign(this.newInstance(width, height));

		final float[][] br = this.bands.get(0).pixels;
		final float[][] bg = this.bands.get(1).pixels;
		final float[][] bb = this.bands.get(2).pixels;
		float[][] ba = null;

		if (this.colourSpace == ColourSpace.RGBA)
			ba = this.bands.get(3).pixels;

		for (int i = 0, y = 0; y < height; y++) {
			for (int x = 0; x < width; x++, i++) {
				final int rgb = data[i];
				final int alpha = ((rgb >> 24) & 0xff);
				final int red = ((rgb >> 16) & 0xff);
				final int green = ((rgb >> 8) & 0xff);
				final int blue = ((rgb) & 0xff);
				br[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[red];
				bg[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[green];
				bb[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[blue];

				if (ba != null)
					ba[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[alpha];
			}
		}

		return this;
	}

	@Override
	protected Float intToT(final int n) {
		return (float) n;
	}

	@Override
	public FImage newBandInstance(final int width, final int height) {
		return new FImage(width, height);
	}

	@Override
	public MBFImage newInstance() {
		return new MBFImage();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.soton.ecs.jsh2.image.MultiBandImage#newInstance(int, int)
	 */
	@Override
	public MBFImage newInstance(final int width, final int height) {
		final MBFImage ret = new MBFImage(width, height, this.bands.size());

		ret.colourSpace = this.colourSpace;

		return ret;
	}

	@Override
	public MBFImageRenderer createRenderer() {
		return new MBFImageRenderer(this);
	}

	@Override
	public MBFImageRenderer createRenderer(final RenderHints options) {
		return new MBFImageRenderer(this, options);
	}

	/**
	 * Get the value of the pixel at coordinate p
	 *
	 * @param p
	 *            The coordinate to get
	 *
	 * @return The pixel value at (x, y)
	 */
	public float[] getPixelNative(final Pixel p) {
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
	public float[] getPixelNative(final int x, final int y) {
		final float[] pixels = new float[this.bands.size()];

		for (int i = 0; i < this.bands.size(); i++) {
			pixels[i] = this.bands.get(i).getPixel(x, y);
		}

		return pixels;
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
	public float[][] getPixelVectorNative(final float[][] f) {
		for (int y = 0; y < this.getHeight(); y++)
			for (int x = 0; x < this.getWidth(); x++)
				f[x + y * this.getWidth()] = this.getPixelNative(x, y);

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
	public void setPixelNative(final int x, final int y, final float[] val) {
		final int np = this.bands.size();
		if (np == val.length)
			for (int i = 0; i < np; i++)
				this.bands.get(i).setPixel(x, y, val[i]);
		else {
			final int offset = val.length - np;
			for (int i = 0; i < np; i++)
				if (i + offset >= 0)
					this.bands.get(i).setPixel(x, y, val[i + offset]);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method assumes the last band in the multiband image is the alpha
	 * channel. This allows a 2-channel MBFImage where the first image is an
	 * FImage and the second an alpha channel, as well as a standard RGBA image.
	 *
	 * @see org.openimaj.image.Image#overlayInplace(org.openimaj.image.Image,
	 *      int, int)
	 */
	@Override
	public MBFImage overlayInplace(final MBFImage image, final int x, final int y) {
		// Assume the alpha channel is the last band
		final FImage alpha = image.getBand(image.numBands() - 1);

		for (int i = 0; i < this.numBands(); i++)
			this.bands.get(i).overlayInplace(image.bands.get(i), alpha, x, y);

		return this;
	}

	/**
	 * Create a random RGB image.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the image
	 */
	public static MBFImage randomImage(final int width, final int height) {
		final MBFImage img = new MBFImage();
		img.colourSpace = ColourSpace.RGB;

		for (int i = 0; i < 3; i++) {
			img.bands.add(FImage.randomImage(width, height));
		}

		return img;
	}

	/**
	 * Convenience method to create an RGB {@link MBFImage} from an
	 * {@link FImage} by cloning the {@link FImage} for each of the R, G and B
	 * bands.
	 *
	 * @param image
	 *            the {@link FImage} to convert
	 * @return the new RGB {@link MBFImage}
	 */
	public static MBFImage createRGB(final FImage image) {
		return new MBFImage(image.clone(), image.clone(), image.clone());
	}

	@Override
	public MBFImage fill(final Float[] colour)
	{
		return super.fill(this.colourSpace.sanitise(colour));
	}

	@Override
	public void setPixel(final int x, final int y, final Float[] val)
	{
		// Check if we have an alpha channel. If we do, we'll use alpha
		// compositing, otherwise, we'll simply copy the pixel colour into
		// the pixel position.
		if (this.colourSpace == ColourSpace.RGBA && this.numBands() >= 4 && val.length >= 4
				&& x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight())
		{
			final float[] p = ImageUtilities.alphaCompositePixel(this.getPixel(x, y), val);
			this.getBand(0).pixels[y][x] = p[0];
			this.getBand(1).pixels[y][x] = p[1];
			this.getBand(2).pixels[y][x] = p[2];
			if (this.numBands() >= 4)
				this.getBand(3).pixels[y][x] = p[3];
		}
		else
			super.setPixel(x, y, val);
	}

	@Override
	protected Float[] createPixelArray(int n) {
		return new Float[n];
	}

	/**
	 * Returns the pixels of the image as a vector (array) of doubles.
	 *
	 * @return the pixels of the image as a vector (array) of doubles.
	 */
	public double[] getDoublePixelVector()
	{
		final int height = getHeight();
		final int width = getWidth();
		final double f[] = new double[width * height * this.numBands()];
		for (int i = 0; i < this.bands.size(); i++) {
			final float[][] pixels = bands.get(i).pixels;

			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++)
					f[x + y * width + i * height * width] = pixels[y][x];
		}
		return f;
	}
}
