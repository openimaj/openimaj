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
package org.openimaj.demos.sandbox.ml.regression;

import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openimaj.io.Cache;
import org.openimaj.ml.timeseries.IncompatibleTimeSeriesException;
import org.openimaj.ml.timeseries.aggregator.MeanSquaredDifferenceAggregator;
import org.openimaj.ml.timeseries.processor.MovingAverageProcessor;
import org.openimaj.ml.timeseries.processor.WindowedLinearRegressionProcessor;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.twitter.finance.YahooFinanceData;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LinearRegressionPlayground {
	/**
	 * @param args
	 * @throws IOException
	 * @throws IncompatibleTimeSeriesException
	 */
	public static void main(String[] args) throws IOException, IncompatibleTimeSeriesException {
		final String stock = "AAPL";
		final String start = "2010-01-01";
		final String end = "2010-12-31";
		final String learns = "2010-01-01";
		final String learne = "2010-05-01";
		final DateTimeFormatter parser = DateTimeFormat.forPattern("YYYY-MM-dd");
		final long learnstart = parser.parseDateTime(learns).getMillis();
		final long learnend = parser.parseDateTime(learne).getMillis();
		YahooFinanceData data = new YahooFinanceData(stock, start, end, "YYYY-MM-dd");
		data = Cache.load(data);
		final DoubleTimeSeries highseries = data.seriesMap().get("High");
		final DoubleTimeSeries yearFirstHalf = highseries.get(learnstart, learnend);
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(timeSeriesToChart("High Value", highseries));
		final DoubleTimeSeries movingAverage = highseries.process(new MovingAverageProcessor(30l * 24l * 60l * 60l
				* 1000l));
		final DoubleTimeSeries halfYearMovingAverage = yearFirstHalf.process(new MovingAverageProcessor(30l * 24l * 60l
				* 60l * 1000l));

		dataset.addSeries(
				timeSeriesToChart(
						"High Value MA",
						movingAverage
				));
		dataset.addSeries(
				timeSeriesToChart(
						"High Value MA Regressed (all seen)",
						movingAverage.process(new WindowedLinearRegressionProcessor(10, 7))
				));
		dataset.addSeries(
				timeSeriesToChart(
						"High Value MA Regressed (latter half unseen)",
						movingAverage.process(new WindowedLinearRegressionProcessor(halfYearMovingAverage, 10, 7))
				));
		displayTimeSeries(dataset, stock, "Date", "Price");
		dataset = new TimeSeriesCollection();
		dataset.addSeries(timeSeriesToChart("High Value", highseries));
		// final DoubleTimeSeries linearRegression = highseries.process(new
		// LinearRegressionProcessor());

		// double lrmsd =
		// MeanSquaredDifferenceAggregator.error(linearRegression,highseries);
		// dataset.addSeries(timeSeriesToChart(String.format("OLR (MSE=%.2f)",lrmsd),linearRegression));
		final DoubleTimeSeries windowedLinearRegression107 = highseries.process(new WindowedLinearRegressionProcessor(10,
				7));
		final DoubleTimeSeries windowedLinearRegression31 = highseries
				.process(new WindowedLinearRegressionProcessor(3, 1));
		final DoubleTimeSeries windowedLinearRegression107unseen = highseries
				.process(new WindowedLinearRegressionProcessor(yearFirstHalf, 10, 7));

		final double e107 = MeanSquaredDifferenceAggregator.error(windowedLinearRegression107, highseries);
		final double e31 = MeanSquaredDifferenceAggregator.error(windowedLinearRegression31, highseries);
		final double e107u = MeanSquaredDifferenceAggregator.error(windowedLinearRegression107unseen, highseries);

		dataset.addSeries(timeSeriesToChart(String.format("OLR (m=7,n=10) (MSE=%.2f)", e107), windowedLinearRegression107));
		dataset.addSeries(timeSeriesToChart(String.format("OLR (m=1,n=3) (MSE=%.2f)", e31), windowedLinearRegression31));
		dataset.addSeries(timeSeriesToChart(String.format("OLR unseen (m=7,n=10) (MSE=%.2f)", e107u),
				windowedLinearRegression107unseen));
		displayTimeSeries(dataset, stock, "Date", "Price");

	}

	private static void displayTimeSeries(TimeSeriesCollection dataset, String name, String xname, String yname) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(name, xname, yname, dataset, true, false, false);
		final ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		final JFrame j = new JFrame();
		j.setContentPane(panel);
		j.pack();
		j.setVisible(true);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static org.jfree.data.time.TimeSeries timeSeriesToChart(String name, DoubleTimeSeries highseries) {
		final TimeSeries ret = new TimeSeries(name);
		for (final IndependentPair<Long, Double> pair : highseries) {
			final DateTime dt = new DateTime(pair.firstObject());
			final Day d = new Day(dt.getDayOfMonth(), dt.getMonthOfYear(), dt.getYear());
			ret.add(d, pair.secondObject());
		}
		return ret;
	}
}
