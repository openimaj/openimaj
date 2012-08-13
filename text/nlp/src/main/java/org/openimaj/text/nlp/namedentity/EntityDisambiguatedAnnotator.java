package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 * An {@link EntityAnnotator} which uses an underlying
 * {@link EntityAliasAnnotator} instance to find initial likely entities and
 * then uses an {@link EntityContextAnnotator} to narrow down if multiple
 * entities are suggested by the {@link EntityAliasAnnotator}.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class EntityDisambiguatedAnnotator extends EntityAnnotator {
	private final double filterThreshold;
	private final EntityAliasAnnotator lua;
	private final EntityContextAnnotator ywa;

	/**
	 * @param threshold
	 *            minimum score required from the context annotator before its
	 *            input is taken seriously
	 * @param lookup
	 *            the alias lookup
	 * @param yagoWiki
	 *            the context lookup
	 */
	public EntityDisambiguatedAnnotator(double threshold, EntityAliasAnnotator lookup, EntityContextAnnotator yagoWiki) {
		super();
		this.filterThreshold = threshold;
		this.lua = lookup;
		this.ywa = yagoWiki;
	}

	@Override
	public Set<HashMap<String, Object>> getAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(List<String> object) {
		final ArrayList<ScoredAnnotation<HashMap<String, Object>>> result = new ArrayList<ScoredAnnotation<HashMap<String, Object>>>();

		final ArrayList<ScoredAnnotation<HashMap<String, Object>>> lookupAnnos = (ArrayList<ScoredAnnotation<HashMap<String, Object>>>) lua
				.annotate(object);
		final ArrayList<ScoredAnnotation<HashMap<String, Object>>> indexAnnos = (ArrayList<ScoredAnnotation<HashMap<String, Object>>>) ywa
				.annotate(object);
		// if there are no lookup results, check if there are any strong
		// contexts.
		if (lookupAnnos.size() == 0) {
			return result;
		}
		// Build a context scoring map
		final HashMap<String, Float> contextScores = new HashMap<String, Float>();
		for (final ScoredAnnotation<HashMap<String, Object>> anno : indexAnnos) {
			final String company = (String) anno.annotation.get(EntityContextAnnotator.URI);
			final float score = (Float) anno.annotation.get(EntityContextAnnotator.SCORE);
			contextScores.put(company, score);
		}
		// Use Context Scoring Map to filter lookup Annotations
		for (final ScoredAnnotation<HashMap<String, Object>> anno : lookupAnnos) {
			@SuppressWarnings("unchecked")
			final ArrayList<String> companies = (ArrayList<String>) anno.annotation.get(EntityAliasAnnotator.URIS);
			String resCompany = null;
			float topScore = 0;
			for (final String company : companies) {
				if (contextScores.containsKey(company)) {
					final float score = contextScores.get(company);
					if (score > filterThreshold && score > topScore) {
						resCompany = company;
						topScore = score;

					}
				}
			}
			if (resCompany != null) {
				anno.annotation.put(EntityContextAnnotator.SCORE, topScore);
				anno.annotation.remove(EntityAliasAnnotator.URIS);
				anno.annotation.put(EntityContextAnnotator.URI, resCompany);
				result.add(new ScoredAnnotation<HashMap<String, Object>>(anno.annotation, 1));
			}
		}

		return result;
	}
}
