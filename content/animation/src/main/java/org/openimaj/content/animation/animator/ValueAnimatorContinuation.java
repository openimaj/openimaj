package org.openimaj.content.animation.animator;

/**
 * Interface for objects which can modify or produce new
 * {@link ValueAnimator}s to continue after a {@link ValueAnimator}
 * finishes its animation. Possible uses are looping and chaining
 * of {@link ValueAnimator}s.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type of value being animated
 * @param <VA> Type of animator
 */
public interface ValueAnimatorContinuation<T, VA extends ValueAnimator<T>> {
	/**
	 * Called once a ValueAnimator has finished in order
	 * to get the next animator if there is one.
	 * @param proxyAnimator the animator that has just finished
	 * @return the new animator
	 */
	ValueAnimator<T> nextValueAnimator(VA proxyAnimator);
}
