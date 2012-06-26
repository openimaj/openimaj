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
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SimpleSequenceFileTextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeries;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;


/**
 * Output the word/time values for each word
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Values extends StageProvider{
	private String outputPath;
	private int valueReduceSplit;
	private boolean sortValueByTime;
	private boolean matlabOutput;
	/**
	 * Assign the output path for the stage
	 * @param outputPath
	 * @param sortValueByTime 
	 * @param matlabOutput 
	 */
	public Values(String outputPath, int valueReduceSplit, boolean sortValueByTime, boolean matlabOutput) {
		this.outputPath = outputPath;
		this.valueReduceSplit = valueReduceSplit;
		this.sortValueByTime = sortValueByTime;
		this.matlabOutput = matlabOutput;
	}
	/**
	 * The index location config option
	 */
	public static final String ARGS_KEY = "INDEX_ARGS";
	public static final String MATLAB_OUT = "org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.matlab_out";
	@Override
	public SequenceFileTextStage<?,?,?, ?,?, ?> stage() {
		if(this.sortValueByTime){
			return new SequenceFileTextStage<Text, BytesWritable, LongWritable, BytesWritable,NullWritable, Text> () {
				@Override
				public void setup(Job job) {
					job.setNumReduceTasks(valueReduceSplit);
					job.getConfiguration().setStrings(Values.ARGS_KEY, new String[]{outputPath.toString()});
					job.getConfiguration().setBoolean(MATLAB_OUT, matlabOutput);
				}
				@Override
				public Class<? extends Mapper<Text, BytesWritable, LongWritable, BytesWritable>> mapper() {
					return MapValuesByTime.class;
				}
				@Override
				public Class<? extends Reducer<LongWritable,BytesWritable,NullWritable,Text>> reducer() {
					return ReduceValuesByTime.class;
				}			
				@Override
				public String outname() {
					return "values";
				}
				
				@Override
				public void finished(Job job) {
					if(matlabOutput){
						try {
							WordIndex.writeToMatlab(outputPath.toString());
							TimeIndex.writeToMatlab(outputPath.toString());
							System.out.println("Done writing the word and time index files to matlab");
						} catch (IOException e) {
							System.out.println("Failed to write the word and time index files");
						}
					}
				}
			};
		}
		else{
			return new SimpleSequenceFileTextStage<Text, BytesWritable, NullWritable, Text> () {
				@Override
				public void setup(Job job) {
					job.setNumReduceTasks(valueReduceSplit);
					job.getConfiguration().setStrings(Values.ARGS_KEY, new String[]{outputPath.toString()});
				}
				@Override
				public Class<? extends Mapper<Text, BytesWritable, NullWritable, Text>> mapper() {
					return MapValuesByWord.class;
				}
				@Override
				public Class<? extends Reducer<NullWritable,Text,NullWritable,Text>> reducer() {
					return ReduceValuesByWord.class;
				}			
				@Override
				public String outname() {
					return "values";
				}
			};
		}
	}
	/**
	 * Construct a time series per word 
	 * 
	 * @param path
	 * @param timeIndex
	 * @param wordIndex
	 * @return hashmap containing a {@link WordDFIDFTimeSeries} instance per word
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
