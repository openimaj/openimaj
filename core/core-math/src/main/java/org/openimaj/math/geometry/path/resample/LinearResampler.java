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
