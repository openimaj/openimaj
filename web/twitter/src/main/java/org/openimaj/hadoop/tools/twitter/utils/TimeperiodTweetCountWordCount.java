package org.openimaj.hadoop.tools.twitter.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;

/**
 * Class encapsulating a number of tweets across which certain words were seen
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TimeperiodTweetCountWordCount implements ReadWriteableBinary{
	
	/**
	 * The timeperiod of these counts
	 */
	public long timeperiod;
	/**
	 * The word counts (of an implicitly defined individual word) in this time period
	 */
	public long wordcount;
	/**
	 * The total number of tweets in this time period
	 */
	public long tweetcount;
	/**
	 * empty words and 0 tweets
	 */
	public TimeperiodTweetCountWordCount() {
	}
	
	/**
	 * @param timeperiod
	 * @param wordcount
	 * @param tweetcount
	 */
	public TimeperiodTweetCountWordCount(long timeperiod, long wordcount,long tweetcount) {
		this.timeperiod = timeperiod;
		this.wordcount = wordcount;
		this.tweetcount = tweetcount;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.timeperiod = in.readLong();
		this.wordcount = in.readLong();
		this.tweetcount =in.readLong();
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		out.writeLong(timeperiod);
		out.writeLong(wordcount);
		out.writeLong(tweetcount);
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof TimeperiodTweetCountWordCount))return false;
		TimeperiodTweetCountWordCount that = (TimeperiodTweetCountWordCount)other;
		return this.timeperiod == that.timeperiod && this.tweetcount == that.tweetcount && this.wordcount == that.wordcount;
		
	}
}