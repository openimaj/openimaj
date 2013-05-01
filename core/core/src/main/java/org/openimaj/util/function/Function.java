package org.openimaj.util.function;

/**
 * Apply a function to some input, producing an appropriate result.
 * 
 * @param <IN>
 *            the type of the input to the function.
 * @param <OUT>
 *            the type of the result.
 */
public interface Function<IN, OUT> {
	/**
	 * Apply the function to the input argument and return the result.
	 * 
	 * @param in
	 *            the input object
	 * @return the result of the function
	 */
	OUT apply(IN in);
}
