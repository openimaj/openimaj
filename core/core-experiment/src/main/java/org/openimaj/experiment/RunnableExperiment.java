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
	 * 
	 * @param context the context provided by the {@link ExperimentRunner}
	 */
	public void finish(ExperimentContext context);
}
