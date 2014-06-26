package org.openimaj.math.geometry.transforms.estimation;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.residuals.AlgebraicResidual2d;
import org.openimaj.math.model.fit.LMedS;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.math.model.fit.RANSAC.StoppingCondition;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.pair.IndependentPair;

/**
 * Helper class to simplify robust estimation of 2D affine transforms without
 * having to deal with the nuts and bolts of the underlying robust model
 * fitters, etc. The overall robust estimation process uses the normalised DLT
 * algorithm (see {@link TransformUtilities#affineMatrix(List)}) coupled with
 * either {@link RANSAC} or {@link LMedS}.
 * <p>
 * Non-linear optimisation is unncessary as the algebraic and geometric
 * distances are equal in the affine case.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class RobustAffineTransformEstimator implements RobustModelFitting<Point2d, Point2d, AffineTransformModel> {
	private RobustModelFitting<Point2d, Point2d, AffineTransformModel> robustFitter;

	/**
	 * Construct using the {@link LMedS} algorithm with the given expected
	 * outlier percentage
	 * 
	 * @param outlierProportion
	 *            expected proportion of outliers (between 0 and 1)
	 */
	public RobustAffineTransformEstimator(double outlierProportion) {
		robustFitter = new LMedS<Point2d, Point2d, AffineTransformModel>(
				new AffineTransformModel(),
				new AlgebraicResidual2d<AffineTransformModel>(),
				outlierProportion, true);
	}

	/**
	 * Construct using the {@link RANSAC} algorithm with the given options.
	 * 
	 * @param threshold
	 *            the threshold on the {@link AlgebraicResidual2d} at which to
	 *            consider a point as an inlier
	 * @param nIterations
	 *            the maximum number of iterations
	 * @param stoppingCondition
	 *            the {@link StoppingCondition} for RANSAC
	 */
	public RobustAffineTransformEstimator(double threshold, int nIterations, StoppingCondition stoppingCondition)
	{
		robustFitter = new RANSAC<Point2d, Point2d, AffineTransformModel>(new AffineTransformModel(),
				new AlgebraicResidual2d<AffineTransformModel>(), threshold, nIterations, stoppingCondition, true);
	}

	@Override
	public boolean fitData(List<? extends IndependentPair<Point2d, Point2d>> data) {
		// Use a robust fitting technique to find the inliers and estimate a
		// model using DLT
		if (!robustFitter.fitData(data)) {
			// just go with full-on DLT estimate rather than a robust one
			robustFitter.getModel().estimate(data);

			return false;
		}

		return true;
	}

	@Override
	public int numItemsToEstimate() {
		return robustFitter.numItemsToEstimate();
	}

	@Override
	public AffineTransformModel getModel() {
		return robustFitter.getModel();
	}

	@Override
	public List<? extends IndependentPair<Point2d, Point2d>> getInliers() {
		return robustFitter.getInliers();
	}

	@Override
	public List<? extends IndependentPair<Point2d, Point2d>> getOutliers() {
		return robustFitter.getOutliers();
	}
}
