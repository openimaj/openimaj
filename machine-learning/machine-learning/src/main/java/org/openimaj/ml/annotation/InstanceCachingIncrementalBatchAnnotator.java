package org.openimaj.ml.annotation;

import java.util.List;
import java.util.Set;

import org.openimaj.experiment.dataset.cache.GroupedListCache;
import org.openimaj.experiment.dataset.cache.InMemoryGroupedListCache;
import org.openimaj.feature.FeatureExtractor;

/**
 * Adaptor that allows a {@link BatchAnnotator} to behave like a
 * {@link IncrementalAnnotator} by caching instances and
 * then performing training only when {@link #annotate(Object)} is
 * called. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object
 * @param <ANNOTATION> Type of annotation
 * @param <EXTRACTOR> Type of object capable of extracting features from the object
 */
public class InstanceCachingIncrementalBatchAnnotator<
	OBJECT, 
	ANNOTATION,
	EXTRACTOR extends FeatureExtractor<?, OBJECT>> 
extends IncrementalAnnotator<OBJECT, ANNOTATION, EXTRACTOR> 
{
	BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR> batchAnnotator;
	GroupedListCache<ANNOTATION, OBJECT> objectCache;
	boolean isInvalid = true;
	
	/**
	 * Construct with an in-memory cache and the given batch annotator.
	 * @param batchAnnotator the batch annotator
	 */
	public InstanceCachingIncrementalBatchAnnotator(BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR> batchAnnotator) {
		super(batchAnnotator.extractor);
		this.batchAnnotator = batchAnnotator;
		this.objectCache = new InMemoryGroupedListCache<ANNOTATION, OBJECT>();
	}
	
	/**
	 * Construct with the given batch annotator and cache implementation.
	 * @param batchAnnotator the batch annotator
	 * @param cache the cache 
	 */
	public InstanceCachingIncrementalBatchAnnotator(BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR> batchAnnotator, GroupedListCache<ANNOTATION, OBJECT> cache) {
		super(batchAnnotator.extractor);
		this.batchAnnotator = batchAnnotator;
		this.objectCache = cache;
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		objectCache.add(annotated.getAnnotations(), annotated.getObject());
		isInvalid = true;
	}

	@Override
	public void reset() {
		objectCache.reset();
		isInvalid = true;
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return objectCache.getDataset().getGroups();
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		if (isInvalid) {
			batchAnnotator.train(objectCache.getDataset());
			isInvalid = false;
		}

		return batchAnnotator.annotate(object);
	}
}
