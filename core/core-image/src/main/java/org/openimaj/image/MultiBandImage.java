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
 * 	A base class for multi-band images. 
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *  
 *  @param <T> The pixel type
 *  @param <I> The concrete subclass type
 *  @param <S> The concrete subclass type of each band
 */
public abstract class MultiBandImage<T extends Comparable<T>, 
									 I extends MultiBandImage<T,I,S>, 
									 S extends SingleBandImage<T,S>> 
	extends 
		Image<T[],I> 
	implements 
		Iterable<S>, 
		SinglebandImageProcessor.Processable<T,S,I>, 
		SinglebandKernelProcessor.Processable<T,S,I> 

{
	private static final long serialVersionUID = 1L;
	
	/** The images for each band in a list */
	public List<S> bands;

	/** The colour-space of this image */
	public ColourSpace colourSpace = ColourSpace.CUSTOM;
	
	/**
	 * 	Default constructor for a multiband image.
	 */
	public MultiBandImage() {
		bands = new ArrayList<S>();
	}

	/**
	 * 	Default constructor for a multiband image.
	 *  @param colourSpace the colour space 
	 */
	public MultiBandImage(ColourSpace colourSpace) {
		this();
		this.colourSpace = colourSpace;
	}
	
	/**
	 * 	Construct a multiband image using each of the given images
	 * 	as the bands (in order).
	 * 
	 * 	@param colourSpace the colour space 
	 *  @param images A set of images to use as the bands in the image.
	 */
	public MultiBandImage(ColourSpace colourSpace, S... images) {
		this(colourSpace);

		if (!ImageUtilities.checkSameSize(images)) {
			throw new IllegalArgumentException("images are not the same size");
		}

		bands.addAll(Arrays.asList(images));
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#abs()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I abs() {
		for (S i : bands) i.abs();
		return (I) this;
	}

	/**
	 * Add the given scalar to each pixel of each band and 
	 * return result as a new image.
	 * 
	 * @param num The value to add to each pixel in every band.
	 * @return A new image containing the result.
	 */
	public I add(T num) {
		I newImage = this.clone();
		newImage.add(num);
		return newImage;
	}

	/**
	 * 	Adds a new band image to the multiband image. The given image
	 * 	must be the same size as the images already in this image.
	 * 
	 *  @param img The image to add as a new band.
	 */
	public void addBand(S img) {
		if (bands.size() > 0) {
			if (!ImageUtilities.checkSize(getHeight(), getWidth(), img)) {
				throw new IllegalArgumentException("images are not the same size");
			}
		}
		bands.add(img);
	}

	/**
	 *  {@inheritDoc}
	 *  The input image must be a {@link MultiBandImage} or a {@link SingleBandImage}. 
	 *  @see org.openimaj.image.Image#addInplace(org.openimaj.image.Image)
	 *  @throws UnsupportedOperationException if the given image is neither a
	 *  	{@link MultiBandImage} nor a {@link SingleBandImage}.
	 */
	@Override
	public I addInplace(Image<?,?> im) {
		if (im instanceof MultiBandImage<?,?,?>) {
			return addInplace((MultiBandImage<?,?,?>) im);
		} else if (im instanceof SingleBandImage<?,?>) {
			return addInplace((SingleBandImage<?,?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 * 	Adds to each pixel the value of the corresponding pixel in the corresponding
	 * 	band in the given image. Side-affects this image.
	 * 
	 *  @param im The image to add to this image.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I addInplace(MultiBandImage<?,?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).addInplace(((MultiBandImage<?,?,?>) im).bands.get(i));

		return (I) this;
	}

	/**
	 * 	Adds to each pixel (in all bandS) the value of corresponding pixel 
	 * 	in the given image. Side-affects this image.
	 * 
	 *  @param im The image to add to this image.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I addInplace(SingleBandImage<?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).addInplace(im);

		return (I) this;
	}

	/**
	 * 	Add the given value to each pixel in every band. Side-affects this image.
	 * 
	 * 	@param num The value to add to each pixel
	 * 	@return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I addInplace(T num) {
		for (S sbi : this) {
			sbi.add(num);
		}

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#addInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I addInplace(T[] num) {
		int np = bands.size();

		assert (num.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).addInplace(num[i]);

		return (I) this;
	}

	/**
	 * 	Clips each band in this image to the same range. 
	 * 	Values outside of the range are set to zero.
	 * 	Side-affects this image.
	 * 
	 * 	@param min The minimum value to clip to
	 * 	@param max The maximum value to clip to.
	 * 	@return this 
	 * 	@see Image#clip(Object, Object)
	 */
	@SuppressWarnings("unchecked")
	public I clip(T min, T max) {
		for (S sbi : this) {
			sbi.clip(min, max);
		}

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#clip(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I clip(T[] min, T[] max) {
		int np = bands.size();

		assert (min.length == np);
		assert (max.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).clip(min[i], max[i]);

		return (I) this;
	}
	
	/**
	 * 	For all bands, sets any values above the given threshold to zero.
	 * 	Side-affects this image.
	 * 
	 * 	@param thresh The threshold above which values are clipped
	 * 	@return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I clipMax(T thresh) {
		for (S sbm : this)
			sbm.clipMax(thresh);

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#clipMax(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I clipMax(T[] thresh) {
		int np = bands.size();

		assert (thresh.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).clipMax(thresh[i]);

		return (I) this;
	}

	/**
	 * Sets all pixels in all bands that have a value below the given 
	 * threshold to zero. Side-affects this image. 
	 * 
	 * @param thresh The threshold below which pixels will be set to zero.
	 * @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I clipMin(T thresh) {
		for (S sbm : this)
			sbm.clipMin(thresh);

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#clipMin(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I clipMin(T[] thresh) {
		int np = bands.size();

		assert (thresh.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).clipMin(thresh[i]);

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#clone()
	 */
	@Override
	public I clone() {
		I newImage = newInstance();

		for (S sbi : this) {
			newImage.bands.add(sbi.clone());
		}

		return newImage;
	}

	/**
	 * 	Delete the band at the given index.
	 * 
	 *  @param index The index of the band to remove.
	 */
	public void deleteBand(int index) {
		bands.remove(index);
	}

	/**
	 * 	Divides all pixels of each band by the given value and returns 
	 * 	result as a new image.
	 * 
	 * 	@param val The value to divide every pixel by.
	 * 	@return A new image containing the result.
	 */
	public I divide(T val) {
		I newImage = this.clone();
		newImage.divide(val);
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#divideInplace(org.openimaj.image.Image)
	 */
	@Override
	public I divideInplace(Image<?,?> im) {
		if (im instanceof MultiBandImage<?,?,?>) {
			return divideInplace((MultiBandImage<?,?,?>) im);
		} else if (im instanceof SingleBandImage<?,?>) {
			return divideInplace((SingleBandImage<?,?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 * 	Divides the pixels in every band of this image by the corresponding
	 * 	pixel in the corresponding band of the given image. Side-affects this
	 * 	image.
	 * 
	 *  @param im The image to divide into this image.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I divideInplace(MultiBandImage<?,?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).divideInplace(((MultiBandImage<?,?,?>) im).bands.get(i));

		return (I) this;
	}

	/**
	 * 	Divides the pixels in every band of this image by the corresponding
	 * 	pixel in the given image. Side-affects this image.
	 * 
	 *  @param im The image to divide into this image.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I divideInplace(SingleBandImage<?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).divideInplace(im);

		return (I) this;
	}

	/**
	 * 	Divide all pixels of every band by the given value. Side-affects this
	 * 	image.
	 * 
	 * 	@param val The value to divide by
	 * 	@return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I divideInplace(T val) {
		for (S sbm : this) {
			sbm.divide(val);
		}
		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#divideInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I divideInplace(T[] val) {
		int np = bands.size();

		assert (val.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).divideInplace(val[i]);

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#extractROI(int, int, org.openimaj.image.Image)
	 */
	@Override
	public I extractROI(int x, int y, I out) {
		for (int i=0; i<bands.size(); i++) {
			S img = bands.get(i);
			img.extractROI(x, y, out.bands.get(i));
		}
		
		return out;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#extractROI(int, int, int, int)
	 */
	@Override
	public I extractROI(int x, int y, int w, int h) {
		I newImage = newInstance();

		for (S sbm : this) {
			newImage.addBand(sbm.extractROI(x, y, w, h));
		}
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#fill(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I fill(T[] colour) {
		for (int b = 0; b < bands.size(); b++)
			bands.get(b).fill(colour[b]);
		return (I) this;
	}

	/**
	 * 	Flatten the bands into a single band using the average value of the
	 * 	pixels at each location.
	 * 
	 * 	@return A new single-band image containing the result.
	 */
	public S flatten() {
		S out = newBandInstance(getWidth(), getHeight());
		
		for (S sbm : this)
			out.addInplace(sbm);
		
		return out.divideInplace(intToT(numBands()));
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
	 * @param i the index
	 * @return the specified colour band
	 */
	public S getBand(int i) {
		return bands.get(i);
	}

	/**
	 * Get the colour space of this image
	 * @return the colour space
	 */
	public ColourSpace getColourSpace() {
		return colourSpace;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getContentArea()
	 */
	@Override
	public Rectangle getContentArea(){
		int minx=this.getWidth(), maxx=0, miny=this.getHeight(), maxy=0;
		for(int i = 0 ; i < this.numBands(); i++){
			Rectangle box = this.getBand(i).getContentArea();
			if(box.minX() < minx) minx = (int) box.minX();
			if(box.maxX() > maxx) maxx = (int) box.maxX();
			if(box.minY() < miny) miny = (int) box.minY();
			if(box.maxY() > maxy) maxy = (int) box.maxY();
		}
		
		return new Rectangle(minx, miny, maxx-minx, maxy-miny);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getField(org.openimaj.image.Image.Field)
	 */
	@Override
	public I getField(Field f) {
		I newImage = newInstance();

		for (S sbm : this) {
			newImage.bands.add(sbm.getField(f));
		}
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getFieldCopy(org.openimaj.image.Image.Field)
	 */
	@Override
	public I getFieldCopy(Field f) {
		I newImage = newInstance();

		for (S sbm : this) {
			newImage.bands.add(sbm.getFieldCopy(f));
		}
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getFieldInterpolate(org.openimaj.image.Image.Field)
	 */
	@Override
	public I getFieldInterpolate(Field f) {
		I newImage = newInstance();

		for (S sbm : this) {
			newImage.bands.add(sbm.getFieldInterpolate(f));
		}

		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getHeight()
	 */
	@Override
	public int getHeight() {
		if (bands.size() > 0)
			return bands.get(0).getHeight();
		return 0;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#getWidth()
	 */
	@Override
	public int getWidth() {
		if (bands.size() > 0)
			return bands.get(0).getWidth();
		return 0;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I internalCopy( I im )
	{
		final int nb = bands.size();
		for (int i=0; i<nb; i++)
			this.bands.get(i).internalCopy(im.getBand(i));

		return (I) this;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#internalAssign(org.openimaj.image.Image)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I internalAssign(I im) {
		bands = im.bands;
		return (I) this;
	}

	/**
	 * 	Converts the given integer to a value that can be used as a pixel value.
	 * 
	 *  @param n The integer to convert.
	 *  @return A value that can be used as a pixel value.
	 */
	protected abstract T intToT(int n);

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#inverse()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I inverse() {
		for (S sbm : this) {
			sbm.inverse();
		}
		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<S> iterator() {
		return bands.iterator();
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#max()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T[] max() {
		List<T> pixels = new ArrayList<T>();

		for (S sbm : this) {
			pixels.add(sbm.max());
		}

		return (T[]) pixels.toArray();
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#min()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T[] min() {
		List<T> pixels = new ArrayList<T>();

		for (S sbm : this) {
			pixels.add(sbm.min());
		}

		return (T[]) pixels.toArray();
	}

	/**
	 * 	Multiplies each pixel of every band by the given value and returns 
	 * 	the result as a new image.
	 * 
	 * 	@param num The value to multiply by.
	 * 	@return A new image containing the result.
	 */
	public I multiply(T num) {
		I newImage = this.clone();
		newImage.multiplyInplace(num);
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#multiplyInplace(org.openimaj.image.Image)
	 */
	@Override
	public I multiplyInplace(Image<?,?> im) {
		if (im instanceof MultiBandImage<?,?,?>) {
			return multiplyInplace((MultiBandImage<?,?,?>) im);
		} else if (im instanceof SingleBandImage<?,?>) {
			return multiplyInplace((SingleBandImage<?,?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 *	Multiplies every pixel in this image by the corresponding pixel in the
	 *	corresponding band in the given image. Side-affects this image.
	 * 
	 *  @param im The image to multiply with this image.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I multiplyInplace(MultiBandImage<?,?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).multiplyInplace(((MultiBandImage<?,?,?>) im).bands.get(i));

		return (I) this;
	}

	/**
	 * 	Multiplies every pixel in this image by the corresponding pixel in the
	 * 	given image. Side-affects this image.
	 * 
	 *  @param im The image to multiply with this image.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I multiplyInplace(SingleBandImage<?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).multiplyInplace(im);

		return (I) this;
	}

	/**
	 * 	Multiplies each pixel of every band by the given value. Side-affects
	 * 	this image.
	 * 
	 * 	@param num The value to multiply this image by
	 * 	@return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I multiplyInplace(T num) {
		for (S sbm : this)
			sbm.multiplyInplace(num);

		return (I) this;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#multiplyInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I multiplyInplace(T[] num) {
		int np = bands.size();

		assert (num.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).multiplyInplace(num[i]);

		return (I) this;
	}

	/**
	 * 	Returns a new instance of an image that represents each band.
	 * @param width The width of the image
	 * @param height The height of the image
	 *  @return A new {@link SingleBandImage} of the appropriate type.
	 */
	public abstract S newBandInstance(int width, int height);

	/**
	 * 	Returns a new instance of a this image type.
	 *  @return A new {@link MBFImage} subclass type.
	 */
	public abstract I newInstance();

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#newInstance(int, int)
	 */
	@Override
	public abstract I newInstance(int width, int height);

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#normalise()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I normalise() {
		for (S sbm : this)
			sbm.normalise();

		return (I) this;
	}

	/**
	 * 	Returns the number of bands in this image.
	 * 
	 *  @return the number of bands in this image.
	 */
	public int numBands() {
		return bands.size();
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#process(org.openimaj.image.processor.SinglebandImageProcessor)
	 */
	@Override
	public I process(SinglebandImageProcessor<T,S> p) {
		I out = newInstance();
		for (S sbm : this)
			out.bands.add(sbm.process(p));

		return out;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor)
	 */
	@Override
	public I process(SinglebandKernelProcessor<T,S> kernel) {
		return process(kernel, false);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor, boolean)
	 */
	@Override
	public I process(SinglebandKernelProcessor<T,S> kernel, boolean pad) {
		I out = newInstance();
		for (S sbm : this)
			out.bands.add(sbm.process(kernel, pad));

		return out;
	}

	/**
	 * 	Processes this image with the given {@link SinglebandImageProcessor}
	 * 	for every band.
	 * 
	 *  @param pp The pixel process to apply to each band in turn.
	 *  @return A new image containing the result.
	 */
	public I process(SinglebandPixelProcessor<T> pp) {
		I out = newInstance();
		for (S sbm : this)
			out.bands.add(sbm.process(pp));

		return out;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandImageProcessor)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I processInplace(SinglebandImageProcessor<T,S> p) {
		for (S sbm : this)
			sbm.processInplace(p);

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor)
	 */
	@Override
	public I processInplace(SinglebandKernelProcessor<T,S> kernel) {
		return processInplace(kernel, false);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor, boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I processInplace(SinglebandKernelProcessor<T,S> kernel, boolean pad) {
		for (S sbm : this)
			sbm.processInplace(kernel, pad);

		return (I) this;
	}

	/**
	 * 	Process this image with the given {@link SinglebandImageProcessor}
	 * 	for every band. Side-affects this image.
	 * 
	 *  @param pp The pixel processor to apply to each band in turn.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I processInplace(SinglebandPixelProcessor<T> pp) {
		for (S sbm : this)
			sbm.processInplace(pp);

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#setPixel(int, int, java.lang.Object)
	 */
	@Override
	public void setPixel(int x, int y, T[] val) {
		int np = bands.size();
		if(np == val.length)
			for (int i = 0; i < np; i++)
				bands.get(i).setPixel(x, y, val[i]);
		else{
			int offset = val.length - np;
			for (int i = 0; i < np; i++)
				if(i + offset >=0)
					bands.get(i).setPixel(x, y, val[i+offset]);
		}
	}
	
	/**
	 * Subtracts the given value from every pixel in every band and 
	 * returns the result as a new image.
	 * 
	 * @param num The value to subtract from this image.
	 * @return A new image containing the result.
	 */
	public I subtract(T num) {
		I newImage = this.clone();
		newImage.subtract(num);
		return newImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#subtractInplace(org.openimaj.image.Image)
	 */
	@Override
	public I subtractInplace(Image<?,?> im) {
		if (im instanceof MultiBandImage<?,?,?>) {
			return subtractInplace((MultiBandImage<?,?,?>) im);
		} else if (im instanceof SingleBandImage<?,?>) {
			return subtractInplace((SingleBandImage<?,?>) im);
		} else {
			throw new UnsupportedOperationException("Unsupported Type");
		}
	}

	/**
	 * 	Subtracts from every pixel in every band the corresponding pixel value
	 * 	in the corresponding band of the given image. Side-affects this image.
	 * 
	 *  @param im The image to subtract from this image
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I subtractInplace(MultiBandImage<?,?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).subtractInplace(((MultiBandImage<?,?,?>) im).bands.get(i));

		return (I) this;
	}
	
	/**
	 * 	Subtracts from every pixel in every band the corresponding pixel value
	 * 	in the given image. Side-affects this image.
	 * 
	 *  @param im The image to subtract from this image.
	 *  @return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I subtractInplace(SingleBandImage<?,?> im) {
		assert (ImageUtilities.checkSameSize(this, im));

		int np = bands.size();

		for (int i = 0; i < np; i++)
			bands.get(i).subtractInplace(im);

		return (I) this;
	}
	
	/**
	 * 	Subtracts the given value from every pixel in every band. Side-affects
	 * 	this image.
	 * 
	 * 	@param num The value to subtract from this image
	 * 	@return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I subtractInplace(T num) {
		for (S sbm : this)
			sbm.subtract(num);

		return (I) this;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#subtractInplace(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I subtractInplace(T[] num) {
		int np = bands.size();

		assert (num.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).subtractInplace(num[i]);

		return (I) this;
	}
	
	/**
	 * 	Sets the value of any pixel below the given threshold to zero and all
	 * 	others to 1 for all bands. Side-affects this image. 
	 * 
	 * 	@param thresh The threshold above which pixels will be set to 1.
	 * 	@return A reference to this image containing the result.
	 */
	@SuppressWarnings("unchecked")
	public I threshold(T thresh) {
		for (S sbm : this)
			sbm.threshold(thresh);

		return (I) this;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#threshold(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I threshold(T[] thresh) {
		int np = bands.size();

		assert (thresh.length == np);

		for (int i = 0; i < np; i++)
			bands.get(i).threshold(thresh[i]);

		return (I) this;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#toByteImage()
	 */
	@Override
	public byte[] toByteImage() {
		int width = getWidth();
		int height = getHeight();
		int nb = bands.size();

		byte[] ppmData = new byte[nb * height * width];

		for (int n = 0; n < nb; n++) {
			byte[] band = bands.get(n).toByteImage();

			for (int j = 0; j < height; j++) {
				for (int i = 0; i < width; i++) {
					ppmData[nb * (i + j * width) + n] = band[i + j * width];
				}
			}
		}
		return ppmData;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#toPackedARGBPixels()
	 */
	@Override
	public int [] toPackedARGBPixels() {
		//TODO: deal better with color spaces
		if (bands.size() == 1) {
			return bands.get(0).toPackedARGBPixels();
		} else if (bands.size() == 3) {
			int width = getWidth();
			int height = getHeight();
			
			byte[] rp = bands.get(0).toByteImage();
			byte[] gp = bands.get(1).toByteImage();
			byte[] bp = bands.get(2).toByteImage();

			int [] data = new int[height*width];
			
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					int red = rp[c + r * width] & 0xff;
					int green = gp[c + r * width] & 0xff;
					int blue = bp[c + r * width] & 0xff;

					int rgb = 0xff << 24 | red << 16 | green << 8 | blue;
					data[c + r*width] = rgb;
				}
			}

			return data;
		} else if (bands.size() == 4) {
			int width = getWidth();
			int height = getHeight();
			
			byte[] ap = bands.get(3).toByteImage();
			byte[] rp = bands.get(0).toByteImage();
			byte[] gp = bands.get(1).toByteImage();
			byte[] bp = bands.get(2).toByteImage();

			int [] data = new int[height*width];
			
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					int alpha = ap[c + r * width] & 0xff;
					int red = rp[c + r * width] & 0xff;
					int green = gp[c + r * width] & 0xff;
					int blue = bp[c + r * width] & 0xff;

					int argb = alpha << 24 | red << 16 | green << 8 | blue;
					data[c + r*width] = argb;
				}
			}

			return data;
		} else {
			throw new UnsupportedOperationException(
					"Unable to create bufferedImage with " + numBands() + " bands");
		}
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.Image#zero()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I zero() {
		for (S sbm : this)
			sbm.zero();

		return (I) this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public I shiftLeftInplace(int count) {
		for (S b : bands) 
			b.shiftLeftInplace(count);
		return (I) this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public I shiftRightInplace(int count) {
		for (S b : bands) 
			b.shiftRightInplace(count);
		return (I) this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public I flipX() {
		for (S b : bands)
			b.flipX();
		
		return (I) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public I flipY() {
		for (S b : bands)
			b.flipY();
		
		return (I) this;
	}
}