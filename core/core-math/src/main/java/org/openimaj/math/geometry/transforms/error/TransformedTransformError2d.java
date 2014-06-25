package org.openimaj.math.geometry.transforms.error;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.error.ModelFitError;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class TransformedTransformError2d implements ModelFitError<Point2d, Point2d, Model<Point2d, Point2d>> {
	Matrix t1;
	Matrix t2;
	Model<Point2d, Point2d> model;

	public TransformedTransformError2d(Matrix t1, Matrix t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public void setModel(Model<Point2d, Point2d> model) {
		this.model = model;
	}

	@Override
	public double computeError(IndependentPair<Point2d, Point2d> data) {
		final Point2d p2_est = model.predict(data.firstObject()).transform(t1);

		final Point2d so = data.secondObject().transform(t2);
		final float dx = so.getX() - p2_est.getX();
		final float dy = so.getY() - p2_est.getY();

		return (dx * dx + dy * dy);
	}

	@Override
	public void computeError(List<? extends IndependentPair<Point2d, Point2d>> data, double[] errors) {
		for (int i = 0; i < data.size(); i++) {
			final Point2d p2_est = model.predict(data.get(i).firstObject()).transform(t1);

			final Point2d so = data.get(i).secondObject().transform(t2);
			final float dx = so.getX() - p2_est.getX();
			final float dy = so.getY() - p2_est.getY();

			errors[i] = (dx * dx + dy * dy);
		}
	}
}
