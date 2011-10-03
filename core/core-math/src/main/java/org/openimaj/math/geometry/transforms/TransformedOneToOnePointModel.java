package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * A TransformedOneToOnePointModel is an extension of a OneToOnePointModel that allows
 * arbitary transform matrices to be applied to both point sets before equality testing.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class TransformedOneToOnePointModel extends OneToOnePointModel {
	protected Matrix secondTransform;
	protected Matrix firstTransform;

	/**
	 * Construct with the given transform matrices for transforming the
	 * points before comparison. The threshold is set to 0, so points must
	 * be at exactly the same transformed location to match.
	 * 
	 * @param firstTransform the first transform matrix 
	 * @param secondTransform the second transform matrix
	 */
	public TransformedOneToOnePointModel(Matrix firstTransform, Matrix secondTransform) {
		this(0, firstTransform, secondTransform);
	}
	
	/**
	 * Construct with the given transform matrices for transforming the
	 * points before comparison. The threshold parameter controls how far
	 * transformed points are allowed to be from each other to still be 
	 * considered the same. 
	 * 
	 * @param threshold the threshold.
	 * @param firstTransform the first transform matrix 
	 * @param secondTransform the second transform matrix
	 */
	public TransformedOneToOnePointModel(float threshold, Matrix firstTransform, Matrix secondTransform) {
		this.threshold = 0;
		this.firstTransform = firstTransform;
		this.secondTransform = secondTransform;
	}
	
	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		double distance = Line2d.distance(data.firstObject().transform(firstTransform), data.secondObject().transform(secondTransform));
		return distance <= threshold;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> data) {
		double error = 0;
		
		for (IndependentPair<Point2d, Point2d> d : data) {
			double distance = Line2d.distance(d.firstObject().transform(firstTransform), d.secondObject().transform(secondTransform));
			if (distance > threshold) error++;
		}
		
		return error / data.size();
	}
	
	@Override
	public TransformedOneToOnePointModel clone() {
		return (TransformedOneToOnePointModel) super.clone();
	}
}
