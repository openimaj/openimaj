package org.openimaj.content.animation.animator;

/**
 * A continuation that loops by resetting the current animator
 * when it finishes.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type
 * @param <VA>
 */
public class LoopingContinuation<T, VA extends ValueAnimator<T>> implements ValueAnimatorContinuation<T, VA> {

	@Override
	public VA nextValueAnimator(VA current) {
		current.reset();
		
		return current;
	}

}
