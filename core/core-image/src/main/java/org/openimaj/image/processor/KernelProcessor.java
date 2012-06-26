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
 * 	A kernel processor interface for objects that are able to process
 * 	an image using convolution. Patches are generated for every pixel in
 * 	the image where a patch of the size required by this class can be placed.
 * 	The {@link #processKernel(Image)} function will then generate a single
 * 	value which is used to build the convolved image.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *  @param <Q> The pixel type that is processed
 *  @param <I> The image type that is processed
 */
public interface KernelProcessor<Q, I extends Image<Q,I>>  extends Processor<I>
{
	/**
	 * 	Get the height of the kernel required by this processor.
	 * 
	 *  @return The height of the kernel required by this processor
	 */
	public abstract int getKernelHeight();
	
	/**
	 * 	Get the width of the kernel required by this processor.
	 * 
	 *  @return The width of the kernel required by this processor. 
	 */
	public abstract int getKernelWidth();
	
	/**
	 * 	Process the patch with this kernel processor and return a value
	 * 	that will be used to build the convolved image.	
	 * 
	 *  @param patch The patch of pixels from the image to process
	 *  @return A value to place in the final convolved image.
	 */
	public abstract Q processKernel(I patch);
}
