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
package org.openimaj.image.feature;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * Transform a {@link FloatFV} into an {@link FImage}.
 * Note: this makes a copy of the data 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FloatFV2FImage implements FeatureExtractor<FImage, FloatFV> {
	/**
	 * the image width
	 */
	public int width;
	
	/**
	 * the image height 
	 */
	public int height;
	
	/**
	 * Construct the converter with the given image size
	 * @param width
	 * @param height
	 */
	public FloatFV2FImage(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public FImage extractFeature(FloatFV object) {
		return new FImage(ArrayUtils.reshape(object.values, width, height));
	}

	/**
	 * Transform a {@link FloatFV} into an {@link FImage}.
	 * Note: this makes a copy of the data.
	 *  
	 * @param fv the feature vector
	 * @param width the image width
	 * @param height the image height
	 * @return the image
	 */
	public static FImage extractFeature(FloatFV fv, int width, int height) {
		return new FImage(ArrayUtils.reshape(fv.values, width, height));
	}
}
