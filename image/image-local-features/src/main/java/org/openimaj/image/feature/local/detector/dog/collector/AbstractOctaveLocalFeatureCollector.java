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
package org.openimaj.image.feature.local.detector.dog.collector;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.Octave;
import org.openimaj.image.feature.local.extraction.FeatureVectorExtractor;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Abstract base class for objects that collate {@link LocalFeature}s as they
 * are extracted from {@link Octave}s. This base class holds a generic list for
 * storing the features and a reference to the feature extractor instance that
 * is responsible for extracting the feature vectors.
 * 
 * Typically the same AbstractOctaveLocalFeatureCollector will be used across
 * all Octaves, and will thus contain all the features from the image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OCTAVE>
 *            the type of {@link Octave} from which features are extracted
 * @param <EXTRACTOR>
 *            the type of {@link FeatureVectorExtractor} which extracts the
 *            feature vectors
 * @param <FEATURE>
 *            the type of {@link LocalFeature} which are extracted
 * @param <IMAGE>
 *            the type of {@link Image} from which features are extracted
 */
public abstract class AbstractOctaveLocalFeatureCollector<OCTAVE extends Octave<?, ?, IMAGE>, EXTRACTOR extends FeatureVectorExtractor<?, ScaleSpaceImageExtractorProperties<IMAGE>>, FEATURE extends LocalFeature<?, ?>, IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		implements
		Collector<OCTAVE, FEATURE, IMAGE>
{
	protected EXTRACTOR featureExtractor;
	protected LocalFeatureList<FEATURE> features = new MemoryLocalFeatureList<FEATURE>();

	/**
	 * Construct the AbstractOctaveLocalFeatureCollector with the given feature
	 * extractor.
	 * 
	 * @param featureExtractor
	 *            the feature extractor
	 */
	public AbstractOctaveLocalFeatureCollector(EXTRACTOR featureExtractor) {
		this.featureExtractor = featureExtractor;
	}

	/**
	 * Get the list of features collected.
	 * 
	 * @return the features
	 */
	@Override
	public LocalFeatureList<FEATURE> getFeatures() {
		return features;
	}
}
