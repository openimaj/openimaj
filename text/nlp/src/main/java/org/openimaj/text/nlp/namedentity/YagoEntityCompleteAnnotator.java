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
package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;

/**
 * {@link EntityAnnotator} wrapper for {@link YagoEntityExactMatcher}
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoEntityCompleteAnnotator extends EntityAnnotator {
	private YagoEntityContextScorer contextScorer;
	private YagoEntityCandidateFinder candidateFinder;
	private int localContextBound = 20;

	/**
	 * Default constructor.
	 * @param contextScorer
	 * @param candidateFinder
	 */
	public YagoEntityCompleteAnnotator(YagoEntityContextScorer contextScorer,
			YagoEntityCandidateFinder candidateFinder) {
		super();
		this.contextScorer = contextScorer;
		this.candidateFinder = candidateFinder;
	}

	@Override
	public Set<HashMap<String, Object>> getAnnotations() {
		// Intentionally blank
		return null;
	}

	@Override
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(
			List<String> tokens) {
		ArrayList<ScoredAnnotation<HashMap<String, Object>>> result = new ArrayList<ScoredAnnotation<HashMap<String, Object>>>();
		// Check if any candidates are found
		List<List<NamedEntity>> candidates = candidateFinder
				.getCandidates(tokens);
		// If none found, return an empty.
		if (candidates.size() == 0) {
			return result;
		}
		// Use Context Scoring to disambiguate candidates
		for (List<NamedEntity> can : candidates) {
			ArrayList<String> companies = new ArrayList<String>();
			for (NamedEntity ent : can) {
				companies.add(ent.rootName);
			}
			//get the localised context for each list of named Entities
			List<String> localContext = getLocalContext(tokens, can.get(0).startToken, can.get(0).stopToken);
			Map<NamedEntity, Float> contextScores = contextScorer
					.getScoresForEntityList(companies, localContext);
			float topScore = 0;
			NamedEntity resEntity = null;
			for (NamedEntity entity : can) {
				if (contextScores.keySet().contains(entity)
						&& contextScores.get(entity) > topScore) {
					resEntity = entity;
					topScore = contextScores.get(entity);
				}
			}
			if (resEntity != null) {
				HashMap<String, Object> annotation = new HashMap<String, Object>();
				annotation.put(YagoEntityContextAnnotator.SCORE, topScore);
				annotation.put(YagoEntityContextAnnotator.URI, resEntity.rootName);
				annotation.put(YagoEntityContextAnnotator.START_TOKEN,
						resEntity.startToken);
				annotation.put(YagoEntityContextAnnotator.END_TOKEN,
						resEntity.stopToken);
				annotation.put(YagoEntityContextAnnotator.TYPE,
						resEntity.type.toString());
				result.add(new ScoredAnnotation<HashMap<String, Object>>(
						annotation, 1));
			}
		}
		return result;
	}

	private List<String> getLocalContext(List<String> tokens, int startToken,
			int stopToken) {
		final int bottom = Math.max(0, startToken - localContextBound);
		final int top = Math.min(tokens.size(), stopToken + localContextBound);
		return tokens.subList(bottom, top);
	}
}
