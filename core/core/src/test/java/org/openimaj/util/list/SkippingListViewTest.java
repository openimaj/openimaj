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
