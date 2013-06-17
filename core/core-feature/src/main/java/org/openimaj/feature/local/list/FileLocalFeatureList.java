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

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.io.IOUtils;
import org.openimaj.util.list.AbstractFileBackedList;

/**
 * A {@link LocalFeatureList} backed by a file. Data is only read as necessary.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the type of local feature
 */
public class FileLocalFeatureList<T extends LocalFeature<?, ?>> extends AbstractFileBackedList<T>
		implements
		LocalFeatureList<T>,
		Cloneable
{
	protected final int veclen;

	protected FileLocalFeatureList(int size, int veclen, boolean isBinary, int headerLength, int recordLength, File file,
			Class<T> clz)
	{
		super(size, isBinary, headerLength, recordLength, file, clz);
		this.veclen = veclen;
	}

	/***
	 * 
	 * Read a file containing a set of local features of a type clz. It is
	 * assumed that clz can instantiate itself either given a vec length or no
	 * parameters and furthermore, that this instantiated instance can write
	 * itself, even when filled with no other data.
	 * 
	 * @param <T>
	 *            the local feature class
	 * @param keypointFile
	 *            the file
	 * @param clz
	 *            the local feature class
	 * @return a list of local feature backed by the file
	 * @throws IOException
	 *             if a problem occurs reading the file
	 */
	public static <T extends LocalFeature<?, ?>> FileLocalFeatureList<T> read(File keypointFile, Class<T> clz)
			throws IOException
	{
		final boolean isBinary = IOUtils.isBinary(keypointFile, LocalFeatureList.BINARY_HEADER);

		// read header
		final int[] header = LocalFeatureListUtils.readHeader(keypointFile, isBinary);
		final int size = header[0];
		final int veclen = header[1];
		final int headerLength = header[2];

		final T instance = LocalFeatureListUtils.newInstance(clz, veclen);

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		instance.writeBinary(new DataOutputStream(buffer));

		final int recordLength = buffer.toByteArray().length;

		return new FileLocalFeatureList<T>(size, veclen, isBinary, headerLength, recordLength, keypointFile, clz);
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
	protected AbstractFileBackedList<T> newInstance(int newSize, boolean isBinary, int newHeaderLength, int recordLength,
			File file)
	{
		return new FileLocalFeatureList<T>(newSize, veclen, isBinary, newHeaderLength, recordLength, file, clz);
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
