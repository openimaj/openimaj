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

/**
 * Abstract base class for all types of {@link FeatureVector} that are backed by
 * a native array.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <ARRAYTYPE>
 *            Primitive a type of the backing array
 */
public abstract class ArrayFeatureVector<ARRAYTYPE> implements FeatureVector {
	private static final long serialVersionUID = 1L;

	/**
	 * Array of all the values in the feature vector
	 */
	public ARRAYTYPE values;

	/**
	 * Get the underlying representation
	 * 
	 * @return the feature as an array
	 */
	@Override
	public ARRAYTYPE getVector() {
		return values;
	}

	/**
	 * Returns a new featurevector that is a subsequence of this vector. The
	 * subsequence begins with the element at the specified index and extends to
	 * the end of this vector.
	 * 
	 * @param beginIndex
	 *            the beginning index, inclusive.
	 * @return the specified subvector.
	 * @exception IndexOutOfBoundsException
	 *                if <code>beginIndex</code> is negative or larger than the
	 *                length of this <code>ArrayFeatureVector</code> object.
	 */
	public abstract ArrayFeatureVector<ARRAYTYPE> subvector(int beginIndex);

	/**
	 * Returns a new string that is a subvector of this vector. The subvector
	 * begins at the specified <code>beginIndex</code> and extends to the
	 * element at index <code>endIndex - 1</code>. Thus the length of the
	 * subvector is <code>endIndex-beginIndex</code>.
	 * 
	 * @param beginIndex
	 *            the beginning index, inclusive.
	 * @param endIndex
	 *            the ending index, exclusive.
	 * @return the specified subvector.
	 * @exception IndexOutOfBoundsException
	 *                if the <code>beginIndex</code> is negative, or
	 *                <code>endIndex</code> is larger than the length of this
	 *                <code>ArrayFeatureVector</code> object, or
	 *                <code>beginIndex</code> is larger than
	 *                <code>endIndex</code>.
	 */
	public abstract ArrayFeatureVector<ARRAYTYPE> subvector(int beginIndex, int endIndex);
}
