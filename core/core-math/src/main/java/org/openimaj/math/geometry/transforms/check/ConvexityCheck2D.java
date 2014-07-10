package org.openimaj.math.geometry.transforms.check;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.Model;
import org.openimaj.util.function.Predicate;

/**
 * Test that a 2d transform model preserves convexity
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 */
public class ConvexityCheck2D<M extends Model<Point2d, Point2d> & MatrixTransformProvider> implements Predicate<M> {
	private final static Rectangle r = new Rectangle(-1, -1, 2, 2);

	@Override
	public boolean test(M m) {
		try {
			return r.transform(m.getTransform()).isConvex() && r.transform(m.getTransform().inverse()).isConvex();
		} catch (final RuntimeException e) {
			// transform might be singular
			return false;
		}
	}
}
