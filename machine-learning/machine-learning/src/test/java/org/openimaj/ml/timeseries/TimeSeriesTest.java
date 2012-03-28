package org.openimaj.ml.timeseries;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

public class TimeSeriesTest {
	
	@Test
	public void testTimeSeries(){
		TimeSeries<String> ts = new TimeSeries<String>();
		ts.add(TimeSeriesData.create(1,"One"));
		ts.add(TimeSeriesData.create(2,"Two"));
		ts.add(TimeSeriesData.create(5,"Five"));
		ts.add(TimeSeriesData.create(9,"Nine"));
		ts.add(TimeSeriesData.create(10,"Ten"));
		
		assertTrue(ts.get(1).equals("One"));
		assertTrue(ts.get(3) == null);
		
		LinkedList<String> get5 = ts.get(5,2,2);
		assertTrue(get5.size() == 5);
		assertTrue(get5.get(0).equals("One"));
		assertTrue(get5.get(1).equals("Two"));
		assertTrue(get5.get(2).equals("Five"));
		assertTrue(get5.get(3).equals("Nine"));
		assertTrue(get5.get(4).equals("Ten"));
		LinkedList<String> get4 = ts.get(6,2,2);
		assertTrue(get4.size() == 4);
		
		LinkedList<String> get3 = ts.get(5,0,2);
		assertTrue(get3.size() == 3);
		assertTrue(get3.get(0).equals("Five"));
		get3 = ts.get(5,2,0);
		assertTrue(get3.size() == 3);
		assertTrue(get3.get(0).equals("One"));
		
		LinkedList<String> get1 = ts.get(5, 1l, 1l);
		assertTrue(get1.get(0).equals("Five"));
		assertTrue(get1.size() == 1);
		LinkedList<String> get0 = ts.get(7, 1l, 1l);
		assertTrue(get0.size() == 0);
		get1 = ts.get(8, 1l, 1l);
		assertTrue(get1.size() == 1);
		assertTrue(get1.get(0).equals("Nine"));
		
		get1 = ts.get(3, 1l, 1l);
		assertTrue(get1.size() == 1);
		assertTrue(get1.get(0).equals("Two"));
		LinkedList<String> get2 = ts.get(2, 1l, 1l);
		assertTrue(get2.size() == 2);
		assertTrue(get2.get(0).equals("One"));
		assertTrue(get2.get(1).equals("Two"));
		
	}
}
