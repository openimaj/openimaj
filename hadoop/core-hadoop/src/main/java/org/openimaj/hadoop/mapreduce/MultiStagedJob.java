package org.openimaj.hadoop.mapreduce;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;

/**
 * A set of hadoop jobs done in series. The final output (directory) of the nth job is used
 * as the map input path of the (n+1)th job.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class MultiStagedJob {
	/**
	 * A stage in a multi step job. Each step is told where the jobs data will come from, where the output
	 * should be directed and then is expected to produce a stage.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 *
	 */
	public static interface Stage{
		/**
		 * @return the name of the output directory of this stage
		 */
		public String outname();
		/**
		 * @param inputs the input paths to be expected
		 * @param output the output location
		 * @return the job to be launched in this stage
		 * @throws Exception 
		 * @throws IOException 
		 */
		public Job stage(Path[] inputs, Path output) throws Exception;
	}
	private Path outputRoot;
	private boolean removePreliminary;
	private LinkedList<Stage> stages;
	private Path[] initial;

	/**
	 * Start a multistaged job specification. The root path holds the final
	 * and all preliminary steps
	 * 
	 * @param initialInput the initial input given to the first stage
	 * @param root the final output location
	 */
	public MultiStagedJob(Path[] initialInput, Path root) {
		this(initialInput, root,false);
	}
	
	/**
	 * 
	 * Start a multistaged job specification. The root path holds the final.
	 * and all preliminary steps
	 * 
	 * @param initialInput the initial input given to the first stage
	 * @param removePreliminary whether all intermediate steps should be removed
	 * @param root the final output location
	 */
	public MultiStagedJob(Path[] initialInput, Path root, boolean removePreliminary) {
		this.outputRoot = root;
		this.initial = initialInput;
		this.removePreliminary = removePreliminary;
		this.stages = new LinkedList<Stage>();
	}
	
	/**
	 * Conveniance function. Finds the input paths using #SequenceFileUtility.getFilePaths 
	 * and uses Path(outpath)
	 * @param inpath
	 * @param outpath
	 * @throws IOException
	 */
	public MultiStagedJob(String inpath, String outpath) throws IOException {
		this(SequenceFileUtility.getFilePaths(inpath, "path"),new Path(outpath));
	}

	/**
	 * Add a stage to the end of the queue of stages.
	 * @param s
	 */
	public void queueStage(Stage s){
		this.stages.offer(s);
	}
	
	/**
	 * Run all the staged jobs. The input/output of each job is at follows:
	 * initial -> stage1 -> output1
	 * output1 -> stage2 -> output2
	 * ...
	 * output(N-1) -> stageN -> final
	 * 
	 * for each output, the directory created is scanned for part files matching the regex "part.*"
	 * @return The path to the final output for convenience (##base##/final by convention)
	 * @throws Exception 
	 */
	public Path runAll() throws Exception{
		Stage s = null;
		Path[] currentInputs = initial;
		Path constructedOutputPath = null;
		while((s = this.stages.pollFirst()) != null){
			constructedOutputPath = constructOutputPath(s.outname());
			Job currentJob = s.stage(currentInputs, constructedOutputPath );
			currentJob.waitForCompletion(true);
			currentInputs = SequenceFileUtility.getFilePaths(constructedOutputPath.toString(), "part");
		}
		return constructedOutputPath;
	}

	private Path constructOutputPath(String outname) {
		String newOutPath = this.outputRoot.toString() + "/" + outname;
		return new Path(newOutPath);
	}
}
