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
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

public class WordIndex extends StageAppender {

	/**
	 * Emits each word with the total number of times the word was seen
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Map extends Mapper<Text, BytesWritable, Text, LongWritable> {
		private int wordTimeCountThresh;

		public Map() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, LongWritable>.Context context) throws IOException,
				InterruptedException
		{
			this.wordTimeCountThresh = context.getConfiguration().getInt(WORDCOUNT_TIMETHRESH, 0);
		};

		@Override
		public void map(final Text key, BytesWritable value,
				final Mapper<Text, BytesWritable, Text, LongWritable>.Context context) throws InterruptedException
		{
			try {
				final long[] largest = new long[] { 0 };
				final boolean[] anyDayOverLimit = new boolean[] { false };
				IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()) {
					@Override
					protected Object readValue(DataInput in) throws IOException {
						final WordDFIDF idf = new WordDFIDF();
						idf.readBinary(in);
						if (idf.wf > wordTimeCountThresh) {
							anyDayOverLimit[0] = true;
						}
						if (largest[0] < idf.Twf)
							largest[0] = idf.Twf;

						return new Object();
					}
				});
				if (anyDayOverLimit[0]) // One of the days was over the day
										// limit
					context.write(key, new LongWritable(largest[0]));

			} catch (final IOException e) {
				System.err.println("Couldnt read word: " + key);
			}

		}
	}

	/**
	 * Writes each word,count
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Reduce extends Reducer<Text, LongWritable, LongWritable, Text> {
		private int wordCountThresh;

		public Reduce() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void setup(Reducer<Text, LongWritable, LongWritable, Text>.Context context) throws IOException,
				InterruptedException
		{
			this.wordCountThresh = context.getConfiguration().getInt(WORDCOUNT_THRESH, 0);
		};

		@Override
		public void reduce(Text word, Iterable<LongWritable> counts,
				final Reducer<Text, LongWritable, LongWritable, Text>.Context context) throws IOException,
				InterruptedException
		{
			long countL = 0;
			for (final LongWritable count : counts) {
				countL += count.get();
			}
			if (countL < this.wordCountThresh)
				return;
			final StringWriter swriter = new StringWriter();
			final CSVPrinter writer = new CSVPrinter(swriter);
			writer.write(new String[] { word.toString(), countL + "" });
			writer.flush();
			context.write(new LongWritable(countL), new Text(swriter.toString()));
		}
	}

	protected static final String WORDCOUNT_THRESH = "org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.wordcountthresh";
	protected static final String WORDCOUNT_TOPN = "org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.wordcounttopn";
	protected static final String WORDCOUNT_TIMETHRESH = "org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.wordtimecountthresh";
	private int wordCountThreshold;
	private int topNWords;
	private int wordTimeThreshold;

	public WordIndex(int wordCountThreshold, int topNWords) {
		this.wordCountThreshold = wordCountThreshold;
		this.topNWords = topNWords;
	}

	public WordIndex(int wordCountThreshold, int wordTimeThreshold, int topNWords) {
		this.wordCountThreshold = wordCountThreshold;
		this.topNWords = topNWords;
		this.wordTimeThreshold = wordTimeThreshold;
	}

	public WordIndex() {
		this.wordCountThreshold = 0;
		this.topNWords = -1;
	}

	/**
	 * @param path
	 * @return map of words to counts and index
	 * @throws IOException
	 */
	public static LinkedHashMap<String, IndependentPair<Long, Long>> readWordCountLines(String path) throws IOException {
		return readWordCountLines(path, "/words");
	}

