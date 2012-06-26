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
package org.openimaj.math.matrix.similarity.processor;

import org.openimaj.math.matrix.similarity.SimilarityMatrix;

/**
 * A similarity matrix processor that sets inverts all values
 * by setting the smallest to the biggest, etc.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class InvertData implements SimilarityMatrixProcessor {
	/**
	 * Default constructor. 
	 */
	public InvertData() {
		//do nothing
	}
	
	@Override
	public void process(SimilarityMatrix matrix) {
		final int rows = matrix.getRowDimension();
		final int cols = matrix.getColumnDimension();
		final double[][] data = matrix.getArray(); 
		
		double max = -Double.MAX_VALUE;
		double min = Double.MAX_VALUE;
		
		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				if (data[r][c] < min) {
					min = data[r][c];
				}
				if (data[r][c] > max) {
					max = data[r][c];
				}
			}
		}
		
		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				data[r][c] = max - (data[r][c] - min);
			}
		}
	}
}
