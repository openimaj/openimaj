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
package org.openimaj.ml.timeseries.processor;

import org.openimaj.ml.regression.LinearRegression;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.array.ArrayUtils;

import Jama.Matrix;

/**
 * Using a {@link LinearRegression} model, a time series is used as input to
 * calculate the coefficients of a linear regression such that value = b * time
 * + c
 * 
 * This is the simplest kind of model that can be applied to a time series
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LinearRegressionProcessor implements TimeSeriesProcessor<double[], Double, DoubleTimeSeries> {

	private LinearRegression reg;
	private boolean regdefined;

	/**
	 * Calculate the regression from the same time series inputed
	 */
	public LinearRegressionProcessor() {
		this.regdefined = false;
	}

	/**
	 * Use reg as the linear regression to predict. The
	 * {@link #process(DoubleTimeSeries)} function simply calls
	 * {@link LinearRegression#predict(Matrix)} with the times in the series as
	 * input
	 * 
	 * @param reg
	 */
	public LinearRegressionProcessor(LinearRegression reg) {
		this.reg = reg;
		this.regdefined = true;
	}

	@Override
	public void process(DoubleTimeSeries series) {
		final Matrix x = new Matrix(new double[][] { ArrayUtils.convertToDouble(series.getTimes()) }).transpose();
		if (!regdefined)
		{
			this.reg = new LinearRegression();
			final Matrix y = new Matrix(new double[][] { series.getData() }).transpose();
			reg.estimate(y, x);
		}
		final Matrix predicted = this.reg.predict(x);
		series.set(series.getTimes(), predicted.transpose().getArray()[0]);
	}

	/**
	 * @param regdefined
	 *            if true, process holds its last {@link LinearRegression}
	 */
	public void holdreg(boolean regdefined) {
		this.regdefined = regdefined;
	}

	/**
	 * @return the {@link LinearRegressionProcessor}'s underlying
	 *         {@link LinearRegression} model
	 */
	public LinearRegression getRegression() {
		return this.reg;
	}

}
