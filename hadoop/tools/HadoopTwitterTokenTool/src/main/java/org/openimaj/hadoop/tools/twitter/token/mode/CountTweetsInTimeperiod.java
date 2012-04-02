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
package org.openimaj.hadoop.tools.twitter.token.mode;

import gnu.trove.TObjectIntHashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.TextLongByteStage;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.TwitterStatus;

import com.jayway.jsonpath.JsonPath;

/**
 * A mapper/reducer whose purpose is to do the following:
 * function(timePeriodLength)
 * So a word in a tweet can happen in the time period between t - 1 and t.
 * First task:
 * 	map input:
 * 		tweetstatus # json twitter status with JSONPath to words
 * 	map output:
 * 		<timePeriod: <word:#freq,tweets:#freq>, -1:<word:#freq,tweets:#freq> > 
 * 	reduce input:
 * 		<timePeriod: [<word:#freq,tweets:#freq>,...,<word:#freq,tweets:#freq>]> 
 *	reduce output:
 *		<timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class CountTweetsInTimeperiod extends StageProvider{
	private String[] nonHadoopArgs;
	public final static String TIMECOUNT_DIR = "timeperiodTweet";

	/**
	 * @param nonHadoopArgs to be sent to the stage
	 */
	public CountTweetsInTimeperiod(String[] nonHadoopArgs) {
		this.nonHadoopArgs = nonHadoopArgs;
	}


	/**
	 * The key in which command line arguments are held for each mapper to read the options instance
	 */
	public static final String ARGS_KEY = "TOKEN_ARGS";
	
	/**
	 * 
	 *  map input:
	 *  	tweetstatus # json twitter status with JSONPath to words
	 *  map output:
	 *  	<timePeriod: <word:#freq,tweets:#freq>, -1:<word:#freq,tweets:#freq> > 
	 *  
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei
	 *         <ss@ecs.soton.ac.uk>
	 * 
	 */
	public static class Map extends Mapper<LongWritable, Text, LongWritable, BytesWritable> {

		/**
		 * Mapper don't care, mapper don't give a fuck
		 */
		public Map(){
			
		}
		public static final LongWritable END_TIME = new LongWritable(-1);
		private static HadoopTwitterTokenToolOptions options;
		private static long timeDeltaMillis;
		private static JsonPath jsonPath;

		protected static synchronized void loadOptions(Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context) throws IOException {
			if (options == null) {
				try {
					options = new HadoopTwitterTokenToolOptions(context
							.getConfiguration().getStrings(ARGS_KEY));
					options.prepare();
					timeDeltaMillis = options.getTimeDelta() * 60 * 1000;
					jsonPath = JsonPath.compile(options.getJsonPath());
				} catch (CmdLineException e) {
					throw new IOException(e);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

		private HashMap<Long, TweetCountWordMap> tweetWordMap;

		@Override
		protected void setup(Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context) throws IOException, InterruptedException {
			loadOptions(context);
			this.tweetWordMap = new HashMap<Long, TweetCountWordMap>();
		}

		@Override
		protected void map(LongWritable key,Text value,Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context) throws java.io.IOException, InterruptedException {
			List<String> tokens = null;
			TwitterStatus status = null;
			DateTime time = null;
			try {
				String svalue = value.toString();
				status = TwitterStatus.fromString(svalue);
				if(status.isInvalid()) return;
				tokens = jsonPath.read(svalue );
				if(tokens == null) {
//					System.err.println("Couldn't read the tokens from the tweet");
					return;
				}
				if(tokens.size() == 0){
					return; //Quietly quit, value exists but was empty
				}
				time = status.createdAt();
				if(time == null){
					System.err.println("Time was null, this usually means the original tweet had no time. Skip this tweet.");
					return;
				}

			} catch (Exception e) {
				System.out.println("Couldn't get tokens from:\n" + value + "\nwith jsonpath:\n" + jsonPath);
				return;
			}
			// Quantise the time to a specific index
			long timeIndex = (time.getMillis() / timeDeltaMillis) * timeDeltaMillis;
			TweetCountWordMap timeWordMap = this.tweetWordMap.get(timeIndex);
//			System.out.println("Tweet time: " + time.getMillis());
			System.out.println("Tweet timeindex: " + timeIndex);
			if (timeWordMap == null) {
				this.tweetWordMap.put(timeIndex,timeWordMap =  new TweetCountWordMap());
			}
			TObjectIntHashMap<String> tpMap = timeWordMap.getTweetWordMap();
			timeWordMap.incrementTweetCount(1);
			List<String> seen = new ArrayList<String>();
			for (String token : tokens) {
				// Apply stop words?
				// Apply junk words?
				// Already seen it?

				if (seen.contains(token))
					continue;
				seen.add(token);
				tpMap.adjustOrPutValue(token, 1, 1);
//				if(token.equals("...")){
//					System.out.println("TOKEN: " + token);
//					System.out.println("TIME: " + timeIndex);
//					System.out.println("NEW VALUE: " + newv);
//				}
			}
		}

		@Override
		protected void cleanup(Mapper<LongWritable, Text, LongWritable, BytesWritable>.Context context) throws IOException, InterruptedException {
			for (Entry<Long, TweetCountWordMap> tpMapEntry : this.tweetWordMap.entrySet()) {
				Long time = tpMapEntry.getKey();
				TweetCountWordMap map = tpMapEntry.getValue();
				ByteArrayOutputStream outarr = new ByteArrayOutputStream();
				IOUtils.writeBinary(outarr, map);
				byte[] arr = outarr.toByteArray();
				BytesWritable toWrite = new BytesWritable(arr);
				context.write(END_TIME, toWrite);
				context.write(new LongWritable(time), toWrite);
			}
		}
	}
	

	/**
	 *  reduce input: 
	 *  	<timePeriod: [<word:#freq,tweets:#freq>,...,<word:#freq,tweets:#freq>]> 
	 *  reduce output:
	 *  	<timePeriod: <<tweet:#freq>,<word:#freq>,<word:#freq>,...>
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 *
	 */
	public static class Reduce extends Reducer<LongWritable, BytesWritable, LongWritable, BytesWritable>{
		
		/**
		 * default construct does nothing
		 */
		public Reduce(){
			
		}
		@Override
		protected void reduce(LongWritable key, Iterable<BytesWritable> values, Reducer<LongWritable, BytesWritable, LongWritable, BytesWritable>.Context context) throws IOException,InterruptedException{
			TweetCountWordMap accum = new TweetCountWordMap();
			for (BytesWritable tweetwordmapbytes : values) {
				TweetCountWordMap tweetwordmap = null;
				tweetwordmap = IOUtils.read(new ByteArrayInputStream(tweetwordmapbytes.getBytes()), TweetCountWordMap.class);
				accum.combine(tweetwordmap);
			}
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			IOUtils.writeBinary(outstream, accum);
			context.write(key, new BytesWritable(outstream.toByteArray()));
		}
	}


	@Override
	public TextLongByteStage stage() {
		TextLongByteStage s = new TextLongByteStage() {
			@Override
			public void setup(Job job) {
				job.getConfiguration().setStrings(CountTweetsInTimeperiod.ARGS_KEY, nonHadoopArgs);
			}
			
			@Override
			public Class<? extends Mapper<LongWritable, Text, LongWritable, BytesWritable>> mapper() {
				return CountTweetsInTimeperiod.Map.class;
			}
			@Override
			public Class<? extends Reducer<LongWritable, BytesWritable, LongWritable, BytesWritable>> reducer() {
				return CountTweetsInTimeperiod.Reduce.class;
			}
			@Override
			public String outname() {
				return TIMECOUNT_DIR;
			}
		};
		return s;
	}
}