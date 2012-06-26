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
package org.openimaj.image.processing.face.feature;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.io.ReadWriteableBinary;

/**
 * Interface for factory objects capable of producing a {@link FacialFeature}
 * from a {@link DetectedFace}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> Type of {@link FacialFeature}
 * @param <Q> Type of {@link DetectedFace}
 */
public interface FacialFeatureFactory<T extends FacialFeature, Q extends DetectedFace> extends ReadWriteableBinary {
	/**
	 * @return The concrete {@link FacialFeature} class.
	 */
	public Class<T> getFeatureClass();
	
	/**
	 * Compute a {@link FacialFeature} for the given
	 * detected face. The second parameter is used to signal
	 * whether the feature is intended to be used as a query in
	 * a {@link FacialFeatureComparator} or not.
	 * 
	 * Certain implementations might build a slightly different feature
	 * representation for query features.
	 * 
	 * @param face
	 * @param isquery
	 * 
	 * @return a newly computed {@link FacialFeature}.
	 */
	public T createFeature(Q face, boolean isquery);
}
