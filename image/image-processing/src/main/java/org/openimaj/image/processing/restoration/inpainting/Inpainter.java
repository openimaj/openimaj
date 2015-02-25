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
import org.openimaj.image.processor.ImageProcessor;

/**
 * Interface defining an implementation of an inpainting algorithm. Inpainting
 * algorithms are {@link ImageProcessor}s, but it is expected that a mask
 * showing which pixels need to be painted is provided before the
 * {@link #processImage(Image)} call.
 * <p>
 * {@link Inpainter}s are necessarily not thread safe, but implementations are
 * expected to be reusable once the mask has been reset. <strong>It is expected
 * that a call to one of the <code>setMask</code> methods is made before every
 * call to {@link #processImage(Image)}.</strong>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            The type of image that this processor can process
 */
public interface Inpainter<IMAGE extends Image<?, IMAGE>>
		extends
		ImageProcessor<IMAGE>
{
	/**
	 * Set the mask. The mask configures which pixels need to be painted.
	 * 
	 * @param mask
	 *            the mask image; should be binary, with 1 values where painting
	 *            needs to occur.
	 */
	public void setMask(FImage mask);

	/**
	 * Set the mask. The mask configures which pixels need to be painted.
	 * 
	 * @param width
	 *            the mask width
	 * @param height
	 *            the mask height
	 * @param mask
	 *            the mask pixels
	 */
	public void setMask(int width, int height, Collection<? extends Iterable<Pixel>> mask);

	/**
	 * Set the mask. The mask configures which pixels need to be painted.
	 * 
	 * @param width
	 *            the mask width
	 * @param height
	 *            the mask height
	 * @param mask
	 *            the mask pixels
	 */
	public void setMask(int width, int height, PixelSet... mask);

	/**
	 * Inpaint the given image, painting all the mask pixels set by a prior call
	 * to {@link #setMask(int,int,Collection)}, {@link #setMask(FImage)} or
	 * {@link #setMask(int,int,PixelSet...)}
	 * 
	 * @param image
	 *            the image to perform inpainting on
	 */
	@Override
	public void processImage(IMAGE image);
}
