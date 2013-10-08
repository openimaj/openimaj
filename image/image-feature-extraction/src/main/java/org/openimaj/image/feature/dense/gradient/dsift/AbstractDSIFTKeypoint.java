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
package org.openimaj.image.feature.dense.gradient.dsift;

import java.io.Serializable;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.io.VariableLength;

/**
 * Abstract base for Dense SIFT keypoints with a location and vector. Also
 * includes the energy of the feature prior to normalisation in case
 * low-contrast features need removing.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of {@link ArrayFeatureVector} that this keypoint provides
 * @param <Q>
 *            Type of primitive array backing the feature
 */
abstract class AbstractDSIFTKeypoint<T extends ArrayFeatureVector<Q>, Q>
		extends
		SpatialLocation
		implements
		Serializable,
		LocalFeature<SpatialLocation, T>,
		VariableLength,
		Cloneable
{
	final static long serialVersionUID = 1234554345;

	/**
	 * Default length of standard SIFT features.
	 */
	final static int DEFAULT_LENGTH = 128;

	/**
	 * The energy of the descriptor prior to normalisation; computed as the sum
	 * of descriptor values divided by the number of sample pixels used to
	 * create it (hence comparable across different window sizes). Can be used
	 * to remove low-contrast descriptors.
	 */
	public float energy;

	/**
	 * The descriptor (normalised)
	 */
	public Q descriptor;

	@Override
	public SpatialLocation getLocation() {
		return this;
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public String asciiHeader() {
		return "";
	}
}
