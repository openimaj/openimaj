package org.openimaj.math.geometry.transforms.residuals;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * The points in the first image are projected by the homography matrix to
 * produce new estimates of the second image points and the second image point
 * projected by the inverse homography to produce estimates of the first.
 * Residuals are computed from both point sets and summed to produce the final
 * geometric residual value.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 */
public class SymmetricTransferResidual2d<M extends Model<Point2d, Point2d> & MatrixTransformProvider>
		implements
		ResidualCalculator<Point2d, Point2d, M>
{
	private Matrix transform;
	private Matrix transformInv;

	@Override
	public void setModel(M model) {
		this.transform = model.getTransform();

		if (transform.getRowDimension() != 3 || transform.getColumnDimension() != 3)
			throw new IllegalArgumentException("Transform matrix must be 3x3");

		transformInv = transform.inverse();
	}

	@Override
	public double computeResidual(IndependentPair<Point2d, Point2d> data) {
		final Point2d p1 = data.getFirstObject();
		final Point2d p2 = data.getSecondObject();

		final Point2d p1t = p1.transform(transform);
		final Point2d p2t = p2.transform(transformInv);

		final float p1x = p1.getX();
		final float p1y = p1.getY();
		final float p1tx = p1t.getX();
		final float p1ty = p1t.getY();
		final float p2x = p2.getX();
		final float p2y = p2.getY();
		final float p2tx = p2t.getX();
		final float p2ty = p2t.getY();

		final float dx12t = (p1x - p2tx);
		final float dy12t = (p1y - p2ty);
		final float dx1t2 = (p1tx - p2x);
		final float dy1t2 = (p1ty - p2y);

		return dx12t * dx12t + dy12t * dy12t + dx1t2 * dx1t2 + dy1t2 * dy1t2;
	}

	@Override
	public void computeResiduals(List<? extends IndependentPair<Point2d, Point2d>> data, double[] residuals) {
		for (int i = 0; i < data.size(); i++) {
			residuals[i] = computeResidual(data.get(i));
		}
	}
}
