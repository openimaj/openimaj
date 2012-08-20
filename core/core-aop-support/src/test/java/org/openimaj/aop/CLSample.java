package org.openimaj.aop;

import org.openimaj.aop.classloader.ClassLoaderTransform;

/**
 * Test object
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class CLSample {
	/**
	 * Sample main method that transforms the class if required
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		if (ClassLoaderTransform.run(CLSample.class, args, new SampleTransformer()))
			return;

		final CLSample s = new CLSample();
		System.out.println("here " + s.getClass().getClassLoader());
	}
}
