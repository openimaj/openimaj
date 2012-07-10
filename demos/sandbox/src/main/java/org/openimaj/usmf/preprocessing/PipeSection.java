package org.openimaj.usmf.preprocessing;

import java.util.ArrayList;

/**
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 *         PipeSection is an abstract class that is intended to make the
 *         construction of a sequence of complex tasks on a particular object
 *         more convenient.
 * 
 *         It should be extended either as a Section that performs one task, or
 *         an empty pipe that takes an Array of internal Pipes. DefaultEmptyPipe
 *         is an extending class that will take an ArrayList of PipeSections.
 * 
 *         For task oriented PipeSections, the doWork() method is where the task
 *         should be encapsulated.
 * 
 * @param <I>
 *            = input class type
 * @param <O>
 *            = output class type
 */
public abstract class PipeSection<I, O> {

	final Class<I> paramClassIn;
	final Class<O> paramClassOut;
	protected boolean singlePipe = true;
	protected ArrayList<PipeSection<?, ?>> innerPipes;

	/**
	 * Constructor for Task PipeSections
	 * 
	 * @param inClass
	 * @param outClass
	 */
	public PipeSection(Class<I> inClass, Class<O> outClass) {
		paramClassIn = inClass;
		paramClassOut = outClass;
	}

	/**
	 * Constructor for containing PipeSections with no task.
	 * 
	 * @param inClass
	 * @param outClass
	 * @param innerPipes
	 * @throws PipeSectionJoinException
	 */
	public PipeSection(Class<I> inClass, Class<O> outClass,
			ArrayList<PipeSection<?, ?>> innerPipes)
			throws PipeSectionJoinException {
		paramClassIn = inClass;
		paramClassOut = outClass;
		singlePipe = false;
		this.innerPipes = innerPipes;
		boolean validWorkflow = true;
		// Check that a workflow has been given
		if (innerPipes == null || innerPipes.size() == 0)
			validWorkflow = false;
		// check that the in and out classes of each section match up
		else {
			if (innerPipes.size() == 1) {
				PipeSection<?, ?> s = innerPipes.get(0);
				if (!(s.paramClassIn.equals(this.paramClassIn) && s.paramClassOut
						.equals(this.paramClassOut))) {
					validWorkflow = false;
				}
			} else if (innerPipes.size() == 2) {
				PipeSection<?, ?> one = innerPipes.get(0);
				PipeSection<?, ?> two = innerPipes.get(1);
				if (!one.paramClassIn.equals(this.paramClassIn)
						|| !one.paramClassOut.equals(two.paramClassIn)
						|| !two.paramClassOut.equals(this.paramClassOut)) {
					validWorkflow = false;
				}
			} else if (innerPipes.size() > 2) {
				Class<?> lastOut = null;
				for (int i = 0; i < innerPipes.size(); i++) {
					PipeSection<?, ?> s = innerPipes.get(i);
					if (i == 0) {
						if (!s.paramClassIn.equals(this.paramClassIn)) {
							validWorkflow = false;
							break;
						} else
							lastOut = s.paramClassOut;
					} else if (i == innerPipes.size()) {
						if (!s.paramClassIn.equals(lastOut)
								|| !s.paramClassOut.equals(this.paramClassOut)) {
							validWorkflow = false;
							break;
						}
					} else if (i != 0 && i != innerPipes.size()) {
						if (!s.paramClassIn.equals(lastOut)) {
							validWorkflow = false;
							break;
						} else
							lastOut = s.paramClassOut;
					}
				}
			}
		}
		// if the ins and outs did not match, complain
		if (!validWorkflow)
			throw new PipeSectionJoinException();
	}

	/**
	 * Publicly available method that executes the task on the job to produce
	 * the result. Warnings are suppressed for paramatised object types, as
	 * these are checked to match in the constructor
	 * 
	 * @param job
	 * @return processed job
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public O pipe(I job) {
		Object result = null;
		if (singlePipe) {
			this.doWork(job);
		} else {
			for (PipeSection p : innerPipes) {
				if (result == null) {
					result = p.pipe(job);
				} else {
					result = p.pipe(p.paramClassIn.cast(result));
				}
			}
		}
		return (O) result;
	}

	/**
	 * Use this to implement the task of this PipeSection
	 * @param job
	 * @return
	 */
	protected abstract O doWork(I job);

}
