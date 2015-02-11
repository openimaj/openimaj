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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.processor.SinglebandKernelProcessor;
import org.openimaj.image.processor.SinglebandPixelProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A base class for multi-band images.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            The pixel type
 * @param <I>
 *            The concrete subclass type
 * @param <S>
 *            The concrete subclass type of each band
 */
public abstract class MultiBandImage<T extends Comparable<T>, I extends MultiBandImage<T, I, S>, S extends SingleBandImage<T, S>>
extends
Image<T[], I>
implements
Iterable<S>,
SinglebandImageProcessor.Processable<T, S, I>,
SinglebandKernelProcessor.Processable<T, S, I>

{
	private static final long serialVersionUID = 1L;

	/** The images for each band in a list */
	public List<S> bands;

	/** The colour-space of this image */
	public ColourSpace colourSpace = ColourSpace.CUSTOM;

	/**
	 * Default constructor for a multiband image.
	 */
	public MultiBandImage() {
		this.bands = new ArrayList<S>();
	}

	/**
	 * Default constructor for a multiband image.
	 *
	 * @param colourSpace
	 *            the colour space
	 */
	public MultiBandImage(final ColourSpace colourSpace) {
		this();
		this.colourSpace = colourSpace;
	}

	/**
	 * Construct a multiband image using each of the given images as the bands
	 * (in order).
	 *
	 * @param colourSpace
	 *            the colour space
	 * @param images
	 *            A set of images to use as the bands in the image.
	 */
	@SafeVarargs
	public MultiBandImage(final ColourSpace colourSpace, final S... images) {
		this(colourSpace);

		if (!ImageUtilities.checkSameSize(images)) {
			throw new IllegalArgumentException("images are not the same size");
		}

		this.bands.addAll(Arrays.asList(images));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#abs()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I abs() {
		for (final S i : this.bands)
			i.abs();
		return (I) this;
	}

	/**
	 * Add the given scalar to each pixel of each band and return result as a
	 * new image.
	 *
	 * @param num
	 *            The value to add to each pixel in every band.
	 * @return A new image containing the result.
	 */
	public I add(final T num) {
		final I newImage = this.clone();
		newImage.add(num);
		return newImage;
	}

	/**
	 * Adds a new band image to the multiband image. The given image must be the
	 * same size as the images already in this image.
	 *
	 * @param img
	 *            The image to add as a new band.
	 */
	public void addBand(final S img) {
		if (this.bands.size() > 0) {
			if (!ImageUtilities.checkSize(this.getHeight(), this.getWidth(), img)) {
				throw new IllegalArgumentException("images are not the same size");
			}
		}
		this.bands.add(img);
	}

	/**
	 * {@inheritDoc} The input image must be a {@link MultiBandImage} or a
	 * {@link SingleBandImage}.
	 *
	 * @see org.openimaj.image.Image#addInplace(org.openimaj.image.Image)
	 * @throws UnsupportedOperationException
	 *             if the given image is neither a {@link MultiBandImage} nor a
	 *             {@link SingleBandImage}.
	 */
	@Override
	public I addInplace(final Image<?, ?> im) {
		if (im instanceof MultiBandImage<?, ?, ?>) {
			return this.addInplace((MultiBandImage<?, ?, ?>) im);
		} else if (im instanceof SingleBandImage<?, ?>) {
			return this.addInplace((SingleBandImage<?, ?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 * Adds to each pixel the value of the corresponding pixel in the
	 * corresponding band in the given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to add to this image.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I addInplace(final MultiBandImage<?, ?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).addInplace(((MultiBandImage<?, ?, ?>) im).bands.get(i));

		return (I) this;
	}

	/**
	 * Adds to each pixel (in all bandS) the value of corresponding pixel in the
	 * given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to add to this image.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I addInplace(final SingleBandImage<?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).addInplace(im);

		return (I) this;
	}

	/**
	 * Add the given value to each pixel in every band. Side-affects this image.
	 *
	 * @param num
	 *            The value to add to each pixel
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I addInplace(final T num) {
		for (final S sbi : this) {
			sbi.addInplace(num);
		}

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#addInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I addInplace(final T[] num) {
		final int np = this.bands.size();

		assert (num.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).addInplace(num[i]);

		return (I) this;
	}

	/**
	 * Sets any pixels that are below min to zero or above max to the highest
	 * normal value that the image allows (usually 1 for floating-point images).
	 * This method may side-affect this image.
	 *
	 * @param min
	 *            The minimum value to clip to
	 * @param max
	 *            The maximum value to clip to.
	 * @return this
	 * @see Image#clip(Object, Object)
	 */
	@SuppressWarnings("unchecked")
	public I clip(final T min, final T max) {
		for (final S sbi : this) {
			sbi.clip(min, max);
		}

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#clip(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I clip(final T[] min, final T[] max) {
		final int np = this.bands.size();

		assert (min.length == np);
		assert (max.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).clip(min[i], max[i]);

		return (I) this;
	}

	/**
	 * For all bands, sets any values above the given threshold to zero.
	 * Side-affects this image.
	 *
	 * @param thresh
	 *            The threshold above which values are clipped
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I clipMax(final T thresh) {
		for (final S sbm : this)
			sbm.clipMax(thresh);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#clipMax(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I clipMax(final T[] thresh) {
		final int np = this.bands.size();

		assert (thresh.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).clipMax(thresh[i]);

		return (I) this;
	}

	/**
	 * Sets all pixels in all bands that have a value below the given threshold
	 * to zero. Side-affects this image.
	 *
	 * @param thresh
	 *            The threshold below which pixels will be set to zero.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I clipMin(final T thresh) {
		for (final S sbm : this)
			sbm.clipMin(thresh);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#clipMin(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I clipMin(final T[] thresh) {
		final int np = this.bands.size();

		assert (thresh.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).clipMin(thresh[i]);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#clone()
	 */
	@Override
	public I clone() {
		final I newImage = this.newInstance();

		for (final S sbi : this) {
			newImage.bands.add(sbi.clone());
		}
		newImage.colourSpace = this.colourSpace;

		return newImage;
	}

	/**
	 * Delete the band at the given index.
	 *
	 * @param index
	 *            The index of the band to remove.
	 */
	public void deleteBand(final int index) {
		this.bands.remove(index);
	}

	/**
	 * Divides all pixels of each band by the given value and returns result as
	 * a new image.
	 *
	 * @param val
	 *            The value to divide every pixel by.
	 * @return A new image containing the result.
	 */
	public I divide(final T val) {
		final I newImage = this.clone();
		newImage.divide(val);
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#divideInplace(org.openimaj.image.Image)
	 */
	@Override
	public I divideInplace(final Image<?, ?> im) {
		if (im instanceof MultiBandImage<?, ?, ?>) {
			return this.divideInplace((MultiBandImage<?, ?, ?>) im);
		} else if (im instanceof SingleBandImage<?, ?>) {
			return this.divideInplace((SingleBandImage<?, ?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 * Divides the pixels in every band of this image by the corresponding pixel
	 * in the corresponding band of the given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to divide into this image.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I divideInplace(final MultiBandImage<?, ?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).divideInplace(((MultiBandImage<?, ?, ?>) im).bands.get(i));

		return (I) this;
	}

	/**
	 * Divides the pixels in every band of this image by the corresponding pixel
	 * in the given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to divide into this image.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I divideInplace(final SingleBandImage<?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).divideInplace(im);

		return (I) this;
	}

	/**
	 * Divide all pixels of every band by the given value. Side-affects this
	 * image.
	 *
	 * @param val
	 *            The value to divide by
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I divideInplace(final T val) {
		for (final S sbm : this) {
			sbm.divideInplace(val);
		}
		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#divideInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I divideInplace(final T[] val) {
		final int np = this.bands.size();

		assert (val.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).divideInplace(val[i]);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#extractROI(int, int,
	 *      org.openimaj.image.Image)
	 */
	@Override
	public I extractROI(final int x, final int y, final I out) {
		for (int i = 0; i < this.bands.size(); i++) {
			final S img = this.bands.get(i);
			img.extractROI(x, y, out.bands.get(i));
		}

		return out;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#extractROI(int, int, int, int)
	 */
	@Override
	public I extractROI(final int x, final int y, final int w, final int h) {
		final I newImage = this.newInstance();

		for (final S sbm : this) {
			newImage.addBand(sbm.extractROI(x, y, w, h));
		}
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#fill(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I fill(final T[] colour) {
		for (int b = 0; b < this.bands.size(); b++)
			this.bands.get(b).fill(colour[b]);
		return (I) this;
	}

	/**
	 * Flatten the bands into a single band using the average value of the
	 * pixels at each location.
	 *
	 * @return A new single-band image containing the result.
	 */
	public S flatten() {
		if (this.bands.size() == 1)
			return bands.get(0).clone();

		final S out = this.newBandInstance(this.getWidth(), this.getHeight());

		for (final S sbm : this)
			out.addInplace(sbm);

		return out.divideInplace(this.intToT(this.numBands()));
	}

	/**
	 * Flatten the bands into a single band by selecting the maximum value pixel
	 * from each band.
	 *
	 * @return A new flattened image
	 */
	public abstract S flattenMax();

	/**
	 * Get the band at index i.
	 *
	 * @param i
	 *            the index
	 * @return the specified colour band
	 */
	public S getBand(final int i) {
		return this.bands.get(i);
	}

	/**
	 * Get the colour space of this image
	 *
	 * @return the colour space
	 */
	public ColourSpace getColourSpace() {
		return this.colourSpace;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getContentArea()
	 */
	@Override
	public Rectangle getContentArea() {
		int minx = this.getWidth(), maxx = 0, miny = this.getHeight(), maxy = 0;
		for (int i = 0; i < this.numBands(); i++) {
			final Rectangle box = this.getBand(i).getContentArea();
			if (box.minX() < minx)
				minx = (int) box.minX();
			if (box.maxX() > maxx)
				maxx = (int) box.maxX();
			if (box.minY() < miny)
				miny = (int) box.minY();
			if (box.maxY() > maxy)
				maxy = (int) box.maxY();
		}

		return new Rectangle(minx, miny, maxx - minx, maxy - miny);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getField(org.openimaj.image.Image.Field)
	 */
	@Override
	public I getField(final Field f) {
		final I newImage = this.newInstance();

		for (final S sbm : this) {
			newImage.bands.add(sbm.getField(f));
		}
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getFieldCopy(org.openimaj.image.Image.Field)
	 */
	@Override
	public I getFieldCopy(final Field f) {
		final I newImage = this.newInstance();

		for (final S sbm : this) {
			newImage.bands.add(sbm.getFieldCopy(f));
		}
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getFieldInterpolate(org.openimaj.image.Image.Field)
	 */
	@Override
	public I getFieldInterpolate(final Field f) {
		final I newImage = this.newInstance();

		for (final S sbm : this) {
			newImage.bands.add(sbm.getFieldInterpolate(f));
		}

		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getHeight()
	 */
	@Override
	public int getHeight() {
		if (this.bands.size() > 0)
			return this.bands.get(0).getHeight();
		return 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#getWidth()
	 */
	@Override
	public int getWidth() {
		if (this.bands.size() > 0)
			return this.bands.get(0).getWidth();
		return 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I internalCopy(final I im)
	{
		final int nb = this.bands.size();
		for (int i = 0; i < nb; i++)
			this.bands.get(i).internalCopy(im.getBand(i));

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I internalAssign(final I im) {
		this.bands = im.bands;
		return (I) this;
	}

	/**
	 * Converts the given integer to a value that can be used as a pixel value.
	 *
	 * @param n
	 *            The integer to convert.
	 * @return A value that can be used as a pixel value.
	 */
	protected abstract T intToT(int n);

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#inverse()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I inverse() {
		for (final S sbm : this) {
			sbm.inverse();
		}
		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<S> iterator() {
		return this.bands.iterator();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#max()
	 */
	@Override
	public T[] max() {
		final List<T> pixels = new ArrayList<T>();

		for (final S sbm : this) {
			pixels.add(sbm.max());
		}

		return pixels.toArray(createPixelArray(this.numBands()));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#min()
	 */
	@Override
	public T[] min() {
		final List<T> pixels = new ArrayList<T>();

		for (final S sbm : this) {
			pixels.add(sbm.min());
		}

		return pixels.toArray(createPixelArray(this.numBands()));
	}

	/**
	 * Create an array of n pixels
	 *
	 * @param n
	 *            number of pixels
	 * @return the array
	 */
	protected abstract T[] createPixelArray(int n);

	/**
	 * Multiplies each pixel of every band by the given value and returns the
	 * result as a new image.
	 *
	 * @param num
	 *            The value to multiply by.
	 * @return A new image containing the result.
	 */
	public I multiply(final T num) {
		final I newImage = this.clone();
		newImage.multiplyInplace(num);
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#multiplyInplace(org.openimaj.image.Image)
	 */
	@Override
	public I multiplyInplace(final Image<?, ?> im) {
		if (im instanceof MultiBandImage<?, ?, ?>) {
			return this.multiplyInplace((MultiBandImage<?, ?, ?>) im);
		} else if (im instanceof SingleBandImage<?, ?>) {
			return this.multiplyInplace((SingleBandImage<?, ?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 * Multiplies every pixel in this image by the corresponding pixel in the
	 * corresponding band in the given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to multiply with this image.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I multiplyInplace(final MultiBandImage<?, ?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).multiplyInplace(((MultiBandImage<?, ?, ?>) im).bands.get(i));

		return (I) this;
	}

	/**
	 * Multiplies every pixel in this image by the corresponding pixel in the
	 * given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to multiply with this image.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I multiplyInplace(final SingleBandImage<?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).multiplyInplace(im);

		return (I) this;
	}

	/**
	 * Multiplies each pixel of every band by the given value. Side-affects this
	 * image.
	 *
	 * @param num
	 *            The value to multiply this image by
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I multiplyInplace(final T num) {
		for (final S sbm : this)
			sbm.multiplyInplace(num);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#multiplyInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I multiplyInplace(final T[] num) {
		final int np = this.bands.size();

		assert (num.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).multiplyInplace(num[i]);

		return (I) this;
	}

	/**
	 * Returns a new instance of an image that represents each band.
	 *
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The height of the image
	 * @return A new {@link SingleBandImage} of the appropriate type.
	 */
	public abstract S newBandInstance(int width, int height);

	/**
	 * Returns a new instance of a this image type.
	 *
	 * @return A new {@link MBFImage} subclass type.
	 */
	public abstract I newInstance();

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#newInstance(int, int)
	 */
	@Override
	public abstract I newInstance(int width, int height);

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#normalise()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I normalise() {
		for (final S sbm : this)
			sbm.normalise();

		return (I) this;
	}

	/**
	 * Returns the number of bands in this image.
	 *
	 * @return the number of bands in this image.
	 */
	public int numBands() {
		return this.bands.size();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#process(org.openimaj.image.processor.SinglebandImageProcessor)
	 */
	@Override
	public I process(final SinglebandImageProcessor<T, S> p) {
		final I out = this.newInstance();
		for (final S sbm : this)
			out.bands.add(sbm.process(p));

		return out;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor)
	 */
	@Override
	public I process(final SinglebandKernelProcessor<T, S> kernel) {
		return this.process(kernel, false);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor,
	 *      boolean)
	 */
	@Override
	public I process(final SinglebandKernelProcessor<T, S> kernel, final boolean pad) {
		final I out = this.newInstance();
		for (final S sbm : this)
			out.bands.add(sbm.process(kernel, pad));

		return out;
	}

	/**
	 * Processes this image with the given {@link SinglebandImageProcessor} for
	 * every band.
	 *
	 * @param pp
	 *            The pixel process to apply to each band in turn.
	 * @return A new image containing the result.
	 */
	public I process(final SinglebandPixelProcessor<T> pp) {
		final I out = this.newInstance();
		for (final S sbm : this)
			out.bands.add(sbm.process(pp));

		return out;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandImageProcessor)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I processInplace(final SinglebandImageProcessor<T, S> p) {
		for (final S sbm : this)
			sbm.processInplace(p);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor)
	 */
	@Override
	public I processInplace(final SinglebandKernelProcessor<T, S> kernel) {
		return this.processInplace(kernel, false);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor,
	 *      boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I processInplace(final SinglebandKernelProcessor<T, S> kernel, final boolean pad) {
		for (final S sbm : this)
			sbm.processInplace(kernel, pad);

		return (I) this;
	}

	/**
	 * Process this image with the given {@link SinglebandImageProcessor} for
	 * every band. Side-affects this image.
	 *
	 * @param pp
	 *            The pixel processor to apply to each band in turn.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInplace(final SinglebandPixelProcessor<T> pp) {
		for (final S sbm : this)
			sbm.processInplace(pp);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#setPixel(int, int, java.lang.Object)
	 */
	@Override
	public void setPixel(final int x, final int y, final T[] val) {
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
	 * Subtracts the given value from every pixel in every band and returns the
	 * result as a new image.
	 *
	 * @param num
	 *            The value to subtract from this image.
	 * @return A new image containing the result.
	 */
	public I subtract(final T num) {
		final I newImage = this.clone();
		newImage.subtract(num);
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#subtractInplace(org.openimaj.image.Image)
	 */
	@Override
	public I subtractInplace(final Image<?, ?> im) {
		if (im instanceof MultiBandImage<?, ?, ?>) {
			return this.subtractInplace((MultiBandImage<?, ?, ?>) im);
		} else if (im instanceof SingleBandImage<?, ?>) {
			return this.subtractInplace((SingleBandImage<?, ?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 * Subtracts from every pixel in every band the corresponding pixel value in
	 * the corresponding band of the given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to subtract from this image
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I subtractInplace(final MultiBandImage<?, ?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).subtractInplace(((MultiBandImage<?, ?, ?>) im).bands.get(i));

		return (I) this;
	}

	/**
	 * Subtracts from every pixel in every band the corresponding pixel value in
	 * the given image. Side-affects this image.
	 *
	 * @param im
	 *            The image to subtract from this image.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I subtractInplace(final SingleBandImage<?, ?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		final int np = this.bands.size();

		for (int i = 0; i < np; i++)
			this.bands.get(i).subtractInplace(im);

		return (I) this;
	}

	/**
	 * Subtracts the given value from every pixel in every band. Side-affects
	 * this image.
	 *
	 * @param num
	 *            The value to subtract from this image
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I subtractInplace(final T num) {
		for (final S sbm : this)
			sbm.subtractInplace(num);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#subtractInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I subtractInplace(final T[] num) {
		final int np = this.bands.size();

		assert (num.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).subtractInplace(num[i]);

		return (I) this;
	}

	/**
	 * Sets the value of any pixel below the given threshold to zero and all
	 * others to 1 for all bands. Side-affects this image.
	 *
	 * @param thresh
	 *            The threshold above which pixels will be set to 1.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I threshold(final T thresh) {
		for (final S sbm : this)
			sbm.threshold(thresh);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#threshold(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I threshold(final T[] thresh) {
		final int np = this.bands.size();

		assert (thresh.length == np);

		for (int i = 0; i < np; i++)
			this.bands.get(i).threshold(thresh[i]);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#toByteImage()
	 */
	@Override
	public byte[] toByteImage() {
		final int width = this.getWidth();
		final int height = this.getHeight();
		final int nb = this.bands.size();

		final byte[] ppmData = new byte[nb * height * width];

		for (int n = 0; n < nb; n++) {
			final byte[] band = this.bands.get(n).toByteImage();

			for (int j = 0; j < height; j++) {
				for (int i = 0; i < width; i++) {
					ppmData[nb * (i + j * width) + n] = band[i + j * width];
				}
			}
		}
		return ppmData;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#toPackedARGBPixels()
	 */
	@Override
	public int[] toPackedARGBPixels() {
		// TODO: deal better with color spaces
		if (this.bands.size() == 1) {
			return this.bands.get(0).toPackedARGBPixels();
		} else if (this.bands.size() == 3) {
			final int width = this.getWidth();
			final int height = this.getHeight();

			final byte[] rp = this.bands.get(0).toByteImage();
			final byte[] gp = this.bands.get(1).toByteImage();
			final byte[] bp = this.bands.get(2).toByteImage();

			final int[] data = new int[height * width];

			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					final int red = rp[c + r * width] & 0xff;
					final int green = gp[c + r * width] & 0xff;
					final int blue = bp[c + r * width] & 0xff;

					final int rgb = 0xff << 24 | red << 16 | green << 8 | blue;
					data[c + r * width] = rgb;
				}
			}

			return data;
		} else if (this.bands.size() == 4) {
			final int width = this.getWidth();
			final int height = this.getHeight();

			final byte[] ap = this.bands.get(3).toByteImage();
			final byte[] rp = this.bands.get(0).toByteImage();
			final byte[] gp = this.bands.get(1).toByteImage();
			final byte[] bp = this.bands.get(2).toByteImage();

			final int[] data = new int[height * width];

			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					final int alpha = ap[c + r * width] & 0xff;
					final int red = rp[c + r * width] & 0xff;
					final int green = gp[c + r * width] & 0xff;
					final int blue = bp[c + r * width] & 0xff;

					final int argb = alpha << 24 | red << 16 | green << 8 | blue;
					data[c + r * width] = argb;
				}
			}

			return data;
		} else {
			throw new UnsupportedOperationException(
					"Unable to create bufferedImage with " + this.numBands() + " bands");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.image.Image#zero()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I zero() {
		for (final S sbm : this)
			sbm.zero();

		return (I) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public I shiftLeftInplace(final int count) {
		for (final S b : this.bands)
			b.shiftLeftInplace(count);
		return (I) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public I shiftRightInplace(final int count) {
		for (final S b : this.bands)
			b.shiftRightInplace(count);
		return (I) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public I flipX() {
		for (final S b : this.bands)
			b.flipX();

		return (I) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public I flipY() {
		for (final S b : this.bands)
			b.flipY();

		return (I) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(final Object other) {
		final I that = (I) other;
		if (this.bands.size() != that.bands.size())
			return false;
		int i = 0;
		for (final S b : this.bands)
		{
			final boolean fail = !b.equals(that.getBand(i));
			if (fail)
				return false;

			i++;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public I replace(final T[] target, final T[] replacement) {
		for (int b = 0; b < this.bands.size(); b++)
			this.bands.get(b).replace(target[b], replacement[b]);
		return (I) this;
	}

	@Override
	public I extractCentreSubPix(float cx, float cy, I out) {
		for (int b = 0; b < this.bands.size(); b++)
			this.bands.get(b).extractCentreSubPix(cx, cy, out.bands.get(b));
		return out;
	}
}
