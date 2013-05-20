package org.openimaj.util.stream.window;

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
 * @param <META>
 *
 */
public class CombinedAggregationFunction<AIN,AOUT,BIN,BOUT,AM,BM> implements Function<
	Aggregation<IndependentPair<AIN,BIN>, IndependentPair<AM,BM>>,
	Aggregation<IndependentPair<AOUT,BOUT>, IndependentPair<AM,BM>>
>{

	private Function<Aggregation<AIN,AM>, Aggregation<AOUT,AM>> fA;
	private Function<Aggregation<BIN,BM>, Aggregation<BOUT,BM>> fB;
	/**
	 * @param fA
	 * @param fB
	 */
	public CombinedAggregationFunction(Function<Aggregation<AIN,AM>, Aggregation<AOUT,AM>> fA, Function<Aggregation<BIN,BM>, Aggregation<BOUT,BM>> fB) {
		this.fA = fA;
		this.fB = fB;
	}
	@Override
	public Aggregation<IndependentPair<AOUT,BOUT>, IndependentPair<AM,BM>> apply(Aggregation<IndependentPair<AIN,BIN>, IndependentPair<AM,BM>> inaggr) {
		IndependentPair<AIN, BIN> in = inaggr.getPayload();
		IndependentPair<AM, BM> meta = inaggr.getMeta();

		Aggregation<AIN, AM> aggra = new Aggregation<AIN, AM>(in.firstObject(), meta.firstObject());
		Aggregation<BIN, BM> aggrb = new Aggregation<BIN, BM>(in.secondObject(), meta.secondObject());
		IndependentPair<Aggregation<AOUT, AM>, Aggregation<BOUT, BM>> pair = IndependentPair.pair(fA.apply(aggra), fB.apply(aggrb));
		IndependentPair<AOUT, BOUT> outP = IndependentPair.pair(pair.firstObject().getPayload(), pair.secondObject().getPayload());
		IndependentPair<AM, BM> outM = IndependentPair.pair(pair.firstObject().getMeta(), pair.secondObject().getMeta());
		return new Aggregation<IndependentPair<AOUT,BOUT>, IndependentPair<AM,BM>>(outP, outM);
	}

}
