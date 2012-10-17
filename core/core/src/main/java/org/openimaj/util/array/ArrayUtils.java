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
package org.openimaj.util.array;

import java.lang.reflect.Array;

/**
 * Collection of utilities for primitive arrays.
 * 
 * @author Jonathan Hare
 * @author Sina Samangooei
 * 
 */
public class ArrayUtils {

	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr
	 *            array of floats
	 * @return the value
	 */
	public static float maxValue(float[] arr) {
		if (arr.length < 0)
			return 0;

		float max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}

	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr
	 *            array of double
	 * @return the value
	 */
	public static double maxValue(double[] arr) {
		if (arr.length < 0)
			return 0;

		double max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}

	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr
	 *            array of bytes
	 * @return the value
	 */
	public static byte maxValue(byte[] arr) {
		if (arr.length < 0)
			return 0;

		byte max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}

	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr
	 *            array of shorts
	 * @return the value
	 */
	public static short maxValue(short[] arr) {
		if (arr.length < 0)
			return 0;

		short max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}

	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr
	 *            array of ints
	 * @return the value
	 */
	public static int maxValue(int[] arr) {
		if (arr.length < 0)
			return 0;

		int max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}

	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr
	 *            array of longs
	 * @return the value
	 */
	public static long maxValue(long[] arr) {
		if (arr.length < 0)
			return 0;

		long max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}

