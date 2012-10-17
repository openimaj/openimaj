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
