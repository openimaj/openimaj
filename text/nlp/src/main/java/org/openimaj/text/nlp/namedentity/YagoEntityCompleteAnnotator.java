package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityCompleteExtractorFactory.YagoEntityCompleteExtractor;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;

/**
 * {@link EntityAnnotator} wrapper for {@link YagoEntityCompleteExtractor}
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoEntityCompleteAnnotator extends EntityAnnotator {
	private YagoEntityContextScorer contextScorer;
	private YagoEntityCandidateFinder candidateFinder;

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
			HashMap<String, Float> contextScores = (HashMap<String, Float>) contextScorer
					.getScoresForEntityList(companies, tokens);
			float topScore = 0;
			String resCompany = null;
			for (String company : companies) {
				if (contextScores.keySet().contains(company)
						&& contextScores.get(company) > topScore) {
					resCompany = company;
					topScore = contextScores.get(company);
				}
			}
			if (resCompany != null) {
				HashMap<String, Object> annotation = new HashMap<String, Object>();
				annotation.put(YagoEntityContextAnnotator.SCORE, topScore);
				annotation.put(YagoEntityContextAnnotator.URI, resCompany);
				NamedEntity ent = getNamedEntity(resCompany, can);
				annotation.put(YagoEntityContextAnnotator.START_TOKEN,
						ent.startToken);
				annotation.put(YagoEntityContextAnnotator.END_TOKEN,
						ent.stopToken);
				result.add(new ScoredAnnotation<HashMap<String, Object>>(
						annotation, 1));
			}
		}
		return result;
	}

	private NamedEntity getNamedEntity(String resCompany, List<NamedEntity> can) {
		for (NamedEntity ent : can) {
			if (ent.rootName.equals(resCompany))
				return ent;
		}
		return null;
	}
}
