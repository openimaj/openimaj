package org.openimaj.text.nlp.namedentity;

import java.util.List;
import java.util.Map;

/**
 * Context scorers provide entites and likelihoods based on some context. Either
 * all entities are available for selection or a subset.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <INPUT>
 */
public abstract class EntityContextScorer<INPUT> {

	/**
	 * Given a context give the likelihoods of each entity.
	 * 
	 * @param context
	 * @return the likelihood of each entity.
	 */
	public abstract Map<String, Float> getScoredEntitiesFromContext(INPUT context);

	/**
	 * Given the context give the likelihood of each entity limited by the
	 * entityUris list
	 * 
	 * @param entityUris
	 * @param context
	 * @return the likelihood of each entity in the list
	 */
	public abstract Map<String, Float> getScoresForEntityList(List<String> entityUris, INPUT context);

}
