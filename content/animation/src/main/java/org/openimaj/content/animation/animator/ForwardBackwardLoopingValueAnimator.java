package org.openimaj.content.animation.animator;

/**
 * A {@link ReversableValueAnimator} that can wrap another
 * {@link ReversableValueAnimator} to produce back and forth 
 * looping behavior.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type produce by animator
 */
public class ForwardBackwardLoopingValueAnimator<T> implements ReversableValueAnimator<T> {
	ReversableValueAnimator<T> animator;
	
	/**
	 * Construct around the given {@link ReversableValueAnimator} to
	 * provide forward/backward looping behavior.
	 * @param animator animator to loop
	 */
	public ForwardBackwardLoopingValueAnimator(ReversableValueAnimator<T> animator) {
		this.animator = animator;
	}
	
	@Override
	public T nextValue() {
		if (animator.hasFinished())
			animator = animator.reverseAnimator();

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

	@Override
	public ReversableValueAnimator<T> reverseAnimator() {
		return animator.reverseAnimator();
	}
	
	/**
	 * Construct a new {@link ForwardBackwardLoopingValueAnimator} from a {@link ReversableValueAnimator}.
	 * 
	 * @param <T> Type produce by animator
	 * @param animator animator to loop
	 * @return new {@link ForwardBackwardLoopingValueAnimator}
	 */
	public static <T> ForwardBackwardLoopingValueAnimator<T> loop(ReversableValueAnimator<T> animator) {
		return new ForwardBackwardLoopingValueAnimator<T>(animator);
	}
}
