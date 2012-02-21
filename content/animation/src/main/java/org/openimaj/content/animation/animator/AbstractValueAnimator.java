package org.openimaj.content.animation.animator;

/**
 * Base class for objects capable of "animating" a value;
 * that is providing a new value everytime {@link #nextValue()} is
 * called, subject to some constraints.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public abstract class AbstractValueAnimator<T, VA extends AbstractValueAnimator<T, VA>> implements ValueAnimator<T> {
	private ValueAnimatorContinuation<T, VA> continuation;
	private VA proxyAnimator;
	private T currentValue;
	
	/**
	 * Number of times {@link #nextValue()} has been called
	 */
	protected int currentCount = 0;
	
	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public AbstractValueAnimator() {
		proxyAnimator = (VA) this;
	}
	
	/**
	 * Construct with the given continuation
	 * @param continuation the continuation
	 */
	public AbstractValueAnimator(ValueAnimatorContinuation<T, VA> continuation) {
		this();
		this.continuation = continuation;
	}
	
	/**
	 * Get the next value. If the animator has finished,
	 * the continuation is checked to see if there are
	 * more animators to run. If not, then the last value 
	 * is returned. 
	 * 
	 * @return the next value.
	 */
	@SuppressWarnings("unchecked")
	public T nextValue() {
		if (proxyAnimator.hasFinished() && proxyAnimator.getContinuation() != null) {
			proxyAnimator = (VA) proxyAnimator.getContinuation().nextValueAnimator(proxyAnimator);
		}
		
		if (proxyAnimator.hasFinished()) {
			return currentValue;
		}
		
		currentValue = proxyAnimator.makeNextValue();
		
		currentCount++;
		
		return currentValue;
	}

	@Override
	public ValueAnimatorContinuation<T, VA> getContinuation() {
		return continuation;
	}
}
