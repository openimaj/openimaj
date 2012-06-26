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
package org.openimaj.ml.timeseries.interpolation;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openimaj.ml.timeseries.processor.interpolation.LinearInterpolationProcessor;
import org.openimaj.ml.timeseries.processor.interpolation.util.TimeSpanUtils;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;
/**
 * Test interpolation related things
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
		
		DoubleTimeSeries dts2 = new LinearInterpolationProcessor(times).interpolate(dts);
		assertArrayEquals(dts2.getData(), dts.getData(),0.01);
		
		DoubleTimeSeries dts3 = new LinearInterpolationProcessor(new long[]{0,11,25,29,31}).interpolate(dts);
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
		LinearInterpolationProcessor interp = new LinearInterpolationProcessor();
		
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
		LinearInterpolationProcessor interp = new LinearInterpolationProcessor();
		
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
