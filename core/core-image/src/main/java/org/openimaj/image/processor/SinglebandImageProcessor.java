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
package org.openimaj.image.processor;

import org.openimaj.image.Image;
import org.openimaj.image.SingleBandImage;

/**
 * An interface for objects that are able to process only
 * {@link SingleBandImage}s.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 *            The type of pixel in the image that this processor can process.
 * @param <S>
 *            The concrete subclass of the single band image that this processor
 *            can process.
 */
public interface SinglebandImageProcessor<T, S extends Image<T, S>>
extends ImageProcessor<S>
{
	/**
	 * An interface for {@link Image}s that are processable by
	 * {@link SinglebandImageProcessor}s.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * @param <T>
	 *            The type of pixel in the image
	 * @param <S>
	 *            The concrete subclass of the single band image
	 * @param <I>
	 *            The type of image that is returned after processing
	 */
	public interface Processable<T, S extends Image<T, S>, I extends Image<?, I>>
	{
		/**
		 * Process with the given {@link SinglebandImageProcessor} returning a
		 * new image.
		 *
		 * @param p
		 *            The processor to process the image with
		 * @return A new image containing the result.
		 */
		public I process(SinglebandImageProcessor<T, S> p);

		/**
		 * Process with the given {@link SinglebandImageProcessor} storing the
		 * result in this processable image. Side-affects this processable
		 * image.
		 *
		 * @param p
		 *            The processor to process the image with
		 * @return A new image containing the result.
		 */
		public I processInplace(SinglebandImageProcessor<T, S> p);
	}
}
