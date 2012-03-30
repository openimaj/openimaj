package org.openimaj.ml.timeseries.interpolation;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

public class LinearTimeSeriesInterpolationTests {
	
	/**
	 * Check to make sure when the time requested is identical that the number
	 * is exactly the same
	 */
	@Test
	public void testIdenticalTimeSeries(){
		long[] times = new long[]{1,10,25,28,30};
		double[] data= new double[]{10,20,30,40,50};
		
		DoubleTimeSeries dts = new DoubleTimeSeries(times,data);
		LinearTimeSeriesInterpolation interp = new LinearTimeSeriesInterpolation(dts);
		
		DoubleTimeSeries dts2 = interp.interpolate(times);
		assertTrue(Arrays.equals(dts2.getData(), dts.getData()));
		
		DoubleTimeSeries dts3 = interp.interpolate(new long[]{0,11,25,29,31});
		double[] dts3d = dts3.getData();
		assertTrue(dts3d[0] == data[0]);
		assertTrue(dts3d[1] != data[1]);
		assertTrue(dts3d[2] == data[2]);
		assertTrue(dts3d[3] != data[3]);
		assertTrue(dts3d[4] == data[4]);
	}
	
	@Test
	public void testInterpolatedTimeSeries(){
		long[] times = new long[]{0,10,20,30,40};
		double[] data= new double[]{10,20,30,40,50};
		
		DoubleTimeSeries dts = new DoubleTimeSeries(times,data);
		LinearTimeSeriesInterpolation interp = new LinearTimeSeriesInterpolation(dts);
		
		DoubleTimeSeries dts3 = interp.interpolate(new long[]{5,15,25,35});
		double[] dts3d = dts3.getData();
		assertTrue(dts3d[0] == (data[0] + data[1])/2 );
		assertTrue(dts3d[1] == (data[1] + data[2])/2);
		assertTrue(dts3d[2] == (data[2] + data[3])/2);
		assertTrue(dts3d[3] == (data[3] + data[4])/2);
	}
	
	@Test
	public void testInterpolatedModes(){
		long[] times = new long[]{0,10,20,30,40};
		double[] data= new double[]{10,20,30,40,50};
		
		DoubleTimeSeries dts = new DoubleTimeSeries(times,data);
		System.out.println(dts);
		LinearTimeSeriesInterpolation interp = new LinearTimeSeriesInterpolation(dts);
		
		DoubleTimeSeries dts2 = interp.interpolate(0l, 41l, 10l);
		DoubleTimeSeries dts3 = interp.interpolate(0l, 5, 10l);
		DoubleTimeSeries dts4 = interp.interpolate(0l, 40l, 5);
		assertTrue(Arrays.equals(dts2.getTimes(),times));
		assertTrue(Arrays.equals(dts3.getTimes(),times));
		assertTrue(Arrays.equals(dts4.getTimes(),times));
	}
}
