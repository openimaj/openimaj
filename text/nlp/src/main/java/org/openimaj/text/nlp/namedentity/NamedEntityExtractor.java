package org.openimaj.text.nlp.namedentity;

import java.util.List;
import java.util.Map;

/**
 * Interface for all entity extractors.
 * 
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 */
public abstract interface NamedEntityExtractor {

	/**
	 * returns the entities.
	 * @param tokens
	 *            = the ordered list of tokens from a tokenised string.
	 * @return HashMap of (Integer = token list index number) to (NamedEntity =
	 *         entity that matched the corresponding token)
	 */
	public Map<Integer, NamedEntity> getEntities(List<String> tokens);

}
