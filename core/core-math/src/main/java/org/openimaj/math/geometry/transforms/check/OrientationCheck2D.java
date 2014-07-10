package org.openimaj.math.geometry.transforms.check;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.Model;
import org.openimaj.util.function.Predicate;

import Jama.Matrix;

/**
 * Test whether a given model that produces a homogenous transform is
 * orientation preserving
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 */
public class OrientationCheck2D<M extends Model<Point2d, Point2d> & MatrixTransformProvider> implements Predicate<M> {

	@Override
	public boolean test(M model) {
		final Matrix H = model.getTransform();

		// Hartley & Zisserman MVG:
		// If the determinant of the top-left 2x2 matrix is > 0 the
		// transformation is orientation-preserving.
		// Else if the determinant is < 0, it is orientation-reversing.
		final double det = H.get(0, 0) * H.get(1, 1) - H.get(1, 0) * H.get(0, 1);
		if (det < 0)
			return false;

		return true;
	}
}
