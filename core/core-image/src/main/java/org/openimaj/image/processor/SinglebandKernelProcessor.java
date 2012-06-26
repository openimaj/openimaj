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

/**
 * A KernelProcessor for single band images.
 * 
 * @see KernelProcessor
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> The pixel type that is processed
 * @param <I> The image type that is processed
 */
public interface SinglebandKernelProcessor<T, I extends Image<T,I>> extends KernelProcessor<T, I> {
	/**
	 * Interfaces for objects that allow themselves to be processed by a
	 * SinglebandKernelProcessor.
	 *
	 * @param <T> The pixel type that is processed
	 * @param <S> The image type of the underlying single band images
	 * @param <I> The image type that is processed
	 */
	public interface Processable<T, S extends Image<T,S>, I extends Image<?,I>> {
		/**
		 * @see Image#process(KernelProcessor)
		 * @param p the processor
		 * @return the processed image
		 */
		public I process(SinglebandKernelProcessor<T,S> p);
		
		/**
		 * @see Image#process(KernelProcessor)
		 * @param p the processor
		 * @return the processed image
		 */
		public I processInplace(SinglebandKernelProcessor<T,S> p);
		
		/**
		 * @see Image#process(KernelProcessor, boolean)
		 * @param p the processor
		 * @param pad Should the image be zero padded so the 
		 * 				kernel reaches the edges of the output
		 * @return the processed image
		 */
		public I process(SinglebandKernelProcessor<T,S> p, boolean pad);
		
		/**
		 * @see Image#processInplace(KernelProcessor, boolean)
		 * @param p the processor
		 * @param pad Should the image be zero padded so the 
		 * 				kernel reaches the edges of the output
		 * @return the processed image
		 */
		public I processInplace(SinglebandKernelProcessor<T,S> p, boolean pad);
	}
}
