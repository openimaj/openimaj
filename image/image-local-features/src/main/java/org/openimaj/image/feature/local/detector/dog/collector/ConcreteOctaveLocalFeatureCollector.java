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

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureImpl;
import org.openimaj.feature.local.Location;
import org.openimaj.feature.local.ScaleSpaceLocation;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.Octave;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.extraction.FeatureVectorExtractor;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Concrete implementation of an {@link AbstractOctaveLocalFeatureCollector}
 * that collects {@link LocalFeature}s in the form of {@link LocalFeatureImpl}
 * with the feature vector provided by the given feature extractor, and the
 * {@link Location} provided by a {@link ScaleSpaceLocation} with an x, y and
 * scale coordinates.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OCTAVE>
 *            the underlying {@link Octave} type
 * @param <FE>
 *            the underlying {@link FeatureVectorExtractor} type
 * @param <IMAGE>
 *            the image type
 */
public class ConcreteOctaveLocalFeatureCollector<OCTAVE extends Octave<?, ?, IMAGE>, FE extends FeatureVectorExtractor<?, ScaleSpaceImageExtractorProperties<IMAGE>>, IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
		extends
		AbstractOctaveLocalFeatureCollector<OCTAVE, FE, LocalFeature<?, ?>, IMAGE>
{
	protected ScaleSpaceImageExtractorProperties<IMAGE> extractionProperties = new ScaleSpaceImageExtractorProperties<IMAGE>();

	/**
	 * Construct with the given feature extractor.
	 * 
	 * @param featureExtractor
	 *            the feature extractor.
	 */
	public ConcreteOctaveLocalFeatureCollector(FE featureExtractor) {
		super(featureExtractor);
	}

	@Override
	public void foundInterestPoint(OctaveInterestPointFinder<OCTAVE, IMAGE> finder, float x, float y, float octaveScale) {
		final int currentScaleIndex = finder.getCurrentScaleIndex();
		extractionProperties.image = finder.getOctave().images[currentScaleIndex];
		extractionProperties.scale = octaveScale;
		extractionProperties.x = x;
		extractionProperties.y = y;

		final float octSize = finder.getOctave().octaveSize;

		addFeature(octSize * x, octSize * y, octSize * octaveScale);
	}

	protected void addFeature(float imx, float imy, float imscale) {
		final FeatureVector[] fvs = featureExtractor.extractFeature(extractionProperties);

		final Location loc = new ScaleSpaceLocation(imx, imy, imscale);

		for (final FeatureVector fv : fvs) {
			features.add(new LocalFeatureImpl<Location, FeatureVector>(loc, fv));
		}
	}
}