	/**
	 * from a report output path get the words
	 * 
	 * @param path
	 *            report output path
	 * @param ext
	 *            where the words are in the path
	 * @return map of words to counts and index
	 * @throws IOException
	 */
	public static LinkedHashMap<String, IndependentPair<Long, Long>> readWordCountLines(String path, String ext)
			throws IOException
	{
		final String wordPath = path + ext;
		final Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		final FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		final FSDataInputStream toRead = fs.open(p);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(toRead, "UTF-8"));
		final CSVParser csvreader = new CSVParser(reader);
		long lineN = 0;
		String[] next = null;
		final LinkedHashMap<String, IndependentPair<Long, Long>> toRet = new LinkedHashMap<String, IndependentPair<Long, Long>>();
		while ((next = csvreader.getLine()) != null && next.length > 0) {
			if (next.length != 2) {
				System.out.println("PROBLEM READLINE LINE: " + Arrays.toString(next));
				continue;
			}
			toRet.put(next[0], IndependentPair.pair(Long.parseLong(next[1]), lineN));
			lineN++;
		}
		return toRet;
	}

	@Override
	public void stage(MultiStagedJob mjob) {
		mjob.removeIntermediate(true);
		final SequenceFileStage<Text, BytesWritable, Text, LongWritable, LongWritable, Text> collateWords = new SequenceFileStage<Text, BytesWritable, Text, LongWritable, LongWritable, Text>()
		{
			@Override
			public void setup(Job job) {
				job.getConfiguration().setInt(WORDCOUNT_THRESH, wordCountThreshold);
				job.getConfiguration().setInt(WORDCOUNT_TIMETHRESH, wordTimeThreshold);
				job.setNumReduceTasks(1);
			}

			@Override
			public Class<? extends Mapper<Text, BytesWritable, Text, LongWritable>> mapper() {
				return WordIndex.Map.class;
			}

			@Override
			public Class<? extends Reducer<Text, LongWritable, LongWritable, Text>> reducer() {
				return WordIndex.Reduce.class;
			}

			@Override
			public String outname() {
				return "words-collated";
			}
		};

		final SequenceFileTextStage<LongWritable, Text, LongWritable, Text, NullWritable, Text> sortedWords = new SequenceFileTextStage<LongWritable, Text, LongWritable, Text, NullWritable, Text>()
		{
			@Override
			public void setup(Job job) {
				job.getConfiguration().setInt(WORDCOUNT_TOPN, topNWords);
				job.setSortComparatorClass(LongWritable.DecreasingComparator.class);
				job.setNumReduceTasks(1);
			}

			@Override
			public Class<? extends Reducer<LongWritable, Text, NullWritable, Text>> reducer() {
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

	public static void main(String[] args) throws IOException {
		final LinkedHashMap<String, IndependentPair<Long, Long>> wi = WordIndex
				.readWordCountLines("/Users/ss/Development/data/trendminer/sheffield/2010/09/tweets.2010-09-01.sparsecsv");
		System.out.println("Number of words index: " + wi.size());
		for (final Entry<String, IndependentPair<Long, Long>> e : wi.entrySet()) {
			if (e.getValue() == null) {
				System.out.println(e.getKey() + " was null!");
			}
		}
		System.out.println(wi.get("!"));
	}

	/**
	 * Write a CSV wordIndex to a {@link MLCell} writen to a .mat data file
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void writeToMatlab(String path) throws IOException {
		final Path wordMatPath = new Path(path + "/words/wordIndex.mat");
		final FileSystem fs = HadoopToolsUtil.getFileSystem(wordMatPath);
		final LinkedHashMap<String, IndependentPair<Long, Long>> wordIndex = readWordCountLines(path);
		final MLCell wordCell = new MLCell("words", new int[] { wordIndex.size(), 2 });

		System.out.println("... reading words");
		for (final Entry<String, IndependentPair<Long, Long>> ent : wordIndex.entrySet()) {
			final String word = ent.getKey();
			final int wordCellIndex = (int) (long) ent.getValue().secondObject();
			final long count = ent.getValue().firstObject();
			wordCell.set(new MLChar(null, word), wordCellIndex, 0);
			wordCell.set(new MLDouble(null, new double[][] { new double[] { count } }), wordCellIndex, 1);
		}
		final ArrayList<MLArray> list = new ArrayList<MLArray>();
		list.add(wordCell);
		new MatFileWriter(Channels.newChannel(fs.create(wordMatPath)), list);
	}

}
