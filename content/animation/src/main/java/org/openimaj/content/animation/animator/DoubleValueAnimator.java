package org.openimaj.content.animation.animator;

public class DoubleValueAnimator extends AbstractValueAnimator<Double, DoubleValueAnimator> implements ReversableValueAnimator<Double> {
	double min;
	double max;
	double incr;
	double current;
	
	public DoubleValueAnimator(double min, double max, double incr) {
		current = min;
		this.min = min;
		this.max = max;
		this.incr = incr;
	}
	
	public DoubleValueAnimator(double min, double max, double incr, ValueAnimatorContinuation<Double, DoubleValueAnimator> cont) {
		super(cont);
		current = min;
		this.min = min;
		this.max = max;
		this.incr = incr;
	}
	
	@Override
	public Double makeNextValue() {
		current += incr;
		return current;
	}

	@Override
	public boolean hasFinished() {
		double next = current+incr;
		return next < min || next > max;
	}

	@Override
	public void reset() {
		current = min;
	}

	@Override
	public ReversableValueAnimator<Double> reverseAnimator() {
		incr *= -1;
		
		return this;
	}
}
