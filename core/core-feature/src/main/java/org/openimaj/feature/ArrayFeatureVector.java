package org.openimaj.feature;


/**
 * Abstract base class for all types of {@link FeatureVector} that are backed by
 * a native array.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <ARRAYTYPE>
 *            Primitive a type of the backing array
 */
public abstract class ArrayFeatureVector<ARRAYTYPE> implements FeatureVector {
	private static final long serialVersionUID = 1L;

	/**
	 * Array of all the values in the feature vector
	 */
	public ARRAYTYPE values;

	/**
	 * Get the underlying representation
	 * 
	 * @return the feature as an array
	 */
	@Override
	public ARRAYTYPE getVector() {
		return values;
	}
}
