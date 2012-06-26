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
 * Utility methods for converting arrays of 
 * (unsigned) bytes to integers and back.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ByteArrayConverter {
	/**
	 * Convert the given byte array to an array of ints.
	 * The bytes are assumed to actually represent int
	 * values in the range 0..255, and so have 128 added
	 * to each value.
	 * 
	 * @param arr the byte array
	 * @return an array of ints
	 */
	public static int[] byteToInt(byte [] arr) {
		int[] ret = new int[arr.length];
		for(int i = 0; i < arr.length; i++){
			ret[i] = arr[i] + 128;
		}
		return ret;
	}
	
	/**
	 * Convert the given 2d byte array to a 2d array of ints.
	 * The bytes are assumed to actually represent int
	 * values in the range 0..255, and so have 128 added
	 * to each value.
	 * 
	 * @param arr the byte array
	 * @return an array of ints
	 */
	public static int[][] byteToInt(byte [][] arr) {
		int[][] ret = new int[arr.length][];
		for(int i = 0; i < arr.length; i++){
			ret[i] = byteToInt(arr[i]);
		}
		return ret;
	}
	
	/**
	 * Convert the given int array to an array of bytes.
	 * The ints are assumed to be in the range 0..255,
	 * and so 128 is subtracted before casting to a byte.
	 * 
	 * @param arr the int array
	 * @return an array of bytes
	 */
	public static byte[] intToByte(int [] arr) {
		byte[] ret = new byte[arr.length];
		for(int i = 0; i < arr.length; i++){
			ret[i] = (byte)(arr[i] - 128);
		}
		return ret;
	}
	
	/**
	 * Convert the given 2d int array to a 2d array of bytes.
	 * The ints are assumed to be in the range 0..255,
	 * and so 128 is subtracted before casting to a byte.
	 * 
	 * @param arr the int array
	 * @return an array of bytes
	 */
	public static byte[][] intToByte(int [][] arr) {
		byte[][] ret = new byte[arr.length][];
		for(int i = 0; i < arr.length; i++){
			ret[i] = intToByte(arr[i]);
		}
		return ret;
	}
}
