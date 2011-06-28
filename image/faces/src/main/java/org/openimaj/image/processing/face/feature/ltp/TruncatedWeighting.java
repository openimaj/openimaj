package org.openimaj.image.processing.face.feature.ltp;

public class TruncatedWeighting implements LTPWeighting {
	float threshold = 6;
	
	public TruncatedWeighting() {}
	
	public TruncatedWeighting(float threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public float weightDistance(float distance) {
		return Math.min(distance, threshold);
	}
}
