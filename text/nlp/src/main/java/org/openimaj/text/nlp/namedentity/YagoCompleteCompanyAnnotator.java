package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;

public class YagoCompleteCompanyAnnotator
		extends
		AbstractAnnotator<List<String>, HashMap<String, Object>, IdentityFeatureExtractor<List<String>>> {
	private double filterThreshold;
	private double strongContextThreshold = 0.7;
	private YagoLookupCompanyAnnotator lua;
	private YagoWikiIndexCompanyAnnotator ywa;

	public YagoCompleteCompanyAnnotator(double threshold, YagoLookupCompanyAnnotator lookup, YagoWikiIndexCompanyAnnotator yagoWiki) {
		super(
				new IdentityFeatureExtractor<List<String>>());
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
			if (indexAnnos.size() == 0)
				return result;
			for (ScoredAnnotation<HashMap<String, Object>> anno : indexAnnos) {
				if ((Float) anno.annotation.get(YagoWikiIndexCompanyAnnotator.SCORE) > strongContextThreshold) {
					result.add(new ScoredAnnotation<HashMap<String, Object>>(
							anno.annotation, 1));
				}
			}
			return result;
		}
		// Build a context scoring map
		HashMap<String, Float> contextScores = new HashMap<String, Float>();
		for (ScoredAnnotation<HashMap<String, Object>> anno : indexAnnos) {
			String company = (String) anno.annotation
					.get(YagoWikiIndexCompanyAnnotator.URI);
			float score = (Float) anno.annotation.get(YagoWikiIndexCompanyAnnotator.SCORE);
			contextScores.put(company, score);
		}
		// Use Context Scoring Map to filter lookup Annotations
		for (ScoredAnnotation<HashMap<String, Object>> anno : lookupAnnos) {
			ArrayList<String> companies = (ArrayList<String>) anno.annotation
					.get(YagoLookupCompanyAnnotator.URIS);
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
				anno.annotation.put(YagoWikiIndexCompanyAnnotator.SCORE, topScore);
				result.add(new ScoredAnnotation<HashMap<String,Object>>(anno.annotation, 1));
			}
		}

		return result;
	}
}
