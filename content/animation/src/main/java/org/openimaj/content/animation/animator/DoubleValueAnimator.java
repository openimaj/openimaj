package org.openimaj.content.animation.animator;

public class DoubleValueAnimator extends AbstractValueAnimator<Double> implements ReversableValueAnimator<Double> {
	double min;
	double max;
	double incr;
	double current;
	
	public DoubleValueAnimator(double min, double max, double incr) {
		super(min);
		
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
		return next < Math.min(min, max) || next > Math.max(max, min);
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
