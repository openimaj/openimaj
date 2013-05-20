package org.openimaj.util.stream.window;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Apply a {@link Function} typed on IN and OUT to each item of a List<IN> to produce
 * a List<OUT>
 * @param <IN>
 * @param <OUT>
 * @param <META>
 */
public class WindowFunction<IN,OUT,META> implements Function<Window<IN,META>,Window<OUT,META>>{

	private Function<IN, OUT> fun;
	/**
	 * @param fun
	 */
	public WindowFunction(Function<IN,OUT> fun) {
		this.fun = fun;
	}
	@Override
	public Window<OUT,META> apply(Window<IN,META> inl) {
		List<OUT> out = new ArrayList<OUT>();
		for (IN in : inl.getPayload()) {
			out.add(fun.apply(in));
		}
		return new Window<OUT, META>(inl.getMeta(), out);
	}

}
