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
package org.openimaj.hadoop.tools.twitter.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;

/**
 * Class encapsulating a number of tweets across which certain words were seen
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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