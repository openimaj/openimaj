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
package org.openimaj.image.pixel.statistics;

import org.openimaj.image.Image;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;

/**
 * Interface for classes capable of building "models"
 * of pixels along a line.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I> Concrete type of {@link Image}
 */
public interface PixelProfileModel<I extends Image<?,I>> {
	/**
	 * Update the model with a new sample.
	 * @param image the image to extract the sample from
	 * @param line the line across with to sample
	 */
	public void updateModel(I image, Line2d line);
	
	/**
	 * Extract numSamples samples from the line in the image and
	 * then compare this model at each overlapping position starting
	 * from the first sample at the beginning of the line.
	 * 
	 * numSamples must be bigger than the number of samples used to
	 * construct the model. In addition, callers are responsible for
	 * ensuring the sampling rate between the new samples and the model
	 * is equal.
	 * 
	 * The point on the line corresponding to the smallest Mahalanobis 
	 * distance is returned.
	 * 
	 * @param image the image to sample
	 * @param line the line to sample along
	 * @param numSamples the number of samples to make
	 * @return the "best" position on the line
	 */
	public Point2d computeNewBest(I image, Line2d line, int numSamples);
	
	/**
	 * Compute the cost of a vector of samples extracted 
	 * along a line in the given image to the internal model. 
	 * 
	 * @param image the image to sample 
	 * @param line the line to sample along
	 * @return the computed cost
	 */
	public float computeCost(I image, Line2d line);
	
	/**
	 * Compute the distance between the centre of the given
	 * line and the given point, normalised as a function of
	 * the length of the sampling line.
	 * 
	 * @param image the image to sample
	 * @param line the line to sample along
	 * @param numSamples the number of samples to make
	 * @param pt the point to compare
	 * @return the normalised distance (0 means same point; 1 means on end of line)
	 */
	public float computeMovementDistance(I image, Line2d line, int numSamples, Point2d pt);
}
