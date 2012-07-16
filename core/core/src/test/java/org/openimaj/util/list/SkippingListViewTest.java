package org.openimaj.util.list;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SkippingListView}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class SkippingListViewTest {
	private ArrayList<Integer> list;

	/**
	 * Setup tests
	 */
	@Before
	public void setup() {
		list = new ArrayList<Integer>();

		for (int i=0; i<10; i++) {
			list.add(i);
		}
	}

	/**
	 * test 1
	 */
	@Test
	public void test1() {
		SkippingListView<Integer> view = new SkippingListView<Integer>(list, 0);
		
		assertEquals(9, view.size());
		
		for (int i=0; i<view.size(); i++)
			assertEquals((int)view.get(i), i+1);
	}
	
	/**
	 * test 2
	 */
	@Test
	public void test2() {
		SkippingListView<Integer> view = new SkippingListView<Integer>(list, 0, 2, 4, 6, 8);
		
		System.out.println(view);
		
		assertEquals(5, view.size());
		
		for (int i=0; i<view.size(); i++)
			assertEquals((int)view.get(i), i*2 + 1);
	}
}
