/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.util.stream.window;

import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

/**
 * Given a combined stream (i.e. a stream of {@link IndependentPair} instances)
 * apply two functions (one to each compo
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <AIN>
 *            The first part of the input pair
 * @param <BIN>
 *            The second part of the input pair
 * 
 * @param <AOUT>
 *            The first part of the output pair
 * @param <BOUT>
 *            The second part of the output pair
 * @param <AM>
 * @param <BM>
 * 
 */
public class CombinedMetaPayloadFunction<AIN, AOUT, BIN, BOUT, AM, BM> implements Function<
		MetaPayload<IndependentPair<AIN, BIN>, IndependentPair<AM, BM>>,
		MetaPayload<IndependentPair<AOUT, BOUT>, IndependentPair<AM, BM>>
		>
{

	private Function<MetaPayload<AIN, AM>, MetaPayload<AOUT, AM>> fA;
	private Function<MetaPayload<BIN, BM>, MetaPayload<BOUT, BM>> fB;

	/**
	 * @param fA
	 * @param fB
	 */
	public CombinedMetaPayloadFunction(Function<MetaPayload<AIN, AM>, MetaPayload<AOUT, AM>> fA,
			Function<MetaPayload<BIN, BM>, MetaPayload<BOUT, BM>> fB)
	{
		this.fA = fA;
		this.fB = fB;
	}

	@Override
	public MetaPayload<IndependentPair<AOUT, BOUT>, IndependentPair<AM, BM>> apply(
			MetaPayload<IndependentPair<AIN, BIN>, IndependentPair<AM, BM>> inaggr)
	{
		final IndependentPair<AIN, BIN> in = inaggr.getPayload();
		final IndependentPair<AM, BM> meta = inaggr.getMeta();

		final MetaPayload<AIN, AM> aggra = new MetaPayload<AIN, AM>(in.firstObject(), meta.firstObject());
		final MetaPayload<BIN, BM> aggrb = new MetaPayload<BIN, BM>(in.secondObject(), meta.secondObject());
		final IndependentPair<MetaPayload<AOUT, AM>, MetaPayload<BOUT, BM>> pair = IndependentPair.pair(fA.apply(aggra),
				fB.apply(aggrb));
		final IndependentPair<AOUT, BOUT> outP = IndependentPair.pair(pair.firstObject().getPayload(), pair
				.secondObject().getPayload());
		final IndependentPair<AM, BM> outM = IndependentPair.pair(pair.firstObject().getMeta(), pair.secondObject()
				.getMeta());
		return new MetaPayload<IndependentPair<AOUT, BOUT>, IndependentPair<AM, BM>>(outP, outM);
	}

}
