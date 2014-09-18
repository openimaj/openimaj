/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
