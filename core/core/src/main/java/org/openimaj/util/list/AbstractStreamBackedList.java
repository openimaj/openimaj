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

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Scanner;

import org.openimaj.data.RandomData;
import org.openimaj.io.Readable;

/**
 * A list of records that can be consumed from a stream. 
 * Records can only be read in order and concurrent access is a really
 * bad idea. Once a list is exhausted by consuming all it's elements it
 * cannot be reused. The stream will be closed automatically when it is 
 * exhausted.
 * 
 * @author Jonathon Hare
 *
 * @param <T> The type of object which can be read by this list
 */
public abstract class AbstractStreamBackedList<T extends Readable> extends AbstractSequentialList<T> implements RandomisableList<T> {
	private Object streamWrapper;
	
	/**
	 * The class from which to generate an instance of items held in the list
	 */
	protected final Class<T> clz;
	
	/**
	 * The size of the list
	 */
	protected final int size;
	
	/**
	 * Does the stream hold binary data (as opposed to ASCII)
	 */
	protected final boolean isBinary;
	/**
	 * The length (in bytes) of the header which identifies the stream type
	 */
	protected final int headerLength;
	/**
	 * The length (in bytes) of each item held in the list
	 */
	protected final int recordLength;
	
	/**
	 * Number of bytes read
	 */
	protected int consumed = 0;

	private InputStream underlyingStream;
	
	/**
	 * Instantiate the list and all instance variables. Also starts the stream as a DataInputStream if the stream is binary and a BufferedReader
	 * otherwise.
	 * 
	 * @param stream the stream
	 * @param size number of elements
	 * @param isBinary is the stream binary
	 * @param headerLength how long is the header
	 * @param recordLength how long is each element
	 * @param clz what class instantiates elements in the list
	 * @throws UnsupportedEncodingException 
	 */
	protected AbstractStreamBackedList(InputStream stream, int size, boolean isBinary, int headerLength, int recordLength, Class<T> clz)  {
		this.size = size;
		this.isBinary = isBinary;
		this.headerLength = headerLength;
		this.recordLength = recordLength;
		this.clz = clz;
		
		if (isBinary)
			this.streamWrapper = new DataInputStream(stream);
		else
		{
			Scanner s = new Scanner(new InputStreamReader(stream));
			for (int i = 0; i < headerLength; i++) {
				s.nextLine();
			}
			this.streamWrapper = s;
			
		}
		this.underlyingStream = stream;
		
	}
	
	/**
	 * Instantiate the list and all instance variables. Also starts the stream as a DataInputStream if the stream is binary and a BufferedReader
	 * otherwise.
	 * 
	 * @param stream the stream
	 * @param size number of elements
	 * @param isBinary is the stream binary
	 * @param headerLength how long is the header
	 * @param recordLength how long is each element
	 * @param charset if the stream is not binary, the charsetName which is sent to the internal InputStreamReader
	 * @param clz what class instantiates elements in the list
	 * 
	 * @throws UnsupportedEncodingException 
	 */
	protected AbstractStreamBackedList(InputStream stream, int size,boolean isBinary, int headerLength, int recordLength,Class<T> clz, String charset) throws UnsupportedEncodingException {
		this.size = size;
		this.isBinary = isBinary;
		this.headerLength = headerLength;
		this.recordLength = recordLength;
		this.clz = clz;
		
		if (isBinary)
			this.streamWrapper = new DataInputStream(stream);
		else
		{
			Scanner s = new Scanner(new InputStreamReader(stream,charset));
			for (int i = 0; i < headerLength; i++) {
				s.nextLine();
			}
			this.streamWrapper = s;
			
		}
		this.underlyingStream = stream;
	}

	/**
	 * Override this id your instances can't be constructed with a no-args ctr
	 * @return
	 */
	protected T newElementInstance() {
		try {
			return clz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected T readRecord(DataInput input) throws IOException {
		T element = newElementInstance();
		element.readBinary(input);
		return element;
	}
	
	protected T readRecordASCII(Scanner br) throws IOException {
		T element = newElementInstance();
		element.readASCII(br);
		return element;
	}
	
	abstract class SLIterator implements ListIterator<T> {
		@Override
		public void add(T e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasNext() {
			if(streamWrapper == null) return false;
			boolean readMore = true;
			readMore &= size == -1;
			if(streamWrapper instanceof Scanner){
				readMore &= ((Scanner)streamWrapper).hasNext();
			}
			readMore = readMore || consumed < size;
			if (readMore) return true;
			close();
			return false;
		}

		@Override
		public boolean hasPrevious() {
			return false;
		}

		@Override
		public int nextIndex() {
			return Math.max(consumed+1, size());
		}

		@Override
		public T previous() {
			return null;
		}

		@Override
		public int previousIndex() {
			return -1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(T e) {
			throw new UnsupportedOperationException();
		}
	}
	
	class SLBinaryIterator extends SLIterator {
		@Override
		public T next() {
			try {
				if (hasNext()) {
					consumed++;
					return readRecord((DataInputStream)streamWrapper);
				}
				return null;
			} catch (IOException e) {
				close();
				throw new RuntimeException(e);
			}
		}
	}
	
	class SLAsciiIterator extends SLIterator {
		@Override
		public T next() {
			try {
				if (hasNext()) {
					consumed++;
					return readRecordASCII((Scanner)streamWrapper);
				}
				return null;
			} catch (IOException e) {
				close();
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public ListIterator<T> listIterator(int index) {
		if (isBinary) return new SLBinaryIterator();
		return new SLAsciiIterator();
	}

	void close() {
		if (streamWrapper != null) {
			try { ((Closeable) underlyingStream).close(); } catch (IOException e) {}
			streamWrapper = null;
		}
	}
	
	@Override
	public int size() {
		return size;
	}
	
	/**
	 * Get the number of records consumed so far
	 * @return number of consumed records
	 */
	public int consumed() {
		return consumed;
	}
	
	/**
	 * Get the number of records remaining in the stream
	 * @return number of remaining records
	 */
	public int remaining() {
		return size-consumed;
	}
	
	class MemoryRandomisableList extends ArrayList<T> implements RandomisableList<T> {
		private static final long serialVersionUID = 1L;
		@Override
		public RandomisableList<T> randomSubList(int nelem) {
			if (nelem > remaining()) 
				throw new IllegalArgumentException("not enough elements in list");
			
			int [] rnd = RandomData.getUniqueRandomInts(nelem, 0, size());
			MemoryRandomisableList newList = new MemoryRandomisableList();
			for (int i : rnd) {
				newList.add(get(i));
			}
			
			return newList;
		}
	}
	
	/** 
	 * This method creates a random sublist in ram from elements consumed
	 * from the target list. 
	 * 
	 * @see org.openimaj.util.list.RandomisableList#randomSubList(int)
	 */
	@Override
	public RandomisableList<T> randomSubList(int nelem) {
		if (nelem > remaining()) 
			throw new IllegalArgumentException("not enough records remaining in list");
		
		MemoryRandomisableList newList = new MemoryRandomisableList();
		int [] rnd = RandomData.getUniqueRandomInts(nelem, 0, remaining());
		Arrays.sort(rnd);
		
		for (int i : rnd) {
			newList.add(get(i));
		}
		
		Collections.shuffle(newList);
		
		return newList;
	}
}