		return max;
	}

	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr
	 *            array of floats
	 * @return the value
	 */
	public static float minValue(float[] arr) {
		if (arr.length < 0)
			return 0;

		float min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}

		return min;
	}

	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr
	 *            array of doubles
	 * @return the value
	 */
	public static double minValue(double[] arr) {
		if (arr.length < 0)
			return 0;

		double min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}

		return min;
	}

	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr
	 *            array of bytes
	 * @return the value
	 */
	public static byte minValue(byte[] arr) {
		if (arr.length < 0)
			return 0;

		byte min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}

		return min;
	}

	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr
	 *            array of shorts
	 * @return the value
	 */
	public static short minValue(short[] arr) {
		if (arr.length < 0)
			return 0;

		short min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}

		return min;
	}

	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr
	 *            array of ints
	 * @return the value
	 */
	public static int minValue(int[] arr) {
		if (arr.length < 0)
			return 0;

		int min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}

		return min;
	}

	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr
	 *            array of longs
	 * @return the value
	 */
	public static long minValue(long[] arr) {
		if (arr.length < 0)
			return 0;

		long min = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}

		return min;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr
	 *            array of floats
	 * @return the index
	 */
	public static int minIndex(float[] arr) {
		float min = Float.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr
	 *            array of floats
	 * @return the index
	 */
	public static int maxIndex(float[] arr) {
		float max = Float.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param dsqout
	 *            array of ints
	 * @return the index
	 */
	public static int minIndex(int[] dsqout) {
		int min = Integer.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < dsqout.length; i++) {
			if (dsqout[i] < min) {
				min = dsqout[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr
	 *            array of ints
	 * @return the index
	 */
	public static int maxIndex(int[] arr) {
		int max = Integer.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param dsqout
	 *            array of longs
	 * @return the index
	 */
	public static int minIndex(long[] dsqout) {
		long min = Long.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < dsqout.length; i++) {
			if (dsqout[i] < min) {
				min = dsqout[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr
	 *            array of longs
	 * @return the index
	 */
	public static int maxIndex(long[] arr) {
		long max = Long.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param dsqout
	 *            array of byte
	 * @return the index
	 */
	public static int minIndex(byte[] dsqout) {
		byte min = Byte.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < dsqout.length; i++) {
			if (dsqout[i] < min) {
				min = dsqout[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr
	 *            array of bytes
	 * @return the index
	 */
	public static int maxIndex(byte[] arr) {
		long max = Byte.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param dsqout
	 *            array of short
	 * @return the index
	 */
	public static int minIndex(short[] dsqout) {
		short min = Short.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < dsqout.length; i++) {
			if (dsqout[i] < min) {
				min = dsqout[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr
	 *            array of shorts
	 * @return the index
	 */
	public static int maxIndex(short[] arr) {
		short max = Short.MIN_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param dsqout
	 *            array of double
	 * @return the index
	 */
	public static int minIndex(double[] dsqout) {
		double min = Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < dsqout.length; i++) {
			if (dsqout[i] < min) {
				min = dsqout[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr
	 *            array of doubles
	 * @return the index
	 */
	public static int maxIndex(double[] arr) {
		double max = -Double.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}

		return index;
	}

	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void sum(float[][] a1, float[][] a2) {
		for (int j = 0; j < a1.length; j++) {
			sum(a1[j], a2[j]);
		}
	}

	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void sum(float[] a1, float[] a2) {
		for (int j = 0; j < a1.length; j++) {
			a1[j] += a2[j];
		}
	}

	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void sum(int[][] a1, int[][] a2) {
		for (int j = 0; j < a1.length; j++) {
			sum(a1[j], a2[j]);
		}
	}

	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void sum(int[] a1, int[] a2) {
		for (int j = 0; j < a1.length; j++) {
			a1[j] += a2[j];
		}
	}

	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void sum(double[][] a1, double[][] a2) {
		for (int j = 0; j < a1.length; j++) {
			sum(a1[j], a2[j]);
		}
	}

	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void sum(double[] a1, double[] a2) {
		for (int j = 0; j < a1.length; j++) {
			a1[j] += a2[j];
		}
	}

	/**
	 * Element-wise subtraction of two arrays. Second array is subtracted from
	 * first, overwriting the first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void subtract(float[][] a1, float[][] a2) {
		for (int j = 0; j < a1.length; j++) {
			subtract(a1[j], a2[j]);
		}
	}

	/**
	 * Element-wise subtraction of two arrays. Second array is subtracted from
	 * first, overwriting the first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void subtract(float[] a1, float[] a2) {
		for (int j = 0; j < a1.length; j++) {
			a1[j] -= a2[j];
		}
	}

	/**
	 * Element-wise subtraction of two arrays. Second array is subtracted from
	 * first, overwriting the first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void subtract(int[][] a1, int[][] a2) {
		for (int j = 0; j < a1.length; j++) {
			subtract(a1[j], a2[j]);
		}
	}

	/**
	 * Element-wise subtraction of two arrays. Second array is subtracted from
	 * first, overwriting the first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void subtract(int[] a1, int[] a2) {
		for (int j = 0; j < a1.length; j++) {
			a1[j] -= a2[j];
		}
	}

	/**
	 * Element-wise subtraction of two arrays. Second array is subtracted from
	 * first, overwriting the first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void subtract(double[][] a1, double[][] a2) {
		for (int j = 0; j < a1.length; j++) {
			subtract(a1[j], a2[j]);
		}
	}

	/**
	 * Element-wise subtraction of two arrays. Second array is subtracted from
	 * first, overwriting the first array
	 * 
	 * @param a1
	 *            first array
	 * @param a2
	 *            second array
	 */
	public static void subtract(double[] a1, double[] a2) {
		for (int j = 0; j < a1.length; j++) {
			a1[j] -= a2[j];
		}
	}

	/**
	 * Normalise length of array to 1.0. Writes over array
	 * 
	 * @param array
	 *            the array
	 * @return the array
	 */
	public static float[] normalise(float[] array) {
		float sumsq = 0.0f;
		for (int i = 0; i < array.length; i++)
			sumsq += array[i] * array[i];

		final float weight = 1.0f / (float) Math.sqrt(sumsq);
		for (int i = 0; i < array.length; i++)
			array[i] *= weight;
		return array;
	}

	/**
	 * Normalise length of array to 1.0. Writes over array
	 * 
	 * @param array
	 *            the array
	 * @return the array
	 */
	public static double[] normalise(double[] array) {
		double sumsq = 0.0f;
		for (int i = 0; i < array.length; i++)
			sumsq += array[i] * array[i];

		final double weight = 1.0f / Math.sqrt(sumsq);
		for (int i = 0; i < array.length; i++)
			array[i] *= weight;
		return array;
	}

	/**
	 * Add a constant to all elements and return the input
	 * 
	 * @param ds
	 *            input array
	 * @param x
	 *            constant to add
	 * @return input
	 */
	public static double[] add(double[] ds, double x) {
		for (int i = 0; i < ds.length; i++) {
			ds[i] += x;
		}
		return ds;
	}

	/**
	 * Reverse the elements in the input and return the input
	 * 
	 * @param ds
	 *            input array
	 * @return input
	 */
	public static double[] reverse(double[] ds) {
		final int len = ds.length;
		final int hlen = len / 2;

		for (int i = 0; i < hlen; i++) {
			final double tmp = ds[i];
			ds[i] = ds[len - i - 1];
			ds[len - i - 1] = tmp;
		}
		return ds;
	}

	/**
	 * Reverse the elements in the input and return the input
	 * 
	 * @param ds
	 *            input array
	 * @return input
	 */
	public static float[] reverse(float[] ds) {
		final int len = ds.length;
		final int hlen = len / 2;

		for (int i = 0; i < hlen; i++) {
			final float tmp = ds[i];
			ds[i] = ds[len - i - 1];
			ds[len - i - 1] = tmp;
		}
		return ds;
	}

	/**
	 * Convert a float array to a double array.
	 * 
	 * @param array
	 *            array of floats to convert
	 * @return array of doubles
	 */
	public static double[] floatToDouble(float[] array) {
		final double[] darr = new double[array.length];

		for (int i = 0; i < array.length; i++) {
			darr[i] = array[i];
		}
		return darr;
	}

	/**
	 * Convert a long array to a double array.
	 * 
	 * @param array
	 *            array of floats to convert
	 * @return array of doubles
	 */
	public static double[] longToDouble(long[] array) {
		final double[] darr = new double[array.length];

		for (int i = 0; i < array.length; i++) {
			darr[i] = array[i];
		}
		return darr;
	}

	/**
	 * Convert a double array to a float array.
	 * 
	 * @param array
	 *            array of doubles to convert
	 * @return array of floats
	 */
	public static float[] doubleToFloat(double[] array) {
		final float[] farr = new float[array.length];

		for (int i = 0; i < array.length; i++) {
			farr[i] = (float) array[i];
		}
		return farr;
	}

	/**
	 * Return the first non-null item from an array.
	 * 
	 * @param <T>
	 *            the type of the elements in the array
	 * @param array
	 *            the array
	 * @return the first non-null object, or null if not found.
	 */
	public static <T> T firstNonNull(T[] array) {
		if (array == null)
			return null;

		for (final T obj : array) {
			if (obj != null) {
				return obj;
			}
		}

		return null;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param <T>
	 *            Type of elements in the array.
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static <T> T[] concatenate(T[]... arrays) {
		int length = 0;
		Class<?> type = null;

		for (final T[] arr : arrays) {
			if (arr != null) {
				length += arr.length;

				if (type == null) {
					type = arr.getClass().getComponentType();
				}
			}
		}

		@SuppressWarnings("unchecked")
		final T[] concat = (T[]) Array.newInstance(type, length);

		int current = 0;
		for (final T[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static double[] concatenate(double[]... arrays) {
		int length = 0;
		for (final double[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}

		final double[] concat = new double[length];

		int current = 0;
		for (final double[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static float[] concatenate(float[]... arrays) {
		int length = 0;
		for (final float[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}

		final float[] concat = new float[length];

		int current = 0;
		for (final float[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static long[] concatenate(long[]... arrays) {
		int length = 0;
		for (final long[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}

		final long[] concat = new long[length];

		int current = 0;
		for (final long[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static short[] concatenate(short[]... arrays) {
		int length = 0;
		for (final short[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}

		final short[] concat = new short[length];

		int current = 0;
		for (final short[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static int[] concatenate(int[]... arrays) {
		int length = 0;
		for (final int[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}

		final int[] concat = new int[length];

		int current = 0;
		for (final int[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static byte[] concatenate(byte[]... arrays) {
		byte length = 0;
		for (final byte[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}

		final byte[] concat = new byte[length];

		byte current = 0;
		for (final byte[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays
	 *            the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static char[] concatenate(char[]... arrays) {
		char length = 0;
		for (final char[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}

		final char[] concat = new char[length];

		char current = 0;
		for (final char[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}

		return concat;
	}

	/**
	 * Compute the sum of values in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static double sumValues(double[] vector) {
		double sum = 0;

		for (final double v : vector)
			sum += v;

		return sum;
	}

	/**
	 * Compute the sum of values in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static float sumValues(float[] vector) {
		float sum = 0;

		for (final float v : vector)
			sum += v;

		return sum;
	}

	/**
	 * Compute the sum of values in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValues(int[] vector) {
		int sum = 0;

		for (final int v : vector)
			sum += v;

		return sum;
	}

	/**
	 * Compute the sum of values in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValues(byte[] vector) {
		int sum = 0;

		for (final int v : vector)
			sum += v;

		return sum;
	}

	/**
	 * Compute the sum of values in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValues(short[] vector) {
		int sum = 0;

		for (final int v : vector)
			sum += v;

		return sum;
	}

	/**
	 * Compute the sum of values in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static long sumValues(long[] vector) {
		long sum = 0;

		for (final long v : vector)
			sum += v;

		return sum;
	}

	/**
	 * Compute the sum of values squared in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static double sumValuesSquared(double[] vector) {
		double sum = 0;

		for (final double v : vector)
			sum += v * v;

		return sum;
	}

	/**
	 * Compute the sum of values squared in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static float sumValuesSquared(float[] vector) {
		float sum = 0;

		for (final float v : vector)
			sum += v * v;

		return sum;
	}

	/**
	 * Compute the sum of values squared in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValuesSquared(int[] vector) {
		int sum = 0;

		for (final int v : vector)
			sum += v * v;

		return sum;
	}

	/**
	 * Compute the sum of values squared in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValuesSquared(byte[] vector) {
		int sum = 0;

		for (final int v : vector)
			sum += v * v;

		return sum;
	}

	/**
	 * Compute the sum of values squared in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValuesSquared(short[] vector) {
		int sum = 0;

		for (final int v : vector)
			sum += v * v;

		return sum;
	}

	/**
	 * Compute the sum of values squared in an array
	 * 
	 * @param vector
	 * @return the sum of all values
	 */
	public static long sumValuesSquared(long[] vector) {
		long sum = 0;

		for (final long v : vector)
			sum += v * v;

		return sum;
	}

	/**
	 * Extract a range
	 * 
	 * @param start
	 * @param length
	 * @return [start...length] (inclusive)
	 */
	public static int[] range(int start, int length) {
		final int[] range = new int[length - start + 1];
		for (int i = start; i <= length; i++) {
			range[i - start] = i;
		}
		return range;
	}

	/**
	 * Reshape a 2D array into a 1D array
	 * 
	 * @param a
	 *            array to reshape
	 * @return the reshaped array
	 */
	public static float[] reshape(float[][] a) {
		final float[] ret = new float[a.length * a[0].length];

		for (int r = 0, i = 0; r < a.length; r++)
			for (int c = 0; c < a[0].length; c++, i++)
				ret[i] = a[r][c];

		return ret;
	}

	/**
	 * Reshape a 2D array into a 1D array
	 * 
	 * @param a
	 *            array to reshape
	 * @return the reshaped array
	 */
	public static double[] reshapeDouble(float[][] a) {
		final double[] ret = new double[a.length * a[0].length];

		for (int r = 0, i = 0; r < a.length; r++)
			for (int c = 0; c < a[0].length; c++, i++)
				ret[i] = a[r][c];

		return ret;
	}

	/**
	 * Reshape a 1D array into a 2D array
	 * 
	 * @param a
	 *            array to reshape
	 * @param out
	 *            the return array, correctly sized
	 * @return the reshaped array
	 */
	public static float[][] reshape(float[] a, float[][] out) {
		for (int r = 0, i = 0; r < out.length; r++)
			for (int c = 0; c < out[0].length; c++, i++)
				out[r][c] = a[i];

		return out;
	}

	/**
	 * Reshape a 1D array into a 2D array
	 * 
	 * @param a
	 *            array to reshape
	 * @param width
	 *            the width of the return array
	 * @param height
	 *            the height of the return array
	 * @return the reshaped array
	 */
	public static float[][] reshape(float[] a, int width, int height) {
		final float[][] ret = new float[height][width];
		return reshape(a, ret);
	}

	/**
	 * Reshape a 1D array into a 2D array
	 * 
	 * @param a
	 *            array to reshape
	 * @param out
	 *            the return array, correctly sized
	 * @return the reshaped array
	 */
	public static float[][] reshape(double[] a, float[][] out) {
		for (int r = 0, i = 0; r < out.length; r++)
			for (int c = 0; c < out[0].length; c++, i++)
				out[r][c] = (float) a[i];

		return out;
	}

	/**
	 * Reshape a 1D array into a 2D array
	 * 
	 * @param a
	 *            array to reshape
	 * @param width
	 *            the width of the return array
	 * @param height
	 *            the height of the return array
	 * @return the reshaped array
	 */
	public static float[][] reshape(double[] a, int width, int height) {
		final float[][] ret = new float[height][width];
		return reshape(a, ret);
	}

	/**
	 * Sort parallel arrays. Arrays are sorted in-place. The first array
	 * determines the order, and is sorted into descending order.
	 * <p>
	 * Implementation inspired by this stackoverflow page: <a href=
	 * "http://stackoverflow.com/questions/951848/java-array-sort-quick-way-to-get-a-sorted-list-of-indices-of-an-array"
	 * > http://stackoverflow.com/questions/951848/java-array-sort-quick-way-to-
	 * get-a-sorted-list-of-indices-of-an-array </a>
	 * 
	 * @param main
	 *            the values to use for determining the order
	 * @param indices
	 *            the second array
	 */
	public static void parallelQuicksortDescending(double[] main, int[] indices) {
		parallelQuicksortDescending(main, indices, 0, indices.length - 1);
	}

	/**
	 * Sort parallel arrays. Arrays are sorted in-place. The first array
	 * determines the order, and is sorted into descending order.
	 * <p>
	 * Implementation inspired by this stackoverflow page: <a href=
	 * "http://stackoverflow.com/questions/951848/java-array-sort-quick-way-to-get-a-sorted-list-of-indices-of-an-array"
	 * > http://stackoverflow.com/questions/951848/java-array-sort-quick-way-to-
	 * get-a-sorted-list-of-indices-of-an-array </a>
	 * 
	 * @param main
	 *            the values to use for determining the order
	 * @param indices
	 *            the second array
	 * @param left
	 *            the starting index
	 * @param right
	 *            the ending index
	 */
	public static void parallelQuicksortDescending(double[] main, int[] indices, int left, int right) {
		if (right <= left)
			return;

		final int i = partitionDesc(main, indices, left, right);

		parallelQuicksortDescending(main, indices, left, i - 1);
		parallelQuicksortDescending(main, indices, i + 1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partitionDesc(double[] a, int[] index, int left, int right) {
		int i = left - 1;
		int j = right;
		while (true) {
			while (a[++i] > a[right])
				// find item on left to swap
				; // a[right] acts as sentinel
			while (a[right] > a[--j])
				// find item on right to swap
				if (j == left)
					break; // don't go out-of-bounds
			if (i >= j)
				break; // check if pointers cross
			exch(a, index, i, j); // swap two elements into place
		}
		exch(a, index, i, right); // swap with partition element
		return i;
	}

	// exchange a[i] and a[j]
	private static void exch(double[] a, int[] index, int i, int j) {
		final double swap = a[i];
		a[i] = a[j];
		a[j] = swap;

		final int b = index[i];
		index[i] = index[j];
		index[j] = b;
	}

	public static int[] indexSort(float[] main) {
		final int[] index = new int[main.length];

		for (int i = 0; i < index.length; i++)
			index[i] = i;

		quicksort(main, index, 0, index.length - 1);

		return index;
	}

	// quicksort a[left] to a[right]
	private static void quicksort(float[] a, int[] index, int left, int right) {
		if (right <= left)
			return;
		final int i = partition(a, index, left, right);
		quicksort(a, index, left, i - 1);
		quicksort(a, index, i + 1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition(float[] a, int[] index,
			int left, int right)
	{
		int i = left - 1;
		int j = right;
		while (true) {
			while (a[index[++i]] < a[index[right]])
				// find item on left to swap
				; // a[right] acts as sentinel
			while (a[index[right]] < a[index[--j]])
				// find item on right to swap
				if (j == left)
					break; // don't go out-of-bounds
			if (i >= j)
				break; // check if pointers cross
			exch(index, i, j); // swap two elements into place
		}
		exch(index, i, right); // swap with partition element
		return i;
	}

	// is x < y ?
	private static boolean less(float x, float y) {
		return (x < y);
	}

	// exchange a[i] and a[j]
	private static void exch(int[] index, int i, int j) {
		final int b = index[i];
		index[i] = index[j];
		index[j] = b;
	}

	/**
	 * Normalise and scale the values so that the maximum value in the array is
	 * 1.
	 * 
	 * @param array
	 *            The array to normalise
	 * @return The array
	 */
	public static double[] normaliseMax(double[] array) {
		return normaliseMax(array, 1d);
	}

	/**
	 * Normalise and scale the values so that the maximum value in the array is
	 * max
	 * 
	 * @param array
	 *            The array to normalise
	 * @param max
	 *            The maximum value
	 * @return The array
	 */
	public static double[] normaliseMax(double[] array, double max) {
		final double m = maxValue(array);
		for (int i = 0; i < array.length; i++)
			array[i] /= m;
		return array;
	}

	/**
	 * Convert the array to a {@link String} by joining the elements with the
	 * given glue.
	 * 
	 * @param s
	 *            the array
	 * @param glue
	 *            the glue
	 * @return the string
	 */
	public static String toString(String[] s, String glue) {
		final int k = s.length;

		if (k == 0)
			return null;

		final StringBuilder out = new StringBuilder();
		out.append(s[0]);

		for (int x = 1; x < k; ++x)
			out.append(glue).append(s[x]);

		return out.toString();
	}
}
