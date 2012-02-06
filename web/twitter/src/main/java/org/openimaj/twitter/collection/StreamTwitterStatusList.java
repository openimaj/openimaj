package org.openimaj.twitter.collection;

import java.io.BufferedInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.openimaj.twitter.TwitterStatus;
import org.openimaj.util.list.AbstractStreamBackedList;


public class StreamTwitterStatusList extends AbstractStreamBackedList<TwitterStatus> implements TwitterStatusList{

	protected StreamTwitterStatusList(InputStream stream, int size,boolean isBinary, int headerLength, int recordLength) {
		super(stream, size, isBinary, headerLength, recordLength, TwitterStatus.class);
	}
	
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * 
	 * @param stream the input stream
	 * @param number of tweets to read from this stream
	 * 
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static <T extends TwitterStatus> StreamTwitterStatusList read(InputStream stream, int nTweets) throws IOException {
		return read(new BufferedInputStream(stream), nTweets);
	}
	
	/**
	 * Construct a new StreamTwitterStatusList from the given input stream.
	 * 
	 * @param stream the input stream
	 * @param number of tweets to read from this stream
	 * 
	 * @param clz the class of local feature to read
	 * @return a new list
	 * @throws IOException if an error occurs reading from the stream
	 */
	public static <T extends TwitterStatus> StreamTwitterStatusList read(BufferedInputStream stream, int nTweets) throws IOException {
		boolean isBinary = false;
				
		//read header
		int size = nTweets;
		int headerLength = 0;
		int recordLength = -1;
		
		return new StreamTwitterStatusList(stream, size, isBinary, headerLength, recordLength);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		for (TwitterStatus k : this) k.writeASCII(out);
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
