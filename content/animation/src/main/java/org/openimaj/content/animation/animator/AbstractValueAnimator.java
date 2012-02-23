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
	 * Number of times {@link #nextValue()} has been called since construction or last reset
	 */
	private int currentCount = 0;
	
	private int startWait = 0;
	private int stopWait = 0;
	private int completedAt = -1;
	
	/**
	 * Construct with initial value
	 * @param initial initial value
	 * @param startWait amount of time in ticks to wait before starting animation.
	 * @param stopWait amount of time in ticks to wait after finishing animation.
	 */
	public AbstractValueAnimator(T initial, int startWait, int stopWait) {
		currentValue = initial;
		this.startWait = startWait;
		this.stopWait = stopWait;
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
		if (!(currentCount < startWait || hasFinished() || completedAt > 0)) {
			currentValue = makeNextValue();
		}
		
		currentCount++;
		
		return currentValue;
	}
	
	protected abstract T makeNextValue();
	
	protected abstract void resetToInitial();
	
	@Override
	public final void reset() {
		resetToInitial();
		currentCount = 0;
		completedAt = -1;
	}
	
	protected abstract boolean complete();
	
	@Override
	public final boolean hasFinished() {
		boolean comp = complete();
		
		if (!comp)
			return false;
		
		if (completedAt < 0)
			completedAt = currentCount;
		
		if (currentCount - completedAt < stopWait) 
			return false;
		
		return true;
	}
}
