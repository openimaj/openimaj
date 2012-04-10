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
import org.openimaj.ml.timeseries.processor.LinearRegressionProcessor;
import org.openimaj.ml.timeseries.processor.MovingAverageProcessor;
import org.openimaj.ml.timeseries.processor.WindowedLinearRegressionProcessor;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
import org.openimaj.twitter.finance.YahooFinanceData;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author ss
 *
 */
public class LinearRegressionPlayground {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String stock = "AAPL";
		String start = "2010-01-01";
		String end = "2010-12-31";
		String learns = "2010-01-01";
		String learne = "2010-05-01";
		YahooFinanceData data = new YahooFinanceData(stock,start,end,"YYYY-MM-dd");
		data = Cache.load(data);
		DoubleTimeSeries highseries = data.seriesMap().get("High");
		DateTimeFormatter parser= DateTimeFormat.forPattern("YYYY-MM-dd");
		long learnstart = parser.parseDateTime(learns).getMillis();
		long learnend = parser.parseDateTime(learne).getMillis();
		DoubleTimeSeries yearFirstHalf = highseries.get(learnstart, learnend);
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(timeSeriesToChart("High Value",highseries));
		dataset.addSeries(timeSeriesToChart("High Value MA",highseries.process(new MovingAverageProcessor(30l * 24l * 60l * 60l * 1000l))));
		dataset.addSeries(timeSeriesToChart("High Value LR",highseries.process(new LinearRegressionProcessor())));
		dataset.addSeries(timeSeriesToChart("High Value WLR-10",highseries.process(new WindowedLinearRegressionProcessor(10,1))));
		dataset.addSeries(timeSeriesToChart("High Value WLR-10 unseen",highseries.process(new WindowedLinearRegressionProcessor(yearFirstHalf,10,1))));
		displayTimeSeries(dataset);
		
	}

	private static void displayTimeSeries(TimeSeriesCollection dataset) {
		JFreeChart chart = ChartFactory.createTimeSeriesChart("APPL 2010", "Day", "High Price", dataset, true, false, false);
		ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		JFrame j = new JFrame();
		j.setContentPane(panel);
		j.pack();
		j.setVisible(true);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static org.jfree.data.time.TimeSeries timeSeriesToChart(String name, DoubleTimeSeries highseries) {
		TimeSeries ret = new TimeSeries(name);
		for (IndependentPair<Long, Double> pair : highseries) {
			DateTime dt = new DateTime(pair.firstObject());
			Day d = new Day(dt.getDayOfMonth(), dt.getMonthOfYear(), dt.getYear());
			ret.add(d,pair.secondObject());
		}
		return ret;
	}
}
