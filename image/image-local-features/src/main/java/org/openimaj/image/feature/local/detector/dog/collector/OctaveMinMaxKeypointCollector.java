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


import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.pyramid.DoGOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.feature.local.keypoints.MinMaxKeypoint;

/**
 * Concrete implementation of an {@link AbstractOctaveLocalFeatureCollector}
 * that collects {@link MinMaxKeypoint}s with the feature vector provided by the 
 * given feature extractor. {@link MinMaxKeypoint}s contain the x, y and scale 
 * coordinates of the interest point along with its dominant orientation and
 * a boolean which determines whether the interest point was detected at a local
 * minima or maxima. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class OctaveMinMaxKeypointCollector extends AbstractOctaveLocalFeatureCollector<
	GaussianOctave<FImage>, GradientFeatureExtractor, MinMaxKeypoint, FImage> 
{
	protected ScaleSpaceImageExtractorProperties<FImage> extractionProperties = new ScaleSpaceImageExtractorProperties<FImage>();

	/**
	 * Construct with the given feature extractor.
	 * @param featureExtractor the feature extractor.
	 */
	public OctaveMinMaxKeypointCollector(GradientFeatureExtractor featureExtractor) {
		super(featureExtractor);
	}
		
	@Override
	public void foundInterestPoint(OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder, float x, float y, float octaveScale) {
		int currentScaleIndex = finder.getCurrentScaleIndex();
		extractionProperties.image = finder.getOctave().images[currentScaleIndex];
		extractionProperties.scale = octaveScale;
		extractionProperties.x = x;
		extractionProperties.y = y;
		
		float octSize = finder.getOctave().octaveSize;
		
		boolean isMaxima = ((DoGOctaveExtremaFinder)finder).getDoGOctave().images[currentScaleIndex].pixels[Math.round(y)][Math.round(x)] > 0.0;
		
		addFeature(octSize * x, octSize * y, octSize * octaveScale, isMaxima);
	}
	
	protected void addFeature(float imx, float imy, float imscale, boolean isMaxima) {
		OrientedFeatureVector[] fvs = featureExtractor.extractFeature(extractionProperties);
		
		for (OrientedFeatureVector fv : fvs) {
			features.add(new MinMaxKeypoint(imx, imy, fv.orientation, imscale, fv.values, isMaxima));
		}
	}
}
