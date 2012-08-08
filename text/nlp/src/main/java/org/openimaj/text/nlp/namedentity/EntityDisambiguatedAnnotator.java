package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;

public class EntityDisambiguatedAnnotator
		extends
		EntityAnnotator {
	private double filterThreshold;	
	private EntityAliasAnnotator lua;
	private EntityContextAnnotator ywa;

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
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(
			List<String> object) {
		ArrayList<ScoredAnnotation<HashMap<String, Object>>> result = new ArrayList<ScoredAnnotation<HashMap<String, Object>>>();

		ArrayList<ScoredAnnotation<HashMap<String, Object>>> lookupAnnos = (ArrayList<ScoredAnnotation<HashMap<String, Object>>>) lua.annotate(object);
		ArrayList<ScoredAnnotation<HashMap<String, Object>>> indexAnnos = (ArrayList<ScoredAnnotation<HashMap<String, Object>>>) ywa.annotate(object);
		// if there are no lookup results, check if there are any strong
		// contexts.
		if (lookupAnnos.size() == 0) {			
				return result;			
		}
		// Build a context scoring map
		HashMap<String, Float> contextScores = new HashMap<String, Float>();
		for (ScoredAnnotation<HashMap<String, Object>> anno : indexAnnos) {
			String company = (String) anno.annotation
					.get(EntityContextAnnotator.URI);
			float score = (Float) anno.annotation.get(EntityContextAnnotator.SCORE);
			contextScores.put(company, score);
		}
		// Use Context Scoring Map to filter lookup Annotations
		for (ScoredAnnotation<HashMap<String, Object>> anno : lookupAnnos) {
			ArrayList<String> companies = (ArrayList<String>) anno.annotation
					.get(EntityAliasAnnotator.URIS);
			String resCompany = null;
			float topScore = 0;
			for (String company : companies) {
				if (contextScores.containsKey(company)) {
					float score = contextScores.get(company);
					if (score > filterThreshold && score > topScore) {
						resCompany = company;
						topScore = score;

					}
				}
			}
			if(resCompany!=null){				
				anno.annotation.put(EntityContextAnnotator.SCORE, topScore);
				anno.annotation.remove(EntityAliasAnnotator.URIS);
				anno.annotation.put(EntityContextAnnotator.URI, resCompany);				
				result.add(new ScoredAnnotation<HashMap<String,Object>>(anno.annotation, 1));
			}
		}

		return result;
	}
}
