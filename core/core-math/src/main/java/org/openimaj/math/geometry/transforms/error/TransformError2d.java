package org.openimaj.math.geometry.transforms.error;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.error.ModelFitError;
import org.openimaj.util.pair.IndependentPair;

public class TransformError2d implements ModelFitError<Point2d, Point2d, Model<Point2d, Point2d>> {
	Model<Point2d, Point2d> model;

	@Override
	public void setModel(Model<Point2d, Point2d> model) {
		this.model = model;
	}

	@Override
	public double computeError(IndependentPair<Point2d, Point2d> data) {
		final Point2d p2_est = model.predict(data.firstObject());

		final float dx = data.secondObject().getX() - p2_est.getX();
		final float dy = data.secondObject().getY() - p2_est.getY();

		return (dx * dx + dy * dy);
	}

	@Override
	public void computeError(List<? extends IndependentPair<Point2d, Point2d>> data, double[] errors) {
		for (int i = 0; i < data.size(); i++) {
			final Point2d p2_est = model.predict(data.get(i).firstObject());

			final Point2d so = data.get(i).secondObject();
			final float dx = so.getX() - p2_est.getX();
			final float dy = so.getY() - p2_est.getY();

			errors[i] = (dx * dx + dy * dy);
		}
	}
}
