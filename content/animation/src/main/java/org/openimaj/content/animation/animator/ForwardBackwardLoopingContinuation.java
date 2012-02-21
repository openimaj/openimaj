package org.openimaj.content.animation.animator;

public class ForwardBackwardLoopingContinuation<T, VA extends ReversableValueAnimator<T>> implements ValueAnimatorContinuation<T, VA> {

	@Override
	public ValueAnimator<T> nextValueAnimator(VA current) {
		return current.reverseAnimator();
	}

}
