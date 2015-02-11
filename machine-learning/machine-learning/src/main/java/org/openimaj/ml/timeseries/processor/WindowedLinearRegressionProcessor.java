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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.ml.regression.LinearRegression;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * An implementation of an autoregressive model such that Xt =
 * b*X{t-offset-window,t-offset} + c
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WindowedLinearRegressionProcessor implements TimeSeriesProcessor<double[], Double, DoubleTimeSeries> {

	private static final int DEFAULT_WINDOW_SIZE = 3;
	private static final int DEFAULT_OFFSET = 1;
	private LinearRegression reg;
	private boolean regdefined;
	private int windowsize;
	private int offset;

	/**
	 * Calculate the regression from the same time series inputed
	 */
	public WindowedLinearRegressionProcessor() {
		this.windowsize = DEFAULT_WINDOW_SIZE;
		this.offset = DEFAULT_OFFSET;
		this.regdefined = false;
	}

	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to
	 * windowsize
	 *
	 * @param windowsize
	 */
	public WindowedLinearRegressionProcessor(int windowsize) {
		this.windowsize = windowsize;
		this.offset = DEFAULT_OFFSET;
		this.regdefined = false;

	}

	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to
	 * windowsize
	 *
	 * @param windowsize
	 * @param offset
	 */
	public WindowedLinearRegressionProcessor(int windowsize, int offset) {
		this.windowsize = windowsize;
		this.offset = offset;
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
	public WindowedLinearRegressionProcessor(LinearRegression reg) {
		this.reg = reg;
		this.regdefined = true;
	}

	public WindowedLinearRegressionProcessor(DoubleTimeSeries yearFirstHalf, int i) {
		final WindowedLinearRegressionProcessor inner = new WindowedLinearRegressionProcessor(i);
		inner.process(yearFirstHalf);
		this.reg = inner.reg;
		this.windowsize = i;
		this.regdefined = true;
	}

	public WindowedLinearRegressionProcessor(DoubleTimeSeries yearFirstHalf, int windowsize, int offset) {
		final WindowedLinearRegressionProcessor inner = new WindowedLinearRegressionProcessor(windowsize, offset);
		inner.process(yearFirstHalf);
		this.reg = inner.reg;
		this.windowsize = windowsize;
		this.offset = offset;
		this.regdefined = true;
	}

	@Override
	public void process(DoubleTimeSeries series) {
		final List<IndependentPair<double[], double[]>> instances = new ArrayList<IndependentPair<double[], double[]>>();
		final double[] data = series.getData();

		for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
			final int start = i - this.windowsize - (offset - 1);
			final double[] datawindow = new double[this.windowsize];
			System.arraycopy(data, start, datawindow, 0, this.windowsize);
			instances.add(IndependentPair.pair(datawindow, new double[] { data[i] }));
		}
		if (!regdefined)
		{
			this.reg = new LinearRegression();
			this.reg.estimate(instances);
		}
		System.out.println(this.reg);
		final Iterator<IndependentPair<double[], double[]>> instanceIter = instances.iterator();
		for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
			data[i] = this.reg.predict(instanceIter.next().firstObject())[0];
		}
		final long[] times = series.getTimes();
		final long begin = times[this.windowsize + (offset - 1)];
		final long end = times[times.length - 1];
		series.internalAssign(series.get(begin, end));
	}

	/**
	 * @param regdefined
	 *            if true, process holds its last {@link LinearRegression}
	 */
	public void holdreg(boolean regdefined) {
		this.regdefined = regdefined;
	}

	/**
	 * @return the {@link WindowedLinearRegressionProcessor}'s underlying
	 *         {@link LinearRegression} model
	 */
	public LinearRegression getRegression() {
		return this.reg;
	}

}
