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
package org.openimaj.image.feature.local.detector.dog.extractor;


import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProvider;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProviderFactory;
import org.openimaj.image.feature.local.extraction.GradientScaleSpaceImageExtractorProperties;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.processing.convolution.FImageGradients;


/**
 * <p>
 * Class capable of extracting local descriptors from a circular region
 * in an image defined by its scale and centre. The actual feature 
 * extracted is determined by the {@link GradientFeatureProvider} that
 * is provided by the {@link GradientFeatureProviderFactory} set during
 * construction.
 * </p>
 * <p>
 * The GradientFeatureExtractor first calculates the dominant orientation
 * of the image patch described by the {@link ScaleSpaceImageExtractorProperties}
 * and then iterates over the pixels in an oriented square, centered on the
 * interest point, passing the gradient and magnitude values of the respective
 * pixel to the {@link GradientFeatureProvider}.
 * </p>
 * <p>
 * The size of the sampling square, relative to scale is set by a single parameter,
 * magnification. For some types of feature provider, this number
 * might need to be set based on the internal settings of the provider. For example,
 * with a {@link SIFTFeatureProvider} this will probably be set to a constant multiplied
 * by the number of spatial bins of the feature. For SIFT, this constant is typically 
 * around 3, so with a standard 4-spatial binned SIFT provider, the magnification
 * factor of the extractor should be about 12.
 * </p>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ColourGradientFeatureExtractor implements ScaleSpaceFeatureExtractor<OrientedFeatureVector, MBFImage> {
	AbstractDominantOrientationExtractor dominantOrientationExtractor;
	
	GradientFeatureProviderFactory factory;
	
	private GradientScaleSpaceImageExtractorProperties<FImage> currentGradientProperties = new GradientScaleSpaceImageExtractorProperties<FImage>();
	
	protected MBFImage image;
	protected FImage[] magnitudes;
	protected FImage[] orientations;
	
	/**
	 * The magnification factor determining the size of the sampling
	 * region relative to the scale of the interest point.
	 */
	protected float magnification = 12;
		
	/**
	 * Construct with the given orientation extractor and gradient feature provider.
	 * The default magnification factor of 12 is used.
	 * 
	 * @param dominantOrientationExtractor the orientation extractor
	 * @param factory the gradient feature provider
	 */
	public ColourGradientFeatureExtractor(AbstractDominantOrientationExtractor dominantOrientationExtractor, GradientFeatureProviderFactory factory) {
		this.dominantOrientationExtractor = dominantOrientationExtractor;
		this.factory = factory;
	}
	
	/**
	 * Construct with the given orientation extractor, gradient feature provider
	 * and magnification factor determining the size of the sampling
	 * region relative to the scale of the interest point.
	 * 
	 * @param dominantOrientationExtractor the orientation extractor
	 * @param factory the gradient feature provider
	 * @param magnification the magnification factor.
	 */
	public ColourGradientFeatureExtractor(AbstractDominantOrientationExtractor dominantOrientationExtractor, GradientFeatureProviderFactory factory, float magnification) {
		this(dominantOrientationExtractor, factory);
		this.magnification = magnification;
	}

	@Override
	public OrientedFeatureVector[] extractFeature(ScaleSpaceImageExtractorProperties<MBFImage> properties) {
		GradientScaleSpaceImageExtractorProperties<FImage> gprops = getCurrentGradientProps(properties);

		float [] dominantOrientations = dominantOrientationExtractor.extractFeatureRaw(gprops);

		OrientedFeatureVector[] ret = new OrientedFeatureVector[dominantOrientations.length];

		for (int i=0; i<dominantOrientations.length; i++) {
			ret[i] = createFeature(dominantOrientations[i]);
		}

		return ret;
	}

	/**
	 * Get the GradientScaleSpaceImageExtractorProperties for the given properties.
	 * The returned properties are the same as the input properties, but with the
	 * gradient images added. 
	 * 
	 * For efficiency, this method always returns the same cached GradientScaleSpaceImageExtractorProperties,
	 * and internally updates this as necessary. The gradient images are only recalculated
	 * when the input image from the input properties is different to the cached one.
	 * 
	 * @param properties input properties
	 * @return cached GradientScaleSpaceImageExtractorProperties 
	 */
	public GradientScaleSpaceImageExtractorProperties<FImage> getCurrentGradientProps(ScaleSpaceImageExtractorProperties<MBFImage> properties) {
		if (properties.image != image) {
			image = properties.image;
			currentGradientProperties.image = image.bands.get(0);

			//only if the size of the image has changed do we need to reset the gradient and orientation images. 
			if (currentGradientProperties.orientation == null || 
					currentGradientProperties.orientation.height != currentGradientProperties.image.height || 
					currentGradientProperties.orientation.width != currentGradientProperties.image.width) {
				currentGradientProperties.orientation = new FImage(currentGradientProperties.image.width, currentGradientProperties.image.height);
				currentGradientProperties.magnitude = new FImage(currentGradientProperties.image.width, currentGradientProperties.image.height);
				
				if (magnitudes == null) {
					magnitudes = new FImage[image.bands.size() - 1];
					orientations = new FImage[image.bands.size() - 1];
				}
				
				for (int i=0; i<magnitudes.length; i++) {
					magnitudes[i] = new FImage(currentGradientProperties.image.width, currentGradientProperties.image.height);
					orientations[i] = new FImage(currentGradientProperties.image.width, currentGradientProperties.image.height);
				}
			}

			FImageGradients.gradientMagnitudesAndOrientations(currentGradientProperties.image, currentGradientProperties.magnitude, currentGradientProperties.orientation);
			
			for (int i=0; i<magnitudes.length; i++) {
				FImageGradients.gradientMagnitudesAndOrientations(image.getBand(i+1), magnitudes[i], orientations[i]);
			}
		}
		
		currentGradientProperties.x = properties.x;
		currentGradientProperties.y = properties.y;
		currentGradientProperties.scale = properties.scale;
		
		return currentGradientProperties;
	}

	/*
	 * Iterate over the pixels in a sampling patch around the given feature coordinates
	 * and pass the information to a feature provider that will extract the relevant
	 * feature vector.
	 */
	protected OrientedFeatureVector createFeature(final float orientation) {
		final float fx = currentGradientProperties.x;
		final float fy = currentGradientProperties.y;
		final float scale = currentGradientProperties.scale; 
		
		//create a new feature provider and initialise it with the dominant orientation
		GradientFeatureProvider [] sfe = new GradientFeatureProvider[magnitudes.length];
		for (int i = 0; i < magnitudes.length; i++) {
			sfe[i] = factory.newProvider();
			sfe[i].setPatchOrientation(orientation);			
		}
		
		//the integer coordinates of the patch
		final int ix = Math.round(fx);
		final int iy = Math.round(fy);

		final float sin = (float) Math.sin(orientation);
		final float cos = (float) Math.cos(orientation);

		//get the amount of extra sampling outside the unit square requested by the feature
		final float oversampling = sfe[0].getOversamplingAmount();
		
		//this is the size of the unit bounding box of the patch in the image in pixels
		final float boundingBoxSize = magnification * scale;
		
		//the amount of extra sampling per side in pixels
		final float extraSampling = oversampling * boundingBoxSize;
		
		//the actual sampling area is bigger than the boundingBoxSize by an extraSampling on each side
		final float samplingBoxSize = extraSampling + boundingBoxSize + extraSampling;
		
		//In the image, the box (with sides parallel to the image frame) that contains the
		//sampling box is:
		final float orientedSamplingBoxSize = Math.abs(sin * samplingBoxSize) + Math.abs(cos * samplingBoxSize);
		
		//now half the size and round to an int so we can iterate
		final int orientedSamplingBoxHalfSize = Math.round(orientedSamplingBoxSize / 2.0f);

		//get the images and their size
		final int width = magnitudes[0].width;
		final int height = magnitudes[0].height;
		
		//now pass over all the pixels in the image that *might* contribute to the sampling area
		for (int y = -orientedSamplingBoxHalfSize; y <= orientedSamplingBoxHalfSize; y++) {
			for (int x = -orientedSamplingBoxHalfSize; x <= orientedSamplingBoxHalfSize; x++) {
				int px = x + ix;
				int py = y + iy;
				
				//check if the pixel is in the image bounds; if not ignore it
				if (px >= 0 && px < width && py >= 0 && py < height) {
					//calculate the actual position of the sample in the patch coordinate system
					float sx = 0.5f + ((-sin * y + cos * x) - (fx - ix)) / boundingBoxSize;
					float sy = 0.5f + ((cos * y + sin * x) - (fy - iy)) / boundingBoxSize;
					
					//if the pixel is in the bounds of the sampling area then add it
					if (sx > -oversampling && sx < 1 + oversampling && sy > -oversampling && sy < 1 + oversampling) {
						for (int i = 0; i < magnitudes.length; i++) {
							sfe[i].addSample(sx, sy, magnitudes[i].pixels[py][px], orientations[i].pixels[py][px]);
						}
					}
				}
			}
		}

		OrientedFeatureVector first = sfe[0].getFeatureVector();
		OrientedFeatureVector fv = new OrientedFeatureVector(sfe.length * first.length(), orientation);
		System.arraycopy(first.values, 0, fv.values, 0, first.values.length);
		
		for (int i = 1; i < magnitudes.length; i++) {
			System.arraycopy(sfe[i].getFeatureVector().values, 0, fv.values, i*first.values.length, first.values.length);
		}
		
		return fv;
	}
}
