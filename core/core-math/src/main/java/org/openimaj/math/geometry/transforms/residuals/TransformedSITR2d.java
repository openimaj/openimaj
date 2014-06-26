package org.openimaj.math.geometry.transforms.residuals;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * An implementation of a {@link SingleImageTransferResidual2d} that
 * pre-transforms both sets of points by predetermined transforms.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 *            type of model
 * 
 */
public class TransformedSITR2d<M extends Model<Point2d, Point2d>> implements ResidualCalculator<Point2d, Point2d, M> {
	Matrix t1;
	Matrix t2;
	M model;

	/**
	 * Construct with the given transforms
	 * 
	 * @param t1
	 *            transform for first point in the pair
	 * @param t2
	 *            transform for second point in the pair
	 */
	public TransformedSITR2d(Matrix t1, Matrix t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public void setModel(M model) {
		this.model = model;
	}

	@Override
	public double computeResidual(IndependentPair<Point2d, Point2d> data) {
		final Point2d p2_est = model.predict(data.firstObject()).transform(t1);

		final Point2d so = data.secondObject().transform(t2);
		final float dx = so.getX() - p2_est.getX();
		final float dy = so.getY() - p2_est.getY();

		return (dx * dx + dy * dy);
	}

	@Override
	public void computeResiduals(List<? extends IndependentPair<Point2d, Point2d>> data, double[] errors) {
		for (int i = 0; i < data.size(); i++) {
			final Point2d p2_est = model.predict(data.get(i).firstObject()).transform(t1);

			final Point2d so = data.get(i).secondObject().transform(t2);
			final float dx = so.getX() - p2_est.getX();
			final float dy = so.getY() - p2_est.getY();

			errors[i] = (dx * dx + dy * dy);
		}
	}
}
