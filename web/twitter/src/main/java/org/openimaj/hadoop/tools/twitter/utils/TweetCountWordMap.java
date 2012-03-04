package org.openimaj.hadoop.tools.twitter.utils;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

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
public class TweetCountWordMap implements ReadWriteableBinary{
	int ntweets ;
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
}