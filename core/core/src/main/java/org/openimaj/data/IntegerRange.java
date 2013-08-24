package org.openimaj.data;

import java.util.Iterator;

/**
 * A range of doubles
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class IntegerRange implements Iterable<Integer>{
	class IntegerRangeIterator implements Iterator<Integer>{
		int current = start;
		
		@Override
		public boolean hasNext() {
			return current < end;
		}

		@Override
		public Integer next() {
			int ret = current;
			current += diff;
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	private int start;
	/**
	 * Range of doubles 
	 * @param start
	 * @param diff
	 * @param end
	 */
	public IntegerRange(int start, int diff, int end) {
		super();
		this.start = start;
		this.diff = diff;
		this.end = end;
	}

	private int diff;
	private int end;

	@Override
	public Iterator<Integer> iterator() {
		return new IntegerRangeIterator();
	}

}
