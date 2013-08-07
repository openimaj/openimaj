package org.openimaj.data;

import java.util.Iterator;

/**
 * A range of doubles
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DoubleRange implements Iterable<Double>{
	class DoubleRangeIterator implements Iterator<Double>{
		double current = start;
		
		@Override
		public boolean hasNext() {
			return current < end;
		}

		@Override
		public Double next() {
			double ret = current;
			current += diff;
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	private double start;
	/**
	 * Range of doubles 
	 * @param start
	 * @param diff
	 * @param end
	 */
	public DoubleRange(double start, double diff, double end) {
		super();
		this.start = start;
		this.diff = diff;
		this.end = end;
	}

	private double diff;
	private double end;

	@Override
	public Iterator<Double> iterator() {
		return new DoubleRangeIterator();
	}

}
