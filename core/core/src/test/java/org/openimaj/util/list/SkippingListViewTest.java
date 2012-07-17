package org.openimaj.util.list;

import static org.junit.Assert.*;

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
			
		assertEquals(5, view.size());
		
		for (int i=0; i<view.size(); i++)
			assertEquals((int)view.get(i), i*2 + 1);
	}
	
	/**
	 * test 3
	 */
	@Test
	public void test3() {
		SkippingListView<Integer> view = new SkippingListView<Integer>(list, 5, 6);
		
		System.out.println(view);
		
		assertArrayEquals(new Integer[] {0, 1, 2, 3, 4, 7, 8, 9}, view.toArray(new Integer[8]));		
	}
	
	/**
	 * test 4
	 */
	@Test
	public void test4() {
		SkippingListView<Integer> view = new SkippingListView<Integer>(list, 1, 2, 3, 6);
		
		assertEquals(6, view.size());
		
		assertArrayEquals(new Integer[] {0, 4, 5, 7, 8, 9}, view.toArray(new Integer[6]));		
	}
}
