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
package org.openimaj.math.model.fit;

import java.util.List;

import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

/**
 * An interface that describes an object capable of fitting data to a model in a
 * more generic way than through the Model's estimate method.
 * 
 * @author Jonathon Hare
 * 
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 * @param <M>
 *            concrete type of model learned
 */
public interface ModelFitting<I, D, M extends Model<I, D>> {
	/**
	 * Attempt to fit the given data to the model.
	 * 
	 * @param data
	 *            Data to be fitted
	 * @return true on success, false otherwise
	 */
	boolean fitData(List<? extends IndependentPair<I, D>> data);

	/**
	 * @return The minimum number of observations required to estimate the
	 *         model.
	 */
	public int numItemsToEstimate();

	/**
	 * @return the trained model object
	 */
	M getModel();
}
