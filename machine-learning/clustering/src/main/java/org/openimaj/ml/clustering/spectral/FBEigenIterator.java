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
package org.openimaj.ml.clustering.spectral;

import java.util.Iterator;

import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * A forward or backward iterator of eigen vector/value pairs
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public final class FBEigenIterator implements
		Iterator<DoubleObjectPair<Vector>> {

	private int pos;
	private int dir;
	private double[] values;
	private Vector[] vectors;
	private int lim;
	/**
	 * @param evd
	 */
	public FBEigenIterator(Eigenvalues evd) {
		this.values = evd.value;
		this.vectors = evd.vector;
		this.pos = values.length-1;
		this.dir = -1;
		this.lim = -1;
	}

	@Override
	public void remove() { throw new UnsupportedOperationException();}

	@Override
	public DoubleObjectPair<Vector> next() {
		DoubleObjectPair<Vector> ret = new DoubleObjectPair<Vector>(this.values[pos], this.vectors[pos]);
		pos += dir;
		return ret;
	}

	@Override
	public boolean hasNext() {
		return pos != lim;
	}
}