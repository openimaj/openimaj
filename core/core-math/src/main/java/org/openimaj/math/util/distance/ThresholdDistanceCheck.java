package org.openimaj.math.util.distance;

/**
 * Implementation of a {@link DistanceCheck} that tests the
 * distance against a fixed threshold.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ThresholdDistanceCheck implements DistanceCheck {
	float threshold;
	
	public ThresholdDistanceCheck(float threshold) {
		this.threshold = threshold;
	}

	@Override
	public boolean check(double distance) {
		return distance <= threshold;
	}
}