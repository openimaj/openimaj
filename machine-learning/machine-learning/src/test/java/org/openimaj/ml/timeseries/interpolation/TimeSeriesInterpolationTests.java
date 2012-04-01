package org.openimaj.ml.timeseries.interpolation;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openimaj.ml.timeseries.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
/**
 * Test interpolation related things
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TimeSeriesInterpolationTests {
	
	/**
	 * Check to make sure when the time requested is identical that the number
	 * is exactly the same
	 */
	@Test
	public void testIdenticalTimeSeries(){
		long[] times = new long[]{1,10,25,28,30};
		double[] data= new double[]{10,20,30,40,50};
		
		DoubleTimeSeries dts = new DoubleTimeSeries(times,data);
		
		DoubleTimeSeries dts2 = new LinearTimeSeriesInterpolation(times).interpolate(dts);
		assertArrayEquals(dts2.getData(), dts.getData(),0.01);
		
		DoubleTimeSeries dts3 = new LinearTimeSeriesInterpolation(new long[]{0,11,25,29,31}).interpolate(dts);
		double[] dts3d = dts3.getData();
		assertTrue(dts3d[0] == data[0]);
		assertTrue(dts3d[1] != data[1]);
		assertTrue(dts3d[2] == data[2]);
		assertTrue(dts3d[3] != data[3]);
		assertTrue(dts3d[4] == data[4]);
	}
	
	/**
	 * Make sure we interpolate properly
	 */
	@Test
	public void testInterpolatedTimeSeries(){
		long[] times = new long[]{0,10,20,30,40};
		double[] data= new double[]{10,20,30,40,50};
		
		DoubleTimeSeries dts = new DoubleTimeSeries(times,data);
		LinearTimeSeriesInterpolation interp = new LinearTimeSeriesInterpolation();
		
		DoubleTimeSeries dts3 = interp.interpolate(dts,new long[]{5,15,25,35});
		double[] dts3d = dts3.getData();
		assertTrue(dts3d[0] == (data[0] + data[1])/2 );
		assertTrue(dts3d[1] == (data[1] + data[2])/2);
		assertTrue(dts3d[2] == (data[2] + data[3])/2);
		assertTrue(dts3d[3] == (data[3] + data[4])/2);
	}
	
	/**
	 * Test the different ways we can interpolate
	 */
	@Test
	public void testInterpolatedModes(){
		long[] times = new long[]{0,10,20,30,40};
		double[] data= new double[]{10,20,30,40,50};
		
		DoubleTimeSeries dts = new DoubleTimeSeries(times,data);
		System.out.println(dts);
		LinearTimeSeriesInterpolation interp = new LinearTimeSeriesInterpolation();
		
		DoubleTimeSeries dts2 = interp.interpolate(dts,0l, 41l, 10l);
		DoubleTimeSeries dts3 = interp.interpolate(dts,0l, 5, 10l);
		DoubleTimeSeries dts4 = interp.interpolate(dts,0l, 40l, 5);
		assertTrue(Arrays.equals(dts2.getTimes(),times));
		assertTrue(Arrays.equals(dts3.getTimes(),times));
		assertTrue(Arrays.equals(dts4.getTimes(),times));
	}
	
	/**
	 * 
	 */
	@Test
	public void testTimeSeriesUtil() {
		assertArrayEquals(TimeSpanUtils.getTime(0l, 5, 2l), new long[]{0,2,4,6,8});
		assertArrayEquals(TimeSpanUtils.getTime(0l, 8l, 2l), new long[]{0,2,4,6,8});
		assertArrayEquals(TimeSpanUtils.getTime(0l, 8l, 5), new long[]{0,2,4,6,8});
	}
}
