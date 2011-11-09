package org.openimaj.math.matrix.similarity.processor;

import org.openimaj.math.matrix.similarity.SimilarityMatrix;

/**
 * A similarity matrix processor that sets all values
 * less than a threshold to 0 and all other values to
 * 1.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class Threshold implements SimilarityMatrixProcessor {
	protected double threshold;

	/**
	 * Default constructor. 
	 * @param threshold the threshold
	 */
	public Threshold(double threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public void process(SimilarityMatrix matrix) {
		final int rows = matrix.getRowDimension();
		final int cols = matrix.getColumnDimension();
		final double[][] data = matrix.getArray(); 
		
		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				if (data[r][c] < threshold) {
					data[r][c] = 0;
				} else {
					data[r][c] = 1;
				}
			}
		}
	}

}
