package org.openimaj.math.matrix.similarity.processor;

import org.openimaj.math.matrix.similarity.SimilarityMatrix;

public class NormaliseData implements SimilarityMatrixProcessor {
	boolean invert = false;
	
	/**
	 * Default constructor. 
	 */
	public NormaliseData() {
		this(false);
	}
	
	/**
	 * Default constructor. 
	 * @param invert 
	 */
	public NormaliseData(boolean invert) {
		this.invert = invert;
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
				double norm = (data[r][c] - min) / (max - min);
				
				if (invert)
					norm = 1 - norm;
				
				data[r][c] = norm;
			}
		}
	}
}
