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
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.io.IOUtils;
import org.openimaj.util.list.AbstractStreamBackedList;

/**
 * A list of {@link LocalFeature}s backed by an input stream. The list is
 * read-only, and can only be read in order (i.e. random access is not
 * possible).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the type of local feature
 */
public class StreamLocalFeatureList<T extends LocalFeature<?, ?>> extends AbstractStreamBackedList<T>
		implements
		LocalFeatureList<T>
{
	int veclen;

	protected StreamLocalFeatureList(InputStream stream, int size, boolean isBinary, int headerLength, int recordLength,
			int veclen, Class<T> clz)
	{
		super(stream, size, isBinary, headerLength, recordLength, clz);
		this.veclen = veclen;
	}

	/**
	 * Construct a new StreamLocalFeatureList from the given input stream.
	 * 
	 * @param <T>
	 *            the type of local feature
	 * @param stream
	 *            the input stream
	 * @param clz
	 *            the class of local feature to read
	 * @return a new list
	 * @throws IOException
	 *             if an error occurs reading from the stream
	 */
	public static <T extends LocalFeature<?, ?>> StreamLocalFeatureList<T> read(InputStream stream, Class<T> clz)
			throws IOException
	{
		return read(new BufferedInputStream(stream), clz);
	}

	/**
	 * Construct a new StreamLocalFeatureList from the given input stream.
	 * 
	 * @param <T>
	 *            the type of local feature
	 * @param stream
	 *            the input stream
	 * @param clz
	 *            the class of local feature to read
	 * @return a new list
	 * @throws IOException
	 *             if an error occurs reading from the stream
	 */
	public static <T extends LocalFeature<?, ?>> StreamLocalFeatureList<T> read(BufferedInputStream stream, Class<T> clz)
			throws IOException
	{
		final boolean isBinary = IOUtils.isBinary(stream, LocalFeatureList.BINARY_HEADER);

		// read header
		final int[] header = LocalFeatureListUtils.readHeader(stream, isBinary, false);
		final int size = header[0];
		final int veclen = header[1];
		final int headerLength = header[2];

		final int recordLength = veclen + 4 * 4;

		return new StreamLocalFeatureList<T>(stream, size, isBinary, headerLength, recordLength, veclen, clz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Q> Q[] asDataArray(Q[] a) {
		if (a.length < size()) {
			a = (Q[]) Array.newInstance(a.getClass().getComponentType(), size());
		}

		int i = 0;
		for (final T t : this) {
			a[i++] = (Q) t.getFeatureVector().getVector();
		}

		return a;
	}

	@Override
	public int vecLength() {
		return veclen;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		LocalFeatureListUtils.writeBinary(out, this);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
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

	@Override
	protected T newElementInstance() {
		return LocalFeatureListUtils.newInstance(clz, this.veclen);
	}

	@Override
	public MemoryLocalFeatureList<T> subList(int fromIndex, int toIndex) {
		return new MemoryLocalFeatureList<T>(super.subList(fromIndex, toIndex));
	}
}
