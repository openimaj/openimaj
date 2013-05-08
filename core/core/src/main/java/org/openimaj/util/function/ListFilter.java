package org.openimaj.util.function;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 */
public class ListFilter<IN> implements Function<List<IN>,List<IN>>{

	private Predicate<IN> pred;

	/**
	 * @param pred
	 */
	public ListFilter(Predicate<IN> pred) {
		this.pred = pred;
	}

	@Override
	public List<IN> apply(List<IN> in) {
		List<IN> ret = new ArrayList<IN>();
		for (IN usmfStatus : in) {
			if(pred.test(usmfStatus)) ret.add(usmfStatus);
		}
		return ret;
	}



}
