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
	 * @param arr array of floats
	 * @return the value
	 */
	public static float maxValue(float[] arr) {
		if (arr.length < 0)
			return 0;
		
		float max = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr array of double
	 * @return the value
	 */
	public static double maxValue(double[] arr) {
		if (arr.length < 0)
			return 0;
		
		double max = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr array of bytes
	 * @return the value
	 */
	public static byte maxValue(byte[] arr) {
		if (arr.length < 0)
			return 0;
		
		byte max = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr array of shorts
	 * @return the value
	 */
	public static short maxValue(short[] arr) {
		if (arr.length < 0)
			return 0;
		
		short max = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr array of ints
	 * @return the value
	 */
	public static int maxValue(int[] arr) {
		if (arr.length < 0)
			return 0;
		
		int max = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	
	/**
	 * Returns the largest value in the array
	 * 
	 * @param arr array of longs
	 * @return the value
	 */
	public static long maxValue(long[] arr) {
		if (arr.length < 0)
			return 0;
		
		long max = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		
		return max;
	}
	
	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr array of floats
	 * @return the value
	 */
	public static float minValue(float[] arr) {
		if (arr.length < 0)
			return 0;
		
		float min = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		
		return min;
	}
	
	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr array of doubles
	 * @return the value
	 */
	public static double minValue(double[] arr) {
		if (arr.length < 0)
			return 0;
		
		double min = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		
		return min;
	}
	
	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr array of bytes
	 * @return the value
	 */
	public static byte minValue(byte[] arr) {
		if (arr.length < 0)
			return 0;
		
		byte min = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		
		return min;
	}
	
	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr array of shorts
	 * @return the value
	 */
	public static short minValue(short[] arr) {
		if (arr.length < 0)
			return 0;
		
		short min = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		
		return min;
	}
	
	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr array of ints
	 * @return the value
	 */
	public static int minValue(int[] arr) {
		if (arr.length < 0)
			return 0;
		
		int min = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		
		return min;
	}
	
	
	/**
	 * Returns the smallest value in the array
	 * 
	 * @param arr array of longs
	 * @return the value
	 */
	public static long minValue(long[] arr) {
		if (arr.length < 0)
			return 0;
		
		long min = arr[0];
		for (int i=1; i<arr.length; i++) {
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		
		return min;
	}
	
	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr array of floats
	 * @return the index
	 */
	public static int minIndex(float[] arr) {
		float min = Float.MAX_VALUE;
		int index = 0;
		for (int i=0; i<arr.length; i++) {
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
	 * @param arr array of floats
	 * @return the index
	 */
	public static int maxIndex(float[] arr) {
		float max = Float.MIN_VALUE;
		int index = 0;
		for (int i=0; i<arr.length; i++) {
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
	 * @param dsqout array of ints
	 * @return the index
	 */
	public static int minIndex(int[] dsqout) {
		int min = Integer.MAX_VALUE;
		int index = 0;
		for (int i=0; i<dsqout.length; i++) {
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
	 * @param arr array of ints
	 * @return the index
	 */
	public static int maxIndex(int[] arr) {
		int max = Integer.MIN_VALUE;
		int index = 0;
		for (int i=0; i<arr.length; i++) {
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
	 * @param dsqout array of longs
	 * @return the index
	 */
	public static int minIndex(long[] dsqout) {
		long min = Long.MAX_VALUE;
		int index = 0;
		for (int i=0; i<dsqout.length; i++) {
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
	 * @param arr array of longs
	 * @return the index
	 */
	public static int maxIndex(long[] arr) {
		long max = Long.MIN_VALUE;
		int index = 0;
		for (int i=0; i<arr.length; i++) {
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
	 * @param dsqout array of byte
	 * @return the index
	 */
	public static int minIndex(byte[] dsqout) {
		byte min = Byte.MAX_VALUE;
		int index = 0;
		for (int i=0; i<dsqout.length; i++) {
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
	 * @param arr array of bytes
	 * @return the index
	 */
	public static int maxIndex(byte[] arr) {
		long max = Byte.MIN_VALUE;
		int index = 0;
		for (int i=0; i<arr.length; i++) {
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
	 * @param dsqout array of short
	 * @return the index
	 */
	public static int minIndex(short[] dsqout) {
		short min = Short.MAX_VALUE;
		int index = 0;
		for (int i=0; i<dsqout.length; i++) {
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
	 * @param arr array of shorts
	 * @return the index
	 */
	public static int maxIndex(short[] arr) {
		short max = Short.MIN_VALUE;
		int index = 0;
		for (int i=0; i<arr.length; i++) {
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
	 * @param dsqout array of double
	 * @return the index
	 */
	public static int minIndex(double[] dsqout) {
		double min = Double.MAX_VALUE;
		int index = 0;
		for (int i=0; i<dsqout.length; i++) {
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
	 * @param arr array of doubles
	 * @return the index
	 */
	public static int maxIndex(double[] arr) {
		double max = -Double.MAX_VALUE;
		int index = 0;
		for (int i=0; i<arr.length; i++) {
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
	 * @param a1 first array
	 * @param a2 second array
	 */
	public static void sum(float [][] a1, float [][] a2) {
		for (int j=0; j<a1.length; j++) {
			sum(a1[j], a2[j]);
		}
	}
	
	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1 first array
	 * @param a2 second array
	 */
	public static void sum(float [] a1, float [] a2) {
		for (int j=0; j<a1.length; j++) {
			a1[j] += a2[j];
		}
	}
	
	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1 first array
	 * @param a2 second array
	 */
	public static void sum(int [][] a1, int [][] a2) {
		for (int j=0; j<a1.length; j++) {
			sum(a1[j], a2[j]);
		}
	}
	
	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1 first array
	 * @param a2 second array
	 */
	public static void sum(int [] a1, int [] a2) {
		for (int j=0; j<a1.length; j++) {
			a1[j] += a2[j];
		}
	}
	
	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1 first array
	 * @param a2 second array
	 */
	public static void sum(double [][] a1, double [][] a2) {
		for (int j=0; j<a1.length; j++) {
			sum(a1[j], a2[j]);
		}
	}
	
	/**
	 * Element-wise summation of two arrays, output writes over first array
	 * 
	 * @param a1 first array
	 * @param a2 second array
	 */
	public static void sum(double [] a1, double [] a2) {
		for (int j=0; j<a1.length; j++) {
			a1[j] += a2[j];
		}
	}
	
	/**
	 * Normalise length of array to 1.0. Writes over array
	 * 
	 * @param array the array
	 */
	public static void normalise(float[] array) {
		float sumsq = 0.0f;
		for (int i = 0; i < array.length; i++)
			sumsq += array[i] * array[i];
		
		float weight = 1.0f / (float) Math.sqrt(sumsq);
		for (int i=0; i<array.length; i++)
			array[i] *= weight;
	}
	
	/**
	 * Normalise length of array to 1.0. Writes over array
	 * 
	 * @param array the array
	 */
	public static void normalise(double[] array) {
		double sumsq = 0.0f;
		for (int i = 0; i < array.length; i++)
			sumsq += array[i] * array[i];
		
		double weight = 1.0f / Math.sqrt(sumsq);
		for (int i=0; i<array.length; i++)
			array[i] *= weight;
	}

	/**
	 * Add a constant to all elements and return the input
	 * 
	 * @param ds input array
	 * @param x constant to add
	 * @return input
	 */
	public static double[] add(double[] ds, double x) {
		for(int i = 0; i < ds.length; i++){
			ds[i] += x;
		}
		return ds;
	}
	
	/**
	 * Reverse the elements in the input and return the input
	 * 
	 * @param ds input array
	 * @return input
	 */
	public static double[] reverse(double[] ds) {
		final int len = ds.length;
		final int hlen = len / 2;
		
		for(int i = 0; i < hlen; i++) {
			double tmp = ds[i];
			ds[i] = ds[len - i - 1];
			ds[len - i - 1] = tmp;
		}
		return ds;
	}
	
	/**
	 * Reverse the elements in the input and return the input
	 * 
	 * @param ds input array
	 * @return input
	 */
	public static float[] reverse(float[] ds) {
		final int len = ds.length;
		final int hlen = len / 2;
		
		for(int i = 0; i < hlen; i++) {
			float tmp = ds[i];
			ds[i] = ds[len - i - 1];
			ds[len - i - 1] = tmp;
		}
		return ds;
	}
	
	/**
	 * Convert a float array to a double array.
	 * @param array array of floats to convert
	 * @return array of doubles
	 */
	public static double[] floatToDouble(float[] array) {
		double[] darr = new double[array.length];
		
		for (int i=0; i<array.length; i++) {
			darr[i] = array[i]; 
		}
		return darr;
	}
	
	/**
	 * Convert a long array to a double array.
	 * @param array array of floats to convert
	 * @return array of doubles
	 */
	public static double[] longToDouble(long[] array) {
		double[] darr = new double[array.length];
		
		for (int i=0; i<array.length; i++) {
			darr[i] = array[i]; 
		}
		return darr;
	}
	
	/**
	 * Convert a double array to a float array.
	 * @param array array of doubles to convert
	 * @return array of floats
	 */
	public static float[] doubleToFloat(double[] array) {
		float[] farr = new float[array.length];
		
		for (int i=0; i<array.length; i++) {
			farr[i] = (float) array[i]; 
		}
		return farr;
	}
	
	/**
	 * Return the first non-null item from an array.
	 * @param <T> the type of the elements in the array
	 * @param array the array
	 * @return the first non-null object, or null if not found.
	 */
	public static <T> T firstNonNull(T[] array) {
		if (array == null) return null;
		
		for (T obj : array) {
			if (obj != null) {
				return obj;
			}
		}
		
		return null;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param <T> Type of elements in the array.
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static <T> T[] concatenate(T[]... arrays) {
		int length = 0;
		Class<?> type = null;
		
		for (T[] arr : arrays) {
			if (arr != null) {
				length += arr.length;
				
				if (type == null) {
					type = arr.getClass().getComponentType();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		T[] concat = (T[]) Array.newInstance(type, length);
        
		int current = 0;
		for (T[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static double[] concatenate(double[]... arrays) {
		int length = 0;
		for (double[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}
		
		double[] concat = new double[length];
        
		int current = 0;
		for (double[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static float[] concatenate(float[]... arrays) {
		int length = 0;
		for (float[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}
		
		float[] concat = new float[length];
        
		int current = 0;
		for (float[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static long[] concatenate(long[]... arrays) {
		int length = 0;
		for (long[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}
		
		long[] concat = new long[length];
        
		int current = 0;
		for (long[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static short[] concatenate(short[]... arrays) {
		int length = 0;
		for (short[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}
		
		short[] concat = new short[length];
        
		int current = 0;
		for (short[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static int[] concatenate(int[]... arrays) {
		int length = 0;
		for (int[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}
		
		int[] concat = new int[length];
        
		int current = 0;
		for (int[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static byte[] concatenate(byte[]... arrays) {
		byte length = 0;
		for (byte[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}
		
		byte[] concat = new byte[length];
        
		byte current = 0;
		for (byte[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}
	
	/**
	 * Concatenate multiple arrays into a single new array.
	 * 
	 * @param arrays the arrays to concatenate.
	 * @return the new concatenated array
	 */
	public static char[] concatenate(char[]... arrays) {
		char length = 0;
		for (char[] arr : arrays) {
			length += (arr == null ? 0 : arr.length);
		}
		
		char[] concat = new char[length];
        
		char current = 0;
		for (char[] arr : arrays) {
			System.arraycopy(arr, 0, concat, current, arr.length);
			current += arr.length;
		}
		
        return concat;
	}

	/**
	 * Compute the sum of values in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static double sumValues(double[] vector) {
		double sum = 0;
		
		for (double v : vector) sum += v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static float sumValues(float[] vector) {
		float sum = 0;
		
		for (float v : vector) sum += v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValues(int[] vector) {
		int sum = 0;
		
		for (int v : vector) sum += v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValues(byte[] vector) {
		int sum = 0;
		
		for (int v : vector) sum += v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValues(short[] vector) {
		int sum = 0;
		
		for (int v : vector) sum += v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static long sumValues(long[] vector) {
		long sum = 0;
		
		for (long v : vector) sum += v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values squared in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static double sumValuesSquared(double[] vector) {
		double sum = 0;
		
		for (double v : vector) sum += v*v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values squared in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static float sumValuesSquared(float[] vector) {
		float sum = 0;
		
		for (float v : vector) sum += v*v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values squared in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValuesSquared(int[] vector) {
		int sum = 0;
		
		for (int v : vector) sum += v*v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values squared in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValuesSquared(byte[] vector) {
		int sum = 0;
		
		for (int v : vector) sum += v*v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values squared in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static int sumValuesSquared(short[] vector) {
		int sum = 0;
		
		for (int v : vector) sum += v*v;
		
		return sum;
	}
	
	/**
	 * Compute the sum of values squared in an array
	 * @param vector
	 * @return the sum of all values
	 */
	public static long sumValuesSquared(long[] vector) {
		long sum = 0;
		
		for (long v : vector) sum += v*v;
		
		return sum;
	}
	
	/**
	 * @param start
	 * @param length
	 * @return [start...length] (inclusive)
	 */
	public static int[] range(int start, int length) {
	    int[] range = new int[length - start + 1];
	    for (int i = start; i <= length; i++) {
	        range[i - start] = i;
	    }
	    return range;
	}
}
