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
package org.openimaj.experiment;


/**
 * Interface for classes implementing experiments that can be
 * run with the {@link ExperimentRunner}. Implementors must write
 * three methods corresponding to the lifecycle of an experiment:
 * setting up the experiment, performing the experiment and concluding
 * the experiment (i.e. writing the results, etc).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface RunnableExperiment {
	/**
	 * Setup of the experiment
	 */
	public void setup();
	
	/**
	 * Perform the experiment 
	 */
	public void perform();
	
	/**
	 * Finalise the experiment, possibly writing the results, cleaning up, etc.
	 * The context gathered by the {@link ExperimentRunner} is provided so that
	 * {@link RunnableExperiment} implementations can access information for
	 * generating their reports. 
	 * <p>
	 * Note that at the point the context is provided,
	 * it is not "locked" and is still recording. The {@link ExperimentRunner}
	 * returns a completed locked context after the experiment has been completely
	 * run. 
	 * 
	 * @param context the context provided by the {@link ExperimentRunner}
	 */
	public void finish(ExperimentContext context);
}
