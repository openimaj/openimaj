package org.openimaj.hadoop.mapreduce;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.openimaj.hadoop.mapreduce.stage.Stage;

/**
 * A StageRunner provides the various components to run an individual stage.
 * StageRunners get given the the arguments of tools and must provide the inputs of jobs,
 * the job output location and the actual stage which will provide the job.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class StageRunner extends Configured implements Tool{
	@Override
	public int run(String[] args) throws Exception {
		args(args);
		Job job = stage().stage(inputs(), output(),this.getConf());
		job.waitForCompletion(true);
		return 0;
	}
	
	/**
	 * @return the stage which should be ran 
	 */
	public abstract Stage<?,?,?,?,?,?,?,?> stage();
	
	/**
	 * @return the output fed to the stage
	 */
	public abstract Path output() ;

	/**
	 * @return the inputs fed to the stage
	 * @throws Exception 
	 */
	public abstract Path[] inputs() throws Exception;

	/**
	 * @param args arguments handed to the {@link Tool#run(String[])}. Given before
	 * outputs, inputs or stages are asked for.
	 * @throws Exception
	 */
	public abstract void args(String[] args) throws Exception;
	
	/**
	 * @param args should be used as a direct proxy for a main method
	 * @throws Exception 
	 */
	public void runMain(String args[]) throws Exception{
		ToolRunner.run(this, args);
	}
	
}