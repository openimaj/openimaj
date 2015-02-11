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
package org.openimaj.ml.timeseries.aggregator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openimaj.ml.regression.LinearRegression;
import org.openimaj.ml.timeseries.series.DoubleSynchronisedTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.util.pair.IndependentPair;

/**
 * An implementation of a general linear regressive such that the values of a
 * timeseries Y are predicted using the values of a set of time series X at some
 * offset over some time window. X may potentially contain Y itself which turns
 * this into an auto-regressive model augmented with extra information.
 * Furthermore, varying window sizes and offsets may be used for each time
 * series X.
 *
 * This is all achieved with {@link SynchronisedTimeSeriesCollection} which
 * model a set of timeseries which are synchronised.
 *
 * When intitalised, the Y time series must be explicitly specified. By default
 * the
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WindowedLinearRegressionAggregator implements SynchronisedTimeSeriesCollectionAggregator<
		DoubleTimeSeries,
		DoubleSynchronisedTimeSeriesCollection,
		DoubleTimeSeries>
{

	private static final int DEFAULT_WINDOW_SIZE = 3;
	private static final int DEFAULT_OFFSET = 1;
	private LinearRegression reg;
	private boolean autoregressive = true;
	private List<IndependentPair<Integer, Integer>> windowOffsets;
	private String ydataName;

	private WindowedLinearRegressionAggregator() {
		this.windowOffsets = new ArrayList<IndependentPair<Integer, Integer>>();
	}

	/**
	 * Calculate the regression from the same time series inputed
	 * 
	 * @param ydataName
	 */
	public WindowedLinearRegressionAggregator(String ydataName) {
		this();
		windowOffsets.add(IndependentPair.pair(DEFAULT_WINDOW_SIZE, DEFAULT_OFFSET));
		this.ydataName = ydataName;
	}

	/**
	 * Calculate the regression from the same time series inputed
	 * 
	 * @param ydataName
	 * @param autoregressive
	 *            whether the ydata should be used in regression
	 */
	public WindowedLinearRegressionAggregator(String ydataName, boolean autoregressive) {
		this();
		windowOffsets.add(IndependentPair.pair(DEFAULT_WINDOW_SIZE, DEFAULT_OFFSET));
		this.ydataName = ydataName;
		this.autoregressive = autoregressive;
	}

	/**
	 * Perform regression s.t. Y = Sum(w_{0-i} * x_{0-i}) + c using the same
	 * window size for all other time series
	 * 
	 * @param ydataName
	 * @param windowsize
	 */
	public WindowedLinearRegressionAggregator(String ydataName, int windowsize) {
		this();
		this.ydataName = ydataName;
		windowOffsets.add(IndependentPair.pair(windowsize, DEFAULT_OFFSET));

	}

	/**
	 * Perform regression s.t. Y = Sum(w_{0-i} * x_{0-i}) + c using the same
	 * window size for all other time series
	 * 
	 * @param ydataName
	 * @param windowsize
	 * @param autoregressive
	 *            whether the ydata should be used in regression
	 */
	public WindowedLinearRegressionAggregator(String ydataName, int windowsize, boolean autoregressive) {
		this();
		this.ydataName = ydataName;
		windowOffsets.add(IndependentPair.pair(windowsize, DEFAULT_OFFSET));
		this.autoregressive = autoregressive;

	}

	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to
	 * windowsize with some offset. The same windowsize and offset is used for
	 * each time series
	 * 
	 * @param ydataName
	 * @param windowsize
	 * @param offset
	 */
	public WindowedLinearRegressionAggregator(String ydataName, int windowsize, int offset) {
		this();
		this.ydataName = ydataName;
		windowOffsets.add(IndependentPair.pair(windowsize, offset));

	}

	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to
	 * windowsize with some offset. The same windowsize and offset is used for
	 * each time series
	 * 
	 * @param ydataName
	 * @param windowsize
	 * @param offset
	 * @param autoregressive
	 *            whether the ydata should be used in regression
	 */
	public WindowedLinearRegressionAggregator(String ydataName, int windowsize, int offset, boolean autoregressive) {
		this();
		this.ydataName = ydataName;
		this.autoregressive = autoregressive;
		windowOffsets.add(IndependentPair.pair(windowsize, offset));

	}

	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to
	 * windowsize with some offset. The same windowsize and offset is used for
	 * each time series
	 * 
	 * @param ydataName
	 * @param windowsize
	 * @param offset
	 * @param autoregressive
	 *            whether the ydata should be used in regression
	 */
	public WindowedLinearRegressionAggregator(String ydataName, int windowsize, int offset, boolean autoregressive,
			DoubleSynchronisedTimeSeriesCollection other)
	{
		this();
		final WindowedLinearRegressionAggregator regress = new WindowedLinearRegressionAggregator(ydataName, windowsize,
				offset, autoregressive);
		regress.aggregate(other);
		this.reg = regress.reg;
		this.ydataName = regress.ydataName;
		this.autoregressive = regress.autoregressive;
		this.windowOffsets = regress.windowOffsets;
	}

	/**
	 * Perform regression s.t. y = Sum(w_{0-i} * x_{0-i}) + c for i from 1 to
	 * windowsize with some offset. The same windowsize and offset is used for
	 * each time series
	 * 
	 * @param ydataName
	 * @param autoregressive
	 *            whether the ydata should be used in regression
	 * @param windowOffsets
	 */
	@SafeVarargs
	public WindowedLinearRegressionAggregator(String ydataName, boolean autoregressive,
			IndependentPair<Integer, Integer>... windowOffsets)
	{
		this();
		this.ydataName = ydataName;
		this.autoregressive = autoregressive;
		for (final IndependentPair<Integer, Integer> independentPair : windowOffsets) {
			this.windowOffsets.add(independentPair);
		}
	}

	// @Override
	// public void process(DoubleTimeSeries series) {
	// Matrix x = new Matrix(new
	// double[][]{ArrayUtils.longToDouble(series.getTimes())}).transpose();
	// List<IndependentPair<double[], double[]>> instances = new
	// ArrayList<IndependentPair<double[], double[]>>();
	// double[] data = series.getData();
	//
	// for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
	// int start = i - this.windowsize - (offset - 1);
	// //
	// System.out.format("Range %d->%d (inclusive) used to calculate: %d\n",start,start+this.windowsize-1,i);
	// double[] datawindow = new double[this.windowsize];
	// System.arraycopy(data, start, datawindow, 0, this.windowsize);
	// instances.add(IndependentPair.pair(datawindow, new double[]{data[i]}));
	// }
	// if(!regdefined)
	// {
	// this.reg = new LinearRegression();
	// this.reg.estimate(instances);
	// }
	// System.out.println(this.reg);
	// Iterator<IndependentPair<double[], double[]>> instanceIter =
	// instances.iterator();
	// for (int i = this.windowsize + (offset - 1); i < series.size(); i++) {
	// data[i] = this.reg.predict(instanceIter.next().firstObject())[0];
	// }
	// }

	/**
	 * @return the {@link WindowedLinearRegressionAggregator}'s underlying
	 *         {@link LinearRegression} model
	 */
	public LinearRegression getRegression() {
		return this.reg;
	}

	@Override
	public DoubleTimeSeries aggregate(DoubleSynchronisedTimeSeriesCollection series) {

		final Set<String> names = series.getNames();
		if (!autoregressive) {
			names.remove(ydataName);
		}
		final DoubleTimeSeries yseries = series.series(ydataName);
		final double[] ydata = yseries.getData();

		series = series.collectionByNames(names);
		final double[] data = series.flatten();
		if (this.windowOffsets.size() != series.nSeries() && this.windowOffsets.size() == 1) {
			final IndependentPair<Integer, Integer> offset = this.windowOffsets.get(0);
			return aggregteSingle(yseries.getTimes(), ydata, data, offset.firstObject(), offset.secondObject(),
					series.nSeries());
		}

		return null;
	}

	private DoubleTimeSeries aggregteSingle(long[] times, double[] ydata, double[] data, int windowsize, int offset,
			int nseries)
	{
		final List<IndependentPair<double[], double[]>> instances = new ArrayList<IndependentPair<double[], double[]>>();
		for (int i = windowsize + (offset - 1); i < ydata.length; i++) {
			final int start = (i - windowsize - (offset - 1)) * nseries;
			// System.out.format("Range %d->%d (inclusive) used to calculate: %d\n",start,start+this.windowsize-1,i);
			final double[] datawindow = new double[windowsize * nseries];
			System.arraycopy(data, start, datawindow, 0, windowsize * nseries);
			instances.add(IndependentPair.pair(datawindow, new double[] { ydata[i] }));
		}
		if (this.reg == null) {
			this.reg = new LinearRegression();
			this.reg.estimate(instances);
		}

		final DoubleTimeSeries ret = new DoubleTimeSeries(times, new double[ydata.length]);

		final Iterator<IndependentPair<double[], double[]>> instanceIter = instances.iterator();
		data = ret.getData();
		for (int i = windowsize + (offset - 1); i < ydata.length; i++) {
			final double[] predicted = this.reg.predict(instanceIter.next().firstObject());
			data[i] = predicted[0];
		}
		return ret.get(times[windowsize + (offset - 1)], times[ydata.length - 1]);
	}

	public LinearRegression getReg() {
		return this.reg;
	}

}
