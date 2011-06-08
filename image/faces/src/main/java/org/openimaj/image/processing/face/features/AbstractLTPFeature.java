package org.openimaj.image.processing.face.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.binarypattern.LocalTernaryPattern;
import org.openimaj.image.feature.dense.binarypattern.UniformBinaryPattern;
import org.openimaj.image.processing.algorithm.DifferenceOfGaussian;
import org.openimaj.image.processing.algorithm.EuclideanDistanceTransform;
import org.openimaj.image.processing.algorithm.GammaCorrection;
import org.openimaj.image.processing.algorithm.MaskedRobustContrastEqualisation;
import org.openimaj.image.processing.face.parts.DetectedFace;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public abstract class AbstractLTPFeature<T extends AbstractLTPFeature<T>> implements FacialFeature<T> {
	FImage[] distanceMaps;
	protected boolean affineMode;
	
	public AbstractLTPFeature(boolean affineMode) {
		this.affineMode = affineMode;
	}

	protected FImage normaliseImage(FImage image) {
		return image.process(new GammaCorrection())
					 .processInline(new DifferenceOfGaussian())
					 .processInline(new MaskedRobustContrastEqualisation());
	}
	
	protected FImage[] extractLTPSlices(FImage image) {
		LocalTernaryPattern ltp = new LocalTernaryPattern(2, 8, 0.1f);
		image.process(ltp);
		
		FImage [] positiveSlices = UniformBinaryPattern.extractPatternImages(ltp.getPositivePattern(), 8);
		FImage [] negativeSlices = UniformBinaryPattern.extractPatternImages(ltp.getNegativePattern(), 8);
		
		List<FImage> slices = new ArrayList<FImage>();
		slices.addAll(Arrays.asList(positiveSlices));
		slices.addAll(Arrays.asList(negativeSlices));
		
		return slices.toArray(new FImage[slices.size()]);
	}
	
	protected FImage[] extractDistanceTransforms(FImage [] slices) {
		FImage [] dist = new FImage[slices.length];
		int width = slices[0].width;
		int height = slices[0].height;
		int [][] indices = new int[height][width];
		
		for (int i=0; i<slices.length; i++) {
			dist[i] = new FImage(width, height);
			
			EuclideanDistanceTransform.squaredEuclideanDistanceBinary(slices[i], dist[i], indices);
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					dist[i].pixels[y][x] = weightDistance((float)Math.sqrt(dist[i].pixels[y][x])); 
				}
			}
		}
		
		return dist;
	}
	
	/**
	 * Determine the weighting scheme for the distances produced
	 * by the EuclideanDistanceTransform.
	 * @param distance the unweighted distance in pixels
	 * @return the weighted distance
	 */
	protected abstract float weightDistance(float distance);
	
	protected FImage getFacePatch(DetectedFace face) {
		return affineMode ? face.affineFacePatch : face.facePatch;
	}
	
	@Override
	public void initialise(DetectedFace face, boolean isQuery) {
		if (isQuery)
			distanceMaps = extractLTPSlices(normaliseImage(getFacePatch(face)));
		else
			distanceMaps = extractDistanceTransforms(extractLTPSlices(normaliseImage(getFacePatch(face))));
	}
	
	@Override
	public double compare(T feature) {
		FImage [] slices = feature.distanceMaps;
		float distance = 0;
		
		for (int i=0; i<distanceMaps.length; i++) {
			for (int y=0; y<slices[i].height; y++) {
				for (int x=0; x<slices[i].width; x++) {
					distance += slices[i].pixels[y][x] * distanceMaps[i].pixels[y][x];
				}
			}
		}
		
		return distance;
	}
}
