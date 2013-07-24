package org.openimaj.feature;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openimaj.data.identity.Identifiable;

/**
 * A simple wrapper for a feature extractor that caches the extracted feature to a {@link HashMap}.
 * If a feature has already been generated for a given object, it will be
 * re-read from the {@link HashMap}
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <FEATURE>
 *            Type of feature
 * @param <OBJECT>
 *            Type of object
 */
public class CachingFeatureExtractor<FEATURE, OBJECT extends Identifiable>
		implements
		FeatureExtractor<FEATURE, OBJECT>
{
	private static Logger logger = Logger.getLogger(CachingFeatureExtractor.class);

	private FeatureExtractor<FEATURE, OBJECT> extractor;
	private boolean force;

	private HashMap<String, FEATURE> cache;

	/**
	 * Construct the cache {@link HashMap}. The given extractor will be used to generate the
	 * features.
	 *
	 * @param extractor
	 *            the feature extractor
	 */
	public CachingFeatureExtractor(FeatureExtractor<FEATURE, OBJECT> extractor) {
		this(extractor, false);
	}

	/**
	 * Construct the cache {@link HashMap} The given extractor will be used to generate the
	 * features. Optionally, all features can be regenerated.
	 *
	 * @param extractor
	 *            the feature extractor
	 * @param force
	 *            if true, then all features will be regenerated and saved,
	 *            rather than being loaded.
	 */
	public CachingFeatureExtractor(FeatureExtractor<FEATURE, OBJECT> extractor, boolean force) {
		this.cache = new HashMap<String,FEATURE>();
		this.extractor = extractor;
		this.force = force;
	}

	@Override
	public FEATURE extractFeature(OBJECT object) {
		FEATURE cachedFeature = this.cache.get(object.getID());


		FEATURE feature = null;
		if (!force && cachedFeature!=null) {
			feature = cachedFeature;

			if (feature != null)
				return feature;
		}

		feature = extractor.extractFeature(object);
		this.cache.put(object.getID(), feature);
		return feature;
	}

	@Override
	public String toString() {
		return this.extractor.toString();
	}

}
