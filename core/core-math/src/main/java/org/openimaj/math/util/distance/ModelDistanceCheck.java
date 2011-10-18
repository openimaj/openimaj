package org.openimaj.math.util.distance;

import org.openimaj.math.model.Model;

/**
 * Implementation of a {@link DistanceCheck} that tests the
 * distance against a fixed threshold.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ModelDistanceCheck implements DistanceCheck {
	Model<Double, Boolean> model;
	
	public ModelDistanceCheck(Model<Double, Boolean> model) {
		this.model = model;
	}

	@Override
	public boolean check(double distance) {
		return model.predict(distance);
	}
	
	/**
	 * @return the model
	 */
	public Model<Double, Boolean> getModel() {
		return model;
	}
}