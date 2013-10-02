package ch.akuhn.matrix;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An ordered list of floating point numbers.
 * 
 * @author Adrian Kuhn
 */
public abstract class Vector {

	/**
	 * Add the given value to the value at the given index
	 * 
	 * @param index
	 * @param value
	 * @return the new value
	 */
	public double add(int index, double value) {
		return put(index, get(index) + value);
	}

	/**
	 * @return the density
	 */
	public double density() {
		return ((double) used()) / size();
	}

	/**
	 * Iterates over all entries. Some vectors omit zero-valued entries.
	 * 
	 * @return value and index of each entry.
	 */
	public Iterable<Entry> entries() {
		return new Iterable<Entry>() {
			@Override
			public Iterator<Entry> iterator() {
				return new Iterator<Entry>() {

					private int index = 0;

					@Override
					public boolean hasNext() {
						return index < size();
					}

					@Override
					public Entry next() {
						if (!hasNext())
							throw new NoSuchElementException();
						return new Entry(index, get(index++));
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

				};
			}
		};
	}

	/**
	 * Get the value at the given index
	 * 
	 * @param index
	 * @return the value
	 */
	public abstract double get(int index);

	/**
	 * Compute the L2 norm
	 * 
	 * @return the L2 norm
	 */
	public double norm() {
		double sum = 0;
		for (final Entry each : entries())
			sum += each.value * each.value;
		return Math.sqrt(sum);
	}

	/**
	 * Set the value at an index
	 * 
	 * @param index
	 * @param value
	 * @return the new value
	 */
	public abstract double put(int index, double value);

	/**
	 * @return the length of the vector
	 */
	public abstract int size();

	/**
	 * @return the sum of values
	 */
	public double sum() {
		double sum = 0;
		for (final Entry each : entries())
			sum += each.value;
		return sum;
	}

	/**
	 * Returns number of non-zero-valued entries.
	 * 
	 * @return a positive integer.
	 */
	public int used() {
		int count = 0;
		for (final Entry each : entries())
			if (each.value != 0)
				count++;
		return count;
	}

	/**
	 * An entry in a sparse vector
	 */
	public final class Entry {
		/**
		 * The index
		 */
		public final int index;
		/**
		 * The value
		 */
		public final double value;

		/**
		 * Construct
		 * 
		 * @param index
		 * @param value
		 */
		public Entry(int index, double value) {
			this.index = index;
			this.value = value;
		}
	}

	/**
	 * Construct a Vector from an array of values
	 * 
	 * @param values
	 * @return the vector
	 */
	public static Vector from(double... values) {
		return new DenseVector(values.clone());
	}

	/**
	 * Copy a range from an array into a vector.
	 * 
	 * @param values
	 * @param start
	 * @param length
	 * @return the vector
	 */
	public static Vector copy(double[] values, int start, int length) {
		return new DenseVector(Arrays.copyOfRange(values, start, start + length));
	}

	/**
	 * Wrap an array in a vector
	 * 
	 * @param values
	 * @return the vector
	 */
	public static Vector wrap(double... values) {
		return new DenseVector(values);
	}

	/**
	 * Create an empty dense vector
	 * 
	 * @param size
	 * @return the vector
	 */
	public static Vector dense(int size) {
		return new DenseVector(size);
	}

	/**
	 * Create an empty sparse vector
	 * 
	 * @param size
	 * @return the vector
	 */
	public static Vector sparse(int size) {
		return new SparseVector(size);
	}

	/**
	 * Returns the dot/scalar product.
	 * 
	 * @param x
	 * @return the dot product
	 * 
	 */
	public double dot(Vector x) {
		double product = 0;
		for (final Entry each : entries())
			product += each.value * x.get(each.index);
		return product;
	}

	/**
	 * y = y + a*<code>this</code>.
	 * 
	 * @param a
	 * @param y
	 * 
	 */
	public void scaleAndAddTo(double a, Vector y) {
		for (final Entry each : entries())
			y.add(each.index, a * each.value);
	}

	/**
	 * I/O
	 * 
	 * @param array
	 * @param start
	 */
	public void storeOn(double[] array, int start) {
		assert start + size() <= array.length;
		Arrays.fill(array, start, start + size(), 0);
		for (final Entry each : entries())
			array[start + each.index] = each.value;
	}

	@Override
	public String toString() {
		final StringWriter out = new StringWriter();
		out.append("(");
		for (final Entry each : entries())
			out.append(each.value + ", ");
		out.append(")");
		return out.toString();
	}

	/**
	 * Multiply by a constant, creating a new vector
	 * 
	 * @param scalar
	 * @return the vector
	 */
	public abstract Vector times(double scalar);

	/**
	 * Multiply by a constant in-place
	 * 
	 * @param scalar
	 * @return this
	 */
	public abstract Vector timesEquals(double scalar);

	/**
	 * Test for equality
	 * 
	 * @param v
	 * @param epsilon
	 * @return true if the same; false otherwise
	 */
	public abstract boolean equals(Vector v, double epsilon);

	/**
	 * @return an array
	 */
	public double[] unwrap() {
		throw new Error("cannot unwrap instance of " + getClass());
	}

	/**
	 * mean-center the vector
	 */
	public void applyCentering() {
		final double[] values = unwrap();
		final double mean = Util.sum(values) / values.length;
		for (int i = 0; i < values.length; i++)
			values[i] -= mean;
	}

	/**
	 * @return the mean of the values
	 */
	public double mean() {
		final double[] values = unwrap();
		return Util.sum(values) / values.length;
	}
}
