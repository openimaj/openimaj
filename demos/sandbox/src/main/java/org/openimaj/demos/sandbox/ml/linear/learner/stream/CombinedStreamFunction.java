package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;


/**
 * Given a combined stream (i.e. a stream of {@link IndependentPair} instances) apply
 * two functions (one to each compo
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <AIN> The first part of the input pair
 * @param <BIN> The second part of the input pair
 *
 * @param <AOUT> The first part of the output pair
 * @param <BOUT> The second part of the output pair
 *
 */
public class CombinedStreamFunction<AIN,AOUT,BIN,BOUT> implements Function<IndependentPair<AIN,BIN>, IndependentPair<AOUT,BOUT>>{

	private Function<AIN, AOUT> fA;
	private Function<BIN, BOUT> fB;
	/**
	 * @param fA
	 * @param fB
	 */
	public CombinedStreamFunction(Function<AIN,AOUT> fA, Function<BIN,BOUT> fB) {
		this.fA = fA;
		this.fB = fB;
	}
	@Override
	public IndependentPair<AOUT, BOUT> apply(IndependentPair<AIN, BIN> in) {
		return IndependentPair.pair(fA.apply(in.firstObject()), fB.apply(in.secondObject()));
	}

}
