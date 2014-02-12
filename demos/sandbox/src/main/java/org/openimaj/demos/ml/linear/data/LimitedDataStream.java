package org.openimaj.demos.ml.linear.data;

import java.util.Iterator;

import org.openimaj.ml.linear.data.DataGenerator;
import org.openimaj.ml.linear.data.LinearPerceptronDataGenerator;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.AbstractStream;

public class LimitedDataStream<I,D> extends AbstractStream<IndependentPair<I,D>>{

	int gen = 0;
	private int total;
	private DataGenerator<I, D> dg;
	public LimitedDataStream(DataGenerator<I,D> dg,int totalDataItems) {
		this.dg = dg;
		this.total = totalDataItems;
	}

	@Override
	public boolean hasNext() {
		return this.gen < this.total;
	}

	@Override
	public IndependentPair<I, D> next() {
		this.gen ++;
		return dg.generate();
	}


}
