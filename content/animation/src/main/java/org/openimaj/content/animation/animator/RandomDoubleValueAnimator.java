package org.openimaj.content.animation.animator;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

public class RandomDoubleValueAnimator implements ValueAnimator<Double> {
	private int seed;
	
	Uniform rng;
	DoubleValueAnimator animator;
	double min;
	double max;
	double incr;
	
	public RandomDoubleValueAnimator(double min, double max, double incr) {
		seed = (int) System.currentTimeMillis();

		this.min = min;
		this.max = max;
		this.incr = incr;
		
		reset();
	}
	
	@Override
	public Double nextValue() {
		if (animator.hasFinished()) {
			setNextAnimator(animator.nextValue());
		}

		return animator.nextValue();
	}

	@Override
	public boolean hasFinished() {
		return false;
	}

	@Override
	public void reset() {
		rng = new Uniform(new MersenneTwister(seed));
		
		double v1 = rng.nextDoubleFromTo(min, max);
		setNextAnimator(v1);
	}
	
	protected void setNextAnimator(double v1) {
		double v2 = rng.nextDoubleFromTo(min, max);
		
		if (v1 < v2 && incr < 0) incr *= -1;
		if (v1 > v2 && incr > 0) incr *= -1;
		
		animator = new DoubleValueAnimator(v1, v2, incr);
	}
}
