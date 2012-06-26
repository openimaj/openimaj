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

import gnu.trove.list.array.TFloatArrayList;

import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.extraction.GradientScaleSpaceImageExtractorProperties;

/**
 * Extract the dominant orientations of a scale-space interest point by
 * looking for peaks in its orientation histogram. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DominantOrientationExtractor extends AbstractDominantOrientationExtractor {
	/**
	 * Default value for the threshold at which other peaks are detected
	 * relative to the biggest peak. Lowe's IJCV paper suggests a
	 * value of 80%.
	 */
	public static float DEFAULT_PEAK_THRESHOLD = 0.8f;
	
	protected OrientationHistogramExtractor oriHistExtractor;
	
	/**
	 * Threshold for peak detection. A value of 1.0 would
	 * result in only a single peak being detected.
	 */
	protected float peakThreshold;
	
	/**
	 * Construct with default values.
	 */
	public DominantOrientationExtractor() {
		this(DEFAULT_PEAK_THRESHOLD, new OrientationHistogramExtractor());
	}
	
	/**
	 * Construct with given parameters.
	 * @param peakThreshold threshold at which other peaks are detected relative to the biggest peak
	 * @param oriHistExtractor the orientation histogram extractor
	 */
	public DominantOrientationExtractor(float peakThreshold, OrientationHistogramExtractor oriHistExtractor) {
		this.peakThreshold = peakThreshold;
		this.oriHistExtractor = oriHistExtractor;
	}
	
	/**
	 * Extract an orientation histogram and find the dominant orientations
	 * by looking for peaks.
	 * 
	 * @param properties Properties describing the interest point in scale space.
	 * @return an array of the angles of the dominant orientations [-PI to PI].
	 */
	@Override
	public float [] extractFeatureRaw(GradientScaleSpaceImageExtractorProperties<FImage> properties) {
		//extract histogram
		float[] hist = getOriHistExtractor().extractFeatureRaw(properties);
		
		//find max
		float maxval = 0;
		for (int i = 0; i < getOriHistExtractor().numBins; i++)
			if (hist[i] > maxval)
				maxval = hist[i];

		float thresh = peakThreshold * maxval;
		
		//search for peaks within peakThreshold of the maximum
		TFloatArrayList dominantOrientations = new TFloatArrayList();
		for (int i = 0; i < getOriHistExtractor().numBins; i++) {
			float prevVal = hist[(i == 0 ? getOriHistExtractor().numBins - 1 : i - 1)];
			float nextVal = hist[(i == getOriHistExtractor().numBins - 1 ? 0 : i + 1)];
			float thisVal = hist[i];
			
			if (thisVal >= thresh && thisVal > prevVal && thisVal > nextVal) {
				//fit a parabola to the peak to find the position of the actual maximum
				float peakDelta = fitPeak(prevVal, thisVal, nextVal);
				float angle = 2.0f * (float)Math.PI * (i + 0.5f + peakDelta) / getOriHistExtractor().numBins - (float)Math.PI;
				
				dominantOrientations.add(angle);
			}
		}

		return dominantOrientations.toArray();
	}
	
	/**
	 * Fit a parabola to three evenly spaced samples and return the relative
	 * position of the peak to the second sample. 
	 */
	float fitPeak(float a, float b, float c) {
		//a is at x=-1, b at x=0, c at x=1
		//y = A*x*x + B*x + C
		//y' = 2*A*x + B
		//solve for A,B,C then for x where y'=0
		
		return 0.5f * (a - c) / (a - 2.0f * b + c);
	}


	/**
	 * @return the orientation histogram extractor
	 */
	public OrientationHistogramExtractor getOriHistExtractor() {
		return oriHistExtractor;
	}
}
