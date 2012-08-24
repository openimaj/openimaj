package org.openimaj.util.sketch;

/**
 * A sketch is a lightweight summary of an object. Sketches might be used to
 * perform fast comparisons between objects, or store summaries of collections
 * without the need to hold all the items in the collection.
 * <p>
 * The {@link Sketcher} interface describes objects capable of producing
 * sketches.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IN>
 *            Type of object being sketched
 * @param <OUT>
 *            Type of produced sketch
 */
public interface Sketcher<IN, OUT> {
	/**
	 * Create a sketch for the given object.
	 * 
	 * @param input
	 *            the object to sketch
	 * @return the sketch of the object.
	 */
	OUT createSketch(IN input);
}
