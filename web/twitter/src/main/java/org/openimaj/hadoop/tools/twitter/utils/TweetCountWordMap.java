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

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

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
public class TweetCountWordMap implements ReadWriteableBinary{
	int ntweets ;
	/**
	 * If the ntweets is set to this value, the ntweets should be ignored
	 */
	public final static int INVALID_TWEET_COUNT = -1;
	
	TObjectIntHashMap<String> wordMap ;
	/**
	 * empty words and 0 tweets
	 */
	public TweetCountWordMap() {
		ntweets = 0;
		wordMap = new TObjectIntHashMap<String>();
	}
	/**
	 * @param ntweets
	 * @param wordMap
	 */
	public TweetCountWordMap(int ntweets,TObjectIntHashMap<String> wordMap ) {
		this.ntweets = ntweets;
		this.wordMap = wordMap;
	}
	@Override
	public void readBinary(DataInput in) throws IOException {
		WriteableStringIntPair tweetPair = new WriteableStringIntPair();
		tweetPair.readBinary(in);
		this.ntweets = tweetPair.secondObject();
		int nWords = in.readInt();
		for (int i = 0; i < nWords; i++) {
			WriteableStringIntPair wordPair = new WriteableStringIntPair();
			wordPair.readBinary(in);
			this.wordMap.put(wordPair.firstObject(), wordPair.secondObject());
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		new WriteableStringIntPair("ntweets",this.ntweets).writeBinary(out);
		out.writeInt(this.wordMap.size());
		this.wordMap.forEachEntry(new TObjectIntProcedure<String>() {
			@Override
			public boolean execute(String word, int count) {
				try {
					new WriteableStringIntPair(word,count).writeBinary(out);
				} catch (IOException e) {}
				return true;
			}
		});
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof TweetCountWordMap))return false;
		final TweetCountWordMap that = (TweetCountWordMap)other;
		boolean eq = this.ntweets == that.ntweets;
		if(!eq) return false;
		return this.wordMap.forEachEntry(new TObjectIntProcedure<String>() {

			@Override
			public boolean execute(String arg0, int arg1) {
				return that.wordMap.get(arg0) == arg1;
			}
		});
		
	}
	/**
	 * @return the word map
	 */
	public TObjectIntHashMap<String> getTweetWordMap() {
		// TODO Auto-generated method stub
		return this.wordMap;
	}
	/**
	 * @param i increment number of tweets by this amount
	 */
	public void incrementTweetCount(int i) {
		this.ntweets += i;
		
	}
	/**
	 * Add values from "that" to those in this if they exist, otherwise create the element
	 * and start a new count
	 * @param that
	 */
	public void combine(TweetCountWordMap that) {
		this.ntweets += that.ntweets;
		if(this.wordMap == null || that.wordMap == null) return;
		that.wordMap.forEachEntry(new TObjectIntProcedure<String>() {

			@Override
			public boolean execute(String word, int count) {
				TweetCountWordMap.this.wordMap.adjustOrPutValue(word, count, count);
				return true;
			}
		});
	}
	/**
	 * @return the number of tweets
	 */
	public long getNTweets() {
		return this.ntweets;
	}
	
	/**
	 * set the number of tweets
	 * @param ntweets 
	 */
	public void setNTweets(int ntweets) {
		this.ntweets = ntweets;
	}
}