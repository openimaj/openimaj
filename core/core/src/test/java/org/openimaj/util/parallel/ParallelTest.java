package org.openimaj.util.parallel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

	@Test
	public void testLargeThreadPool(){
		final List<Integer> intsList = new ArrayList<Integer>();

		for (int i = 0; i < 8; i++) {
			intsList.add(i);
		}

		final List<Integer> out = Collections.synchronizedList(new ArrayList<Integer>());

		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(8);
		Parallel.forEach(intsList, new Operation<Integer>() {

			@Override
			public void perform(Integer object) {
				out.add(object);
			}
		}, executor);
		System.out.println("Active count: " + executor.getActiveCount());
		System.out.println("Max Pool size: " + executor.getMaximumPoolSize());
		System.out.println("Core Pool size: " + executor.getCorePoolSize());
		System.out.println("Largest Pool size: " + executor.getLargestPoolSize());
		assertTrue(executor.getLargestPoolSize() >= 8);

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
