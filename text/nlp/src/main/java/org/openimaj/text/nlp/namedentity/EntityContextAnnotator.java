package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoWikiIndexFactory.EntityContextScorerLuceneWiki;

/**
 * Entity annotator based on context. The list of tokens are treated as a
 * document and a {@link EntityContextScorerLuceneWiki} instance is used to
 * annotate likely entities to the list of tokens.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class EntityContextAnnotator extends EntityAnnotator {

	private final EntityContextScorerLuceneWiki ywi;

	/**
	 * @param ywi
	 *            the underlying instance
	 */
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
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(List<String> object) {
		final HashMap<String, Float> results = ywi.getScoredEntitiesFromContext(object);
		final ArrayList<ScoredAnnotation<HashMap<String, Object>>> ret = new ArrayList<ScoredAnnotation<HashMap<String, Object>>>();
		for (final String company : results.keySet()) {
			final HashMap<String, Object> annotation = new HashMap<String, Object>();
			annotation.put(URI, company);
			annotation.put(TYPE, "Company");
			annotation.put(SCORE, results.get(company));
			ret.add(new ScoredAnnotation<HashMap<String, Object>>(annotation, 1));
		}
		return ret;
	}

}
