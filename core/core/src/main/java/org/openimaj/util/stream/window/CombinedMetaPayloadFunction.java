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
public class CombinedMetaPayloadFunction<AIN,AOUT,BIN,BOUT,AM,BM> implements Function<
	MetaPayload<IndependentPair<AIN,BIN>, IndependentPair<AM,BM>>,
	MetaPayload<IndependentPair<AOUT,BOUT>, IndependentPair<AM,BM>>
>{

	private Function<MetaPayload<AIN,AM>, MetaPayload<AOUT,AM>> fA;
	private Function<MetaPayload<BIN,BM>, MetaPayload<BOUT,BM>> fB;
	/**
	 * @param fA
	 * @param fB
	 */
	public CombinedMetaPayloadFunction(Function<MetaPayload<AIN,AM>, MetaPayload<AOUT,AM>> fA, Function<MetaPayload<BIN,BM>, MetaPayload<BOUT,BM>> fB) {
		this.fA = fA;
		this.fB = fB;
	}
	@Override
	public MetaPayload<IndependentPair<AOUT,BOUT>, IndependentPair<AM,BM>> apply(MetaPayload<IndependentPair<AIN,BIN>, IndependentPair<AM,BM>> inaggr) {
		IndependentPair<AIN, BIN> in = inaggr.getPayload();
		IndependentPair<AM, BM> meta = inaggr.getMeta();

		MetaPayload<AIN, AM> aggra = new MetaPayload<AIN, AM>(in.firstObject(), meta.firstObject());
		MetaPayload<BIN, BM> aggrb = new MetaPayload<BIN, BM>(in.secondObject(), meta.secondObject());
		IndependentPair<MetaPayload<AOUT, AM>, MetaPayload<BOUT, BM>> pair = IndependentPair.pair(fA.apply(aggra), fB.apply(aggrb));
		IndependentPair<AOUT, BOUT> outP = IndependentPair.pair(pair.firstObject().getPayload(), pair.secondObject().getPayload());
		IndependentPair<AM, BM> outM = IndependentPair.pair(pair.firstObject().getMeta(), pair.secondObject().getMeta());
		return new MetaPayload<IndependentPair<AOUT,BOUT>, IndependentPair<AM,BM>>(outP, outM);
	}

}
