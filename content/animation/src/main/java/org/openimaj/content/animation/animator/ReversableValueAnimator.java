package org.openimaj.content.animation.animator;

/**
 * Interfaces for {@link ValueAnimator}s that can be reversed.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public interface ReversableValueAnimator<T> extends ValueAnimator<T> {
	/**
	 * Reverse the animator and return it, or make a new animator
	 * with the same state as this animator, but reversed direction.
	 * @return reversed animator.
	 */
	public ReversableValueAnimator<T> reverseAnimator();
}
