package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoWikiIndexFactory.EntityContextScorerLuceneWiki;

import edu.stanford.nlp.util.StringUtils;

public class EntityContextAnnotator
		extends
		EntityAnnotator {
	
	private EntityContextScorerLuceneWiki ywi;

	public EntityContextAnnotator(EntityContextScorerLuceneWiki ywi) {
		super();
		this.ywi = ywi;
	}

	@Override
	public Set<HashMap<String, Object>> getAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(
			List<String> object) {
		HashMap<String,Float> results = ywi.getScoredEntitiesFromContext(object);
		ArrayList<ScoredAnnotation<HashMap<String,Object>>> ret = new ArrayList<ScoredAnnotation<HashMap<String,Object>>>();
		for(String company : results.keySet()){
			HashMap<String,Object> annotation = new HashMap<String, Object>();
			annotation.put(URI, company);
			annotation.put(TYPE, "Company");
			annotation.put(SCORE, results.get(company));
			ret.add(new ScoredAnnotation<HashMap<String,Object>>(annotation,1));
		}
		return ret;
	}

}
