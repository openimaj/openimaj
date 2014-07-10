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
package org.openimaj.math.model;

import java.util.List;

import org.openimaj.util.pair.IndependentPair;

/**
 * An extension to a {@link Model} that allows the model to be estimated from a
 * series of observations of both the independent and dependent variables.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 */
public interface EstimatableModel<I, D> extends Model<I, D>, Cloneable {
	/**
	 * Estimates the model from the observations in the list of data. The data
	 * must contain at least {@link #numItemsToEstimate()} pairs of dependent
	 * and independent data. It may contain more, in which case the estimate
	 * method may choose to make use of this data for validation, or obtaining a
	 * better model by a least squares method for example.
	 * 
	 * @param data
	 *            Data with which to estimate the model
	 * @return true if a successful estimate was made, false otherwise
	 * @see #numItemsToEstimate()
	 */
	public boolean estimate(List<? extends IndependentPair<I, D>> data);

	/**
	 * @return The minimum number of observations required to estimate the
	 *         model.
	 */
	public int numItemsToEstimate();

	/**
	 * Clone the model
	 * 
	 * @return a cloned copy
	 */
	public EstimatableModel<I, D> clone();
}
