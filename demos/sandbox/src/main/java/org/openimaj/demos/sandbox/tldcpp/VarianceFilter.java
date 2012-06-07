package org.openimaj.demos.sandbox.tldcpp;

import org.openimaj.image.FImage;

public class VarianceFilter {

	private IntegralImage integralImg;
	private IntegralImage integralImg_squared;

	public boolean enabled;
	public int[][] windowOffsets;

	public DetectionResult detectionResult;

	public float minVar;

	public VarianceFilter() {
		enabled = true;
		minVar = 0;
		integralImg = null;
		integralImg_squared = null;
	}

	private double calcVariance(int offIndex) {

		double[][] ii1 = integralImg.data;
		double[][] ii2 = integralImg_squared.data;
		int[][] off = this.windowOffsets;
		int[] tlXY = off[offIndex + 0];
		int[] blXY = off[offIndex + 1];
		int[] trXY = off[offIndex + 2];
		int[] brXY = off[offIndex + 3];
		int[] area = off[offIndex + 5];
		double mX  = (ii1[brXY[1]][brXY[0]] - ii1[trXY[1]][trXY[0]] - ii1[blXY[1]][blXY[0]] + ii1[tlXY[1]][tlXY[0]]) / (float) area[0]; //Sum of Area divided by area
		double mX2 = (
			ii2[brXY[1]][brXY[0]] - 
			ii2[trXY[1]][trXY[0]] - 
			ii2[blXY[1]][blXY[0]] + 
			ii2[tlXY[1]][tlXY[0]]
		) / (float) area[0];
		return mX2 - mX*mX;
	}

	public void nextIteration(FImage img) {
		if(!enabled) return;
		integralImg = new IntegralImage();
		integralImg.calcIntImg(img,false);

		integralImg_squared = new IntegralImage();
		integralImg_squared.calcIntImg(img, true);
	}

	

	public boolean filter(int i) {
		if(!enabled) return true;

		float bboxvar = (float) calcVariance(DetectorCascade.TLD_WINDOW_OFFSET_SIZE*i);

		detectionResult.variances[i] = bboxvar;

		if(bboxvar < minVar) {
			return false;
		}

		return true;
	}
}
