package org.openimaj.demos.sandbox.ml.regression;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;

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
import org.terrier.utility.ArrayUtils;

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
		String start = "2010-01-01";
		String end = "2010-12-31";
		String learns = "2010-01-01";
		String learne = "2010-05-01";
		linearRegressStocks(start,end,learns,learne,"MSFT","AAPL");
		
		
	}

	private static void linearRegressStocks(String start, String end,String learns, String learne, String ...stocks) throws IncompatibleTimeSeriesException, IOException {
		DoubleSynchronisedTimeSeriesCollection dstsc = new DoubleSynchronisedTimeSeriesCollection();
		for (String stock : stocks ){			
			YahooFinanceData data = new YahooFinanceData(stock,start,end,"YYYY-MM-dd");
			data = Cache.load(data);
			DoubleTimeSeries highseries = data.seriesMap().get("High");
			DateTimeFormatter parser= DateTimeFormat.forPattern("YYYY-MM-dd");
			dstsc.addTimeSeries(stock, highseries);
		}
		TSCollection dataset = new TSCollection();
		timeSeriesToChart(dstsc,dataset);
		DoubleSynchronisedTimeSeriesCollection movingAverage = dstsc.processInternal(new MovingAverageProcessor(30l * 24l * 60l * 60l * 1000l));
		timeSeriesToChart(movingAverage,dataset,"-MA");		
		displayTimeSeries(dataset,ArrayUtils.join(stocks, " & "),"Date","Price");
		
		dataset = new TSCollection();
		timeSeriesToChart("AAPL",dstsc.series("AAPL"),dataset);
		
		DoubleTimeSeries interp = dstsc.series("AAPL").process(new WindowedLinearRegressionProcessor(10, 7));
		timeSeriesToChart("AAPL-interp",interp,dataset);
		long[] interpTimes = interp.getTimes();
		DoubleTimeSeries importantAAPL = dstsc.series("AAPL").get(interpTimes[0], interpTimes[interpTimes.length-1]);
		DoubleSynchronisedTimeSeriesCollection aaplinterp = new DoubleSynchronisedTimeSeriesCollection(
				IndependentPair.pair("AAPL",importantAAPL),
				IndependentPair.pair("AAPL-interp",interp)
		);
		System.out.println("AAPL linear regression SSE: " + new SquaredSummedDifferenceAggregator().aggregate(aaplinterp));
		
		DoubleTimeSeries interpmsft = new WindowedLinearRegressionAggregator("AAPL", 10, 7, true).aggregate(dstsc);
		timeSeriesToChart("AAPL-interpmstf",interpmsft,dataset);
		interpTimes = interpmsft.getTimes();
		importantAAPL = dstsc.series("AAPL").get(interpTimes[0], interpTimes[interpTimes.length-1]);
		DoubleSynchronisedTimeSeriesCollection aaplmsftinterp = new DoubleSynchronisedTimeSeriesCollection(
				IndependentPair.pair("AAPL",importantAAPL),
				IndependentPair.pair("AAPLMSFT-interp",interpmsft)
		);
		System.out.println("AAPL+MSFT linear regression SSE: " + new SquaredSummedDifferenceAggregator().aggregate(aaplmsftinterp));
		displayTimeSeries(dataset,ArrayUtils.join(stocks, " & ") + " Interp","Date","Price");
		
		dataset = new TSCollection();
		DoubleTimeSeries highseries = dstsc.series("AAPL");
		DateTimeFormatter parser= DateTimeFormat.forPattern("YYYY-MM-dd");
		long learnstart = parser.parseDateTime(learns).getMillis();
		long learnend = parser.parseDateTime(learne).getMillis();
		DoubleSynchronisedTimeSeriesCollection aaplworddfidf = loadwords("AAPL",dstsc.series("AAPL"));
		DoubleSynchronisedTimeSeriesCollection yearFirstHalf = aaplworddfidf.get(learnstart, learnend);
		DoubleTimeSeries interpidf107 = new WindowedLinearRegressionAggregator("AAPL", 10, 7, true).aggregate(aaplworddfidf);
		DoubleTimeSeries interpidf31 = new WindowedLinearRegressionAggregator("AAPL", 3, 1, true).aggregate(aaplworddfidf);
		DoubleTimeSeries interpidf107unseen = new WindowedLinearRegressionAggregator("AAPL", 10, 7, true,yearFirstHalf).aggregate(aaplworddfidf);
		
		
		double e107 = MeanSquaredDifferenceAggregator.error(interpidf107,highseries);
		double e31 = MeanSquaredDifferenceAggregator.error(interpidf31,highseries);
		double e107u = MeanSquaredDifferenceAggregator.error(interpidf107unseen,highseries);
		
//		dataset.addSeries(timeSeriesToChart(String.format("OLR (m=7,n=10) (MSE=%.2f)",e107),windowedLinearRegression107));
//		dataset.addSeries(timeSeriesToChart(String.format("OLR (m=1,n=3) (MSE=%.2f)",e31),windowedLinearRegression31));
//		dataset.addSeries(timeSeriesToChart(String.format("OLR unseen (m=7,n=10) (MSE=%.2f)",e107u),windowedLinearRegression107unseen));
		timeSeriesToChart("High Value",highseries,dataset);
		timeSeriesToChart(String.format("OLR (m=7,n=10) (MSE=%.2f)",e107),interpidf107,dataset);
		timeSeriesToChart(String.format("OLR (m=1,n=3) (MSE=%.2f)",e31),interpidf31,dataset);
		timeSeriesToChart(String.format("OLR unseen (m=7,n=10) (MSE=%.2f)",e107u),interpidf107unseen,dataset);
		displayTimeSeries(dataset,ArrayUtils.join(stocks, " & ") + " Interp","Date","Price");
	}

	private static DoubleSynchronisedTimeSeriesCollection loadwords(String name,DoubleTimeSeries stocks) throws IOException, IncompatibleTimeSeriesException {
		WordDFIDFTimeSeriesCollection AAPLwords = IOUtils.read(new File("/Users/ss/Development/data/trendminer-data/datasets/sheffield/2010/part-r-00000"), WordDFIDFTimeSeriesCollection.class);
		AAPLwords.processInternalInplace(new IntervalSummationProcessor<WordDFIDF[],WordDFIDF, WordDFIDFTimeSeries>(stocks.getTimes()));
		
		DoubleSynchronisedTimeSeriesCollection coll = new DoubleSynchronisedTimeSeriesCollection();
		coll.addTimeSeries(name, stocks);
		for (String aname : AAPLwords.getNames()) {
			coll.addTimeSeries(aname, AAPLwords.series(aname).doubleTimeSeries());
		}
		return coll;
	}

	private static void displayTimeSeries(TSCollection dataset, String name, String xname, String yname) {
		JFreeChart chart = ChartFactory.createTimeSeriesChart(name,xname,yname, dataset, true, false, false);
		ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		JFrame j = new JFrame();
		j.setContentPane(panel);
		j.pack();
		j.setVisible(true);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static void timeSeriesToChart(DoubleSynchronisedTimeSeriesCollection dstsc, TSCollection coll,String ... append) {
		for (String seriesName: dstsc.getNames()) {
			DoubleTimeSeries series = dstsc.series(seriesName);
			TimeSeries ret = new TimeSeries(seriesName + ArrayUtils.join(append, "-"));
			for (IndependentPair<Long, Double> pair : series) {
				DateTime dt = new DateTime(pair.firstObject());
				Day d = new Day(dt.getDayOfMonth(), dt.getMonthOfYear(), dt.getYear());
				ret.add(d,pair.secondObject());
			}
			coll.addSeries(ret);
		}
	}
	private static void timeSeriesToChart(String name, DoubleTimeSeries highseries, TSCollection coll) {
		TimeSeries ret = new TimeSeries(name);
		for (IndependentPair<Long, Double> pair : highseries) {
			DateTime dt = new DateTime(pair.firstObject());
			Day d = new Day(dt.getDayOfMonth(), dt.getMonthOfYear(), dt.getYear());
			ret.add(d,pair.secondObject());
		}
		coll.addSeries(ret);
	}
}
