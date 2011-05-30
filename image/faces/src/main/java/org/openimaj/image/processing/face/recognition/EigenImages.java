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
package org.openimaj.image.processing.face.recognition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.parts.FacePipeline;
import org.openimaj.image.processing.face.parts.FacialDescriptor;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class EigenImages {	
	protected double[] mean;
	protected Matrix eigenvectors;

	/**
	 * Mean-center the data. Each row of data corresponds to an image.
	 * @param data the data
	 */
	protected void normaliseData(Matrix data) {
		double [][] darr = data.getArray();
		mean = new double[darr[0].length];
		
		for (int r=0; r<darr.length; r++) {
			for (int c=0; c<darr[0].length; c++) {
				mean[c] += darr[r][c];
			}
		}
		
		for (int c=0; c<darr[0].length; c++) {
			mean[c] /= darr.length;
		}
		
		for (int r=0; r<darr.length; r++) {
			for (int c=0; c<darr[0].length; c++) {
				darr[r][c] /= (mean[c]);
			}
		}
	}
	
	public void learnBasis(Matrix data, int ndims) {
		normaliseData(data);
		
		Matrix T_T = data.transpose();
		Matrix TT_T = data.times(T_T);
		EigenvalueDecomposition eig = TT_T.eig();
		Matrix all_eigenvectors = T_T.times(eig.getV());
		eigenvectors = all_eigenvectors.getMatrix(0, all_eigenvectors.getRowDimension()-1, Math.max(0, all_eigenvectors.getColumnDimension() - ndims), all_eigenvectors.getColumnDimension()-1);
	}
	
}
