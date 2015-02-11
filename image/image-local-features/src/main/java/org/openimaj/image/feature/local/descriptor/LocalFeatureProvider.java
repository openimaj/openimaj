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
package org.openimaj.image.feature.local.descriptor;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;

/**
 * Base interface for classes capable of building local descriptors.
 * Local descriptors are constructed by being handed
 * values in a unit square sampling patch (the patch itself in
 * terms of image coordinates doesn't have to be square, but the 
 * coordinates will be normalised to lie in a unit square).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T> The type of {@link FeatureVector}
 * 
 */
public interface LocalFeatureProvider<T extends FeatureVector> extends FeatureVectorProvider<T> {	
	/**
	 * Get the amount of required over-sampling outside of 
	 * the unit square patch. An oversampling of 0.5 would
	 * result in methods such as {@link SIFTFeatureProvider#addSample(float, float, float, float)}
	 * being called with pixels in the range -0.5 to 1.5.
	 * @return the amount over sampling above the unit square
	 */
	public abstract float getOversamplingAmount();
}
