package org.openimaj.util.set;

import static org.junit.Assert.*;

import org.junit.Test;


public class DisjointSetForestTest {
	@Test
	public void test1() {
		DisjointSetForest<Integer> set = new DisjointSetForest<Integer>();
		
		assertEquals(set.size(), 0);
		assertEquals(set.numSets(), 0);
		
		set.add(0);
		assertEquals(set.size(), 1);
		assertEquals(set.numSets(), 1);
		assertEquals(set.size(0), 1);
		
		set.add(1);
		assertEquals(set.size(), 2);
		assertEquals(set.numSets(), 2);
		assertEquals(set.size(0), 1);
		assertEquals(set.size(1), 1);
		
		int r = set.union(0, 1);
		assertEquals(set.size(), 2);
		assertEquals(set.numSets(), 1);
		assertEquals(set.size(r), 2);
	}
}
