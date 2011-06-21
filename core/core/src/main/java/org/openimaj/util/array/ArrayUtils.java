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

/**
 * Collection of utilities for primative arrays.
 * 
 * @author Jonathan Hare 
 * @author Sina Samangooei
 *
 */
public class ArrayUtils {

	/**
	 * Returns the index to the smallest value in the array
	 * 
	 * @param arr array of floats
	 * @return the index
	 */
	public static int minIndex(float[] arr) {
		float min = Float.MAX_VALUE;
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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
		int index = -1;
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

	public static double[] add(double[] ds, double x) {
		for(int i = 0; i < ds.length; i++){
			ds[i] += x;
		}
		return ds;
	}
}
