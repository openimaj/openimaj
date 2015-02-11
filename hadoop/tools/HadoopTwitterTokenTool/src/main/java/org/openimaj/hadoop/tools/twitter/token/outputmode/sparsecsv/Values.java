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
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.hadoop.mapreduce.stage.helper.SimpleSequenceFileTextStage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeries;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;

/**
 * Output the word/time values for each word
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Values extends StageProvider {
	private String outputPath;
	private int valueReduceSplit;
	private boolean sortValueByTime;
	private boolean matlabOutput;

	/**
	 * Assign the output path for the stage
	 *
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
	public SequenceFileTextStage<?, ?, ?, ?, ?, ?> stage() {
		if (this.sortValueByTime) {
			return new SequenceFileTextStage<Text, BytesWritable, LongWritable, BytesWritable, NullWritable, Text>() {
				@Override
				public void setup(Job job) {
					job.setNumReduceTasks(valueReduceSplit);
					job.getConfiguration().setStrings(Values.ARGS_KEY, new String[] { outputPath.toString() });
					job.getConfiguration().setBoolean(MATLAB_OUT, matlabOutput);
				}

				@Override
				public Class<? extends Mapper<Text, BytesWritable, LongWritable, BytesWritable>> mapper() {
					return MapValuesByTime.class;
				}

				@Override
				public Class<? extends Reducer<LongWritable, BytesWritable, NullWritable, Text>> reducer() {
					return ReduceValuesByTime.class;
				}

				@Override
				public String outname() {
					return "values";
				}

				@Override
				public void finished(Job job) {
					if (matlabOutput) {
						try {
							WordIndex.writeToMatlab(outputPath.toString());
							TimeIndex.writeToMatlab(outputPath.toString());
							System.out.println("Done writing the word and time index files to matlab");
						} catch (final IOException e) {
							System.out.println("Failed to write the word and time index files");
						}
					}
				}
			};
		}
		else {
			return new SimpleSequenceFileTextStage<Text, BytesWritable, NullWritable, Text>() {
				@Override
				public void setup(Job job) {
					job.setNumReduceTasks(valueReduceSplit);
					job.getConfiguration().setStrings(Values.ARGS_KEY, new String[] { outputPath.toString() });
				}

				@Override
				public Class<? extends Mapper<Text, BytesWritable, NullWritable, Text>> mapper() {
					return MapValuesByWord.class;
				}

				@Override
				public Class<? extends Reducer<NullWritable, Text, NullWritable, Text>> reducer() {
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
	 * @return hashmap containing a {@link WordDFIDFTimeSeries} instance per
	 *         word
	 * @throws IOException
	 */
	public static LinkedHashMap<String, WordDFIDFTimeSeries> readWordDFIDF(String path,
			LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex,
			LinkedHashMap<String, IndependentPair<Long, Long>> wordIndex) throws IOException
			{
		final LinkedHashMap<String, WordDFIDFTimeSeries> tsMap = new LinkedHashMap<String, WordDFIDFTimeSeries>();

		final long[] timeReverseIndex = new long[timeIndex.size()];
		for (final Entry<Long, IndependentPair<Long, Long>> l : timeIndex.entrySet()) {
			final long lineNum = l.getValue().secondObject();
			timeReverseIndex[(int) lineNum] = l.getKey();
		}

		final String[] wordReverseIndex = new String[wordIndex.size()];
		for (final Entry<String, IndependentPair<Long, Long>> w : wordIndex.entrySet()) {
			final long lineNum = w.getValue().secondObject();
			wordReverseIndex[(int) lineNum] = w.getKey();
		}
		final String wordPath = path + "/values";
		final Path p = HadoopToolsUtil.getInputPaths(wordPath)[0];
		final FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		final FSDataInputStream toRead = fs.open(p);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(toRead));
		final CSVParser csvreader = new CSVParser(reader);
		String[] next = null;

		while ((next = csvreader.getLine()) != null && next.length > 0) {
			// writer.writeln(new String[]{wordI + "",timeI + "",idf.wf +
			// "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
			final int wordI = Integer.parseInt(next[0]);
			final int timeI = Integer.parseInt(next[1]);
			final long wf = Long.parseLong(next[2]);
			final long tf = Long.parseLong(next[3]);
			final long Twf = Long.parseLong(next[4]);
			final long Ttf = Long.parseLong(next[5]);
			final long time = timeReverseIndex[timeI];
			final WordDFIDF wordDFIDF = new WordDFIDF(time, wf, tf, Twf, Ttf);
			final String word = wordReverseIndex[wordI];
			WordDFIDFTimeSeries current = tsMap.get(word);
			if (current == null) {
				tsMap.put(word, current = new WordDFIDFTimeSeries());
			}
			current.add(time, wordDFIDF);
		}

		return tsMap;
			}
}
