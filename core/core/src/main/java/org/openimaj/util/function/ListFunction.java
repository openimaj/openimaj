package org.openimaj.util.function;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Apply a {@link Function} typed on IN and OUT to each item of a List<IN> to produce
 * a List<OUT>
 * @param <IN>
 * @param <OUT>
 */
public class ListFunction<IN,OUT> implements Function<List<IN>,List<OUT>>{

	private Function<IN, OUT> fun;
	/**
	 * @param fun
	 */
	public ListFunction(Function<IN,OUT> fun) {
		this.fun = fun;
	}
	@Override
	public List<OUT> apply(List<IN> inl) {
		List<OUT> out = new ArrayList<OUT>();
		for (IN in : inl) {
			out.add(fun.apply(in));
		}
		return out;
	}

}
