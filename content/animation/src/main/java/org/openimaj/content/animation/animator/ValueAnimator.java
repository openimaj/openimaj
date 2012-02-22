package org.openimaj.content.animation.animator;

/**
 * Interface for objects capable of "animating" a value;
 * that is providing a new value everytime {@link #nextValue()} is
 * called, subject to some constraints.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type of value produced
 */
public interface ValueAnimator<T> {
	/**
	 * Get the next value. If the animator has finished,
	 * the continuation is checked to see if there are
	 * more animators to run. If not, then the last value 
	 * is returned. 
	 * 
	 * @return the next value.
	 */
	public T nextValue();

	/**
	 * Has the animator finished animating the value.
	 * @return true if the animator has finished; false otherwise
	 */
	public boolean hasFinished();
	
	/**
	 * Reset the animator back to its initial condition.
	 */
	public void reset();
}
