package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;

/**
 * {@link EntityAnnotator} wrapper for {@link YagoEntityContextScorer}
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoEntityContextAnnotator
		extends
		EntityAnnotator {
	
	private YagoEntityContextScorer ywi;

	/**
	 * Default Constructor
	 * @param ywi
	 */
	public YagoEntityContextAnnotator(YagoEntityContextScorer ywi) {
		super();
		this.ywi = ywi;
	}

	@Override
	public Set<HashMap<String, Object>> getAnnotations() {
		// Intentionally blank
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
