package org.openimaj.ml.annotation;

import java.util.List;
import java.util.Set;

import org.openimaj.experiment.dataset.cache.GroupedListCache;
import org.openimaj.experiment.dataset.cache.InMemoryGroupedListCache;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.IdentityFeatureExtractor;

/**
 * Adaptor that allows a {@link BatchAnnotator} to behave like a
 * {@link IncrementalAnnotator} by caching extracted features and
 * then performing training only when {@link #annotate(Object)} is
 * called. 
 * <p>
 * Because the features are cached, the internal annotator must 
 * rely on a {@link IdentityFeatureExtractor}, and thus not perform
 * any extraction itself.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object
 * @param <ANNOTATION> Type of annotation
 * @param <FEATURE> Type of feature extracted and cached.
 * @param <EXTRACTOR> Type of object capable of extracting features from the object
 */
public class FeatureCachingIncrementalBatchAnnotator<
	OBJECT, 
	ANNOTATION,
	FEATURE,
	EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>> 
extends IncrementalAnnotator<OBJECT, ANNOTATION, EXTRACTOR> 
{
	BatchAnnotator<FEATURE, ANNOTATION, IdentityFeatureExtractor<FEATURE>> batchAnnotator;
	GroupedListCache<ANNOTATION, FEATURE> featureCache;
	boolean isInvalid = true;
	
	/**
	 * Construct with the given feature extractor and batch annotator, and
	 * use an in-memory cache.
	 * 
	 * @param extractor the extractor
	 * @param batchAnnotator the annotator
	 */
	public FeatureCachingIncrementalBatchAnnotator(EXTRACTOR extractor, BatchAnnotator<FEATURE, ANNOTATION, IdentityFeatureExtractor<FEATURE>> batchAnnotator) {
		super(extractor);
		this.featureCache = new InMemoryGroupedListCache<ANNOTATION, FEATURE>();
		this.batchAnnotator = batchAnnotator;
	}
	
	/**
	 * Construct with the given feature extractor and batch annotator, and
	 * use an in-memory cache.
	 * 
	 * @param extractor the extractor
	 * @param batchAnnotator the annotator
	 * @param cache the cache
	 */
	public FeatureCachingIncrementalBatchAnnotator(EXTRACTOR extractor, 
			BatchAnnotator<FEATURE, ANNOTATION, IdentityFeatureExtractor<FEATURE>> batchAnnotator,
			GroupedListCache<ANNOTATION, FEATURE> cache) {
		super(extractor);
		this.batchAnnotator = batchAnnotator;
		this.featureCache = cache;
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		FEATURE fv = extractor.extractFeature(annotated.getObject());
		
		featureCache.add(annotated.getAnnotations(), fv);
		isInvalid = true;
	}

	@Override
	public void reset() {
		featureCache.reset();
		isInvalid = true;
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return featureCache.getDataset().getGroups();
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		if (isInvalid) {
			batchAnnotator.train(featureCache.getDataset());
			isInvalid = false;
		}

		return batchAnnotator.annotate(extractor.extractFeature(object));
	}
}
