package org.openimaj.content.animation.animator;

public class LoopingContinuation<T, VA extends ValueAnimator<T>> implements ValueAnimatorContinuation<T, VA> {

	@Override
	public ValueAnimator<T> nextValueAnimator(VA current) {
		current.reset();
		
		return current;
	}

}
