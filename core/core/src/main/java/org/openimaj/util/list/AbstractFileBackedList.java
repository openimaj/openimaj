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
package org.openimaj.util.list;

import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.openimaj.data.RandomData;
import org.openimaj.io.Readable;

/**
 * A (currently) read-only list that iterates over a file. Both ascii and binary
 * files are supported. Binary files are required to have fixed length records.
 * 
 * @author Jonathon Hare
 * 
 * @param <T>
 *            the type of readable item held in array
 */
public abstract class AbstractFileBackedList<T extends Readable> extends AbstractList<T>
		implements
			RandomisableList<T>,
			Cloneable
{
	protected final int size;
	protected final Class<T> clz;

	protected final boolean isBinary;
	protected final int headerLength;
	protected final int recordLength;

	protected final File file;

	private int ascii_offset = 0;
	protected String charset;

	protected AbstractFileBackedList(int size, boolean isBinary, int headerLength, int recordLength, File file,
			Class<T> clz)
	{
		this.size = size;
		this.isBinary = isBinary;
		this.headerLength = headerLength;
		this.recordLength = recordLength;
		this.file = file;
		this.clz = clz;
		this.charset = Charset.defaultCharset().name();
	}

	protected AbstractFileBackedList(int size, boolean isBinary, int headerLength, int recordLength, File file,
			Class<T> clz, String charset)
	{
		this.size = size;
		this.isBinary = isBinary;
		this.headerLength = headerLength;
		this.recordLength = recordLength;
		this.file = file;
		this.clz = clz;
		this.charset = charset;
	}

	/**
	 * Override this if your instances can't be constructed with a no-args
	 * constructor
	 * 
	 * @return
	 */
	protected T newElementInstance() {
		try {
			return clz.newInstance();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected T readRecord(DataInput input) throws IOException {
		final T element = newElementInstance();
		element.readBinary(input);
		return element;
	}

	protected T readRecordASCII(Scanner br) throws IOException {
		final T element = newElementInstance();
		element.readASCII(br);
		return element;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		for (final T k : this) {
			if (k.equals(o))
				return true;
		}

		return false;
	}

	class FLBinaryIterator implements Iterator<T> {
		protected int count = 0;
		protected RandomAccessFile raf;

		FLBinaryIterator() {
			try {
				this.raf = new RandomAccessFile(file, "r");
				raf.seek(headerLength);
			} catch (final IOException e) {
				close();
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasNext() {
			if (count < size())
				return true;
			close();
			return false;
		}

		protected void close() {
			if (raf != null) {
				try {
					raf.close();
					raf = null;
				} catch (final IOException e) {
				}
			}
		}

		@Override
		public T next() {
			try {
				if (raf == null)
					return null;

				final T k = readRecord(raf);
				count++;
				return k;
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Modifying a FileKeypointList isn't supported");
		}

		@Override
		public void finalize() {
			close();
		}
	}

	class FLAsciiIterator implements Iterator<T> {
		protected int count = 0;
		protected Scanner br;

		FLAsciiIterator() {
			reset();
			for (int i = 0; i < ascii_offset; i++)
				next();
			count = 0;
		}

		protected void reset() {
			try {
				close();
				final FileInputStream fis = new FileInputStream(file);
				br = new Scanner(fis, charset);
				for (int i = 0; i < headerLength; i++)
					br.nextLine();
			} catch (final IOException e) {
				close();
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasNext() {
			if (count < size())
				return true;
			close();
			return false;
		}

		protected void close() {
			if (br != null) {
				br.close();
				br = null;
			}
		}

		@Override
		public T next() {
			try {
				if (br == null)
					return null;

				final T k = readRecordASCII(br);
				count++;
				return k;
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Modifying a FileList isn't supported");
		}

		@Override
		public void finalize() {
			close();
		}
	}

	@Override
	public Iterator<T> iterator() {
		if (isBinary)
			return new FLBinaryIterator();
		return new FLAsciiIterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E[] toArray(E[] a) {
		if (a.length < size)
			return (E[]) Arrays.copyOf(toArray(), size, a.getClass());

		int i = 0;
		for (final T k : this)
			a[i++] = (E) k;

		if (a.length > size)
			a[size] = null;

		return a;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		final List<T> seen = new ArrayList<T>();
		for (final T k : this) {
			if (c.contains(k))
				seen.add(k);
		}
		return seen.size() == c.size();
	}

	@Override
	public T get(int index) {
		if (index > size)
			throw new IllegalArgumentException("Index out of bounds");

		if (!isBinary) {
			int count = 0;
			for (final T k : this) {
				if (count++ == index)
					return k;
			}
		} else {
			final long offset = (index * recordLength) + headerLength;
			RandomAccessFile raf = null;
			try {
				raf = new RandomAccessFile(file, "r");
				raf.seek(offset);
				return readRecord(raf);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (raf != null)
					try {
						raf.close();
					} catch (final IOException e) {
					}
			}
		}
		return null;
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException("Modifying a FileList isn't supported");
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException("Modifying a FileList isn't supported");
	}

	@Override
	public int indexOf(Object o) {
		int count = 0;
		for (final T k : this) {
			if (o.equals(k))
				return count;
			count++;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		int count = 0;
		int found = -1;
		for (final T k : this) {
			if (o.equals(k))
				found = count;
			count++;
		}
		return found;
	}

	protected abstract AbstractFileBackedList<T> newInstance(int newSize, boolean isBinary, int newHeaderLength,
			int recordLength, File file);

	@Override
	public RandomisableList<T> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || fromIndex > size || toIndex < 0 || toIndex > size)
			throw new IllegalArgumentException("bad offsets");

		if (!isBinary) {
			final AbstractFileBackedList<T> fkl = newInstance(toIndex - fromIndex, isBinary, headerLength, recordLength,
					file);
			fkl.ascii_offset = ascii_offset + fromIndex;
			return fkl;
		} else {
			final int newHeaderLength = headerLength + (fromIndex * recordLength);
			final int newSize = toIndex - fromIndex;

			final AbstractFileBackedList<T> fkl = newInstance(newSize, isBinary, newHeaderLength, recordLength, file);
			return fkl;
		}
	}

	class FLRandomSubList extends AbstractFileBackedList<T> {
		protected final int[] indices;

		FLRandomSubList(boolean isBinary, int headerLength, int recordLength, File file, int[] indices, Class<T> clz) {
			super(indices.length, isBinary, headerLength, recordLength, file, clz);
			this.indices = indices;
		}

		@Override
		public int size() {
			return indices.length;
		}

		@Override
		public T get(int index) {
			return super.get(indices[index]);
		}

		@Override
		public RandomisableList<T> subList(int fromIndex, int toIndex) {
			if (fromIndex < 0 || fromIndex > size() || toIndex < 0 || toIndex > size())
				throw new IllegalArgumentException("bad offsets");

			final int[] newIndices = new int[toIndex - fromIndex];

			return new FLRandomSubList(isBinary, headerLength, recordLength, file, newIndices, clz);
		}

		class FLRandomBinaryIterator extends FLBinaryIterator {
			@Override
			public T next() {
				try {
					if (count >= indices.length)
						return null;

					final int idx = indices[count];
					raf.seek(headerLength + (idx * recordLength));

					final T k = readRecord(raf);
					count++;
					return k;
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		class FLRandomAsciiIterator extends FLAsciiIterator {
			@Override
			public T next() {
				try {
					if (count >= indices.length)
						return null;

					int offset = 0;
					if (count > 0)
						offset = indices[count] - indices[count - 1];

					if (offset < 0) {
						reset();
						offset = indices[count];
					}

					for (int i = 0; i < offset - 1; i++) {
						readRecordASCII(br);
					}

					final T k = readRecordASCII(br);
					count++;
					return k;
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public Iterator<T> iterator() {
			if (isBinary)
				return new FLRandomBinaryIterator();
			return new FLRandomAsciiIterator();
		}

		@Override
		public RandomisableList<T> randomSubList(int nelem) {
			if (nelem > size())
				throw new IllegalArgumentException("number of requested elements is greater than the list size");

			final int[] newindices = RandomData.getUniqueRandomInts(nelem, 0, size());

			for (int i = 0; i < newindices.length; i++)
				newindices[i] = indices[newindices[i]];

			return new FLRandomSubList(isBinary, headerLength, recordLength, file, newindices, clz);
		}

		@Override
		public T readRecord(DataInput input) throws IOException {
			return AbstractFileBackedList.this.readRecord(input);
		}

		@Override
		public T readRecordASCII(Scanner br) throws IOException {
			return AbstractFileBackedList.this.readRecordASCII(br);
		}

		@Override
		protected AbstractFileBackedList<T> newInstance(int newSize, boolean isBinary, int newHeaderLength,
				int recordLength, File file)
		{
			return AbstractFileBackedList.this.newInstance(newSize, isBinary, newHeaderLength, recordLength, file);
		}
	}

	@Override
	public RandomisableList<T> randomSubList(int nelem) {
		if (nelem > size())
			throw new IllegalArgumentException("number of requested elements is greater than the list size");

		final int[] indices = RandomData.getUniqueRandomInts(nelem, 0, size());

		return new FLRandomSubList(isBinary, headerLength, recordLength, file, indices, clz);
	}

	@Override
	public Object[] toArray() {
		final Object[] objs = new Object[size()];
		int i = 0;
		for (final T o : this) {
			objs[i++] = o;
		}
		return objs;
	}
}
