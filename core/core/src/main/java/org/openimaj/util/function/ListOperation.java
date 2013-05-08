package org.openimaj.util.function;

import java.util.List;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Apply an operation to a list of items
 *
 * @param <T>
 */
public class ListOperation<T> implements Operation<List<T>> {

	private Operation<T> op;

	/**
	 * The operation to apply to a {@link List} of <T> instances
	 * @param op
	 */
	public ListOperation(Operation<T> op) {
		this.op = op;
	}

	@Override
	public void perform(List<T> object) {
		for (T t : object) {
			this.op.perform(t);
		}
	}

}
