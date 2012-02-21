package org.openimaj.content.animation.animator;

public interface ReversableValueAnimator<T> extends ValueAnimator<T> {
	public ValueAnimator<T> reverseAnimator();
}
