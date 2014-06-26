package org.openimaj.math.geometry.transforms.residuals;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.util.pair.IndependentPair;

/**
 * Compute the 2d geometric Single Image Transfer residual. This is the squared
 * distance between the second point in the pair and the first point one the
 * transform has been applied.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <M>
 *            the concrete type of model
 */
public class SingleImageTransferResidual2d<M extends Model<Point2d, Point2d>>
		implements
		ResidualCalculator<Point2d, Point2d, M>
{
	M model;

	@Override
	public void setModel(M model) {
		this.model = model;
	}

	@Override
	public double computeResidual(IndependentPair<Point2d, Point2d> data) {
		final Point2d p2_est = model.predict(data.firstObject());

		final float dx = data.secondObject().getX() - p2_est.getX();
		final float dy = data.secondObject().getY() - p2_est.getY();

		return (dx * dx + dy * dy);
	}

	@Override
	public void computeResiduals(List<? extends IndependentPair<Point2d, Point2d>> data, double[] errors) {
		for (int i = 0; i < data.size(); i++) {
			final Point2d p2_est = model.predict(data.get(i).firstObject());

			final Point2d so = data.get(i).secondObject();
			final float dx = so.getX() - p2_est.getX();
			final float dy = so.getY() - p2_est.getY();

			errors[i] = (dx * dx + dy * dy);
		}
	}
}
