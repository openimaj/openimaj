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
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

/**
 * Convenience class which holds all the components required to calculate DF-IDF
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class WordDFIDF implements ReadWriteable, Comparable<WordDFIDF> {
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

	/**
	 * Helpful for reading
	 */
	public WordDFIDF() {
		Ttf = Twf = tf = wf = 0;
	}

	/**
	 * @param timeperiod
	 *            the timeperiod
	 * @param wf
	 *            Word count in this timeperiod
	 * @param tf
	 *            Tweet count in this timeperiod
	 * @param twf
	 *            Word count across all time
	 * @param ttf
	 *            Tweet count across all time
	 */
	public WordDFIDF(long timeperiod, long wf, long tf, long twf, long ttf) {
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
	 * 
	 * @return the DF-IDF score
	 */
	public double dfidf() {
		final double wf = this.wf;
		final double tf = this.tf;
		final double Twf = this.Twf;
		final double Ttf = this.Ttf;
		if (tf == 0 || Ttf == 0)
			return 0;

		return (wf / tf) * Math.log(Ttf / Twf);
	}

	@Override
	public int compareTo(WordDFIDF other) {
		return new Long(timeperiod).compareTo(other.timeperiod);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WordDFIDF))
			return false;
		final WordDFIDF that = (WordDFIDF) obj;
		return that.compareTo(this) == 0;
	}

	@Override
	public int hashCode() {
		return (int) (timeperiod ^ (timeperiod >>> 32));
	}

	@Override
	public String toString() {
		final String format = "(wf=%s, tf=%s, Twf=%s, Ttf=%s, DFIDF=%.5f)";
		return String.format(format, wf, tf, Twf, Ttf, dfidf());
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		this.timeperiod = in.nextLong();
		this.wf = in.nextLong();
		this.tf = in.nextLong();
		this.Twf = in.nextLong();
		this.Ttf = in.nextLong();
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.printf("%s %s %s %s %s", this.timeperiod, this.wf, this.tf, this.Twf, this.Ttf);
	}
}
