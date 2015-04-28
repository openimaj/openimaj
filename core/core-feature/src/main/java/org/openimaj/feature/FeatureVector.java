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
package org.openimaj.feature;

import java.io.Serializable;

import org.openimaj.io.ReadWriteable;

/**
 * Interface for objects that represent feature vectors.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface FeatureVector extends Cloneable, Serializable, ReadWriteable {
	/**
	 * Get the underlying data array.
	 *
	 * @return underlying data
	 */
	public Object getVector();

	/**
	 * Get the length of this vector
	 *
	 * @return the length of this vector
	 */
	public int length();

	/**
	 * Element-wise normalisation to 0..1 using separated expected minimum and
	 * maximum values for each element of the underlying feature vector.
	 *
	 * @param min
	 *            an array containing the minimum expected values
	 * @param max
	 *            an array containing the maximum expected values
	 * @return copy of the feature vector with each value normalised to 0..1
	 */
	public DoubleFV normaliseFV(double[] min, double[] max);

	/**
	 * Min-Max normalisation of the FV. Each element of the underlying feature
	 * vector is normalised to 0..1 based on the provided minimum and maximum
	 * expected values.
	 *
	 * @param min
	 *            the minimum expected value
	 * @param max
	 *            the maximum expected value
	 * @return copy of the feature vector with each value normalised to 0..1
	 */
	public DoubleFV normaliseFV(double min, double max);

	/**
	 * Normalise the FV to unit length
	 *
	 * @return a copy of the feature vector as a DoubleFV, normalised to unit
	 *         length
	 */
	public DoubleFV normaliseFV();

	/**
	 * Convert the FV to a DoubleFV representation
	 *
	 * @return a copy of the feature vector as a DoubleFV
	 */
	public DoubleFV asDoubleFV();

	/**
	 * Convert the FV to a 1-dimensional double array representation
	 *
	 * @return a copy of the feature vector as a double array
	 */
	public double[] asDoubleVector();

	/**
	 * Lp Norm of the FV.
	 *
	 * @param p
	 *            the norm to compute
	 *
	 * @return feature vector normalised using the Lp norm
	 */
	public DoubleFV normaliseFV(double p);

	/**
	 * Get an element of the feature as a double value
	 *
	 * @param i
	 *            the element index
	 * @return the value as a double
	 */
	public double getAsDouble(int i);

	/**
	 * Set an element of the feature from a double value
	 *
	 * @param i
	 *            the element index
	 * @param v
	 *            the value
	 */
	public void setFromDouble(int i, double v);

	/**
	 * Construct a new instance of this featurevector. Implementors must return
	 * an instance of themselves (rather than a different type of feature).
	 *
	 * @return a new instance.
	 */
	public FeatureVector newInstance();
}
