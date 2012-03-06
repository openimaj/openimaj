package org.openimaj.hadoop.mapreduce;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;
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
		 * @param conf the job configuration
		 * @return the job to be launched in this stage
		 * @throws Exception 
		 * @throws IOException 
		 */
		public Job stage(Path[] inputs, Path output, Configuration conf) throws Exception;
	}
	private Path outputRoot;
	private boolean removePreliminary;
	private LinkedList<Stage> stages;
	private Path[] initial;
	private String[] toolArgs;

	/**
	 * Start a multistaged job specification. The root path holds the final
	 * and all preliminary steps
	 * 
	 * @param initialInput the initial input given to the first stage
	 * @param root the final output location
	 */
	public MultiStagedJob(Path[] initialInput, Path root, String[] args) {
		this(initialInput, root,false, args);
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
	public MultiStagedJob(Path[] initialInput, Path root, boolean removePreliminary, String args[]) {
		this.outputRoot = root;
		this.initial = initialInput;
		this.removePreliminary = removePreliminary;
		this.stages = new LinkedList<Stage>();
		this.toolArgs = args;
	}
	
	/**
	 * Conveniance function. Finds the input paths using #SequenceFileUtility.getFilePaths 
	 * and uses Path(outpath)
	 * @param inpath
	 * @param outpath
	 * @throws IOException
	 */
	public MultiStagedJob(String inpath, String outpath, String[] args) throws IOException {
		this(SequenceFileUtility.getFilePaths(inpath, "path"),new Path(outpath),args);
	}

	/**
	 * Add a stage to the end of the queue of stages.
	 * @param s
	 */
	public void queueStage(Stage s){
		this.stages.offer(s);
	}
	
	private static class InnerToolRunner extends Configured implements Tool{
		
		
		private Stage stage;
		private Path[] inputs;
		private Path outputs;
		public InnerToolRunner(Stage s, Path[] currentInputs,Path constructedOutputPath) {
			this.stage = s;
			this.inputs = currentInputs;
			this.outputs = constructedOutputPath;
		}
		@Override
		public int run(String[] args) throws Exception {
			Job job = stage.stage(inputs, outputs,this.getConf());
			job.waitForCompletion(true);
			return 0;
		}
		
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
			// If the output directory already exists, carry on!
			if(!fileExists(constructedOutputPath.toString())){
				ToolRunner.run(new InnerToolRunner(s,currentInputs, constructedOutputPath ), this.toolArgs);			
			}
			currentInputs = SequenceFileUtility.getFilePaths(constructedOutputPath.toString(), "part");
		}
		return constructedOutputPath;
	}
	
	private static boolean fileExists(String path) throws IOException{
		URI outuri = SequenceFileUtility.convertToURI(path);
		FileSystem fs = getFileSystem(outuri);
		Path p = new Path(outuri.toString());
		return fs.exists(p);
	}
	
	private static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}
	
	private Path constructOutputPath(String outname) {
		String newOutPath = this.outputRoot.toString() + "/" + outname;
		return new Path(newOutPath);
	}
}
