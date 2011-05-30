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
