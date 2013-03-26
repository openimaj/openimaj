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
package org.openimaj.experiment.evaluation.retrieval.analysers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.RetrievalAnalyser;

/**
 * {@link RetrievalAnalyser} that computes the precision after N documents have
 * been retrieved (P@N).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <QUERY>
 *            Type of query
 * @param <DOCUMENT>
 *            Type of document
 */
public class PrecisionAtN<QUERY, DOCUMENT extends Identifiable>
		implements
		RetrievalAnalyser<PrecisionAtNResult<QUERY>, QUERY, DOCUMENT>
{
	protected int N;

	/**
	 * Construct with the given N.
	 * 
	 * @param n
	 *            N, the number of top-ranked documents to consider.
	 */
	public PrecisionAtN(int n) {
		N = n;
	}

	@Override
	public PrecisionAtNResult<QUERY> analyse(Map<QUERY, List<DOCUMENT>> results, Map<QUERY, Set<DOCUMENT>> relevant) {
		final PrecisionAtNResult<QUERY> scores = new PrecisionAtNResult<QUERY>(N);

		for (final QUERY query : relevant.keySet()) {
			final List<DOCUMENT> qres = results.get(query);

			if (qres != null) {
				final List<DOCUMENT> topN = qres.subList(0, Math.min(N, qres.size()));
				scores.allScores.put(query, score(topN, relevant.get(query)));
			} else {
				scores.allScores.put(query, 0);
			}
		}

		return null;
	}

	private double score(List<DOCUMENT> topN, Set<DOCUMENT> rel) {
		int count = 0;

		for (final DOCUMENT ret : topN) {
			if (rel.contains(ret))
				count++;
		}

		return (double) count / (double) topN.size();
	}
}
