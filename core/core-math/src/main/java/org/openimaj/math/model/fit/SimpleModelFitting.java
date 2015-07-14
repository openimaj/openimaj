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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.model.EstimatableModel;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.math.util.distance.DistanceCheck;
import org.openimaj.math.util.distance.ThresholdDistanceCheck;
import org.openimaj.util.pair.IndependentPair;

/**
 * Example robust fitting, that simply wraps the models estimate method. Inliers
 * and outliers are estimated by verifying the model against the data.
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
public class SimpleModelFitting<I, D, M extends EstimatableModel<I, D>> implements RobustModelFitting<I, D, M> {
	List<IndependentPair<I, D>> inl;
	List<IndependentPair<I, D>> outl;
	M model;

	ResidualCalculator<I, D, M> errorModel;
	DistanceCheck dc;

	/**
	 * Creates a SimpleModelFitting object to fit data to a model
	 *
	 * @param m
	 *            model to fit data to
	 * @param errorModel
	 *            the error model
	 * @param errorThreshold
	 *            the error threshold
	 */
	public SimpleModelFitting(M m, ResidualCalculator<I, D, M> errorModel,
			double errorThreshold)
	{
		model = m;
		this.errorModel = errorModel;
		this.dc = new ThresholdDistanceCheck(errorThreshold);
	}

	/**
	 * Creates a SimpleModelFitting object to fit data to a model
	 *
	 * @param m
	 *            model to fit data to
	 * @param errorModel
	 *            the error model
	 * @param dc
	 *            the error/distance check that determines whether a point is
	 *            valid
	 */
	public SimpleModelFitting(M m, ResidualCalculator<I, D, M> errorModel, DistanceCheck dc)
	{
		model = m;
		this.errorModel = errorModel;
		this.dc = dc;
	}

	@Override
	public List<? extends IndependentPair<I, D>> getInliers() {
		return inl;
	}

	@Override
	public List<? extends IndependentPair<I, D>> getOutliers() {
		return outl;
	}

	@Override
	public boolean fitData(List<? extends IndependentPair<I, D>> data) {
		if (!model.estimate(data))
			return false;

		errorModel.setModel(model);

		inl = new ArrayList<IndependentPair<I, D>>();
		outl = new ArrayList<IndependentPair<I, D>>();

		for (int i = 0; i < data.size(); i++) {
			if (dc.check(errorModel.computeResidual(data.get(i))))
				inl.add(data.get(i));
			else
				outl.add(data.get(i));
		}

		return true;
	}

	@Override
	public M getModel() {
		return model;
	}

	@Override
	public int numItemsToEstimate() {
		return model.numItemsToEstimate();
	}
}
