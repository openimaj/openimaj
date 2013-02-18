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
import org.openimaj.ml.timeseries.collection.TimeSeriesCollection;
import org.openimaj.ml.timeseries.converter.DoubleProviderTimeSeriesConverter;
import org.openimaj.ml.timeseries.processor.GaussianTimeSeriesProcessor;
import org.openimaj.ml.timeseries.processor.IntervalSummationProcessor;
import org.openimaj.ml.timeseries.processor.MovingAverageProcessor;
import org.openimaj.ml.timeseries.processor.interpolation.LinearInterpolationProcessor;
import org.openimaj.ml.timeseries.processor.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesCollection;
import org.openimaj.ml.timeseries.series.DoubleTimeSeriesProvider;
import org.openimaj.twitter.finance.YahooFinanceData;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class WordDFIDFTSPlayground {
	/**
	 * @param args
	 * @throws IOException
	 * @throws IncompatibleTimeSeriesException
	 */
	public static void main(String[] args) throws IOException, IncompatibleTimeSeriesException {
		TSCollection coll = new TSCollection();
		final String input = "/Users/ss/Development/data/trendminer-data/datasets/sheffield/2010/AAPLwithhashtags.specific.fixed";
		WordDFIDFTimeSeriesCollection AAPLwords = IOUtils.read(new File(input), WordDFIDFTimeSeriesCollection.class);
		AAPLwords = AAPLwords.collectionByNames("#apple");
		final DateTimeFormatter f = DateTimeFormat.forPattern("YYYY MM dd");
		final DateTime begin = f.parseDateTime("2010 01 01");
		final DateTime end = f.parseDateTime("2010 12 31");
		final long gap = 24 * 60 * 60 * 1000;
		final long[] times = TimeSpanUtils.getTime(begin.getMillis(), end.getMillis(), gap);
		AAPLwords.processInternalInplace(new IntervalSummationProcessor<WordDFIDF[], WordDFIDF, WordDFIDFTimeSeries>(
				times));
		DoubleTimeSeriesCollection converted = AAPLwords.convertInternal(
				new DoubleProviderTimeSeriesConverter<WordDFIDF[], WordDFIDF, WordDFIDFTimeSeries>(),
				new MovingAverageProcessor(30 * 24 * 60 * 60 * 1000l),
				new DoubleTimeSeriesCollection()
				);
		timeSeriesToChart(AAPLwords, coll);
		timeSeriesToChart(converted, coll, " - movingaverage");
		converted = AAPLwords.convertInternal(
				new DoubleProviderTimeSeriesConverter<WordDFIDF[], WordDFIDF, WordDFIDFTimeSeries>(),
				new GaussianTimeSeriesProcessor(3),
				new DoubleTimeSeriesCollection()
				);
		timeSeriesToChart(converted, coll, " - gaussian");
		displayTimeSeries(coll, "AAPL words DFIDF", "date", "dfidf sum");

		// Load the finance data
		YahooFinanceData data = new YahooFinanceData("AAPL", begin, end);
		data = Cache.load(data);
		coll = new TSCollection();

		timeSeriesToChart("AAPL Moving Average",
				data.seriesByName("High").process(new MovingAverageProcessor(30 * 24 * 60 * 60 * 1000l)), coll);
		timeSeriesToChart(
				"AAPL Interpolated",
				data.seriesByName("High").process(
						new LinearInterpolationProcessor(begin.getMillis(), end.getMillis(), gap)), coll);
		timeSeriesToChart("AAPL", data.seriesByName("High"), coll);
		displayTimeSeries(coll, "AAPL High", "date", "price");
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

	private static void timeSeriesToChart(TimeSeriesCollection<?, ?, ?, ? extends DoubleTimeSeriesProvider> dstsc,
			TSCollection coll, String... append)
	{
		for (final String seriesName : dstsc.getNames()) {
			final DoubleTimeSeries series = dstsc.series(seriesName).doubleTimeSeries();
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
