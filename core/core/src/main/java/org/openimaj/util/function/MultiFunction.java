package org.openimaj.util.function;

import java.util.List;

/**
 * Apply a function to some input, producing zero or more appropriate results.
 * 
 * @param <IN>
 *            the type of the input to the function.
 * @param <OUT>
 *            the type of the result.
 */
public interface MultiFunction<IN, OUT> {
	/**
	 * Apply the function to the input argument and return the result(s).
	 * 
	 * @param in
	 *            the input object
	 * @return the result(s) of applying the function.
	 */
	List<OUT> apply(IN in);
}
