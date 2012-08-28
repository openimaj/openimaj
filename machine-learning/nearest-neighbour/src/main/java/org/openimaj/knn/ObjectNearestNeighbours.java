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
package org.openimaj.knn;

import java.util.List;

import org.openimaj.util.comparator.DistanceComparator;

/**
 * Abstract base class for k-nearest-neighbour calculations with any form of
 * object that can be compared with a {@link DistanceComparator}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object being compared.
 */
public abstract class ObjectNearestNeighbours<T> implements NearestNeighbours<T, float[]> {
	protected DistanceComparator<T> distance;

	/**
	 * Construct with the given distance measure
	 * 
	 * @param distance
	 *            the distance measure
	 */
	public ObjectNearestNeighbours(DistanceComparator<T> distance) {
		this.distance = distance;
	}

	/**
	 * Static method to find a distance between a query vector and each of a set
	 * of points. Results are stored in the dsq_out array, much must have the
	 * same length as the number of points.
	 * 
	 * @param <T>
	 *            Type of object being compared.
	 * @param distance
	 *            the distance measure
	 * @param qu
	 *            The query vector.
	 * @param pnts
	 *            The points to compare against.
	 * @param dsq_out
	 *            The resultant distances.
	 */
	public static <T> void distanceFunc(final DistanceComparator<T> distance, final T qu, final List<T> pnts,
			float[] dsq_out)
	{
		final int N = pnts.size();

		if (distance.isDistance()) {
			for (int n = 0; n < N; ++n) {
				dsq_out[n] = (float) distance.compare(qu, pnts.get(n));
			}
		} else {
			for (int n = 0; n < N; ++n) {
				dsq_out[n] = -(float) distance.compare(qu, pnts.get(n));
			}
		}
	}
}
