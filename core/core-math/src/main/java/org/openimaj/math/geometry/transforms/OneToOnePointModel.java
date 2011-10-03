package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

/**
 * A OneToOnePointModel models a one-to-one mapping of points
 * in a 2d space. For purposes of validation and error calculation,
 * a threshold on the Euclidean distance between a pair of points
 * can be set to determine equality.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class OneToOnePointModel implements Model<Point2d, Point2d> {
	float threshold;
	
	public OneToOnePointModel() {
		this(0);
	}
	
	public OneToOnePointModel(float threshold) {
		this.threshold = 0;
	}
	
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		//OneToOnePointModel doesn't need estimating
	}

	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		double distance = Line2d.distance(data.firstObject(), data.secondObject());
		return distance <= threshold;
	}

	@Override
	public Point2d predict(Point2d data) {
		return data;
	}

	@Override
	public int numItemsToEstimate() {
		return 0;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> data) {
		double error = 0;
		
		for (IndependentPair<Point2d, Point2d> d : data) {
			double distance = Line2d.distance(d.firstObject(), d.secondObject());
			if (distance > threshold) error++;
		}
		
		return error / data.size();
	}
	
	@Override
	public OneToOnePointModel clone() {
		try {
			return (OneToOnePointModel) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
