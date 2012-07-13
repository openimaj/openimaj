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
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.list.AbstractStreamBackedList;
	
	

/**
 * Converts an input stream into a list {@link USMFStatus} instances using various methods. 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class StreamTwitterStatusList<T extends USMFStatus> extends AbstractStreamBackedList<T> implements TwitterStatusList<T>{
	
	private Class<? extends GeneralJSON> seedClass=USMFStatus.class;
	
	protected StreamTwitterStatusList(InputStream stream, int size,boolean isBinary, int headerLength, int recordLength,Class<T> clazz,String charset) throws IOException{
		super(stream, size, isBinary, headerLength, recordLength,clazz,charset);
	}
	
	protected StreamTwitterStatusList(InputStream stream, int size,boolean isBinary, int headerLength, int recordLength,Class<? extends GeneralJSON> inputClass, Class<T> instanceClass,String charset) throws IOException{
		super(stream, size, isBinary, headerLength, recordLength,instanceClass,charset);
		this.seedClass = inputClass;
	}
	
	protected StreamTwitterStatusList(InputStream stream, int size,boolean isBinary, int headerLength, int recordLength,Class<T> clazz) throws IOException{
		super(stream, size, isBinary, headerLength, recordLength,clazz);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected T newElementInstance() {
		return (T) new USMFStatus(seedClass);
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
	public static StreamTwitterStatusList<USMFStatus> read(InputStream stream, int nTweets) throws IOException {
		return read(new BufferedInputStream(stream), nTweets,USMFStatus.class);
	}
	
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * 
	 * @param stream the input stream
	 * 
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamTwitterStatusList<USMFStatus> read(InputStream stream) throws IOException {
		return read(new BufferedInputStream(stream), -1,USMFStatus.class);
	}
	
		
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * 
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 * @param charset the charset used to read the stream
	 * 
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static StreamTwitterStatusList<USMFStatus> read(InputStream stream, int nTweets,String charset) throws IOException {
		return read(new BufferedInputStream(stream), nTweets,USMFStatus.class,charset);
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
	public static StreamTwitterStatusList<USMFStatus> read(InputStream stream, String charset) throws IOException {
		return read(new BufferedInputStream(stream), -1,USMFStatus.class,charset);
	}
	
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * @param <T> the type of the USMFStatus instances returned
	 * 
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 * @param clazz the class to instantiate
	 * 
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static <T extends USMFStatus> StreamTwitterStatusList<T> read(InputStream stream, int nTweets, Class<T> clazz) throws IOException {
		return read(new BufferedInputStream(stream), nTweets,clazz);
	}
	
	/**
	 * @param stream
	 * @param generalJSON
	 * @return a list of USMFStatus instances
	 * @throws IOException
	 */
	public static StreamTwitterStatusList<USMFStatus> readUSMF(InputStream stream, Class<? extends GeneralJSON> generalJSON) throws IOException {
		StreamTwitterStatusList<USMFStatus> a = read(new BufferedInputStream(stream), -1,generalJSON, USMFStatus.class);
		return a;
	}
	
	/**
	 * @param stream
	 * @param generalJSON
	 * @param charset the charset to read with
	 * @return a list of USMFStatus instances
	 * @throws IOException
	 */
	public static StreamTwitterStatusList<USMFStatus> readUSMF(InputStream stream, Class<? extends GeneralJSON> generalJSON, String charset) throws IOException {
		StreamTwitterStatusList<USMFStatus> a = read(new BufferedInputStream(stream), -1,generalJSON, USMFStatus.class,charset);
		return a;
	}
	
	/**
	 * @param stream
	 * @param nTweets number of tweets to read
	 * @param generalJSON
	 * @param charset the charset to read with
	 * @return a list of USMFStatus instances
	 * @throws IOException
	 */
	public static StreamTwitterStatusList<USMFStatus> readUSMF(InputStream stream, int nTweets,Class<? extends GeneralJSON> generalJSON, String charset) throws IOException {
		StreamTwitterStatusList<USMFStatus> a = read(new BufferedInputStream(stream), -1,generalJSON, USMFStatus.class,charset);
		return a;
	}
	
	/**
	 * @param stream
	 * @param nTweets 
	 * @param generalJSON
	 * @return a list of USMFStatus instances
	 * @throws IOException
	 */
	public static StreamTwitterStatusList<USMFStatus> readUSMF(InputStream stream, int nTweets, Class<? extends GeneralJSON> generalJSON) throws IOException {
		StreamTwitterStatusList<USMFStatus> a = read(new BufferedInputStream(stream), nTweets,generalJSON, USMFStatus.class);
		return a;
	}
	
	
	/**
	 * @param <T> 
	 * @param stream
	 * @param nTweets 
	 * @param inputClass the class of used by the {@link USMFStatus} instances to read
	 * @param instanceClass the class of the {@link USMFStatus} instances
	 * @return a list of USMFStatus instances
	 * @throws IOException
	 */
	public static <T extends USMFStatus> StreamTwitterStatusList<T> read(InputStream stream, int nTweets, Class<? extends GeneralJSON> inputClass, Class<T> instanceClass) throws IOException {
		StreamTwitterStatusList<T> a = read(new BufferedInputStream(stream), nTweets,inputClass, instanceClass, "UTF-8");
		return a;
	}
	
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * @param <T> the instance type
	 * 
	 * @param stream the input stream
	 * @param nTweets the number of tweets expected
	 * @param inputClass the input class
	 * @param instanceClass the instance class
	 * @param charset the charset of the reader
	 * 
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static <T extends USMFStatus> StreamTwitterStatusList<T> read(BufferedInputStream stream, int nTweets,Class<? extends GeneralJSON> inputClass, Class<T> instanceClass, String charset) throws IOException {
		boolean isBinary = false;
				
		//read header
		int size = nTweets;
		int headerLength = 0;
		int recordLength = -1;
		
		return new StreamTwitterStatusList<T>(stream, size, isBinary, headerLength, recordLength,inputClass,instanceClass,charset);
	}
	
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * @param <T> the type of the USMFStats instances returned
	 * 
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 * @param clz the class of local feature to read
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static <T extends USMFStatus> StreamTwitterStatusList<T> read(BufferedInputStream stream, int nTweets,Class<T> clz) throws IOException {
		boolean isBinary = false;
				
		//read header
		int size = nTweets;
		int headerLength = 0;
		int recordLength = -1;
		
		return new StreamTwitterStatusList<T>(stream, size, isBinary, headerLength, recordLength,clz);
	}
	
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * @param <T> the type of status instance returned
	 * 
	 * @param stream the input stream
	 * @param nTweets of tweets to read from this stream
	 * @param clazz the class of local feature to read
	 * @param charset the charset to read with
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static <T extends USMFStatus> StreamTwitterStatusList<T> read(BufferedInputStream stream, int nTweets,Class<T> clazz, String charset) throws IOException {
		boolean isBinary = false;
				
		//read header
		int size = nTweets;
		int headerLength = 0;
		int recordLength = -1;
		
		return new StreamTwitterStatusList<T>(stream, size, isBinary, headerLength, recordLength,clazz,charset);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		for (USMFStatus k : this) {
			k.writeASCII(out);
			out.println();
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		throw new UnsupportedOperationException();
	}
	
}
