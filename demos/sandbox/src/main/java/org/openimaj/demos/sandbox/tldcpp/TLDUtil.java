package org.openimaj.demos.sandbox.tldcpp;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.MeanCenter;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

public class TLDUtil {
	
	/**
	 * Resize image to fit in a {@link NormalizedPatch#TLD_PATCH_SIZE} squared image and mean center 
	 * @param img
	 * @return new (image.process is called) image
	 */
	public static FImage tldNormalizeImg(FImage img) {
		int size = NormalizedPatch.TLD_PATCH_SIZE;
		
//		resize(img, result, cvSize(size,size)); //Default is bilinear
		ResizeProcessor resizeProc = new ResizeProcessor(size, size, false);
		FImage result = img.process(resizeProc);
		result.processInline(new MeanCenter());
		return result;
	}
	
	public static FImage tldExtractNormalizedPatch(FImage img, int x, int y, int w, int h) {
		FImage subImage = img.extractROI(x, y, w, h); // tldExtractSubImage(img, Rect(x,y,w,h));
		return tldNormalizeImg(subImage);
	}

	//TODO: Rename
	public static FImage tldExtractNormalizedPatch(FImage img, int[] boundary) {
		return tldExtractNormalizedPatch(img, boundary[0],boundary[1],boundary[2],boundary[3]);
	}

	public static FImage tldExtractNormalizedPatchRect(FImage currImg,Rectangle currBB) {
		return tldExtractNormalizedPatch(currImg,(int)currBB.x,(int) currBB.y,(int)currBB.width,(int)currBB.height);
		
	}

	public static float tldCalcVariance(FImage valueImg) {
		float[][] value = valueImg.pixels;
		float mean = MeanCenter.patchMean(value);
	    float temp = 0;
	    int n = valueImg.width * valueImg.height;
	    for(int y = 0; y < valueImg.height; y++) {
	    	for(int x = 0; x < valueImg.width; x++) {
	    		temp += (value[y][x] - mean) * (value[y][x] - mean); 
	    	}
	    }
	    return temp / n;

	}

	

	/**
	 * {@link #tldOverlapNorm(Rectangle, Rectangle)} called on every window and boundary.
	 * @param windows
	 * @param numWindows
	 * @param boundary
	 * @param overlap
	 */
	public static void tldOverlap(Rectangle[] windows, int numWindows, Rectangle boundary,float[] overlap) {
		for(int i = 0; i < numWindows; i++) {
			overlap[i] = (float) tldOverlapNorm(windows[i],boundary);
		}
	}

	/**
	 * {@link Rectangle#overlapping(Rectangle)} called and normalised by:
	 * 
	 * @param A
	 * @param B
	 * @return intersect / (areaA + areaB - intersect)
	 */
	public static float tldOverlapNorm(Rectangle A, Rectangle B) {
		Rectangle overlap = A.overlapping(B);
		double intersect = overlap == null ? 0 : overlap.calculateArea();
		double areaA = A.calculateArea();
		double areaB = B.calculateArea();
		
		return (float) (intersect / (areaA + areaB - intersect));
	}
}
