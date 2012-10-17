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
package org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries;

import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;

/**
 * Given a set of specific words, this a mapper is prepared which emits only the dfidf values of those specific words
 * and a reducer is created which emits a serialised time series for each word.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SpecificWordStageProvider extends StageProvider {

	protected static final String WORD_TIME_SERIES = "org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries.wts";
	public static final String SPECIFIC_WORD = "specificword";
	private List<String> wordtimeseries;

	public SpecificWordStageProvider(List<String> wordtimeseries) {
		this.wordtimeseries = wordtimeseries;
	}

	@Override
	public SequenceFileTextStage<Text, BytesWritable, Text, BytesWritable, NullWritable, Text> stage() {
		SequenceFileTextStage<Text,BytesWritable, Text, BytesWritable, NullWritable,Text> writeSpecificWords = new SequenceFileTextStage<Text,BytesWritable, Text, BytesWritable,NullWritable,Text>() {
			@Override
			public void setup(Job job) {
				job.getConfiguration().setStrings(WORD_TIME_SERIES, wordtimeseries.toArray(new String[wordtimeseries.size()]));
			}
			
			@Override
			public Class<? extends Mapper<Text, BytesWritable, Text, BytesWritable>> mapper() {
				return SpecificWordSelectionMapper.class;
			}
			
			@Override
			public Class<? extends Reducer<Text, BytesWritable, NullWritable, Text>> reducer() {
				return WordDFIDFTimeSeriesReducer.class;
			}
			
			@Override
			public String outname() {
				return SPECIFIC_WORD;
			}
		};
		return writeSpecificWords;
	}

}
