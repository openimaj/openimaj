package org.openimaj.ml.timeseries.series;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;
import org.openimaj.ml.timeseries.collection.TimeSeriesCollection;

/**
 * A set of time {@link DoubleTimeSeries} which may not be synchronised.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleTimeSeriesCollection extends TimeSeriesCollection<double[], Double, DoubleTimeSeriesCollection,DoubleTimeSeries> implements ReadWriteableASCII{

	@Override
	public DoubleTimeSeries internalNewInstance() {
		return new DoubleTimeSeries();
	}

	@Override
	public DoubleTimeSeriesCollection newInstance() {
		return new DoubleTimeSeriesCollection();
	}
	
	@Override
	public Map<String, double[]> getData() {
		Map<String, double[]> ret = new HashMap<String, double[]>();
		for (Entry<String, DoubleTimeSeries>  es : this.timeSeriesHolder.entrySet()) {
			ret.put(es.getKey(), es.getValue().getData());
		}
		return ret;
	}
	
	@Override
	public void internalAssign(DoubleTimeSeriesCollection interpolate) {
		this.timeSeriesHolder = interpolate.timeSeriesHolder;
		
	}

	@Override
	public String toString() {
		String retstr = "A set time series: " + this.timeSeriesHolder.size() + "\n";
		for (Entry<String, DoubleTimeSeries> dts : this.timeSeriesHolder.entrySet()) {
			retstr += dts.getKey() + "\n";
			retstr += dts.getValue() + "\n";
		}
		return retstr;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		
		while(in.hasNext()){
			String name = in.next();
			DoubleTimeSeries v = internalNewInstance();
			v.readASCII(in);
			in.nextLine();
			this.timeSeriesHolder.put(name,v);
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		for (Entry<String, DoubleTimeSeries> es : this.timeSeriesHolder.entrySet()) {
			out.println(es.getKey());
			es.getValue().writeASCII(out);
		}
	}
}
