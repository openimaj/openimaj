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

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.mapreduce.stage.IdentityReducer;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.TextLongByteStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.JsonPathFilterSet;
import org.openimaj.hadoop.tools.twitter.token.mode.TextEntryType;
import org.openimaj.hadoop.tools.twitter.token.mode.WritableEnumCounter;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.TimeFrequencyHolder.TimeFrequency;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.USMFStatus;

import com.jayway.jsonpath.JsonPath;

/**
 * A mapper/reducer whose purpose is to do the following:
 * function(timePeriodLength) So a word in a tweet can happen in the time period
 * between t - 1 and t. First task: map input: tweetstatus # json twitter status
 * with JSONPath to words map output: <timePeriod: <word:#freq,tweets:#freq>,
 * -1:<word:#freq,tweets:#freq> > reduce input: <timePeriod:
 * [<word:#freq,tweets:#freq>,...,<word:#freq,tweets:#freq>]> reduce output:
 * <timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...>
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CountTweetsInTimeperiod extends StageProvider {

	private String[] nonHadoopArgs;
	private boolean inmemoryCombine;
	private boolean buildTimeIndex = true;
	private long timedelta;
	/**
	 * option for the timecount dir location
	 */
	public final static String TIMECOUNT_DIR = "timeperiodTweet";

	/**
	 * A time index holding tweet totals and cumulative totals for each time
	 * period
	 */
	public final static String TIMEINDEX_FILE = "timeindex";

	/**
	 * where to find the global stats file
	 */
	public final static String GLOBAL_STATS_FILE = "globalstats";
	private static final String TIMEDELTA = "org.openimaj.hadoop.tools.twitter.token.mode.dfidf.timedelta";
	/**
	 * A time index holding tweet totals and cumulative totals for each time
	 * period
	 */
	public final static String TIMEINDEX_LOCATION_PROP = "org.openimaj.hadoop.tools.twitter.token.mode.dfidf.timeindex";

	/**
	 * @param nonHadoopArgs
	 *            to be sent to the stage
	 * @param timedelta
	 *            the time delta between which to quantise time periods
	 */
	public CountTweetsInTimeperiod(String[] nonHadoopArgs, long timedelta) {
		this.nonHadoopArgs = nonHadoopArgs;
		this.inmemoryCombine = false;
		this.timedelta = timedelta;
	}

	/**
	 * @param nonHadoopArgs
	 *            to be sent to the stage
	 * @param inMemoryCombine
	 *            whether an in memory combination of word counts should be
	 *            performed
	 * @param timedelta
	 *            the time delta between which to quantise time periods
	 */
	public CountTweetsInTimeperiod(String[] nonHadoopArgs, boolean inMemoryCombine,
			long timedelta)
	{
		this.nonHadoopArgs = nonHadoopArgs;
		this.inmemoryCombine = inMemoryCombine;
		this.timedelta = timedelta;
	}

	/**
	 *
	 * map input: tweetstatus # json twitter status with JSONPath to words map
	 * output: <timePeriod: <word:#freq,tweets:#freq>,
	 * -1:<word:#freq,tweets:#freq> >
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         <ss@ecs.soton.ac.uk>
	 *
	 */
	public static class Map extends Mapper<LongWritable, Text, LongWritable, BytesWritable> {

		/**
		 * Mapper don't care, mapper don't give a fuck
		 */
		public Map() {

		}

		/**
		 * The time used to signify the end, used to count total numbers of
		 * times a given word appears
		 */
		public static final LongWritable END_TIME = new LongWritable(-1);
		/**
		 * A total of the number of tweets, must be ignored!
		 */
		public static final LongWritable TOTAL_TIME = new LongWritable(-2);
		private HadoopTwitterTokenToolOptions options;
		private long timeDeltaMillis;
		private JsonPath jsonPath;
		private JsonPathFilterSet filters;

		protected synchronized void loadOptions(Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context)
				throws IOException
		{
			if (options == null) {
				try {
					options = new HadoopTwitterTokenToolOptions(context.getConfiguration().getStrings(
							HadoopTwitterTokenToolOptions.ARGS_KEY));
					options.prepare();
					filters = options.getFilters();
					timeDeltaMillis = context.getConfiguration().getLong(CountTweetsInTimeperiod.TIMEDELTA, 60) * 60 * 1000;
					jsonPath = JsonPath.compile(options.getJsonPath());

				} catch (final CmdLineException e) {
					throw new IOException(e);
				} catch (final Exception e) {
					throw new IOException(e);
				}
			}
		}

		private HashMap<Long, TweetCountWordMap> tweetWordMap;

		@Override
		protected void setup(Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context) throws IOException,
				InterruptedException
		{
			loadOptions(context);
			this.tweetWordMap = new HashMap<Long, TweetCountWordMap>();
		}

		@Override
		protected void map(LongWritable key, Text value,
				Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context) throws java.io.IOException,
				InterruptedException
		{
			List<String> tokens = null;
			USMFStatus status = null;
			DateTime time = null;
			try {
				final String svalue = value.toString();
				status = new USMFStatus(options.getStatusType().type());
				status.fillFromString(svalue);
				if (status.isInvalid())
					return;
				if (!filters.filter(svalue))
					return;
				tokens = jsonPath.read(svalue);
				if (tokens == null) {
					context.getCounter(TextEntryType.INVALID_JSON).increment(1);
					// System.err.println("Couldn't read the tokens from the tweet");
					return;
				}
				if (tokens.size() == 0) {
					context.getCounter(TextEntryType.INVALID_ZEROLENGTH).increment(1);
					return; // Quietly quit, value exists but was empty
				}
				time = status.createdAt();
				if (time == null) {
					context.getCounter(TextEntryType.INVALID_TIME).increment(1);
					// System.err.println("Time was null, this usually means the original tweet had no time. Skip this tweet.");
					return;
				}

			} catch (final Exception e) {
				// System.out.println("Couldn't get tokens from:\n" + value +
				// "\nwith jsonpath:\n" + jsonPath);
				return;
			}
			// Quantise the time to a specific index
			final long timeIndex = (time.getMillis() / timeDeltaMillis) * timeDeltaMillis;
			TweetCountWordMap timeWordMap = this.tweetWordMap.get(timeIndex);
			// System.out.println("Tweet time: " + time.getMillis());
			// System.out.println("Tweet timeindex: " + timeIndex);
			if (timeWordMap == null) {
				this.tweetWordMap.put(timeIndex, timeWordMap = new TweetCountWordMap());
			}
			final TObjectIntHashMap<String> tpMap = timeWordMap.getTweetWordMap();
			timeWordMap.incrementTweetCount(1);
			final List<String> seen = new ArrayList<String>();
			for (final String token : tokens) {
				// Apply stop words?
				// Apply junk words?
				// Already seen it?

				if (seen.contains(token))
					continue;
				seen.add(token);
				tpMap.adjustOrPutValue(token, 1, 1);
				// if(token.equals("...")){
				// System.out.println("TOKEN: " + token);
				// System.out.println("TIME: " + timeIndex);
				// System.out.println("NEW VALUE: " + newv);
				// }
			}
			context.getCounter(TextEntryType.VALID).increment(1);
		}

		@Override
		protected void cleanup(Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context)
				throws IOException, InterruptedException
		{
			System.out.println("Cleaing up mapper, seen " + this.tweetWordMap.entrySet().size() + " time slots");
			for (final Entry<Long, TweetCountWordMap> tpMapEntry : this.tweetWordMap.entrySet()) {
				final Long time = tpMapEntry.getKey();
				final TweetCountWordMap map = tpMapEntry.getValue();
				System.out.println("... time( " + time + ") seen " + map.getTweetWordMap().size() + " words");
				final ByteArrayOutputStream outarr = new ByteArrayOutputStream();
				IOUtils.writeBinary(outarr, map);
				final byte[] arr = outarr.toByteArray();
				final BytesWritable toWrite = new BytesWritable(arr);
				context.write(END_TIME, toWrite);
				context.write(new LongWritable(time), toWrite);
				context.getCounter(TextEntryType.ACUAL_EMITS).increment(1);
			}
		}
	}

	/**
	 * Identical to the {@link IdentityReducer} but constructs a time index
	 * found in {@link #TIMEINDEX_FILE}
	 *
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class TimeIndexReducer extends
			Reducer<LongWritable, BytesWritable, LongWritable, BytesWritable>
	{
		private TimeFrequencyHolder timeMap;

		/**
		 *
		 */
		public TimeIndexReducer() {
			timeMap = new TimeFrequencyHolder();
		}

		@Override
		protected void reduce(LongWritable time, Iterable<BytesWritable> values, Context context) throws IOException,
				InterruptedException
		{
			if (time.get() == Map.END_TIME.get()) {
				// End time can be ignored entirley in terms of the time index,
				// but still pass them on!
				for (final BytesWritable tweetwordmapbytes : values) {
					context.write(time, tweetwordmapbytes);
				}
			}
			else {
				final TweetCountWordMap accum = new TweetCountWordMap();
				for (final BytesWritable tweetwordmapbytes : values) {
					TweetCountWordMap tweetwordmap = null;
					tweetwordmap = IOUtils.read(new ByteArrayInputStream(tweetwordmapbytes.getBytes()),
							TweetCountWordMap.class);
					accum.combine(tweetwordmap);
					context.write(time, tweetwordmapbytes);
				}
				final TimeFrequency tf = new TimeFrequency(time.get(), accum.getNTweets());
				timeMap.put(tf.time, tf);
			}

		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			final String output = context.getConfiguration().getStrings(TIMEINDEX_LOCATION_PROP)[0];
			final Path indexOut = new Path(output + "/" + context.getTaskAttemptID());
			System.out.println("Writing time index to: " + indexOut);
			System.out.println("Timemap contains: " + this.timeMap.size());
			CountTweetsInTimeperiod.writeTimeIndex(this.timeMap, indexOut);
		}
	}

	/**
	 * reduce input: <timePeriod:
	 * [<word:#freq,tweets:#freq>,...,<word:#freq,tweets:#freq>]> reduce output:
	 * <timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...>
	 *
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class InMemoryCombiningReducer extends
			Reducer<LongWritable, BytesWritable, LongWritable, BytesWritable>
	{

		/**
		 * default construct does nothing
		 */
		public InMemoryCombiningReducer() {

		}

		@Override
		protected void reduce(LongWritable key, Iterable<BytesWritable> values,
				Reducer<LongWritable, BytesWritable, LongWritable, BytesWritable>.Context context) throws IOException,
				InterruptedException
		{
			final TweetCountWordMap accum = new TweetCountWordMap();
			for (final BytesWritable tweetwordmapbytes : values) {
				TweetCountWordMap tweetwordmap = null;
				tweetwordmap = IOUtils.read(new ByteArrayInputStream(tweetwordmapbytes.getBytes()),
						TweetCountWordMap.class);
				accum.combine(tweetwordmap);
			}
			final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			IOUtils.writeBinary(outstream, accum);
			context.write(key, new BytesWritable(outstream.toByteArray()));
		}
	}

	@Override
	public TextLongByteStage stage() {
		final TextLongByteStage s = new TextLongByteStage() {
			private Path actualOutputLocation;

			@Override
			public void setup(Job job) {
				job.getConfiguration().setStrings(HadoopTwitterTokenToolOptions.ARGS_KEY, nonHadoopArgs);
				job.getConfiguration().setLong(TIMEDELTA, timedelta);
				job.getConfiguration().setStrings(TIMEINDEX_LOCATION_PROP,
						new Path(actualOutputLocation, TIMEINDEX_FILE).toString());
				if (!inmemoryCombine) {
					if (!buildTimeIndex) {
						job.setNumReduceTasks(0);
					}
					else {
						job.setNumReduceTasks(10);
					}
				}
			}

			@Override
			public Class<? extends Mapper<LongWritable, Text, LongWritable, BytesWritable>> mapper() {
				return CountTweetsInTimeperiod.Map.class;
			}

			@Override
			public Class<? extends Reducer<LongWritable, BytesWritable, LongWritable, BytesWritable>> reducer() {
				if (inmemoryCombine)
					return CountTweetsInTimeperiod.InMemoryCombiningReducer.class;
				else if (buildTimeIndex)
					return CountTweetsInTimeperiod.TimeIndexReducer.class;
				else
					return super.reducer();
			}

			@Override
			public Job stage(Path[] inputs, Path output, Configuration conf) throws Exception {
				this.actualOutputLocation = output;
				return super.stage(inputs, output, conf);
			}

			@Override
			public String outname() {
				return TIMECOUNT_DIR;
			}

			@Override
			public void finished(Job job) {
				Counters counters;
				try {
					counters = job.getCounters();
				} catch (final IOException e) {
					// System.out.println("Counters not found!");
					return;
				}
				// Prepare a writer to the actual output location
				final Path out = new Path(actualOutputLocation, GLOBAL_STATS_FILE);

				FileSystem fs;
				try {
					fs = HadoopToolsUtil.getFileSystem(out);
					final FSDataOutputStream os = fs.create(out);
					IOUtils.writeASCII(os, new WritableEnumCounter<TextEntryType>(counters, TextEntryType.values()) {
						@Override
						public TextEntryType valueOf(String str) {
							return TextEntryType.valueOf(str);
						}

					});
				} catch (final IOException e) {
				}

			}
		};
		return s;
	}

	/**
	 * Write a timeindex to a {@link Path}
	 *
	 * @param timeMap
	 * @param indexOut
	 * @throws IOException
	 */
	public static void writeTimeIndex(TimeFrequencyHolder timeMap, Path indexOut) throws IOException {
		FSDataOutputStream os = null;
		try {

			final FileSystem fs = HadoopToolsUtil.getFileSystem(indexOut);
			os = fs.create(indexOut, true);
			IOUtils.writeBinary(os, timeMap);
			os.flush();
		} finally {
			os.close();
		}
	}

	/**
	 * Read a {@link TimeFrequencyHolder} from a {@link Path}. Path is assumed
	 * to be a directory containing many {@link TimeFrequencyHolder} instances.
	 *
	 * @param indexOut
	 * @return a new {@link TimeFrequencyHolder}
	 * @throws IOException
	 */
	public static TimeFrequencyHolder readTimeIndex(Path indexOut) throws IOException {
		if (!HadoopToolsUtil.fileExists(indexOut.toString())) {
			return null;
		}
		System.out.println("Reading time index from: " + indexOut);
		final TimeFrequencyHolder tfh = new TimeFrequencyHolder();

		final FileSystem fs = HadoopToolsUtil.getFileSystem(indexOut);
		final FileStatus[] indexParts = fs.listStatus(indexOut);
		for (final FileStatus fileStatus : indexParts) {
			System.out.println("Reading index part: " + fileStatus.getPath());
			FSDataInputStream in = null;
			try {
				in = fs.open(fileStatus.getPath());
				final TimeFrequencyHolder tempTfh = IOUtils.read(in, TimeFrequencyHolder.class);
				tempTfh.forEachEntry(new TLongObjectProcedure<TimeFrequency>() {
					@Override
					public boolean execute(long a, TimeFrequency b) {
						tfh.put(a, b); // This is safe because each time
						// frequency should contain completely
						// unique times!
						return true;
					}
				});
			} finally {
				in.close();
			}
		}
		tfh.recalculateCumulativeFrequencies();
		return tfh;

	}

	/**
	 * @param outpath
	 * @return the index location if it exists
	 */
	public static Path constructIndexPath(Path outpath) {
		final Path retPath = new Path(new Path(outpath, CountTweetsInTimeperiod.TIMECOUNT_DIR), TIMEINDEX_FILE);
		return retPath;
	}
}
