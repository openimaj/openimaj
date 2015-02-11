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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.lemurproject.ireval.IREval;
import org.lemurproject.ireval.RetrievalEvaluator.Document;
import org.lemurproject.ireval.RetrievalEvaluator.Judgment;
import org.lemurproject.ireval.SetRetrievalEvaluator;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.Ranked;
import org.openimaj.experiment.evaluation.retrieval.RetrievalAnalyser;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEngine;
import org.openimaj.experiment.evaluation.retrieval.Scored;

/**
 * An evaluator suitable for any kind of result document from the
 * {@link RetrievalEngine}. Retrieval statistics are calculated using
 * {@link IREval}.
 * <p>
 * If the retrieved documents are instances of {@link Ranked}, then the rank is
 * determined through {@link Ranked#getRank()}, otherwise the rank of each
 * result document is automatically determined from its position in the results
 * list.
 * <p>
 * Similarly, if the retrieved documents are instances of {@link Scored}, then
 * the score is determined through {@link Scored#getScore()}, otherwise the
 * score of each result document is set as 1.0/rank.
 * <p>
 * If the queries are {@link Identifiable}, then the query ids in the outputted
 * {@link SetRetrievalEvaluator} are the ID of the query; if the queries are not
 * {@link Identifiable}, then the {@link Object#toString()} method is used
 * instead.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <QUERY>
 *            Type of query
 * @param <DOCUMENT>
 *            Type of document
 */
public class IREvalAnalyser<QUERY, DOCUMENT extends Identifiable>
implements RetrievalAnalyser<
IREvalResult,
QUERY,
DOCUMENT>
{
	protected static <Q, D extends Identifiable> TreeMap<String, ArrayList<Document>> convertResults(
			Map<Q, List<D>> results)
			{
		final TreeMap<String, ArrayList<Document>> allRankings = new TreeMap<String, ArrayList<Document>>();

		for (final Entry<Q, List<D>> entry : results.entrySet()) {
			String key;

			if (entry.getKey() instanceof Identifiable)
				key = ((Identifiable) entry.getKey()).getID();
			else
				key = entry.getKey().toString();

			final ArrayList<Document> docs = new ArrayList<Document>();
			for (int i = 0; i < entry.getValue().size(); i++) {
				final D doc = entry.getValue().get(i);

				int rnk = i + 1;
				if (doc instanceof Ranked)
					rnk = ((Ranked) doc).getRank();

				double score = (1.0 / rnk);
				if (doc instanceof Scored)
					score = ((Scored) doc).getScore();

				docs.add(new Document(doc.getID(), rnk, score));
			}

			allRankings.put(key, docs);
		}

		return allRankings;
			}

	protected static <Q, D extends Identifiable> TreeMap<String, ArrayList<Judgment>> convertRelevant(
			Map<Q, Set<D>> relevant)
			{
		final TreeMap<String, ArrayList<Judgment>> allJudgments = new TreeMap<String, ArrayList<Judgment>>();

		for (final Entry<Q, Set<D>> entry : relevant.entrySet()) {
			String key;

			if (entry.getKey() instanceof Identifiable)
				key = ((Identifiable) entry.getKey()).getID();
			else
				key = entry.getKey().toString();

			final ArrayList<Judgment> docs = new ArrayList<Judgment>();
			for (final D doc : entry.getValue()) {
				docs.add(new Judgment(doc.getID(), 1));
			}

			allJudgments.put(key, docs);
		}

		return allJudgments;
			}

	@Override
	public IREvalResult analyse(Map<QUERY, List<DOCUMENT>> results, Map<QUERY, Set<DOCUMENT>> relevant) {
		return new IREvalResult(IREval.create(convertResults(results), convertRelevant(relevant)));
	}
}
