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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.openimaj.citation.ReferenceListener;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.experiment.agent.ExperimentAgent;
import org.openimaj.experiment.agent.TimeTracker;
import org.openimaj.experiment.annotations.Time;

/**
 * Support for running {@link RunnableExperiment}s and automatically creating
 * and populating the context of the experiments.
 * <p>
 * <b>Usage notes:</b> Much of the collection of data for building the 
 * {@link ExperimentContext} works through dynamic byte code augmentation
 * performed by the {@link ExperimentAgent}. On most JVMs the agent will be
 * loaded dynamically at runtime on the first call to a method on the 
 * {@link ExperimentAgent} class. However, the Java byte code for a class can 
 * only be augmented <b>before</b> the class is loaded, so it is important
 * that the {@link ExperimentRunner} is used before any classes for the experiment
 * are used for the first time. If this is not possible, you can manually 
 * initialise the {@link ExperimentAgent} by calling {@link ExperimentAgent#initialise()}
 * at the earliest possible point in your code (i.e. the first line of a main method).
 * Also, bear in mind that your main class (and its superclasses) will not be passed
 * to the agent for augmentation as they will already be loaded.   
 * <p>
 * <b>Implementation notes:</b> The {@link ExperimentRunner} can only run a
 * single experiment at a time. This is because global static objects and variables
 * must be used to track the state of a running experiment. It is however safe for an
 * experiment to make use of multiple threads for the experiments execution.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ExperimentRunner {
	static {
		try {
			ExperimentAgent.initialise();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ExperimentRunner() {};

	/**
	 * Run an experiment, filling in the context of the experiment as
	 * it runs.
	 * 
	 * @param experiment the experiment to run
	 * @return the experiments context
	 */
	public static synchronized ExperimentContext runExperiment(RunnableExperiment experiment) {
		return InternalRunner.runExperiment(experiment);
	}

	/**
	 * Inner class the does the work. This is used so that the 
	 * class will be transformed and the @Time annotations processed.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	private static class InternalRunner {
		public static synchronized ExperimentContext runExperiment(RunnableExperiment experiment) {
			Set<Reference> oldRefs = ReferenceListener.reset();
			Map<String, SummaryStatistics> oldTimes = TimeTracker.reset();

			ExperimentContext context = new ExperimentContext(experiment);

			runSetup(experiment);
			runPerform(experiment);
			runFinish(experiment, context);

			context.lock();

			ReferenceListener.addReferences(oldRefs);
			TimeTracker.addMissing(oldTimes);

			return context;
		}

		@Time(identifier = "Setup Experiment")
		protected static void runSetup(RunnableExperiment experiment) {
			experiment.setup();
		}

		@Time(identifier = "Perform Experiment")
		protected static void runPerform(RunnableExperiment experiment) {
			experiment.perform();
		}

		@Time(identifier = "Finish Experiment")
		protected static void runFinish(RunnableExperiment experiment, ExperimentContext context) {
			experiment.finish(context);
		}
	}
}
