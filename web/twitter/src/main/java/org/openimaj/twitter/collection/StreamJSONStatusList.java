/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.twitter.collection;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;
import org.openimaj.util.list.AbstractStreamBackedList;


import com.google.gson.Gson;


/**
 * A list of json maps
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StreamJSONStatusList extends AbstractStreamBackedList<ReadableWritableJSON> {

	/**
	 * a readable json hashmap
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class ReadableWritableJSON extends HashMap<String,Object> implements ReadWriteable{
		/**
		 *
		 */
		private static final long serialVersionUID = 6001988896541110142L;
		/**
		 *
		 */
		public ReadableWritableJSON() {
		}
		private transient Gson gson = new Gson();
		@Override
		public void readASCII(Scanner in) throws IOException {
			if(in == null) return;
			ReadableWritableJSON inner = gson.fromJson(in.nextLine(), ReadableWritableJSON.class);
			for (java.util.Map.Entry<String, Object> entry: inner.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}
		}

		@Override
		public String asciiHeader() {
			return "";
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			ReadableWritableJSON inner = gson.fromJson(in.readUTF(), ReadableWritableJSON.class);
			for (java.util.Map.Entry<String, Object> entry: inner.entrySet()) {
				this.put(entry.getKey(), entry.getValue());
			}

		}

		@Override
		public byte[] binaryHeader() {
			return "".getBytes();
		}

		@Override
		public void writeASCII(PrintWriter out) throws IOException {
			gson.toJson(this, out);
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(gson.toJson(this));
		}
	}

	protected StreamJSONStatusList(InputStream stream, int size,boolean isBinary, int headerLength, int recordLength,String charset) throws IOException{
		super(stream, size, isBinary, headerLength, recordLength,ReadableWritableJSON.class,charset);
	}


	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 *
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 *
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamJSONStatusList read(InputStream stream, int nTweets) throws IOException {
		return read(new BufferedInputStream(stream), nTweets);
	}

	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 *
	 * @param stream the input stream
	 *
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamJSONStatusList read(InputStream stream) throws IOException {
		return read(new BufferedInputStream(stream), -1);
	}


	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 *
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 * @param charset
	 *
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamJSONStatusList read(InputStream stream, int nTweets,String charset) throws IOException {
		return read(new BufferedInputStream(stream), nTweets,charset);
	}

	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 *
	 * @param stream the input stream
	 * @param charset the charset of the underlying stream
	 *
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamJSONStatusList read(InputStream stream, String charset) throws IOException {
		return read(new BufferedInputStream(stream), -1,charset);
	}



	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 *
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamJSONStatusList read(BufferedInputStream stream, int nTweets) throws IOException {
		boolean isBinary = false;

		//read header
		int size = nTweets;
		int headerLength = 0;
		int recordLength = -1;

		return new StreamJSONStatusList(stream, size, isBinary, headerLength, recordLength,"UTF-8");
	}

	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 *
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 * @param charset the charset to read the stream as
	 *
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamJSONStatusList read(BufferedInputStream stream, int nTweets, String charset) throws IOException {
		boolean isBinary = false;

		//read header
		int size = nTweets;
		int headerLength = 0;
		int recordLength = -1;

		return new StreamJSONStatusList(stream, size, isBinary, headerLength, recordLength,charset);
	}
}
