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
package org.openimaj.image.processing.restoration.inpainting;

import java.util.Collection;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;

/**
 * Abstract base for {@link Inpainter} implementations that consume a mask image
 * (rather than connected components or pixel sets). Provides the necessary
 * methods to build the mask image for all the various calls to
 * <code>setMask</code>.
 * <p>
 * All <code>setMask</code> implementations call {@link #initMask()}, which
 * subclasses should implement to perform any required initialisation.
 * {@link #processImage(Image)} performs checks on the image dimensions and then
 * calls {@link #performInpainting(Image)}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of image that this processor can process
 */
@SuppressWarnings("javadoc")
public abstract class AbstractImageMaskInpainter<IMAGE extends Image<?, IMAGE>>
		implements
		Inpainter<IMAGE>
{
	/**
	 * The mask image
	 */
	protected FImage mask;

	@Override
	public void setMask(FImage mask) {
		this.mask = mask;
		initMask();
	}

	@Override
	public void setMask(int width, int height, Collection<? extends Iterable<Pixel>> mask) {
		this.mask = new FImage(width, height);

		for (final Iterable<Pixel> ps : mask) {
			for (final Pixel p : ps) {
				if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height)
					this.mask.pixels[p.y][p.x] = 1;
			}
		}
		initMask();
	}

	@Override
	public void setMask(int width, int height, PixelSet... mask) {
		this.mask = new FImage(width, height);

		for (final Iterable<Pixel> ps : mask) {
			for (final Pixel p : ps) {
				if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height)
					this.mask.pixels[p.y][p.x] = 1;
			}
		}
		initMask();
	}

	/**
	 * Perform any initialisation once the mask has been set. Does nothing by
	 * default; subclasses should override as required.
	 */
	protected void initMask() {

	};

	@Override
	public final void processImage(IMAGE image) {
		if (mask == null)
			throw new IllegalArgumentException("Mask has not been set");

		if (image.getWidth() != mask.getWidth() || image.getHeight() != mask.getHeight())
			throw new IllegalArgumentException("Image and mask size do not match");

		performInpainting(image);
	}

	/**
	 * Perform the inpainting of the given image
	 * 
	 * @param image
	 *            the image to inpaint
	 */
	protected abstract void performInpainting(IMAGE image);
}
