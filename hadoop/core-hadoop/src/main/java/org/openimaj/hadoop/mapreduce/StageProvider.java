package org.openimaj.hadoop.mapreduce;

import org.openimaj.hadoop.mapreduce.MultiStagedJob.Stage;

/**
 * Something with the ability of providing a stage in a multistaged job
 * @author ss
 *
 */
public interface StageProvider {
	/**
	 * @return a job stage
	 */
	public Stage stage();
}
