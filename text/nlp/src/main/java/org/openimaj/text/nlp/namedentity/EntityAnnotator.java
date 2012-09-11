package org.openimaj.text.nlp.namedentity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;

/**
 * An entity annotator (given a list of strings) assigns entity annotations
 * which are {@link Map} instances mapping token indexes to entities.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class EntityAnnotator
		extends
			AbstractAnnotator<List<String>, HashMap<String, Object>, IdentityFeatureExtractor<List<String>>>
{

	/**
	 * The types of entities which can be annotated
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public enum EntityType {
		/**
		 * An organisation
		 */
		Organisation
	}

	static String URIS = "URIS";
	static String START_TOKEN = "START";
	static String END_TOKEN = "END";
	static String TYPE = "TYPE";
	static String URI = "URI";
	static String SCORE = "SCORE";

	/**
	 * Default entity annotator calls
	 * {@link AbstractAnnotator#AbstractAnnotator(org.openimaj.feature.FeatureExtractor)}
	 * with an {@link IdentityFeatureExtractor}
	 */
	public EntityAnnotator() {
		super(new IdentityFeatureExtractor<List<String>>());
	}

}
