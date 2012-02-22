package org.openimaj.content.animation.animator;

/**
 * Base class for objects capable of "animating" a value;
 * that is providing a new value everytime {@link #nextValue()} is
 * called, subject to some constraints.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type of value produced 
 */
public abstract class AbstractValueAnimator<T> implements ValueAnimator<T> {
	private T currentValue;
	
	/**
	 * Number of times {@link #nextValue()} has been called
	 */
	protected int currentCount = 0;
	
	/**
	 * Construct with initial value
	 * @param initial initial value
	 */
	public AbstractValueAnimator(T initial) {
		currentValue = initial;
	}
	
	/**
	 * Get the next value. If the animator has finished,
	 * the continuation is checked to see if there are
	 * more animators to run. If not, then the last value 
	 * is returned. 
	 * 
	 * @return the next value.
	 */
	@Override
	public T nextValue() {
		if (hasFinished()) {
			return currentValue;
		}
		
		currentValue = makeNextValue();
		
		currentCount++;
		
		return currentValue;
	}
	
	protected abstract T makeNextValue();
}
