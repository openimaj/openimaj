package org.openimaj.demos.sandbox.tldcpp;

import org.openimaj.image.FImage;

public class ForegroundDetector {
	public int fgThreshold;
	public int minBlobSize;
	public FImage bgImg;
	public DetectionResult detectionResult;
	public void release() {
		// TODO Auto-generated method stub
		
	}

	public void nextIteration(FImage img) {
		// TODO Auto-generated method stub
		
	}

	boolean isActive() {
		return bgImg != null;
	}

}
