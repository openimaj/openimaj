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
package org.openimaj.math.util;

import org.openimaj.util.array.ArrayUtils;

/**
 * 
 * Some basic statistical operations on float arrays
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class FloatArrayStatsUtils {
	/**
	 * Find the mean of a single dimensional float array. returns 0 if the array
	 * is empty.
	 * 
	 * @param arr
	 * @return the mean
	 */
	public static float mean(float[] arr) {
		if (arr.length == 0) {
			return 0;
		}
		int count = 1;
		float mean = arr[0];
		for (int i = 1; i < arr.length; i++) {
			count++;
			mean = mean + (arr[i] - mean) / count;
		}
		return mean;
	}

	/**
	 * Calculate the mean of a two dimensional float array. returns 0 if the
	 * array is empty.
	 * 
	 * @param arr
	 * @return the mean
	 */
	public static float mean(float[][] arr) {
		if (arr.length == 0) {
			return 0;
		}
		int firstRowIndex = 0;
		while (arr[firstRowIndex].length == 0)
			firstRowIndex++;
		int firstColIndex = 1;

		int count = 1;
		float mean = arr[firstRowIndex][0];

		for (int i = firstRowIndex; i < arr.length; i++) {
			for (int j = firstColIndex; j < arr[i].length; j++) {
				count++;
				mean = mean + (arr[i][j] - mean) / count;
			}
			firstColIndex = 0;
		}

		return mean;
	}

	/**
	 * Calculate the variance of a one dimensional float array. If the length of
	 * the array is less than 2, variance is 0.
	 * 
	 * @param arr
	 * @return the variance
	 */
	public static float var(float[] arr) {
		if (arr.length < 2) {
			return 0;
		}

		int count = 1;
		float oldMean = arr[0];
		float newMean = arr[0];
		float var = 0;

		for (int i = 1; i < arr.length; i++) {
			count++;
			final float x = arr[i];
			newMean = oldMean + (x - oldMean) / count;
			var = var + (x - oldMean) * (x - newMean);
			oldMean = newMean;
		}

		return var / (count - 1);
	}

	/**
	 * Calculate the variance of a one dimensional float array. If the length of
	 * the array is less than 2, variance is 0.
	 * 
	 * @param arr
	 * @return the variance
	 */
	public static float var(float[][] arr) {
		if (arr.length == 0) {
			return 0;
		}

		int firstRowIndex = 0;
		while (arr[firstRowIndex].length == 0)
			firstRowIndex++;
		int firstColIndex = 1;

		int count = 1;
		float oldMean = arr[firstRowIndex][0];
		float newMean = arr[firstRowIndex][0];
		float var = 0;

		for (int i = firstRowIndex; i < arr.length; i++) {
			for (int j = firstColIndex; j < arr[i].length; j++) {
				count++;
				final float x = arr[i][j];
				newMean = oldMean + (x - oldMean) / count;
				var = var + (x - oldMean) * (x - newMean);
				oldMean = newMean;
			}
			firstColIndex = 0;
		}

		return count > 1 ? var / (count - 1) : 0;
	}

	/**
	 * Calculate the standard deviation of a 2D array. Calls
	 * {@link FloatArrayStatsUtils#var(float[][])} and does a Math.sqrt.
	 * 
	 * @param arr
	 * @return the standard deviation
	 */
	public static float std(float[][] arr) {
		return (float) Math.sqrt(var(arr));
	}

	/**
	 * Calculate the standard deviation of a 1D array. Calls
	 * {@link FloatArrayStatsUtils#var(float[])} and does a Math.sqrt.
	 * 
	 * @param arr
	 * @return the standard deviation
	 */
	public static float std(float[] arr) {
		return (float) Math.sqrt(var(arr));
	}

	/**
	 * Calculate the sum of a 2D array.
	 * 
	 * @param arr
	 * @return the sum
	 */
	public static float sum(float[][] arr) {
		float sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += sum(arr[i]);
		return sum;
	}

	/**
	 * Calculate the sum of a 1D array.
	 * 
	 * @param arr
	 * @return the sum
	 */
	public static float sum(float[] arr) {
		float sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += arr[i];
		return sum;
	}

	/**
	 * Calculate the sum of the squared values of a 2D array.
	 * 
	 * @param arr
	 * @return sum of squares
	 */
	public static float sumSq(float[][] arr) {
		float sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += sumSq(arr[i]);
		return sum;
	}

	/**
	 * Calculate the sum the squared values of a 1D array.
	 * 
	 * @param arr
	 * @return sum of squares
	 */
	public static float sumSq(float[] arr) {
		float sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += arr[i] * arr[i];
		return sum;
	}

	/**
	 * Calculate the sum of the absolute values of a 2D array.
	 * 
	 * @param arr
	 * @return the sum absolute values
	 */
	public static float sumAbs(float[][] arr) {
		float sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += sumAbs(arr[i]);
		return sum;
	}

	/**
	 * Calculate the sum the absolute values of a 1D array.
	 * 
	 * @param arr
	 * @return the sum absolute values
	 */
	public static float sumAbs(float[] arr) {
		float sum = 0;
		for (int i = 0; i < arr.length; i++)
			sum += Math.abs(arr[i]);
		return sum;
	}

	/**
	 * Calculate the median of the given array. Uses the quick select algorithm
	 * ({@link ArrayUtils#quickSelect(float[], int)}).
	 * 
	 * @param arr
	 *            the array
	 * @return the median value
	 */
	public static float median(float[] arr) {
		final int median = arr.length / 2;

		if (arr.length % 2 == 0) {
			final float a = ArrayUtils.quickSelect(arr, median);
			final float b = ArrayUtils.quickSelect(arr, median - 1);

			return (a + b) / 2f;
		}
		return ArrayUtils.quickSelect(arr, median);
	}

	/**
	 * Calculate the median of the given sub-array. Uses the quick select
	 * algorithm ({@link ArrayUtils#quickSelect(float[], int, int, int)}).
	 * 
	 * @param arr
	 *            the array
	 * @param start
	 *            starting point in the array (inclusive)
	 * @param stop
	 *            stopping point in the array (exclusive)
	 * @return the median value
	 */
	public static float median(float[] arr, int start, int stop) {
		final int median = arr.length / 2;

		if (arr.length % 2 == 0) {
			final float a = ArrayUtils.quickSelect(arr, median, start, stop);
			final float b = ArrayUtils.quickSelect(arr, median - 1, start, stop);

			return (a + b) / 2f;
		}
		return ArrayUtils.quickSelect(arr, median, start, stop);
	}
}
