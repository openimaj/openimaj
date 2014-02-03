package org.openimaj.ml.linear.kernel;

import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

/**
 * A function which takes in two T instances and returns a double
 * @param <T>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public interface Kernel<T> extends Function<IndependentPair<T, T>, Double>{

}
