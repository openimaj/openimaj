package org.openimaj.math.statistics.distribution;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * Test the histograms
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class MultidimensionalHistogramTest {
	/**
	 * Test if the result of {@link MultidimensionalHistogram#getIndex(int...)} coresponds with
	 * {@link MultidimensionalHistogram#getCoordinates(int)}
	 */
	@Test
	public void testIndexToCoordiante(){
		float[][] testVals = new float[][]{
			new float[]{0.5f,0.0f,0.0f},
			new float[]{0.0f,0.5f,0.0f},
			new float[]{0.0f,0.0f,0.5f},
			new float[]{0.7f,0.2f,0.5f},
		};
		int[] dims = new int[]{4,10,20};
		MultidimensionalHistogram hist = new MultidimensionalHistogram(dims);
		for (float[] val : testVals) {
			int[] valIndex = valIndex(val,dims);
			assertTrue(checkIndex(hist,valIndex));
		}
	}

	private boolean checkIndex(MultidimensionalHistogram hist, int[] val1Index) {
		int index = hist.getIndex(val1Index);
		int[] calcIndex = hist.getCoordinates(index);
		return Arrays.equals(val1Index, calcIndex);
	}

	private int[] valIndex(float[] v, int... ndims) {
		int[] coord = new int[ndims.length];
		for (int i = 0; i < ndims.length; i++) {
			coord[i] = (int) Math.floor(v[i] * ndims[i]);
		}
		return coord;
	}
}
