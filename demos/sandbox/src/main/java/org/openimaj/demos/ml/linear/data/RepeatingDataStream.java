package org.openimaj.demos.ml.linear.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.ml.linear.data.DataGenerator;
import org.openimaj.ml.linear.data.LinearPerceptronDataGenerator;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

public class RepeatingDataStream<I, D> extends AbstractStream<IndependentPair<I,D>> {

	private DataGenerator<I, D> dg;
	private int total;
	private ArrayList<IndependentPair<I, D>> items;
	private Iterator<IndependentPair<I, D>> innerIter;

	public RepeatingDataStream(DataGenerator<I,D> dg,int totalDataItems) {
		this.dg = dg;
		this.total = totalDataItems;
		this.items = new ArrayList<IndependentPair<I,D>>();
		
		for (int i = 0; i < total; i++) {
			items.add(dg.generate());
		}
		refresh();
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public IndependentPair<I, D> next() {
		if(!innerIter.hasNext()) refresh();
		return innerIter.next();
	}

	private void refresh() {
		this.innerIter = this.items.iterator();
	}

}
