package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;
import org.openimaj.text.nlp.namedentity.YagoEntityExactMatcherFactory.YagoEntityExactMatcher;

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
