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
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Scanner;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.io.VariableLength;

class LocalFeatureListUtils {

	protected static <T extends LocalFeature<?, ?>> void writeBinary(DataOutput out, LocalFeatureList<T> list)
			throws IOException
	{
		out.writeInt(list.size());
		out.writeInt(list.vecLength());
		for (final T k : list)
			k.writeBinary(out);
	}

	protected static <T extends LocalFeature<?, ?>> void writeASCII(PrintWriter out, LocalFeatureList<T> list)
			throws IOException
	{
		final Locale def = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);

		out.println(list.size() + " " + list.vecLength());
		for (final T k : list)
			k.writeASCII(out);

		Locale.setDefault(def);
	}

	protected static <T extends LocalFeature<?, ?>> void readBinary(File file,
			MemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
	{
		BufferedInputStream bis = null;

		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			readBinary(bis, memoryKeypointList, clz);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (final IOException e) {
				}
		}
	}

	protected static <T extends LocalFeature<?, ?>> void readBinary(BufferedInputStream bis,
			MemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
	{
		DataInputStream dis = null;

		dis = new DataInputStream(bis);
		// read the header line
		dis.read(new byte[memoryKeypointList.binaryHeader().length]);
		final int nItems = dis.readInt();
		final int veclen = dis.readInt();

		memoryKeypointList.clear();
		memoryKeypointList.cached_veclen = veclen;

		for (int i = 0; i < nItems; i++) {
			final T t = newInstance(clz, veclen);
			t.readBinary(dis);

			memoryKeypointList.add(t);
		}
	}

	protected static <T extends LocalFeature<?, ?>> void readBinary(DataInput in,
			MemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
	{
		final int nItems = in.readInt();
		final int veclen = in.readInt();

		memoryKeypointList.clear();
		memoryKeypointList.cached_veclen = veclen;

		for (int i = 0; i < nItems; i++) {
			final T t = newInstance(clz, veclen);
			t.readBinary(in);

			memoryKeypointList.add(t);
		}
	}

	protected static <T extends LocalFeature<?, ?>> void readASCII(File file,
			MemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
	{
		BufferedInputStream bis = null;

		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			readASCII(bis, memoryKeypointList, clz);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (final IOException e) {
				}
		}
	}

	protected static <T extends LocalFeature<?, ?>> void readASCII(BufferedInputStream bis,
			MemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
	{
		final Scanner in = new Scanner(bis);

		// read the header line
		final String head = in.nextLine().trim();
		final String[] h = head.split(" ");

		final int nItems = Integer.decode(h[0]);
		int veclen = 0;
		if (h.length > 1)
		{
			veclen = Integer.decode(h[1]);
		}
		else {
			veclen = Integer.decode(in.nextLine().trim());
		}

		memoryKeypointList.clear();
		memoryKeypointList.cached_veclen = veclen;

		for (int i = 0; i < nItems; i++) {
			final T t = newInstance(clz, veclen);
			t.readASCII(in);
			memoryKeypointList.add(t);
		}
	}

	public static <T> T newInstance(Class<T> cls, int length) {
		try {
			if (VariableLength.class.isAssignableFrom(cls)) {
				return cls.getConstructor(Integer.TYPE).newInstance(length);
			}

			return cls.newInstance();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected static int[] readHeader(File keypointFile, boolean isBinary) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(keypointFile);
			return readHeader(fis, isBinary, true);
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (final IOException e) {
				}
		}
	}

	protected static int[] readHeader(InputStream stream, boolean isBinary, boolean close) throws IOException {
		if (isBinary) {
			DataInputStream dis = null;
			try {
				dis = new DataInputStream(stream);

				for (int i = 0; i < LocalFeatureList.BINARY_HEADER.length; i++)
					dis.readByte();

				// read the header
				final int nItems = dis.readInt();
				final int veclen = dis.readInt();

				return new int[] { nItems, veclen, 8 + LocalFeatureList.BINARY_HEADER.length };
			} finally {
				if (close && dis != null)
					try {
						dis.close();
					} catch (final IOException e) {
					}
			}
		} else {
			InputStreamReader fr = null;
			BufferedReader br = null;
			int nlines = 1;
			final boolean isBuffered = stream.markSupported();
			try {
				if (isBuffered)
					stream.mark(1024);
				fr = new InputStreamReader(stream);
				br = new BufferedReader(fr);

				// read the header line
				final String head = br.readLine().trim();
				if (isBuffered)
					stream.reset();
				final String[] h = head.split(" ");

				final int nItems = Integer.decode(h[0]);
				int veclen = 0;
				if (h.length > 1)
				{
					veclen = Integer.decode(h[1]);
				}
				else {
					veclen = Integer.decode(br.readLine().trim());
					nlines++;
				}

				return new int[] { nItems, veclen, nlines };
			} finally {
				if (close && br != null)
					try {
						br.close();
					} catch (final IOException e) {
					}
				if (close && fr != null)
					try {
						fr.close();
					} catch (final IOException e) {
					}
			}
		}
	}
}
