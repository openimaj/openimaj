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
package org.openimaj.image.feature.local.detector.ipd.extractor;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProvider;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProviderFactory;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.engine.InterestPointImageExtractorProperties;
import org.openimaj.image.feature.local.extraction.FeatureExtractor;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;


/**
 * <p>
 * Class capable of extracting local descriptors from an interest point {@link InterestPointData} from an interest point detector {@link InterestPointDetector}. 
 * The actual feature extracted is determined by the {@link GradientFeatureProvider} that
 * is provided by the {@link GradientFeatureProviderFactory} set during
 * construction.
 * </p>
 * <p>
 * The GradientFeatureExtractor first calculates the dominant orientation
 * of the image patch described by the {@link InterestPointImageExtractorProperties}
 * and then iterates over the pixels in an oriented square, centered on the
 * interest point, passing the gradient and magnitude values of the respective
 * pixel to the {@link GradientFeatureProvider}.
 * </p>
 * <p>
 * The size of the sampling square is exactly equal to the patch in the properties, this is in turn controlled
 * by the interest point's scale and possibly its shape. For some types of feature provider, this number
 * might need to be set based on the internal settings of the provider. 
 * </p>
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class InterestPointGradientFeatureExtractor implements FeatureExtractor<InterestPointImageExtractorProperties<Float,FImage>> {
	private static final Float INVALID_PIXEL_VALUE = Float.NaN;

	DominantOrientationExtractor dominantOrientationExtractor;
	
	GradientFeatureProviderFactory factory;
	
	
	/**
	 * @param factory object used to construct {@link GradientFeatureProvider} instances which in turn
	 * constructon the actual features
	 */
	public InterestPointGradientFeatureExtractor(GradientFeatureProviderFactory factory) {
		this(new DominantOrientationExtractor(), factory);
	}
	
	/**
	 * @param dominantOrientationExtractor how dominant orientations are located
	 * @param factory object used to construct {@link GradientFeatureProvider} instances which in turn
	 * constructon the actual features
	 */
	public InterestPointGradientFeatureExtractor(DominantOrientationExtractor dominantOrientationExtractor, GradientFeatureProviderFactory factory) {
		this.dominantOrientationExtractor = dominantOrientationExtractor;
		this.factory = factory;
	}
	

	@Override
	public OrientedFeatureVector[] extractFeature(InterestPointImageExtractorProperties<Float,FImage> properties) {
		float [] dominantOrientations = dominantOrientationExtractor.extractFeatureRaw(properties);

		OrientedFeatureVector[] ret = new OrientedFeatureVector[dominantOrientations.length];

		for (int i=0; i<dominantOrientations.length; i++) {
			ret[i] = createFeature(properties, dominantOrientations[i]);
		}

		return ret;
	}

	/*
	 * Iterate over the pixels in a sampling patch provided in the properties instance
	 * and pass the information to a feature provider that will extract the relevant
	 * feature vector.
	 */
	protected OrientedFeatureVector createFeature(InterestPointImageExtractorProperties<Float,FImage> properties, float orientation) {
		GradientFeatureProvider provider = factory.newProvider();
		provider.setPatchOrientation(orientation);
		//pass over all the pixels in the subimage, they are the sampling area
		for (int y = 0; y < properties.featureWindowSize; y++) {
			for (int x = 0; x < properties.featureWindowSize; x++) {
				
				//check if the pixel is in the image bounds; if not ignore it
				if (properties.image.pixels[y][x] != INVALID_PIXEL_VALUE) {
					//calculate the actual position of the sample in the patch coordinate system
					float sx = (0.5f + x) / properties.featureWindowSize;
					float sy = (0.5f + y )/ properties.featureWindowSize;
					
					provider.addSample(sx, sy, 
						dominantOrientationExtractor.getOriHistExtractor().getCurrentGradient().pixels[y][x], 
						dominantOrientationExtractor.getOriHistExtractor().getCurrentOrientation().pixels[y][x]
					);
				}
			}
		}

		return provider.getFeatureVector();
	}
}
