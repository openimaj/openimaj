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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.util.ReflectionUtils;
import org.joda.time.DateTime;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.CountTweetsInTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.CountWordsAcrossTimeperiod;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.TimeIndex;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.finance.YahooFinanceData;
import org.openimaj.util.pair.IndependentPair;

public class CorrelationOutputMode extends TwitterTokenOutputMode {


	@Override
	public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
		// Get time period
		Path[] paths = HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountTweetsInTimeperiod.TIMECOUNT_DIR);
		IndependentPair<Long, Long> startend = readBegginingEndTime(paths,opts);
		// Get yahoo finance data for this time period
		YahooFinanceData finance = new YahooFinanceData("AAPL", new DateTime(startend.firstObject()), new DateTime(startend.secondObject()));
		Map<String, double[]> timeperiodFinance = finance.results();
		String financeOut = outputPath + "/financedata";
		Path p = HadoopToolsUtil.getOutputPath(financeOut);
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		FSDataOutputStream os = fs.create(p);
		IOUtils.writeASCII(os, finance);
		// Correlate words with this time period's finance data		
		MultiStagedJob stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts) , CountWordsAcrossTimeperiod.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new CorrelateWordTimeSeries(financeOut,startend));
		stages.runAll();
	}

	private IndependentPair<Long, Long> readBegginingEndTime(Path[] paths, HadoopTwitterTokenToolOptions opts) throws Exception {
		MultiStagedJob stages = new MultiStagedJob(
				paths,
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		stages.queueStage(new TimeIndex().stage());
		stages.runAll();
		LinkedHashMap<Long, IndependentPair<Long, Long>> tindex = TimeIndex.readTimeCountLines(outputPath);
		Iterator<Long> ks = tindex.keySet().iterator();
		long first = ks.next();
		long last = first;
		for (; ks.hasNext();) {
			last=ks.next();
		}
		return IndependentPair.pair(first, last);
	}

	private Reader createReader(Path p) throws IOException {
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		return new Reader(fs, p, new Configuration()); 
	}

}
