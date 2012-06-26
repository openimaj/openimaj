package org.openimaj.demos.sandbox.tldcpp.detector;

import org.openimaj.image.FImage;

/**
 * The variance filter forms the first step of the {@link DetectorCascade#detect(FImage)}.
 * For each window in the {@link DetectorCascade} a variance is calculated using {@link IntegralImage} 
 * for speed.
 * 
 * Windows are filtered by whether they contain more variance than minVar which is the variance of 
 * the original bounding box being tracked
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class VarianceFilter {

	private IntegralImage integralImg;
	private IntegralImage integralImg_squared;

	/**
	 * whether the variance filter is enabled
	 */
	public boolean enabled;
	int[][] windowOffsets;

	/**
	 * the results of the variance check are saved to this results instance
	 */
	public DetectionResult detectionResult;

	/**
	 * the minimum variance (variance of the original bounding box)
	 */
	public float minVar;

	/**
	 * enabled with a 0 minvar (most permissive)
	 */
	public VarianceFilter() {
		enabled = true;
		minVar = 0;
		integralImg = null;
		integralImg_squared = null;
	}

	private double calcVariance(int winIndex) {
		int offIndex = DetectorCascade.TLD_WINDOW_OFFSET_SIZE * winIndex;
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

	/**
	 * calculates the integralImage and the integral(image*image) (these are used to calculate variance)
	 * @param img
	 */
	public void nextIteration(FImage img) {
		if(!enabled) return;
		integralImg = new IntegralImage();
		integralImg.calcIntImg(img,false);

		integralImg_squared = new IntegralImage();
		integralImg_squared.calcIntImg(img, true);
	}

	

	/**
	 * @param windowIndex
	 * @return whether the ith window has a variance higher than minvar 
	 */
	public boolean filter(int windowIndex) {
		if(!enabled) return true;

		float bboxvar = (float) calcVariance(windowIndex);

		detectionResult.variances[windowIndex] = bboxvar;

		if(bboxvar < minVar) {
			return false;
		}

		return true;
	}
}
