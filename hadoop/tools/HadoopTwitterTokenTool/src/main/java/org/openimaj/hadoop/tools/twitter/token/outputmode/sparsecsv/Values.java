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
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
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
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SimpleSequenceFileTextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeries;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;


public class Values extends StageProvider{
	private String outputPath;
	/**
	 * Assign the output path for the stage
	 * @param outputPath
	 */
	public Values(String outputPath) {
		this.outputPath = outputPath;
	}
	public static final String ARGS_KEY = "INDEX_ARGS";
	/**
	 * Emits each word with the total number of times the word was seen
	 * @author ss
	 *
	 */
	public static class Map extends Mapper<Text,BytesWritable,NullWritable,Text>{
		
		public static String[] options;
		private static HashMap<String, IndependentPair<Long, Long>> wordIndex;
		private static HashMap<Long, IndependentPair<Long, Long>> timeIndex;
		private StringWriter swriter;
		private CSVPrinter writer;

		public Map() {
			// TODO Auto-generated constructor stub
		}
		
		protected static synchronized void loadOptions(Mapper<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException {
			if (options == null) {
				try {
					options = context.getConfiguration().getStrings(ARGS_KEY);
					wordIndex = WordIndex.readWordCountLines(options[0]);
					timeIndex = TimeIndex.readTimeCountLines(options[0]);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

		@Override
		protected void setup(Mapper<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException, InterruptedException {
			loadOptions(context);
			swriter = new StringWriter();
			writer = new CSVPrinter(swriter);
		}

		public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,NullWritable,Text>.Context context){
			try {
				IndependentPair<Long, Long> wordIndexPair = wordIndex.get(key.toString());
				if(wordIndexPair == null) return;
				final long wordI = wordIndexPair.secondObject();
				IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
					@Override
					protected Object readValue(DataInput in) throws IOException {
						WordDFIDF idf = new WordDFIDF();
						idf.readBinary(in);
						long timeI = timeIndex.get(idf.timeperiod).secondObject();
						writer.writeln(new String[]{wordI + "",timeI + "",idf.wf + "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
						return new Object();
					}
				});
				writer.flush();
			} catch (IOException e) {
				System.err.println("Couldnt read timeperiod from word: " + key);
			}
			
		}
		
		public void cleanup(Mapper<Text,BytesWritable,NullWritable,Text>.Context context){
			try {
				context.write(NullWritable.get(), new Text(this.swriter.toString()));
			} catch (Exception e) {
				System.err.println("Couldn't cleanup!");
			}
		}
	}
	/**
	 * Writes each word,count
	 * @author ss
	 *
	 */
	public static class Reduce extends Reducer<NullWritable,Text,NullWritable,Text>{
		public Reduce() {
			// TODO Auto-generated constructor stub
		}
		public void reduce(NullWritable timeslot, Iterable<Text> manylines, Reducer<NullWritable,Text,NullWritable,Text>.Context context){
			try {
				for (Text lines : manylines) {
					context.write(NullWritable.get(), new Text(lines.toString() ));
					return;
				}
				
			} catch (Exception e) {
				System.err.println("Couldn't reduce to final file");
			}
		}
	}
	@Override
	public SimpleSequenceFileTextStage<Text, BytesWritable, NullWritable, Text> stage() {
		return new SimpleSequenceFileTextStage<Text, BytesWritable, NullWritable, Text> () {
			@Override
			public void setup(Job job) {
				job.setNumReduceTasks(1);
				job.getConfiguration().setStrings(Values.ARGS_KEY, new String[]{outputPath.toString()});
			}
			@Override
			public Class<? extends Mapper<Text, BytesWritable, NullWritable, Text>> mapper() {
				return Values.Map.class;
			}
			@Override
			public Class<? extends Reducer<NullWritable,Text,NullWritable,Text>> reducer() {
				return Values.Reduce.class;
			}			
			@Override
			public String outname() {
				return "values";
			}
		};
	}
	/**
	 * Construct a time series per word 
	 * 
	 * @param path
	 * @param timeIndex
	 * @param wordIndex
	 * @return
	 * @throws IOException 
	 */
	public static LinkedHashMap<String, WordDFIDFTimeSeries> readWordDFIDF(String path,LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex,LinkedHashMap<String, IndependentPair<Long, Long>> wordIndex) throws IOException {
		LinkedHashMap<String, WordDFIDFTimeSeries> tsMap = new LinkedHashMap<String, WordDFIDFTimeSeries>();
		
		long[] timeReverseIndex = new long[timeIndex.size()];
		for (Entry<Long, IndependentPair<Long, Long>> l : timeIndex.entrySet()) {
			long lineNum = l.getValue().secondObject();
			timeReverseIndex[(int) lineNum] = l.getKey();
		}
		
		String[] wordReverseIndex = new String[wordIndex.size()];
		for (Entry<String, IndependentPair<Long, Long>> w : wordIndex.entrySet()) {
			long lineNum = w.getValue().secondObject();
			wordReverseIndex[(int) lineNum] = w.getKey();
		}
		String wordPath = path + "/values";
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead));
		CSVParser csvreader = new CSVParser(reader);
		long lineN = 0;
		String[] next = null;
		
		
		while((next = csvreader.getLine())!=null && next.length > 0){
//			writer.writeln(new String[]{wordI + "",timeI + "",idf.wf + "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
			int wordI = Integer.parseInt(next[0]);
			int timeI = Integer.parseInt(next[1]);
			long wf = Long.parseLong(next[2]);
			long tf = Long.parseLong(next[3]);
			long Twf = Long.parseLong(next[4]);
			long Ttf = Long.parseLong(next[5]);
			long time = timeReverseIndex[timeI];
			WordDFIDF wordDFIDF = new WordDFIDF(time, wf, tf, Twf, Ttf);
			String word = wordReverseIndex[wordI];
			WordDFIDFTimeSeries current = tsMap.get(word);
			if(current == null){
				tsMap.put(word, current = new WordDFIDFTimeSeries());
			}
			current.add(time, wordDFIDF);
			lineN ++;
		}
		
		return tsMap;
	}
}
