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
package org.openimaj.feature.local.list;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.openimaj.data.RandomData;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocationFilter;
import org.openimaj.io.IOUtils;

/**
 * An in-memory list of local features.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the type of local feature
 */
public class MemoryLocalFeatureList<T extends LocalFeature<?, ?>> extends ArrayList<T> implements LocalFeatureList<T> {
	private static final long serialVersionUID = 1L;

	protected int cached_veclen = -1;

	/**
	 * Construct an empty feature list
	 */
	public MemoryLocalFeatureList() {
	}

	/**
	 * Construct an empty list with the given feature-vector length.
	 * 
	 * @param veclen
	 *            the expected length of the feature vectors of each local
	 *            feature.
	 */
	public MemoryLocalFeatureList(int veclen) {
		super();
		this.cached_veclen = veclen;
	}

	/**
	 * Construct a local feature list from the given collection of local
	 * features.
	 * 
	 * @param c
	 *            Collection of local feature to add to the list instance.
	 */
	public MemoryLocalFeatureList(Collection<? extends T> c) {
		super(c);
		if (size() > 0)
			cached_veclen = this.get(0).getFeatureVector().length();
	}

	/**
	 * Construct an empty list with the given feature-vector length and
	 * pre-allocate the underlying array with space for initialCapacity local
	 * features. The list will automatically grow once initialCapacity is
	 * reached.
	 * 
	 * @param veclen
	 *            the expected length of the feature vectors of each local
	 *            feature.
	 * @param initialCapacity
	 *            the initial capacity of the list.
	 */
	public MemoryLocalFeatureList(int veclen, int initialCapacity) {
		super(initialCapacity);
		this.cached_veclen = veclen;
	}

	/**
	 * Create a MemoryLocalFeatureList by reading all the local features from
	 * the specified file.
	 * 
	 * @param <T>
	 *            the type of local feature
	 * @param keypointFile
	 *            the file from which to read the features
	 * @param clz
	 *            the class of local feature
	 * @return a new MemoryLocalFeatureList populated with features from the
	 *         file
	 * @throws IOException
	 *             if an error occurs reading the file
	 */
	public static <T extends LocalFeature<?, ?>> MemoryLocalFeatureList<T> read(File keypointFile, Class<T> clz)
			throws IOException
	{
		final boolean isBinary = IOUtils.isBinary(keypointFile, LocalFeatureList.BINARY_HEADER);
		final MemoryLocalFeatureList<T> list = new MemoryLocalFeatureList<T>();

		if (isBinary) {
			LocalFeatureListUtils.readBinary(keypointFile, list, clz);
		} else {
			LocalFeatureListUtils.readASCII(keypointFile, list, clz);
		}

		return list;
	}

	/**
	 * Create a MemoryLocalFeatureList by reading all the local features from
	 * the specified stream.
	 * 
	 * @param <T>
	 *            the type of local feature
	 * @param stream
	 *            the input stream from which to read the features
	 * @param clz
	 *            the class of local feature
	 * @return a new MemoryLocalFeatureList populated with features from the
	 *         file
	 * @throws IOException
	 *             if an error occurs reading the file
	 */
	public static <T extends LocalFeature<?, ?>> MemoryLocalFeatureList<T> read(InputStream stream, Class<T> clz)
			throws IOException
	{
		return read(new BufferedInputStream(stream), clz);
	}

	/**
	 * Create a MemoryLocalFeatureList by reading all the local features from
	 * the specified stream.
	 * 
	 * @param <T>
	 *            the type of local feature
	 * @param stream
	 *            the input stream from which to read the features
	 * @param clz
	 *            the class of local feature
	 * @return a new MemoryLocalFeatureList populated with features from the
	 *         file
	 * @throws IOException
	 *             if an error occurs reading the file
	 */
	public static <T extends LocalFeature<?, ?>> MemoryLocalFeatureList<T> read(BufferedInputStream stream, Class<T> clz)
			throws IOException
	{
		final boolean isBinary = IOUtils.isBinary(stream, LocalFeatureList.BINARY_HEADER);
		final MemoryLocalFeatureList<T> list = new MemoryLocalFeatureList<T>();

		if (isBinary) {
			LocalFeatureListUtils.readBinary(stream, list, clz);
		} else {
			LocalFeatureListUtils.readASCII(stream, list, clz);
		}

		return list;
	}

