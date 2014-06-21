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

import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.processor.SinglebandKernelProcessor;

/**
 * A base class for representing a single band image of any pixel type.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <Q>
 *            The pixel type
 * @param <I>
 *            The concrete image subclass type.
 */
public abstract class SingleBandImage<Q extends Comparable<Q>, I extends SingleBandImage<Q, I>>
		extends
		Image<Q, I>
		implements
		SinglebandImageProcessor.Processable<Q, I, I>,
		SinglebandKernelProcessor.Processable<Q, I, I>
{
	private static final long serialVersionUID = 1L;

	/** The image height */
	public int height;

	/** The image width */
	public int width;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.Image#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.Image#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.Image#process(org.openimaj.image.processor.KernelProcessor)
	 */
	@Override
	public I process(KernelProcessor<Q, I> p) {
		return process(p, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.Image#process(org.openimaj.image.processor.KernelProcessor,
	 *      boolean)
	 */
	@Override
	public I process(KernelProcessor<Q, I> p, boolean pad) {
		final I newImage = newInstance(width, height);
		final I tmp = newInstance(p.getKernelWidth(), p.getKernelHeight());

		final int hh = p.getKernelHeight() / 2;
		final int hw = p.getKernelWidth() / 2;

		if (!pad) {
			for (int y = hh; y < getHeight() - (p.getKernelHeight() - hh); y++) {
				for (int x = hw; x < getWidth() - (p.getKernelWidth() - hw); x++) {
					newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
				}
			}
		} else {
			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
				}
			}
		}

		return newImage;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor)
	 */
	@Override
	public I process(SinglebandKernelProcessor<Q, I> p) {
		return process(p, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor,
	 *      boolean)
	 */
	@Override
	public I process(SinglebandKernelProcessor<Q, I> p, boolean pad) {
		final I newImage = newInstance(width, height);
		final I tmp = newInstance(p.getKernelWidth(), p.getKernelHeight());

		final int hh = p.getKernelHeight() / 2;
		final int hw = p.getKernelWidth() / 2;

		if (!pad) {
			for (int y = hh; y < getHeight() - (p.getKernelHeight() - hh); y++) {
				for (int x = hw; x < getWidth() - (p.getKernelWidth() - hw); x++) {
					newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
				}
			}
		} else {
			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
				}
			}
		}

		return newImage;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor)
	 */
	@Override
	public I processInplace(SinglebandKernelProcessor<Q, I> p) {
		return processInplace(p, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor,
	 *      boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public I processInplace(SinglebandKernelProcessor<Q, I> p, boolean pad) {
		final I newImage = process(p, pad);
		this.internalAssign(newImage);
		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#process(org.openimaj.image.processor.SinglebandImageProcessor)
	 */
	@Override
	public I process(SinglebandImageProcessor<Q, I> p) {
		final I newImage = this.clone();
		newImage.processInplace(p);
		return newImage;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandImageProcessor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I processInplace(SinglebandImageProcessor<Q, I> p) {
		p.processImage((I) this);
		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.Image#fill(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public I fill(Q colour) {
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++)
				this.setPixel(x, y, colour);

		return (I) this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.Image#clone()
	 */
	@Override
	public abstract I clone();

	@Override
	public boolean equals(Object obj) {
		@SuppressWarnings("unchecked")
		final I that = (I) obj;
		for (int y = 0; y < getHeight(); y++)
			for (int x = 0; x < getWidth(); x++)
			{
				final boolean fail = !this.getPixel(x, y).equals(that.getPixel(x, y));
				if (fail)
					return false;
			}

		return true;
	}
}
