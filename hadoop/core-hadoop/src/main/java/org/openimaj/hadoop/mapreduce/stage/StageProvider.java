package org.openimaj.hadoop.mapreduce.stage;


/**
 * Something with the ability of providing a stage in a multistaged job
 * @author ss
 *
 */
public abstract class StageProvider {
	
	/**
	 * 
	 */
	public StageProvider() {}

	/**
	 * @return a job stage
	 */
	public abstract Stage<?,?,?,?,?,?,?,?> stage();
}
