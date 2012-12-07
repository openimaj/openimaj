package org.openimaj.hadoop.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;

/**
 * Map-Reduce based tool that merges sequence files
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SequenceFileMerger extends Configured implements Tool {
	@Option(name = "--input", aliases = "-i", usage = "input paths or uris", multiValued = true, required = true)
	private List<String> inputs;

	@Option(name = "--output", aliases = "-o", usage = "output path", required = true)
	private String output;

	@Option(name = "--num-outputs", aliases = "-n", usage = "number of outputs", required = true)
	private int numOutputs;

	/**
	 * Runs the tool
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new SequenceFileMerger(), args);
	}

	@Override
	public int run(String[] args) throws Exception {
		final CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: hadoop jar SequenceFileMerger.jar [options...]");
			parser.printUsage(System.err);
			return 1;
		}

		final List<Path> allPaths = new ArrayList<Path>();
		for (final String p : inputs) {
			allPaths.addAll(Arrays.asList(HadoopToolsUtil.getInputPaths(p)));
		}

		final Job job = TextBytesJobUtil.createJob(allPaths, new Path(output), null, this.getConf());
		job.setJarByClass(this.getClass());
		job.setMapperClass(Mapper.class);
		job.setReducerClass(Reducer.class);
		job.setNumReduceTasks(this.numOutputs);

		job.waitForCompletion(true);

		return 0;
	}
}
