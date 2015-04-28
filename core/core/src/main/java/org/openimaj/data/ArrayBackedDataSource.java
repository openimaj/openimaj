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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Random;

import org.openimaj.util.array.ArrayIterator;

/**
 * A {@link DataSource} backed by an array.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 *            Type of object
 *
 */
public abstract class ArrayBackedDataSource<T> implements DataSource<T> {
	protected T[] data;
	protected Random rng;

	/**
	 * Construct with data
	 *
	 * @param data
	 *            the data
	 */
	public ArrayBackedDataSource(T[] data) {
		this.data = data;
		this.rng = new Random();
	}

	/**
	 * Construct with data and a random generator for random sampling
	 *
	 * @param data
	 *            the data
	 * @param rng
	 *            the random generator
	 */
	public ArrayBackedDataSource(T[] data, Random rng) {
		this.data = data;
		this.rng = rng;
	}

	@Override
	public final void getData(int startRow, int stopRow, T[] output) {
		for (int i = startRow, j = 0; i < stopRow; i++, j++)
			output[j] = data[i];
	}

	@Override
	public final void getRandomRows(T[] output) {
		final int k = output.length;
		final int[] ind = RandomData.getUniqueRandomInts(k, 0, data.length, rng);

		for (int i = 0; i < k; i++)
			output[i] = data[ind[i]];
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public T getData(int row) {
		return data[row];
	}

	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator<T>(data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] createTemporaryArray(int size) {
		return (T[]) Array.newInstance(data.getClass().getComponentType(), size);
	}
}
