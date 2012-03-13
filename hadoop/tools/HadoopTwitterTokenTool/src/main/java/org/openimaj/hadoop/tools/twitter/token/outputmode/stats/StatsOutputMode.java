package org.openimaj.hadoop.tools.twitter.token.outputmode.stats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.MultiStagedJob;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.DFIDFTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.WordIndex;
import org.openimaj.util.pair.IndependentPair;

public class StatsOutputMode implements TwitterTokenOutputMode {

	private MultiStagedJob stages;

	@Override
	public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode, String outputPath, boolean replace) throws Exception {
HadoopToolsUtil.validateOutput(outputPath,replace);
		
		this.stages = new MultiStagedJob(
				HadoopToolsUtil.getInputPaths(completedMode.finalOutput(opts),DFIDFTokenMode.WORDCOUNT_DIR),
				HadoopToolsUtil.getOutputPath(outputPath),
				opts.getArgs()
		);
		// Three stage process
		// 1a. Write all the words (word per line)
		stages.queueStage(new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output, Configuration conf) throws IOException {
				Job job = new Job(conf);
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(LongWritable.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(WordIndex.Map.class);
				job.setReducerClass(WordIndex.Reduce.class);
				job.setSortComparatorClass(LongWritable.Comparator.class);
				job.setNumReduceTasks(1);
				return job;
			}
			
			@Override
			public String outname() {
				return "words";
			}
		});
		final Path wordIndex = stages.runAll();
		
		HashMap<String, IndependentPair<Long, Long>> wordCountLines = WordIndex.readWordCountLines(wordIndex.toString(),"");
		StatsWordMatch matches = new StatsWordMatch();
		for (Entry<String, IndependentPair<Long, Long>> entry : wordCountLines.entrySet()) {
			String word = entry.getKey();
			IndependentPair<Long, Long> countLine = entry.getValue();
			Long count = countLine.firstObject();	
			matches.updateStats(word, count);
		}
		
		System.out.println(matches);
	}

}
