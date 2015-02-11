/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.ml.annotation;

import java.util.List;
import java.util.Set;

import org.openimaj.data.dataset.cache.GroupedListCache;
import org.openimaj.data.dataset.cache.InMemoryGroupedListCache;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.IdentityFeatureExtractor;

/**
 * Adaptor that allows a {@link BatchAnnotator} to behave like a
 * {@link IncrementalAnnotator} by caching extracted features and then
 * performing training only when {@link #annotate(Object)} is called.
 * <p>
 * Because the features are cached, the internal annotator must rely on a
 * {@link IdentityFeatureExtractor} or similar, and thus not perform any
 * extraction itself.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 *            Type of object
 * @param <ANNOTATION>
 *            Type of annotation
 * @param <FEATURE>
 *            Type of feature extracted and cached.
 */
public class FeatureCachingIncrementalBatchAnnotator<OBJECT, ANNOTATION, FEATURE>
extends IncrementalAnnotator<OBJECT, ANNOTATION>
{
	BatchAnnotator<FEATURE, ANNOTATION> batchAnnotator;
	GroupedListCache<ANNOTATION, FEATURE> featureCache;
	private FeatureExtractor<FEATURE, OBJECT> extractor;
	boolean isInvalid = true;

	/**
	 * Construct with the given feature extractor and batch annotator, and use
	 * an in-memory cache.
	 *
	 * @param extractor
	 *            the extractor
	 * @param batchAnnotator
	 *            the annotator
	 */
	public FeatureCachingIncrementalBatchAnnotator(FeatureExtractor<FEATURE, OBJECT> extractor,
			BatchAnnotator<FEATURE, ANNOTATION> batchAnnotator)
	{
		this.extractor = extractor;
		this.featureCache = new InMemoryGroupedListCache<ANNOTATION, FEATURE>();
		this.batchAnnotator = batchAnnotator;
	}

	/**
	 * Construct with the given feature extractor and batch annotator, and use
	 * an in-memory cache.
	 *
	 * @param extractor
	 *            the extractor
	 * @param batchAnnotator
	 *            the annotator
	 * @param cache
	 *            the cache
	 */
	public FeatureCachingIncrementalBatchAnnotator(FeatureExtractor<FEATURE, OBJECT> extractor,
			BatchAnnotator<FEATURE, ANNOTATION> batchAnnotator,
			GroupedListCache<ANNOTATION, FEATURE> cache)
	{
		this.extractor = extractor;
		this.batchAnnotator = batchAnnotator;
		this.featureCache = cache;
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		final FEATURE fv = extractor.extractFeature(annotated.getObject());

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
