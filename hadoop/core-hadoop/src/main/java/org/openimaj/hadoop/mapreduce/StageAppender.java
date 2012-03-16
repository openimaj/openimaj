package org.openimaj.hadoop.mapreduce;


/**
 * Something with the ability to append one or more stages to a MultistagedJob
 * @author ss
 *
 */
public interface StageAppender {
	/**
	 * @param stages what should i add myself to?
	 */
	public void stage(MultiStagedJob stages);
}
