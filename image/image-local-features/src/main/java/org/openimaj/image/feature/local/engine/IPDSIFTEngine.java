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
package org.openimaj.image.feature.local.engine;

import java.util.List;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProvider;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProviderFactory;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.processing.convolution.FImageGradients;

public class IPDSIFTEngine {
	
	private static final float DEFAULT_THRESHOLD = 0.5f;


	private static final float INVALID_PIXEL_VALUE = Float.NaN;
	
	
	private InterestPointDetector detector;
	private float threshold = DEFAULT_THRESHOLD;


	private GradientFeatureProviderFactory factory;
	public IPDSIFTEngine(InterestPointDetector detector){
		this.detector = detector;
	}
	/**
	 * Find the interest points using the provided detector and extract a SIFT descriptor per point.
	 * @param image to extract features from
	 * @return extracted interest point features
	 */
	public LocalFeatureList<InterestPointFeature> findKeypoints(FImage image) {
		this.detector.findInterestPoints(image);
		List<InterestPointData> points = this.detector.getInterestPoints(threshold);
		return extractSIFTFeatures(image,points);
	}
	private LocalFeatureList<InterestPointFeature> extractSIFTFeatures(FImage image, List<InterestPointData> points) {
		LocalFeatureList<InterestPointFeature> featureList = new MemoryLocalFeatureList<InterestPointFeature>();
		
		FImage orientation = image.clone();
		FImage gradient = image.clone();
		
		OrientationHistogramExtractor oriHistExtractor = new OrientationHistogramExtractor ();
		DominantOrientationExtractor dominantOrientationExtractor = new DominantOrientationExtractor(
			DominantOrientationExtractor.DEFAULT_PEAK_THRESHOLD,oriHistExtractor
		);
		GradientFeatureProviderFactory factory = new SIFTFeatureProvider();
		for(InterestPointData point : points){
			
			InterestPointImageExtractorProperties property = new InterestPointImageExtractorProperties(image,point);
			FImage subImage = property.image;
			
			float [] patches = dominantOrientationExtractor.extractFeatureRaw(property);
			for(float patchOrientation : patches ){
				GradientFeatureProvider provider = factory.newProvider();
				provider.setPatchOrientation(patchOrientation);
				float boundingBoxSize = subImage.width;
				//pass over all the pixels in the subimage, they are the sampling area
				for (int y = 0; y < boundingBoxSize; y++) {
					for (int x = 0; x <= boundingBoxSize; x++) {
						
						//check if the pixel is in the image bounds; if not ignore it
						if (subImage.pixels[y][x] != INVALID_PIXEL_VALUE) {
							//calculate the actual position of the sample in the patch coordinate system
							float sx = 0.5f + x / boundingBoxSize;
							float sy = 0.5f + y / boundingBoxSize;
							
							provider.addSample(sx, sy, 
								oriHistExtractor.getCurrentGradient().pixels[y][x], 
								oriHistExtractor.getCurrentOrientation().pixels[y][x]
							);
						}
					}
				}
				
				OrientedFeatureVector featureVector = provider.getFeatureVector();
				featureList.add(new InterestPointFeature(featureVector,point));
				
			}
		}
		return featureList;
	}
}
