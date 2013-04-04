package org.openimaj.data.dataset;

import org.openimaj.util.stream.Stream;

/**
 * A {@link Dataset} backed by a {@link Stream} of data.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <INSTANCE>
 *            The type of instance in the dataset
 */
public interface StreamingDataset<INSTANCE> extends Dataset<INSTANCE>, Stream<INSTANCE> {

}
