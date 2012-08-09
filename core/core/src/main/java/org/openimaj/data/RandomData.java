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
package org.openimaj.data;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Date;
import java.util.Random;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * Utility functions for creating random data
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class RandomData {
	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed float values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @return 2d array of random floats
	 */
	public static float [][] getRandomFloatArray(int rows, int cols, float min, float max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		float [][] data = new float[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = rnd.nextFloatFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed float values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return 2d array of random floats
	 */
	public static float [][] getRandomFloatArray(int rows, int cols, float min, float max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		float [][] data = new float[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = rnd.nextFloatFromTo(min, max);

		return data;
	}

	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed double values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @return 2d array of random doubles
	 */
	public static double [][] getRandomDoubleArray(int rows, int cols, double min, double max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		double [][] data = new double[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = rnd.nextDoubleFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed double values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return 2d array of random doubles
	 */
	public static double [][] getRandomDoubleArray(int rows, int cols, double min, double max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		double [][] data = new double[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = rnd.nextDoubleFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return 2d array of random ints
	 */
	public static int [][] getRandomIntArray(int rows, int cols, int min, int max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		int [][] data = new int[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = rnd.nextIntFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed long values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @return 2d array of random longs
	 */
	public static long [][] getRandomLongArray(int rows, int cols, long min, long max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		long [][] data = new long[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = rnd.nextLongFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed long values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return 2d array of random longs
	 */
	public static long [][] getRandomLongArray(int rows, int cols, long min, long max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		long [][] data = new long[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = rnd.nextLongFromTo(min, max);

		return data;
	}

	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed short values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return 2d array of random shorts
	 */
	public static short [][] getRandomShortArray(int rows, int cols, short min, short max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		short [][] data = new short[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = (short) rnd.nextIntFromTo(min, max);

		return data;
	}

	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed byte values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @return 2d array of random bytes
	 */
	public static byte [][] getRandomByteArray(int rows, int cols, byte min, byte max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		byte [][] data = new byte[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = (byte) rnd.nextIntFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a two dimensional array of pseudorandom, uniformly 
	 * distributed byte values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param rows number of rows
	 * @param cols number of cols
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return 2d array of random bytes
	 */
	public static byte [][] getRandomByteArray(int rows, int cols, byte min, byte max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		byte [][] data = new byte[rows][cols];

		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = (byte) rnd.nextIntFromTo(min, max);

		return data;
	}

	/**
	 * Returns an array of N unique pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive).
	 * 
	 * Internally this method creates an array of all the numbers
	 * in the range min->max and shuffles. The array is then cropped
	 * to N elements.
	 * 
	 * @param N number of unique random numbers 
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of N unique ints
	 */
	public static int [] getUniqueRandomIntsA(int N, int min, int max) {
		return getUniqueRandomIntsA(N, min, max, new Random());
	}

	/**
	 * Returns an array of N unique pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive).
	 * 
	 * Internally this method uses a hashset to store numbers.
	 * The hashset is continually filled with random numbers in the
	 * range min->max until its size is N.
	 * 
	 * @param N number of unique random numbers 
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of N unique ints
	 */
	public static int [] getUniqueRandomIntsS(int N, int min, int max) {
		return getUniqueRandomIntsS(N, min, max, new Random());
	}

	/**
	 * Returns an array of N unique pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive).
	 * 
	 * Internally this method chooses between the array and
	 * hashset methods depending on the ratio of the range between
	 * min and max and the number of ints required. 
	 * 
	 * @param N number of unique random numbers 
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of N unique ints
	 */
	public static int [] getUniqueRandomInts(int N, int min, int max) {
		return getUniqueRandomInts(N, min, max, new Random());
	}

	/**
	 * Returns an array of N unique pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive).
	 * 
	 * Internally this method creates an array of all the numbers
	 * in the range min->max and shuffles. The array is then cropped
	 * to N elements.
	 * 
	 * @param N number of unique random numbers 
	 * @param min minimum value
	 * @param max maximum value
	 * @param rnd random generator to use
	 * @return array of N unique ints
	 */
	public static int [] getUniqueRandomIntsA(int N, int min, int max, Random rnd) {
		int rng = max-min;
		if (rng < N)
			throw new IllegalArgumentException("Cannot generate more random numbers than the range allows");

		TIntArrayList allData = new TIntArrayList(rng);

		for (int i=min; i<max; i++)
			allData.add(i);

		allData.shuffle(rnd);

		int [] data = new int[N];
		allData.toArray(data, 0, N);

		return data;
	}

	/**
	 * Returns an array of N unique pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive).
	 * 
	 * Internally this method uses a hashset to store numbers.
	 * The hashset is continually filled with random numbers in the
	 * range min->max until its size is N.
	 * 
	 * @param N number of unique random numbers 
	 * @param min minimum value
	 * @param max maximum value
	 * @param rnd random generator to use
	 * @return array of N unique ints
	 */
	public static int [] getUniqueRandomIntsS(int N, int min, int max, Random rnd) {
		int rng = max-min;
		if (rng < N)
			throw new IllegalArgumentException("Cannot generate more random numbers than the range allows");

		TIntHashSet set = new TIntHashSet(N);

		for (int i=0; i<N; i++) {
			while (true) {
				int r = rnd.nextInt(rng) + min;
				if (!set.contains(r)) {
					set.add(r);
					break;
				}
			}
		}

		return set.toArray();
	}

	/**
	 * Returns an array of N unique pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive).
	 * 
	 * Internally this method chooses between the array and
	 * hashset methods depending on the ratio of the range between
	 * min and max and the number of ints required. 
	 * 
	 * @param N number of unique random numbers 
	 * @param min minimum value
	 * @param max maximum value
	 * @param rng random generator to use
	 * @return array of N unique ints
	 */
	public static int [] getUniqueRandomInts(int N, int min, int max, Random rng) {
		//0.4 load factor seems a fairly sensible time-tradeoff
		if (((double)N / (max-min)) > 0.4) {
			return getUniqueRandomIntsA(N, min, max,rng);
		}
		return getUniqueRandomIntsS(N, min, max,rng);
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed float values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of random floats
	 */
	public static float [] getRandomFloatArray(int length, float min, float max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		float [] data = new float[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextFloatFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed float values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return array of random floats
	 */
	public static float [] getRandomFloatArray(int length, float min, float max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		float [] data = new float[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextFloatFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed double values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 *  
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of random doubles
	 */
	public static double [] getRandomDoubleArray(int length, double min, double max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		double [] data = new double[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextDoubleFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed double values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return array of random doubles
	 */
	public static double [] getRandomDoubleArray(int length, double min, double max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		double [] data = new double[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextDoubleFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of random ints
	 */
	public static int [] getRandomIntArray(int length, int min, int max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		int [] data = new int[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextIntFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return array of random ints
	 */
	public static int [] getRandomIntArray(int length, int min, int max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		int [] data = new int[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextIntFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed long values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of random longs
	 */
	public static long [] getRandomLongArray(int length, long min, long max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		long [] data = new long[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextLongFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed long values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return array of random longs
	 */
	public static long [] getRandomLongArray(int length, long min, long max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		long [] data = new long[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextLongFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed short values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return array of random shorts
	 */
	public static short [] getRandomShortArray(int length, short min, short max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		short [] data = new short[length];

		for (int i=0; i<length; i++)
			data[i] = (short) rnd.nextIntFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed byte values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * The generator is seeded with the time at which the method is called.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @return array of random bytes
	 */
	public static byte [] getRandomByteArray(int length, byte min, byte max) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(new Date()));
		byte [] data = new byte[length];

		for (int i=0; i<length; i++)
			data[i] = (byte) rnd.nextIntFromTo(min, max);

		return data;
	}
	
	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed byte values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed random seed
	 * @return array of random bytes
	 */
	public static byte [] getRandomByteArray(int length, byte min, byte max, int seed) {
		Uniform rnd = new Uniform(min, max, new MersenneTwister(seed));
		byte [] data = new byte[length];

		for (int i=0; i<length; i++)
			data[i] = (byte) rnd.nextIntFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed float values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param mt Mersenne twister
	 * @return array of random floats
	 */
	public static float [] getRandomFloatArray(int length, float min, float max, MersenneTwister mt) {
		Uniform rnd = new Uniform(min, max, mt);
		float [] data = new float[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextFloatFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed double values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param mt Mersenne twister
	 * @return array of random doubles
	 */
	public static double [] getRandomDoubleArray(int length, double min, double max, MersenneTwister mt) {
		Uniform rnd = new Uniform(min, max, mt);
		double [] data = new double[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextDoubleFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed int values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param mt Mersenne twister
	 * @return array of random ints
	 */
	public static int [] getRandomIntArray(int length, int min, int max, MersenneTwister mt) {
		Uniform rnd = new Uniform(min, max, mt);
		int [] data = new int[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextIntFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed long values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param mt Mersenne twister
	 * @return array of random longs
	 */
	public static long [] getRandomLongArray(int length, long min, long max, MersenneTwister mt) {
		Uniform rnd = new Uniform(min, max, mt);
		long [] data = new long[length];

		for (int i=0; i<length; i++)
			data[i] = rnd.nextLongFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed short values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param mt Mersenne twister
	 * @return array of random shorts
	 */
	public static short [] getRandomShortArray(int length, short min, short max, MersenneTwister mt) {
		Uniform rnd = new Uniform(min, max, mt);
		short [] data = new short[length];

		for (int i=0; i<length; i++)
			data[i] = (short) rnd.nextIntFromTo(min, max);

		return data;
	}

	/**
	 * Returns a one dimensional array of pseudorandom, uniformly 
	 * distributed byte values between min (inclusive) and 
	 * max (exclusive), drawn from this random number generator's sequence.
	 * 
	 * @see cern.jet.random.Uniform
	 * 
	 * @param length length of array
	 * @param min minimum value
	 * @param max maximum value
	 * @param mt Mersenne twister
	 * @return array of random bytes
	 */
	public static byte [] getRandomByteArray(int length, byte min, byte max, MersenneTwister mt) {
		Uniform rnd = new Uniform(min, max, mt);
		byte [] data = new byte[length];

		for (int i=0; i<length; i++)
			data[i] = (byte) rnd.nextIntFromTo(min, max);

		return data;
	}
}
