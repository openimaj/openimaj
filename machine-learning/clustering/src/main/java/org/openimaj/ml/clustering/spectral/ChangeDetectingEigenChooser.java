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

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.eigenvalues.FewEigenvalues;

/**
 * Attempts to automatically choose the number of eigen vectors based on the
 * relative gap between eigen values. In spectral clustering the gap between the
 * eigen values of "good" clusters jumps. This class ignores the gap between 0 and
 * the next item because 0s represent completely isolated objects and in all but the trivial
 * case we must stop after we have run out of 0s.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ChangeDetectingEigenChooser extends EigenChooser {

	private double relativeGap;
	private double maxSelect;

	/**
	 * @param relativeGap the gap between previous and current (treated as absolute if previous value == 0)
	 * @param maxSelect
	 */
	public ChangeDetectingEigenChooser(double relativeGap, double maxSelect) {
		this.relativeGap = relativeGap;
		this.maxSelect = maxSelect;
	}

	@Override
	public int nEigenVectors(Iterator<DoubleObjectPair<Vector>> vals, int totalEigenVectors) {
		int count = 0;
		double prevDiff = 0;
		double prevVal = vals.next().first;
		for (;vals.hasNext();) {
			double val = vals.next().first;
			if(val < 0) break;
			double diff = Math.abs(val - prevVal);
			if(prevDiff != 0){
				double l = prevDiff * relativeGap;
				if(diff > l) {
					count++;
					break;
				}
			}
			prevDiff = diff;
			prevVal = val;
			count ++;
		}
		int maxCount = (int) (totalEigenVectors * maxSelect);
		if(count > maxCount){
			return maxCount;
		}
		return count;
	}

	@Override
	public FewEigenvalues prepare(final SparseMatrix laplacian) {
		int total = laplacian.columnCount();
		FewEigenvalues eig = FewEigenvalues.of(laplacian);
		return eig.greatest((int) (total*maxSelect));
	}


}
