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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

/**
 * Constructs a {@link YagoEntityExactMatcher} from provided resource folder or default.
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoEntityExactMatcherFactory {

	/**
	 * Build a {@link YagoEntityExactMatcher} from the default YagoEntity folder
	 * path. See {@link EntityExtractionResourceBuilder} for details on
	 * constructing this folder.
	 * 
	 * @return {@link YagoEntityExactMatcher}
	 */
	public static YagoEntityExactMatcher getMatcher() {
		return getMatcher(EntityExtractionResourceBuilder.getDefaultRootPath());
	}

	/**
	 * Build a {@link YagoEntityExactMatcher} from the provided resource path.
	 * See {@link EntityExtractionResourceBuilder} for details on constructing
	 * this folder.
	 * 
	 * @param yagoEntityFolderPath
	 * @return {@link YagoEntityExactMatcher}
	 */
	public static YagoEntityExactMatcher getMatcher(String yagoEntityFolderPath) {
		YagoEntityCandidateFinder ycf = null;
		ycf = YagoEntityCandidateFinderFactory
				.createFromAliasFile(yagoEntityFolderPath
						+ File.separator
						+ EntityExtractionResourceBuilder.DEFAULT_ALIAS_NAME);
		YagoEntityContextScorer ycs = null;
		ycs = YagoEntityContextScorerFactory
				.createFromIndexFile(yagoEntityFolderPath
						+ File.separator
						+ EntityExtractionResourceBuilder.DEFAULT_CONTEXT_NAME);
		return new YagoEntityExactMatcher(ycs, ycf);
	}

	/**
	 * The class that will extract unique Entities from a given list of tokens.
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class YagoEntityExactMatcher {

		private YagoEntityContextScorer contextScorer;
		/**
		 * made public so that you have access to the candidateFinder to setNGrams.
		 */
		public YagoEntityCandidateFinder candidateFinder;

		/**
		 * Default constructor.
		 * 
		 * @param contextScorer
		 * @param candidateFinder
		 */
		public YagoEntityExactMatcher(YagoEntityContextScorer contextScorer,
				YagoEntityCandidateFinder candidateFinder) {
			this.contextScorer = contextScorer;
			this.candidateFinder = candidateFinder;
		}

		/**
		 * Returns a list of most likely unique Named Entities. These will not
		 * overlap in the tokens that they have matched.
		 * 
		 * @param possibleEntityTokens
		 * @param contextTokens
		 * @return list {@link NamedEntity}
		 */
		public List<NamedEntity> matchExact(List<String> possibleEntityTokens,
				List<String> contextTokens) {
			List<NamedEntity> result = new ArrayList<NamedEntity>();
			// Check if any candidates are found
			List<List<NamedEntity>> candidates = candidateFinder
					.getCandidates(possibleEntityTokens);
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
				// get the localised context for each list of named Entities
				Map<NamedEntity, Float> contextScores = contextScorer
						.getScoresForEntityList(companies, contextTokens);
				float topScore = 0;
				NamedEntity resEntity = null;
				for (NamedEntity entity : can) {
					if (contextScores.keySet().contains(entity)
							&& contextScores.get(entity) > topScore) {
						resEntity = entity;
						for (NamedEntity te : contextScores.keySet()) {
							if (resEntity.equals(te)) {
								resEntity.type = te.type;
							}
						}
						topScore = contextScores.get(entity);
					}
				}
				if (resEntity != null)
					result.add(resEntity);
			}
			return result;
		}

		/**
		 * @see #matchExact(List, List)
		 * @param possibleEntityTokens
		 * @param context
		 * @return list of {@link NamedEntity}
		 */
		public List<NamedEntity> matchExact(
				List<TokenAnnotation> possibleEntityTokens,
				String context) {
			List<NamedEntity> result = new ArrayList<NamedEntity>();
			// Check if any candidates are found
			List<List<NamedEntity>> candidates = candidateFinder
					.getCandidatesFromReversableTokenList(possibleEntityTokens);
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
				// get the localised context for each list of named Entities
				Map<NamedEntity, Float> contextScores = contextScorer
						.getScoresForEntityList(companies, context);
				float topScore = 0;
				NamedEntity resEntity = null;
				for (NamedEntity entity : can) {
					if (contextScores.keySet().contains(entity)
							&& contextScores.get(entity) > topScore) {
						resEntity = entity;
						for (NamedEntity te : contextScores.keySet()) {
							if (resEntity.equals(te)) {
								resEntity.type = te.type;								
							}
						}
						topScore = contextScores.get(entity);
					}
				}
				if (resEntity != null)
					result.add(resEntity);
			}
			return result;
		}
	}

}
