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
package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.io.IOUtils;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLDouble;


public class TimeIndex extends StageProvider{

	/**
	 * Emits each word with the total number of times the word was seen
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Map extends Mapper<LongWritable,BytesWritable,LongWritable,LongWritable>{
		public Map() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void map(final LongWritable key, BytesWritable value, final Mapper<LongWritable,BytesWritable,LongWritable,LongWritable>.Context context){
			try {
				final TweetCountWordMap periodCountWordCount = IOUtils.read(new ByteArrayInputStream(value.getBytes()), TweetCountWordMap.class);
				if(!key.equals(CountTweetsInTimeperiod.Map.END_TIME)){
					context.write(key, new LongWritable(periodCountWordCount.getNTweets()));
				}
				
			} catch (Exception e) {
				System.err.println("Couldnt read timeperiod: " + key);
			}
		}
	}
	/**
	 * Writes each word,count
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Reduce extends Reducer<LongWritable,LongWritable,NullWritable,Text>{
		public Reduce() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void reduce(LongWritable timeslot, Iterable<LongWritable> counts, Reducer<LongWritable,LongWritable,NullWritable,Text>.Context context){
			try {
				String timeStr = timeslot.toString();
				long total = 0;
				for (LongWritable count : counts) {
					total += count.get();
				}
				StringWriter swriter = new StringWriter();
				CSVPrinter writer = new CSVPrinter(swriter);
				writer.write(new String[]{timeStr,total + ""});
				writer.flush();
				String toWrote = swriter.toString();
				context.write(NullWritable.get(), new Text(toWrote));
				return;
				
			} catch (Exception e) {
				System.err.println("Couldn't reduce to final file");
			}
		}
	}
	
	/**
	 * from a report output path get the words
	 * @param path report output path
	 * @return map of time to an a pair containing <count, lineindex> 
	 * @throws IOException 
	 */
	public static LinkedHashMap<Long, IndependentPair<Long, Long>> readTimeCountLines(String path) throws IOException {
		String wordPath = path + "/times";
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead));
		CSVParser csvreader = new CSVParser(reader);
		long lineN = 0;
		String[] next = null;
		LinkedHashMap<Long, IndependentPair<Long, Long>> toRet = new LinkedHashMap<Long, IndependentPair<Long,Long>>();
		while((next = csvreader.getLine())!=null && next.length > 0){
			toRet.put(Long.parseLong(next[0]), IndependentPair.pair(Long.parseLong(next[1]), lineN));
			lineN ++;
		}
		return toRet;
	}

	@Override
	public SequenceFileTextStage<LongWritable,BytesWritable, LongWritable,LongWritable,NullWritable,Text>stage() {
		return new SequenceFileTextStage<LongWritable,BytesWritable, LongWritable,LongWritable,NullWritable,Text>() {
			
			@Override
			public void setup(Job job) {
				job.setSortComparatorClass(LongWritable.Comparator.class);
				job.setNumReduceTasks(1);
			}
			@Override
			public Class<? extends Mapper<LongWritable, BytesWritable, LongWritable, LongWritable>> mapper() {
				return TimeIndex.Map.class;
			}
			@Override
			public Class<? extends Reducer<LongWritable, LongWritable,NullWritable,Text>> reducer() {
				return TimeIndex.Reduce.class;
			}
			
			@Override
			public String outname() {
				return "times";
			}
		};
	}

	/**
	 * Write a CSV timeIndex to a {@link MLCell} writen to a .mat data file
	 * @param path
	 * @throws IOException
	 */
	public static void writeToMatlab(String path) throws IOException {
		Path timeMatPath = new Path(path + "/times/timeIndex.mat");
		FileSystem fs = HadoopToolsUtil.getFileSystem(timeMatPath);
		LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex = readTimeCountLines(path);
		MLCell timeCell = new MLCell("times",new int[]{timeIndex.size(),2});
		
		System.out.println("... reading times");
		for (Entry<Long, IndependentPair<Long, Long>> ent : timeIndex.entrySet()) {
			long time = (long)ent.getKey();
			int timeCellIndex = (int)(long)ent.getValue().secondObject();
			long count = ent.getValue().firstObject();
			timeCell.set(new MLDouble(null, new double[][]{new double[]{time}}), timeCellIndex,0);
			timeCell.set(new MLDouble(null, new double[][]{new double[]{count}}), timeCellIndex,1);
		}
		ArrayList<MLArray> list = new ArrayList<MLArray>();
		list.add(timeCell);
		new MatFileWriter(Channels.newChannel(fs.create(timeMatPath)),list );
	}

}
