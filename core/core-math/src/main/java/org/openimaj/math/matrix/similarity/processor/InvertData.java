package org.openimaj.math.matrix.similarity.processor;

import org.openimaj.math.matrix.similarity.SimilarityMatrix;

/**
 * A similarity matrix processor that sets inverts all values
 * by setting the smallest to the biggest, etc.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
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
