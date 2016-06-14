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
package org.openimaj.math.geometry.path.resample;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.path.Polyline;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.util.Interpolation;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;

/**
 * Simple linear resampling. Only the end points are guaranteed to be preserved.
 */
public class LinearResampler implements Function<Polyline, Polyline>, Operation<Polyline> {
	int targetVertices;

	/**
	 * Construct with the given number of target vertices for lines to have
	 *
	 * @param targetVertices
	 *            number of required vertices
	 */
	public LinearResampler(int targetVertices) {
		this.targetVertices = targetVertices;
	}

	@Override
	public Polyline apply(Polyline in) {
		final double length = in.calculateLength();
		final double step = length / (targetVertices - 1);

		final Polyline out = new Polyline();
		out.points.add(in.begin().copy());

		Point2d begin = in.points.get(0);
		Point2d end = in.points.get(1);
		double lastLength = Line2d.distance(begin, end);
		double distance = lastLength;
		double pos = step;
		for (int i = 1, c = 1; i < targetVertices - 1; i++, pos += step) {
			while (pos > distance) {
				// move through segments until we find the correct one
				begin = end;
				c++;
				end = in.points.get(c);
				lastLength = Line2d.distance(begin, end);
				distance += lastLength;
			}

			final double offset = lastLength - (distance - pos);

			final Point2d np = begin.copy();
			for (int j = 0; j < 2; j++) {
				final double n = Interpolation.lerp(offset, 0, begin.getOrdinate(j).doubleValue(), lastLength, end
						.getOrdinate(j).doubleValue());
				np.setOrdinate(j, n);
			}
			out.points.add(np);
		}
		out.points.add(in.end().copy());

		return out;
	}

	/**
	 * Apply the resampling operation in-place
	 */
	@Override
	public void perform(Polyline object) {
		final Polyline n = apply(object);
		object.points = n.points;
	}
}
