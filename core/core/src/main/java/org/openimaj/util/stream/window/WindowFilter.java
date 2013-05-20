package org.openimaj.util.stream.window;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Apply a {@link Function} typed on IN and OUT to each item of a List<IN> to produce
 * a List<OUT>
 * @param <IN>
 * @param <META>
 */
public class WindowFilter<IN,META> implements Function<Window<IN,META>,Window<IN,META>>{

	private Predicate<IN> pred;

	/**
	 * @param pred
	 */
	public WindowFilter(Predicate<IN> pred) {
		this.pred = pred;
	}

	@Override
	public Window<IN,META> apply(Window<IN,META> in) {
		List<IN> ret = new ArrayList<IN>();
		for (IN usmfStatus : in.getPayload()) {
			if(pred.test(usmfStatus)) ret.add(usmfStatus);
		}
		return new Window<IN, META>(in.getMeta(), ret);
	}



}
