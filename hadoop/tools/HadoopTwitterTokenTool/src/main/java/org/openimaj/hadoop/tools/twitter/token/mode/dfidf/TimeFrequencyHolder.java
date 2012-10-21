package org.openimaj.hadoop.tools.twitter.token.mode.dfidf;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.TimeFrequencyHolder.TimeFrequency;
import org.openimaj.io.ReadWriteableBinary;

/**
 * A {@link ReadWriteableBinary} {@link TLongObjectHashMap}
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TimeFrequencyHolder extends TLongObjectHashMap<TimeFrequency> implements
		ReadWriteableBinary {

	/**
	 * default
	 */
	public TimeFrequencyHolder() {
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		this.forEachEntry(new TLongObjectProcedure<TimeFrequency>() {

			@Override
			public boolean execute(long a, TimeFrequency b) {
				builder.append(String.format("%d - %s\n", a, b));
				return true;
			}

		});
		return builder.toString();
	}

	/**
	 * Holds the number of a thing at a moment in time and the total number of
	 * that thing seen across all time
	 * 
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
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
			cumulativeFrequency = in.readLong();
		}

		@Override
		public byte[] binaryHeader() {
			return "".getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeLong(time);
			out.writeLong(periodFrequency);
			out.writeLong(cumulativeFrequency);
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
			TimeFrequency nHolder = new TimeFrequency();
			TimeFrequency future, past;
			if (this.time > other.time) {
				future = this;
				past = other;
			}
			else if (other.time > this.time) {
				future = other;
				past = this;
			}
			else {
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
			TimeFrequency ntf = new TimeFrequency(l, nTweets);
			return combine(ntf);
		}
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		long count = in.readLong();
		for (int i = 0; i < count; i++) {
			TimeFrequency tf = new TimeFrequency();
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
				} catch (IOException e) {
					return false;
				}
				return true;
			}
		});
	}
}
