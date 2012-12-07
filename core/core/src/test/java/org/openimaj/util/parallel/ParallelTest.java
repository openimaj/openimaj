package org.openimaj.util.parallel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openimaj.util.parallel.partition.GrowingChunkPartitioner;
import org.openimaj.util.parallel.partition.RangePartitioner;

/**
 * Tests for {@link Parallel}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ParallelTest {
	/**
	 * Test the for-each loop with implicit {@link GrowingChunkPartitioner}
	 */
	@Test
	public void testForEach() {
		final Set<Integer> intsList = new HashSet<Integer>();

		for (int i = 0; i < Math.random() * 1000000; i++) {
			intsList.add(i);
		}

		final Set<Integer> out = Collections.synchronizedSet(new HashSet<Integer>());

		Parallel.forEach(intsList, new Operation<Integer>() {

			@Override
			public void perform(Integer object) {
				out.add(object);
			}
		});

		assertEquals(intsList.size(), out.size());
	}

	/**
	 * Test the for-each loop with implicit {@link RangePartitioner}
	 */
	@Test
	public void testForEach2() {
		final List<Integer> intsList = new ArrayList<Integer>();

		for (int i = 0; i < Math.random() * 1000000; i++) {
			intsList.add(i);
		}

		final Set<Integer> out = Collections.synchronizedSet(new HashSet<Integer>());

		Parallel.forEach(intsList, new Operation<Integer>() {

			@Override
			public void perform(Integer object) {
				out.add(object);
			}
		});

		assertEquals(intsList.size(), out.size());
	}
}
