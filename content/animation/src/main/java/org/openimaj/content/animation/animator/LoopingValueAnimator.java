package org.openimaj.content.animation.animator;

/**
 * A {@link ValueAnimator} that can wrap another
 * {@link ValueAnimator} to produce looping behavior by
 * resetting when the animator has finished.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type produce by animator
 */
public class LoopingValueAnimator<T> implements ValueAnimator<T> {
	ValueAnimator<T> animator;
	
	/**
	 * Construct around the given {@link ValueAnimator} to
	 * provide looping behavior.
	 * @param animator animator to loop
	 */
	public LoopingValueAnimator(ValueAnimator<T> animator) {
		this.animator = animator;
	}
	
	@Override
	public T nextValue() {
		if (animator.hasFinished())
			animator.reset();

		return animator.nextValue();
	}

	@Override
	public boolean hasFinished() {
		return false;
	}

	@Override
	public void reset() {
		animator.reset();
	}
	
	/**
	 * Construct a new {@link LoopingValueAnimator} from a {@link ValueAnimator}.
	 * 
	 * @param <T> Type produce by animator
	 * @param animator animator to loop
	 * @return new {@link LoopingValueAnimator}
	 */
	public static <T> LoopingValueAnimator<T> loop(ValueAnimator<T> animator) {
		return new LoopingValueAnimator<T>(animator);
	}
}
