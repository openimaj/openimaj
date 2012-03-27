package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.util.ReflectionUtils;
import org.joda.time.DateTime;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.mapreduce.SingleStagedJob;
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
		IndependentPair<Long, Long> startend = readBegginingEndTime(paths);
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

	private IndependentPair<Long, Long> readBegginingEndTime(Path[] paths) {
		Reader reader = null;
		long first,last;
		first = last = -1;
		try {
			Configuration config = new Configuration();
			reader = createReader(paths[0]);

			LongWritable key = ReflectionUtils.newInstance((Class<LongWritable>) reader.getKeyClass(), config);
			BytesWritable val = ReflectionUtils.newInstance((Class<BytesWritable>)reader.getValueClass(), config);
			while(first==-1){
				reader.next(key);
				first = key.get();
			}
			while (reader.next(key)) {
				last = key.get();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) try { reader.close(); } catch (IOException e1) {}
		}
		return IndependentPair.pair(first, last);
	}

	private Reader createReader(Path p) throws IOException {
		FileSystem fs = HadoopToolsUtil.getFileSystem(p);
		return new Reader(fs, p, new Configuration()); 
	}

}
