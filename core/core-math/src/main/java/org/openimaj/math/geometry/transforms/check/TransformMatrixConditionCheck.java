package org.openimaj.math.geometry.transforms.check;

import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.Model;
import org.openimaj.util.function.Predicate;

/**
 * A check for {@link Model}s that produce transform matrices (via the
 * {@link MatrixTransformProvider}) that tests whether the condition number is
 * below a threshold.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <M>
 */
public class TransformMatrixConditionCheck<M extends Model<?, ?> & MatrixTransformProvider> implements Predicate<M> {
	double threshold;

	/**
	 * Construct the check with the given threshold. The condition number of the
	 * transform matrix must be under this threshold to be considered valid.
	 * 
	 * @param threshold
	 *            the threshold
	 */
	public TransformMatrixConditionCheck(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public boolean test(M model) {
		final double cond = model.getTransform().cond();
		return cond < threshold;
	}

}
