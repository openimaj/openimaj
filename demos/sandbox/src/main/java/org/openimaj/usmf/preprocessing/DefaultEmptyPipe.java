package org.openimaj.usmf.preprocessing;

import java.util.ArrayList;

/**
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 *         This is an instantiable extension of PipeSection used to connect a
 *         flow of internal PipeSections
 * 
 * @param <I>
 *            = input class type
 * @param <O>
 *            = output class type
 * 
 */
public class DefaultEmptyPipe<I, O> extends PipeSection<I, O> {

	/**
	 * Only the ArrrayList constructor is made available from the super class to
	 * force populating with internal Pipes. Throws Exception if the internal
	 * pipes types do not match end to end.
	 * 
	 * @param inClass
	 * @param outClass
	 * @param innerPipes
	 * @throws PipeSectionJoinException
	 */
	public DefaultEmptyPipe(Class<I> inClass, Class<O> outClass,
			ArrayList<PipeSection<?, ?>> innerPipes)
			throws PipeSectionJoinException {
		super(inClass, outClass, innerPipes);
	}

	@Override
	protected O doWork(I job) {
		return null;
	};

}
