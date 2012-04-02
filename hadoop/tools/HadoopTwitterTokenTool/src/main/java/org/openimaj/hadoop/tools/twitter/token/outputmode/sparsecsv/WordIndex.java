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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.mapreduce.stage.StageAppender;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;


public class WordIndex extends StageAppender {

	/**
	 * Emits each word with the total number of times the word was seen
	 * @author ss
	 *
	 */
	public static class Map extends Mapper<Text,BytesWritable,Text,LongWritable>{
		public Map() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,Text,LongWritable>.Context context){
			try {
				IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
					boolean readmore = true;
					@Override
					protected Object readValue(DataInput in) throws IOException {
						if(readmore){
							WordDFIDF idf = new WordDFIDF();
							readmore = false;
							idf.readBinary(in);
							try {
								context.write(key, new LongWritable(idf.Twf));
							} catch (InterruptedException e) {
								throw new IOException("");
							}
							
						}
						return new Object();
					}
				});
				
			} catch (IOException e) {
				System.err.println("Couldnt read word: " + key);
			}
		}
	}
	/**
	 * Writes each word,count
	 * @author ss
	 *
	 */
	public static class Reduce extends Reducer<Text,LongWritable,LongWritable,Text>{
		public Reduce() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void reduce(Text word, Iterable<LongWritable> counts, final Reducer<Text,LongWritable,LongWritable,Text>.Context context) throws IOException, InterruptedException{
			long countL = 0;
			for (LongWritable count : counts) {
				countL += count.get();
			}
			StringWriter swriter = new StringWriter();
			CSVPrinter writer = new CSVPrinter(swriter);
			writer.write(new String[]{word.toString(),countL + ""});
			writer.flush();
			context.write(new LongWritable(countL), new Text(swriter.toString()));
		}
	}
	
	/**
	 * @param path
	 * @return map of words to counts and index
	 * @throws IOException
	 */
	public static LinkedHashMap<String, IndependentPair<Long, Long>> readWordCountLines(String path) throws IOException {
		return readWordCountLines(path,"/words");
	}
	/**
	 * from a report output path get the words
	 * @param path report output path
	 * @param ext where the words are in the path
	 * @return map of words to counts and index
	 * @throws IOException 
	 */
	public static LinkedHashMap<String, IndependentPair<Long, Long>> readWordCountLines(String path, String ext) throws IOException {
		String wordPath = path + ext;
		Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataInputStream toRead = fs.open(p);
		BufferedReader reader = new BufferedReader(new InputStreamReader(toRead,"UTF-8"));
		CSVParser csvreader = new CSVParser(reader);
		long lineN = 0;
		String[] next = null;
		LinkedHashMap<String, IndependentPair<Long, Long>> toRet = new LinkedHashMap<String, IndependentPair<Long,Long>>();
		while((next = csvreader.getLine())!=null && next.length > 0){
			if(next.length != 2){
				System.out.println("PROBLEM READLINE LINE: " + Arrays.toString(next));
				continue;
			}
			toRet.put(next[0], IndependentPair.pair(Long.parseLong(next[1]), lineN));
			lineN ++;
		}
		return toRet;
	}
	@Override
	public void stage(MultiStagedJob mjob) {
		mjob.removeIntermediate(true);
		SequenceFileStage<Text,BytesWritable, Text, LongWritable, LongWritable,Text> collateWords = new SequenceFileStage<Text,BytesWritable, Text, LongWritable, LongWritable,Text>() {
			@Override
			public void setup(Job job) {
				job.setNumReduceTasks(1);
			}
			@Override
			public Class<? extends Mapper<Text, BytesWritable, Text,LongWritable>> mapper() {
				return WordIndex.Map.class;
			}
			@Override
			public Class<? extends Reducer<Text,LongWritable,LongWritable,Text>> reducer() {
				return WordIndex.Reduce.class;
			}
			
			@Override
			public String outname() {
				return "words-collated";
			}
		};
		
		SequenceFileTextStage<LongWritable, Text, LongWritable, Text, NullWritable, Text> sortedWords = new SequenceFileTextStage<LongWritable, Text, LongWritable, Text, NullWritable, Text>(){
			@Override
			public void setup(Job job) {
				job.setNumReduceTasks(1);
			}
			
			@Override
			public Class<? extends Reducer<LongWritable,Text,NullWritable,Text>> reducer() {
				return WordIndexSort.Reduce.class;
			}
			
			@Override
			public String outname() {
				return "words";
			}
		};
		
		mjob.queueStage(collateWords);
		mjob.queueStage(sortedWords);
	}

}
