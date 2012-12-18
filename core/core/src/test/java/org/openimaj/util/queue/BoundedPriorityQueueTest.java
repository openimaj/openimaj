package org.openimaj.util.queue;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for the {@link BoundedPriorityQueue}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BoundedPriorityQueueTest {
	/**
	 * Test ordered insertion
	 */
	@Test
	public void testOrderedInsert() {
		final BoundedPriorityQueue<Integer> queue = new BoundedPriorityQueue<Integer>(5);
		assertEquals(null, queue.offerItem(0));
		assertEquals(null, queue.offerItem(1));
		assertEquals(null, queue.offerItem(2));
		assertEquals(null, queue.offerItem(3));
		assertEquals(null, queue.offerItem(4));
		assertEquals(new Integer(5), queue.offerItem(5));

		assertEquals(new Integer(0), queue.peek());
		assertEquals(new Integer(4), queue.peekTail());

		assertEquals(new Integer(4), queue.offerItem(-1));

		assertEquals(new Integer(-1), queue.peek());
		assertEquals(new Integer(3), queue.peekTail());
	}

	/**
	 * Test unordered insertion
	 */
	@Test
	public void testUnorderedInsert() {
		final BoundedPriorityQueue<Integer> queue = new BoundedPriorityQueue<Integer>(5);
		assertEquals(null, queue.offerItem(3));
		assertEquals(null, queue.offerItem(1));
		assertEquals(null, queue.offerItem(0));
		assertEquals(null, queue.offerItem(4));
		assertEquals(null, queue.offerItem(2));
		assertEquals(new Integer(5), queue.offerItem(5));

		assertEquals(new Integer(0), queue.peek());
		assertEquals(new Integer(4), queue.peekTail());

		assertEquals(new Integer(4), queue.offerItem(-1));

		assertEquals(new Integer(-1), queue.peek());
		assertEquals(new Integer(3), queue.peekTail());
	}
}
