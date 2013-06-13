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
package org.openimaj.hadoop.tools.twitter.token.mode.dfidf;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.TimeFrequencyHolder.TimeFrequency;
import org.openimaj.io.ReadWriteableBinary;

/**
 * A {@link ReadWriteableBinary} {@link TLongObjectHashMap}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TimeFrequencyHolder extends TLongObjectHashMap<TimeFrequency> implements
		ReadWriteableBinary
{

	/**
	 * Holds the number of a thing at a moment in time and the total number of
	 * that thing seen across all time
	 * 
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class TimeFrequency implements ReadWriteableBinary {
		long time;
		long periodFrequency;
		long cumulativeFrequency;

		/**
		 * default
		 */
		public TimeFrequency() {

		}

		@Override
		public String toString() {
			return String.format("t(%d) tf(%d) Tf(%d)", this.time, periodFrequency, cumulativeFrequency);
		}

		/**
		 * initialise, presumed to be at the beginning of some time period so
		 * the cumulative == the period frequency
		 * 
		 * @param time
		 * @param frequency
		 */
		public TimeFrequency(long time, long frequency) {
			this.time = time;
			this.periodFrequency = frequency;
			this.cumulativeFrequency = frequency;

		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			time = in.readLong();
			periodFrequency = in.readLong();
			this.cumulativeFrequency = periodFrequency;
		}

		@Override
		public byte[] binaryHeader() {
			return "".getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeLong(time);
			out.writeLong(periodFrequency);
		}

		/**
		 * Given a {@link TimeFrequency} instance, keep count of cumulative
		 * frequency and set the periodFrequency to the one furthest along in
		 * time
		 * 
		 * @param other
		 * @return a new {@link TimeFrequency} instance
		 */
		public TimeFrequency combine(TimeFrequency other) {
			final TimeFrequency nHolder = new TimeFrequency();
			TimeFrequency future, past;
			if (this.time > other.time) { // this is the future time instance
											// (so should be the time we
											// remember)
				future = this;
				past = other;
			}
			else if (other.time > this.time) { // other is the future time
												// instance
				future = other;
				past = this;
			}
			else { // equal time instances, choose other as the "true" value
				nHolder.time = other.time;
				nHolder.periodFrequency = other.periodFrequency;
				nHolder.cumulativeFrequency = other.cumulativeFrequency;
				return nHolder;
			}
			nHolder.time = future.time;
			nHolder.periodFrequency = future.periodFrequency;
			nHolder.cumulativeFrequency = past.cumulativeFrequency + future.periodFrequency;
			return nHolder;

		}

		/**
		 * @param l
		 * @param nTweets
		 * @return a new {@link TimeFrequency} instance
		 */
		public TimeFrequency combine(long l, long nTweets) {
			final TimeFrequency ntf = new TimeFrequency(l, nTweets);
			return combine(ntf);
		}
	}

	/**
	 * default
	 */
	public TimeFrequencyHolder() {
	}

	/**
	 * For every held {@link TimeFrequency} reset {@link TimeFrequency}
	 * cumulativeFrequency = {@link TimeFrequency} periodFrequency and then go
	 * through each in key-value order and use
	 * {@link TimeFrequency#combine(TimeFrequency)} to calculate a cumulative
	 * count
	 */
	public void recalculateCumulativeFrequencies() {
		final long[] sortedKeys = this.keys();
		Arrays.sort(sortedKeys);
		TimeFrequency current = null;
		for (int i = 0; i < sortedKeys.length; i++) {
			final long k = sortedKeys[i];
			final TimeFrequency held = this.get(k);
			held.cumulativeFrequency = held.periodFrequency;
			if (current == null)
			{
				current = held;
			}
			else {
				current = current.combine(this.get(k));
			}
			this.put(k, current);
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		final long[] sortedKeys = this.keys();
		Arrays.sort(sortedKeys);
		for (int i = 0; i < sortedKeys.length; i++) {
			final long k = sortedKeys[i];
			builder.append(String.format("%d - %s\n", k, this.get(k)));
		}
		return builder.toString();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final long count = in.readLong();
		for (int i = 0; i < count; i++) {
			final TimeFrequency tf = new TimeFrequency();
			tf.readBinary(in);
			this.put(tf.time, tf);
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		out.writeLong(this.size());
		this.forEachEntry(new TLongObjectProcedure<TimeFrequency>() {

			@Override
			public boolean execute(long a, TimeFrequency b) {
				try {
					b.writeBinary(out);
				} catch (final IOException e) {
					return false;
				}
				return true;
			}
		});
	}
}
