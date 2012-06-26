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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class StageRunner extends Configured implements Tool{
	@Override
	public int run(String[] args) throws Exception {
		args(args);
		Stage<?, ?, ?, ?, ?, ?, ?, ?> thestage = stage();
		Job job = thestage.stage(inputs(), output(),this.getConf());
		if(shouldWait()){
			job.waitForCompletion(true);
			thestage.finished(job);
		}
		else{
			job.submit();
		}
		return 0;
	}
	
	/**
	 * @return Whether this stage runner should wait for the stage to complete
	 */
	public boolean shouldWait() {
		return true;
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