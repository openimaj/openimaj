package org.openimaj.ml.linear.data;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <X> 
 * @param <Y> 
 */
public class FixedDataGenerator<X,Y> implements DataGenerator<X,Y>{

	private int index;
	private List<IndependentPair<X,Y>> data;
	/**
	 * @param d
	 */
	public FixedDataGenerator(List<IndependentPair<X, Y>> d) {
		this.data = d;
	}
	@Override
	public IndependentPair<X, Y> generate() {
		return this.data.get(nextIndex());
	}
	private int nextIndex() {
		int ret = this.index;
		this.index = (index + 1) % data.size();
		return ret;
	}
	
		
}
