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

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeries;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeriesCollection;
import org.openimaj.io.Cache;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.timeseries.IncompatibleTimeSeriesException;
import org.openimaj.ml.timeseries.aggregator.MeanSquaredDifferenceAggregator;
import org.openimaj.ml.timeseries.aggregator.SquaredSummedDifferenceAggregator;
import org.openimaj.ml.timeseries.aggregator.WindowedLinearRegressionAggregator;
import org.openimaj.ml.timeseries.processor.IntervalSummationProcessor;
import org.openimaj.ml.timeseries.processor.MovingAverageProcessor;
import org.openimaj.ml.timeseries.processor.WindowedLinearRegressionProcessor;
import org.openimaj.ml.timeseries.series.DoubleSynchronisedTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.twitter.finance.YahooFinanceData;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class MultipleLinearRegressionPlayground {
	/**
	 * @param args
	 * @throws IOException
	 * @throws IncompatibleTimeSeriesException
	 */
	public static void main(String[] args) throws IOException, IncompatibleTimeSeriesException {
		final String start = "2010-01-01";
		final String end = "2010-12-31";
		final String learns = "2010-01-01";
		final String learne = "2010-05-01";
		linearRegressStocks(start, end, learns, learne, "MSFT", "AAPL");

	}

	@SuppressWarnings("unchecked")
	private static void linearRegressStocks(String start, String end, String learns, String learne, String... stocks)
			throws IncompatibleTimeSeriesException, IOException
	{
		final DoubleSynchronisedTimeSeriesCollection dstsc = new DoubleSynchronisedTimeSeriesCollection();
		for (final String stock : stocks) {
			YahooFinanceData data = new YahooFinanceData(stock, start, end, "YYYY-MM-dd");
			data = Cache.load(data);
			final DoubleTimeSeries highseries = data.seriesMap().get("High");
			dstsc.addTimeSeries(stock, highseries);
		}
		TSCollection dataset = new TSCollection();
		timeSeriesToChart(dstsc, dataset);
		final DoubleSynchronisedTimeSeriesCollection movingAverage = dstsc.processInternal(new MovingAverageProcessor(30l
				* 24l * 60l * 60l * 1000l));
		timeSeriesToChart(movingAverage, dataset, "-MA");
		displayTimeSeries(dataset, StringUtils.join(stocks, " & "), "Date", "Price");

		dataset = new TSCollection();
		timeSeriesToChart("AAPL", dstsc.series("AAPL"), dataset);

		final DoubleTimeSeries interp = dstsc.series("AAPL").process(new WindowedLinearRegressionProcessor(10, 7));
		timeSeriesToChart("AAPL-interp", interp, dataset);
		long[] interpTimes = interp.getTimes();
		DoubleTimeSeries importantAAPL = dstsc.series("AAPL").get(interpTimes[0], interpTimes[interpTimes.length - 1]);
		final DoubleSynchronisedTimeSeriesCollection aaplinterp = new DoubleSynchronisedTimeSeriesCollection(
				IndependentPair.pair("AAPL", importantAAPL),
				IndependentPair.pair("AAPL-interp", interp)
				);
		System.out
				.println("AAPL linear regression SSE: " + new SquaredSummedDifferenceAggregator().aggregate(aaplinterp));

		final DoubleTimeSeries interpmsft = new WindowedLinearRegressionAggregator("AAPL", 10, 7, true).aggregate(dstsc);
		timeSeriesToChart("AAPL-interpmstf", interpmsft, dataset);
		interpTimes = interpmsft.getTimes();
		importantAAPL = dstsc.series("AAPL").get(interpTimes[0], interpTimes[interpTimes.length - 1]);
		final DoubleSynchronisedTimeSeriesCollection aaplmsftinterp = new DoubleSynchronisedTimeSeriesCollection(
				IndependentPair.pair("AAPL", importantAAPL),
				IndependentPair.pair("AAPLMSFT-interp", interpmsft)
				);
		System.out.println("AAPL+MSFT linear regression SSE: "
				+ new SquaredSummedDifferenceAggregator().aggregate(aaplmsftinterp));
		displayTimeSeries(dataset, StringUtils.join(stocks, " & ") + " Interp", "Date", "Price");

		dataset = new TSCollection();
		final DoubleTimeSeries highseries = dstsc.series("AAPL");
		final DateTimeFormatter parser = DateTimeFormat.forPattern("YYYY-MM-dd");
		final long learnstart = parser.parseDateTime(learns).getMillis();
		final long learnend = parser.parseDateTime(learne).getMillis();
		final DoubleSynchronisedTimeSeriesCollection aaplworddfidf = loadwords("AAPL", dstsc.series("AAPL"));
		final DoubleSynchronisedTimeSeriesCollection yearFirstHalf = aaplworddfidf.get(learnstart, learnend);
		final DoubleTimeSeries interpidf107 = new WindowedLinearRegressionAggregator("AAPL", 10, 7, true)
				.aggregate(aaplworddfidf);
		final DoubleTimeSeries interpidf31 = new WindowedLinearRegressionAggregator("AAPL", 3, 1, true)
				.aggregate(aaplworddfidf);
		final DoubleTimeSeries interpidf107unseen = new WindowedLinearRegressionAggregator("AAPL", 10, 7, true,
				yearFirstHalf).aggregate(aaplworddfidf);

		final double e107 = MeanSquaredDifferenceAggregator.error(interpidf107, highseries);
		final double e31 = MeanSquaredDifferenceAggregator.error(interpidf31, highseries);
		final double e107u = MeanSquaredDifferenceAggregator.error(interpidf107unseen, highseries);

		// dataset.addSeries(timeSeriesToChart(String.format("OLR (m=7,n=10) (MSE=%.2f)",e107),windowedLinearRegression107));
		// dataset.addSeries(timeSeriesToChart(String.format("OLR (m=1,n=3) (MSE=%.2f)",e31),windowedLinearRegression31));
		// dataset.addSeries(timeSeriesToChart(String.format("OLR unseen (m=7,n=10) (MSE=%.2f)",e107u),windowedLinearRegression107unseen));
		timeSeriesToChart("High Value", highseries, dataset);
		timeSeriesToChart(String.format("OLR (m=7,n=10) (MSE=%.2f)", e107), interpidf107, dataset);
		timeSeriesToChart(String.format("OLR (m=1,n=3) (MSE=%.2f)", e31), interpidf31, dataset);
		timeSeriesToChart(String.format("OLR unseen (m=7,n=10) (MSE=%.2f)", e107u), interpidf107unseen, dataset);
		displayTimeSeries(dataset, StringUtils.join(stocks, " & ") + " Interp", "Date", "Price");
	}

	private static DoubleSynchronisedTimeSeriesCollection loadwords(String name, DoubleTimeSeries stocks)
			throws IOException, IncompatibleTimeSeriesException
	{
		final WordDFIDFTimeSeriesCollection AAPLwords = IOUtils.read(new File(
				"/Users/ss/Development/data/trendminer-data/datasets/sheffield/2010/part-r-00000"),
				WordDFIDFTimeSeriesCollection.class);
		AAPLwords.processInternalInplace(new IntervalSummationProcessor<WordDFIDF[], WordDFIDF, WordDFIDFTimeSeries>(
				stocks.getTimes()));

		final DoubleSynchronisedTimeSeriesCollection coll = new DoubleSynchronisedTimeSeriesCollection();
		coll.addTimeSeries(name, stocks);
		for (final String aname : AAPLwords.getNames()) {
			coll.addTimeSeries(aname, AAPLwords.series(aname).doubleTimeSeries());
		}
		return coll;
	}

	private static void displayTimeSeries(TSCollection dataset, String name, String xname, String yname) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(name, xname, yname, dataset, true, false, false);
		final ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		final JFrame j = new JFrame();
		j.setContentPane(panel);
		j.pack();
		j.setVisible(true);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static void timeSeriesToChart(DoubleSynchronisedTimeSeriesCollection dstsc, TSCollection coll,
			String... append)
	{
		for (final String seriesName : dstsc.getNames()) {
			final DoubleTimeSeries series = dstsc.series(seriesName);
			final TimeSeries ret = new TimeSeries(seriesName + StringUtils.join(append, "-"));
			for (final IndependentPair<Long, Double> pair : series) {
				final DateTime dt = new DateTime(pair.firstObject());
				final Day d = new Day(dt.getDayOfMonth(), dt.getMonthOfYear(), dt.getYear());
				ret.add(d, pair.secondObject());
			}
			coll.addSeries(ret);
		}
	}

	private static void timeSeriesToChart(String name, DoubleTimeSeries highseries, TSCollection coll) {
		final TimeSeries ret = new TimeSeries(name);
		for (final IndependentPair<Long, Double> pair : highseries) {
			final DateTime dt = new DateTime(pair.firstObject());
			final Day d = new Day(dt.getDayOfMonth(), dt.getMonthOfYear(), dt.getYear());
			ret.add(d, pair.secondObject());
		}
		coll.addSeries(ret);
	}
}
