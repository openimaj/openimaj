package org.openimaj.image.processing.face.feature.ltp;

public class GaussianWeighting implements LTPWeighting {
	private float sigma = 3;
	
	public GaussianWeighting() {}
	
	public GaussianWeighting(float sigma) {
		this.sigma= sigma;
	}
	
	@Override
	public float weightDistance(float distance) {
		return (float) Math.exp( -(distance / sigma) * (distance / sigma) / 2);
	}
}
