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
package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.mapreduce.stage.helper.SequenceFileTextStage;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CorrelateWordTimeSeries extends SequenceFileTextStage<Text, BytesWritable, NullWritable, Text, NullWritable, Text>{
	/**
	 * the directory this stage will output
	 */
	public static final String CORRELATE_WORDTIME = "correlate_wordtime";
	public static final String PERIOD_START = "org.openimaj.hadoop.tools.twitter.time.startperiod";
	public static final String PERIOD_END = "org.openimaj.hadoop.tools.twitter.time.endperiod";
	public static final String FINANCE_DATA = "org.openimaj.hadoop.tools.twitter.finance.data";
	private String finance;
	private long start;
	private long end;

	public CorrelateWordTimeSeries(String financelocation,IndependentPair<Long, Long> startend) {
		this.finance = financelocation;
		this.start = startend.firstObject();
		this.end = startend.secondObject();
	}
	
	@Override
	public void setup(org.apache.hadoop.mapreduce.Job job) {
		job.getConfiguration().setLong(PERIOD_START, start);
		job.getConfiguration().setLong(PERIOD_END, end);
		job.getConfiguration().setStrings(FINANCE_DATA, finance);
		job.setNumReduceTasks(1);
	};
	
	@Override
	public String outname() {
		return CORRELATE_WORDTIME;
	}
	
	@Override
	public Class<? extends Mapper<Text, BytesWritable, NullWritable, Text>> mapper() {
		return WordTimeperiodValueMapper.class;
	}
	
//	@Override
//	public Class<? extends Reducer<Text, BytesWritable, NullWritable, Text>> reducer() {
//		return WordValueCorrelationReducer.class;
//	}
}
