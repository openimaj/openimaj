package org.openimaj.math.model;

/**
 * The Model interface defines a mathematical model which links dependent and
 * independent variables. The model can be used to create predictions of the
 * dependent variables given the independent ones.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <I>
 *            type of independent data
 * @param <D>
 *            type of dependent data
 */
public interface Model<I, D> extends Cloneable {

	/**
	 * Uses the model to predict dependent data from an independent value.
	 * 
	 * @param data
	 *            the data (independent variable)
	 * @return Dependent variable(s) predicted from the independent ones.
	 */
	public abstract D predict(I data);

	/**
	 * Clone the model
	 * 
	 * @return a cloned copy
	 */
	public Model<I, D> clone();
}
