package org.openimaj.math.model.fit.residuals;

import org.openimaj.math.model.Model;
import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.pair.IndependentPair;

/**
 * An implementation of a {@link ResidualCalculator} that uses a
 * {@link DistanceComparator} to compute the error between the predicted and
 * observed data point. In the case that the given {@link DistanceComparator} is
 * a similarity measure, the similarities will be multiplied by -1 to ensure
 * error increases with decreasing similarity.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 * @param <M>
 *            type of model
 */
public class DistanceComparatorResidual<I, D, M extends Model<I, D>> extends AbstractResidualCalculator<I, D, M> {
	protected DistanceComparator<D> comparator;
	private int multiplier = 1;

	/**
	 * Construct with the given {@link DistanceComparator}.
	 * 
	 * @param comparator
	 *            the {@link DistanceComparator}
	 */
	public DistanceComparatorResidual(DistanceComparator<D> comparator) {
		this.comparator = comparator;

		if (!comparator.isDistance())
			this.multiplier = -1;
	}

	@Override
	public double computeResidual(IndependentPair<I, D> data) {
		final D predicted = model.predict(data.firstObject());

		return multiplier * comparator.compare(data.getSecondObject(), predicted);
	}
}
