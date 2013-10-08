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
 * comparative value of the eigen value with the first eigen value seen.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class AbsoluteValueEigenChooser extends EigenChooser{

	private double absoluteGap;
	private double maxSelect;

	/**
	 * @param absoluteGap the gap between the first and the current value 
	 * @param maxSelect
	 */
	public AbsoluteValueEigenChooser(double absoluteGap, double maxSelect) {
		this.absoluteGap = absoluteGap;
		this.maxSelect = maxSelect;
	}
	
	@Override
	public int nEigenVectors(Iterator<DoubleObjectPair<Vector>> vals, int totalEigenVectors) {
		double max = -Double.MAX_VALUE;
		double[] valids = new double[totalEigenVectors];
		valids[0] = vals.next().first; // Skip the first item in the calculation of max
		int i = 1; // start from the second index
		for (; vals.hasNext();) {
			double val = vals.next().first;
			if(val < 0) break;
			valids[i] = val;
			max = Math.max(max, valids[i]);
			i++;
		}
		int maxindex = i+1;
		int count = 2; // the first and the second must be included
		double first = valids[1]; // the second is what we compare against
		for (int j = 2; j < maxindex; j++) {
			double diff = Math.abs(first - valids[j]);
			if(diff / max > absoluteGap) 
				break;
			count++;
		}
		return count;
	}

	@Override
	public FewEigenvalues prepare(final SparseMatrix laplacian) {
		int total = laplacian.columnCount();
		FewEigenvalues eig = FewEigenvalues.of(laplacian);
		return eig.greatest((int) (total*maxSelect));
	}
	
	@Override
	public String toString() {
		return String.format("AbsVal=%2.2f,%2.2f",this.absoluteGap,this.maxSelect);
	}

}