	/**
	 * Create a MemoryLocalFeatureList by reading all the local features from
	 * the specified {@link DataInput}. Reading of the header is skipped, and it
	 * is assumed that the data is in binary format.
	 * 
	 * @param <T>
	 *            the type of local feature
	 * @param in
	 *            the data input from which to read the features
	 * @param clz
	 *            the class of local feature
	 * @return a new MemoryLocalFeatureList populated with features from the
	 *         file
	 * @throws IOException
	 *             if an error occurs reading the file
	 */
	public static <T extends LocalFeature<?, ?>> MemoryLocalFeatureList<T> readNoHeader(DataInput in, Class<T> clz)
			throws IOException
	{
		final MemoryLocalFeatureList<T> list = new MemoryLocalFeatureList<T>();

		LocalFeatureListUtils.readBinary(in, list, clz);

		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Q> Q[] asDataArray(Q[] a) {
		if (a.length < size()) {
			System.out.println(a.getClass());
			a = (Q[]) Array.newInstance(a.getClass().getComponentType(), size());
		}

		int i = 0;
		for (final T t : this) {
			a[i++] = (Q) t.getFeatureVector().getVector();
		}

		return a;
	}

	@Override
	public MemoryLocalFeatureList<T> randomSubList(int nelem) {
		MemoryLocalFeatureList<T> kl;

		if (nelem > size()) {
			kl = new MemoryLocalFeatureList<T>(this);
			Collections.shuffle(kl);
		} else {
			final int[] rnds = RandomData.getUniqueRandomInts(nelem, 0, this.size());
			kl = new MemoryLocalFeatureList<T>(cached_veclen);

			for (final int idx : rnds)
				kl.add(this.get(idx));
		}

		return kl;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		resetVecLength();
		LocalFeatureListUtils.writeBinary(out, this);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		resetVecLength();
		LocalFeatureListUtils.writeASCII(out, this);
	}

	@Override
	public byte[] binaryHeader() {
		return LocalFeatureList.BINARY_HEADER;
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	/*
	 * @see org.openimaj.feature.local.list.LocalFeatureList#vecLength()
	 */
	@Override
	public int vecLength() {
		resetVecLength();

		if (cached_veclen == -1) {
			if (size() > 0) {
				cached_veclen = get(0).getFeatureVector().length();
			}
		}
		return cached_veclen;
	}

	/**
	 * Reset the internal feature vector length to the length of the first
	 * feature. You must call this if you change the length of the features
	 * within the list.
	 */
	public void resetVecLength() {
		if (size() > 0) {
			cached_veclen = get(0).getFeatureVector().length();
		}
	}

	/**
	 * Create a new list by applying a {@link LocationFilter} to all the
	 * elements of this list. Only items which are accepted by the filter will
	 * be added to the new list.
	 * 
	 * @param locationFilter
	 *            the location filter
	 * @return a filtered list
	 */
	public MemoryLocalFeatureList<T> filter(LocationFilter locationFilter) {
		final MemoryLocalFeatureList<T> newlist = new MemoryLocalFeatureList<T>();
		for (final T t : this) {
			if (locationFilter.accept(t.getLocation()))
				newlist.add(t);
		}
		return newlist;
	}

	@Override
	public MemoryLocalFeatureList<T> subList(int fromIndex, int toIndex) {
		return new MemoryLocalFeatureList<T>(super.subList(fromIndex, toIndex));
	}
}
