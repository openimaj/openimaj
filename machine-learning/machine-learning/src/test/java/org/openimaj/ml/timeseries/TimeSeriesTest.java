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
package org.openimaj.ml.timeseries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openimaj.ml.timeseries.series.ConcreteTimeSeries;
import org.openimaj.ml.timeseries.series.DoubleTimeSeries;

public class TimeSeriesTest {
	
	
	@Test
	public void testDoubleTimeSeriesError(){
		long[] times = new long[]{1,2,5,9,10};
		double[] values = new double[]{1,2,5,9,10};
		DoubleTimeSeries ts = new DoubleTimeSeries(times,values);
		
		DoubleTimeSeries e1 = ts.get(2, 1);
		assertTrue(e1.size() == 0);
		DoubleTimeSeries e2 = ts.get(2, -1, -1);
		assertTrue(e2.size() == 0);
	}
	
	@Test
	public void testConcreteTimeSeriesError(){
		class ConcreteTimeSeriesString extends ConcreteTimeSeries<String,ConcreteTimeSeriesString>{

			@Override
			public ConcreteTimeSeriesString newInstance() {
				return new ConcreteTimeSeriesString();
			}
			
		}
		ConcreteTimeSeries<String,ConcreteTimeSeriesString> ts = new ConcreteTimeSeriesString();
		ts.add(1,"One");
		ts.add(2,"Two");
		ts.add(5,"Five");
		ts.add(9,"Nine");
		ts.add(10,"Ten");
		
		ConcreteTimeSeriesString e1 = ts.get(5, 1);
		assertTrue(e1.size() == 0);
		ConcreteTimeSeriesString e2 = ts.get(2, -1, -1);
		assertTrue(e2.size() == 0);
	}
	
	@Test
	public void testDoubleTimeSeriesIterator() throws TimeSeriesSetException{
		DoubleTimeSeries ts = new DoubleTimeSeries();
		long[] times = new long[]{1,2,5,9,10};
		double[] values = new double[]{1,2,5,9,10};
		
		ts.set(times, values);
//		for (IndependentPair<Long,Double> independentPair : ts) {
//			System.out.println(ts);
//		}
	}
	
	/**
	 * @throws TimeSeriesSetException 
	 * 
	 */
	@Test
	public void testDoubleTimeSeries() throws TimeSeriesSetException{
		DoubleTimeSeries ts = new DoubleTimeSeries();
		long[] times = new long[]{1,2,5,9,10};
		double[] values = new double[]{1,2,5,9,10};
		
		ts.set(times, values);
		
		assertTrue(ts.get(1).getData()[0] == 1);
		assertTrue(ts.get(3).getData().length == 0);
		
		double[] get5 = ts.get(5,2,2).getData();
		assertTrue(get5.length == 5);
		assertTrue(get5[0] == 1);
		assertTrue(get5[4] == 10);
		double[] get4 = ts.get(6,2,2).getData();
		assertTrue(get4.length == 4);
		
		double[] get3 = ts.get(5,0,2).getData();
		assertTrue(get3.length == 3);
		assertTrue(get3[0] == 5 );
		get3 = ts.get(5,2,0).getData();
		assertTrue(get3.length == 3);
		assertTrue(get3[0] == 1);
		
		double[] get1 = ts.get(5, 1l, 1l).getData();
		assertTrue(get1[0] == 5);
		assertTrue(get1.length == 1);
		double[] get0 = ts.get(7, 1l, 1l).getData();
		assertTrue(get0.length == 0);
		get1 = ts.get(8, 1l, 1l).getData();
		assertTrue(get1.length == 1);
		assertTrue(get1[0] == 9);
		get1 = ts.get(10, 0l, 100l).getData();
		assertTrue(get1.length == 1);
		assertTrue(get1[0] == 10);
		get1 = ts.get(1, 100l, 0l).getData();
		assertTrue(get1.length == 1);
		assertTrue(get1[0] == 1);
		
		get1 = ts.get(3, 1l, 1l).getData();
		assertTrue(get1.length == 1);
		assertTrue(get1[0] == 2);
		double[] get2 = ts.get(2, 1l, 1l).getData();
		assertTrue(get2.length == 2);
		assertTrue(get2[0] == 1);
		assertTrue(get2[1] == 2);
		
		DoubleTimeSeries get3d = ts.get(0, 7);
		get3 = get3d.getData();
		assertEquals(get3.length, 3);
	}
	
	@Test
	public void testGenericTimeSeries(){
		class ConcreteTimeSeriesString extends ConcreteTimeSeries<String,ConcreteTimeSeriesString>{

			@Override
			public ConcreteTimeSeriesString newInstance() {
				return new ConcreteTimeSeriesString();
			}
			
		}
		ConcreteTimeSeries<String,ConcreteTimeSeriesString> ts = new ConcreteTimeSeriesString();
		ts.add(1,"One");
		ts.add(2,"Two");
		ts.add(5,"Five");
		ts.add(9,"Nine");
		ts.add(10,"Ten");
		
		assertTrue(ts.get(1).getData()[0].equals("One"));
		assertTrue(ts.get(3).size() == 0);
		
		ConcreteTimeSeriesString get5d = ts.get(5,2,2);
		String[] get5 = get5d.getData();
		long[] get5l = get5d.getTimes();
		assertTrue(Arrays.equals(get5l, new long[]{1,2,5,9,10}));
		assertTrue(get5.length == 5);
		assertTrue(get5[0].equals("One"));
		assertTrue(get5[1].equals("Two"));
		assertTrue(get5[2].equals("Five"));
		assertTrue(get5[3].equals("Nine"));
		assertTrue(get5[4].equals("Ten"));
		
		get5 = new String[5];
		get5 = ts.get(5,2,2,get5d).getData();
		get5l = get5d.getTimes();
		assertTrue(Arrays.equals(get5l, new long[]{1,2,5,9,10}));
		assertTrue(get5[0].equals("One"));
		assertTrue(get5[1].equals("Two"));
		assertTrue(get5[2].equals("Five"));
		assertTrue(get5[3].equals("Nine"));
		assertTrue(get5[4].equals("Ten"));
		
		ConcreteTimeSeries<String,?> get4d = ts.get(6,2,2);
		String[] get4 = get4d.getData();
		assertTrue(get4.length == 4);
		get5l = get5d.getTimes();
		assertTrue(Arrays.equals(get4d.getTimes(), new long[]{2,5,9,10}));
		
		String[] get3 = ts.get(5,0,2).getData();
		assertTrue(get3.length == 3);
		assertTrue(get3[0].equals("Five"));
		get3 = ts.get(5,2,0).getData();
		assertTrue(get3.length == 3);
		assertTrue(get3[0].equals("One"));
		
		String[] get1 = ts.get(5, 1l, 1l).getData();
		assertTrue(get1[0].equals("Five"));
		assertTrue(get1.length == 1);
		String[] get0 = ts.get(7, 1l, 1l).getData();
		assertTrue(get0.length == 0);
		get1 = ts.get(8, 1l, 1l).getData();
		assertTrue(get1.length == 1);
		assertTrue(get1[0].equals("Nine"));
		
		get1 = ts.get(3, 1l, 1l).getData();
		assertTrue(get1.length == 1);
		assertTrue(get1[0].equals("Two"));
		ConcreteTimeSeries<String,?> get2d = ts.get(2, 1l, 1l);
		String[] get2 = get2d.getData();
		assertTrue(Arrays.equals(get2d.getTimes(), new long[]{1,2}));
		assertTrue(get2.length == 2);
		assertTrue(get2[0].equals("One"));
		assertTrue(get2[1].equals("Two"));		
	}
	
}
