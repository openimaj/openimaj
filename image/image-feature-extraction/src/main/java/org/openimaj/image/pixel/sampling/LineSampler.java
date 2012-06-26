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
package org.openimaj.image.pixel.sampling;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.line.Line2d;

/**
 * {@link LineSampler} defines an interface for objects capable
 * of extracting information from pixels along a line in an image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <I> Type of image
 * @param <T> Type of the result of the sampling 
 */
public interface LineSampler<I extends Image<?, I>, T> {
	/**
	 * Extract a numSamples samples along the given line
	 * in the given image. Implementors may choose to 
	 * interpret the line in different ways; some may sample
	 * numSamples between the start and end points, whilst
	 * others may choose different sampling points along
	 * the direction of the line.
	 * 
	 * @param line the line
	 * @param image the image to sample from
	 * @param numSamples the number of samples
	 * @return the samples
	 */
	public abstract T extractSamples(Line2d line, I image, int numSamples);
	
	/**
	 * Get the a line representing the extremities of
	 * the pixels that are actually sampled.
	 * @param line the line
	 * @param image the image to sample from
	 * @param numSamples the number of samples
	 * @return the sampling line
	 */
	public abstract Line2d getSampleLine(Line2d line, I image, int numSamples);
}
