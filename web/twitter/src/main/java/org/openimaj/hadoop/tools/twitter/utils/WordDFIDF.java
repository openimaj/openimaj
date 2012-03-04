package org.openimaj.hadoop.tools.twitter.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;

/**
 * Convenience class which holds all the components required to calculate DF-IDF 
 * 
 * @author ss
 *
 */
public class WordDFIDF implements ReadWriteableBinary, Comparable<WordDFIDF> {
	/**
	 * Total number of tweets in all timeperiods
	 */
	public long Ttf;
	/**
	 * Number of tweets containing this word in all timeperiods
	 */
	public long Twf;
	/**
	 * Number of tweets in this timeperiod
	 */
	public long tf;
	
	/**
	 * Number of tweets containing this word in this time period
	 */
	public long wf;
	/**
	 * the measurment time period
	 */
	public long timeperiod;
	
	public WordDFIDF(){
		
	}
	/**
	 * @param timeperiod the timeperiod
	 * @param wf Word count in this timeperiod
	 * @param tf Tweet count in this timeperiod
	 * @param twf Word count across all time
	 * @param ttf Tweet count across all time
	 */
	public WordDFIDF(long timeperiod,long wf,long tf,long twf,long ttf) {
		this.timeperiod = timeperiod;
		this.wf = wf;
		this.tf = tf;
		this.Twf = twf;
		this.Ttf = ttf;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeLong(timeperiod);
		out.writeLong(wf);
		out.writeLong(tf);
		out.writeLong(Twf);
		out.writeLong(Ttf);
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		timeperiod = in.readLong();
		wf = in.readLong();
		tf = in.readLong();
		Twf = in.readLong();
		Ttf = in.readLong();
	}
	
	/**
	 * DF-IDF as defined by "Event Detection in Twitter by J. Weng et. al. 2011"
	 * @return the DF-IDF score
	 */
	public double dfidf(){
		double wf = this.wf;
		double tf = this.tf;
		double Twf = this.Twf;
		double Ttf = this.Ttf;
		
		return (wf/tf) * Math.log(Ttf / Twf);
	}

	@Override
	public int compareTo(WordDFIDF other) {
		return new Long(timeperiod).compareTo(other.timeperiod);
	}
}
