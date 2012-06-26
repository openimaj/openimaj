package org.openimaj.demos.sandbox.tldcpp.videotld;

import java.util.List;

import org.openimaj.demos.sandbox.tldcpp.detector.NormalizedPatch;
import org.openimaj.demos.sandbox.tldcpp.detector.ScaleIndexRectangle;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.MeanCenter;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Variaus TLD utility functions
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
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
		result.processInplace(new MeanCenter());
		return result;
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

	/**
	 * The overlap of the window at the firstIndex to all windows in the confidentIndices
	 * @param windows
	 * @param firstIndex
	 * @param confidentIndices
	 * @param distances
	 * @param distIDX
	 */
	public static void tldOverlapOne(ScaleIndexRectangle[] windows, int firstIndex, List<Integer> confidentIndices, float[] distances, int distIDX) {
		Rectangle comp = windows[firstIndex];
		int i = 0;
		for (int idx : confidentIndices) {
			ScaleIndexRectangle other = windows[idx];
			distances[distIDX + i] = tldOverlapNorm(comp,other);
			i++;
		}
	}
}
