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
package org.openimaj.math.statistics.distribution;

import org.openimaj.feature.MultidimensionalDoubleFV;

/**
 * Simple Histogram based on a MultidimensionalDoubleFV.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MultidimensionalHistogram extends MultidimensionalDoubleFV {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a histogram with the given number of bins
	 * per dimension.
	 * @param nbins number of bins per dimension
	 */
	public MultidimensionalHistogram(int... nbins) {
		super(nbins);
	}

	/**
	 * Normalise to unit length 
	 */
	public void normalise() {
		double sum = 0;

		for (int i=0; i<values.length; i++)
			sum += values[i];

		for (int i=0; i<values.length; i++)
			values[i] /= sum;
	}
	
	/**
	 * Compute the maximum value in the histogram
	 * @return the maximum value
	 */
	public double max()
	{
		double max = Double.MIN_VALUE;
		for( int i = 0; i < values.length; i++ )
			max = Math.max( values[i], max );
		return max;
	}
	
	@Override
	public MultidimensionalHistogram clone() {
		return (MultidimensionalHistogram) super.clone();
	}
}
