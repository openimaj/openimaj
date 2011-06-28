package org.openimaj.image.processing.face.features;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.binarypattern.LocalTernaryPattern;
import org.openimaj.image.feature.dense.binarypattern.UniformBinaryPattern;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.algorithm.DifferenceOfGaussian;
import org.openimaj.image.processing.algorithm.EuclideanDistanceTransform;
import org.openimaj.image.processing.algorithm.GammaCorrection;
import org.openimaj.image.processing.algorithm.MaskedRobustContrastEqualisation;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public abstract class AbstractLTPFeature<T extends AbstractLTPFeature<T, Q>, Q extends DetectedFace> implements FacialFeature<T, Q> {
	private static final long serialVersionUID = 1L;
	
	protected List<List<Pixel>> ltpPixels;
	protected FImage[] distanceMaps;
	protected FaceAligner<Q> aligner;
	
	public AbstractLTPFeature(FaceAligner<Q> aligner) {
		this.aligner = aligner;
	}

	protected FImage normaliseImage(FImage image) {
		FImage mask = aligner.getMask();
		
		if (mask == null) {
			return image.process(new GammaCorrection())
			 .processInline(new DifferenceOfGaussian())
			 .processInline(new MaskedRobustContrastEqualisation());
		}
		
		return image.process(new GammaCorrection())
					 .processInline(new DifferenceOfGaussian())
					 .processInline(new MaskedRobustContrastEqualisation(), mask)
					 .multiply(mask);
	}
	
	protected List<List<Pixel>> extractLTPSlicePixels(FImage image) {
		LocalTernaryPattern ltp = new LocalTernaryPattern(2, 8, 0.1f);
		image.process(ltp);
		
		List<List<Pixel>> positiveSlices = UniformBinaryPattern.extractPatternPixels(ltp.getPositivePattern(), 8);
		List<List<Pixel>> negativeSlices = UniformBinaryPattern.extractPatternPixels(ltp.getNegativePattern(), 8);
		
		positiveSlices.addAll(negativeSlices);
		
		return positiveSlices;
	}
	
	protected FImage[] extractDistanceTransforms(FImage [] slices) {
		FImage [] dist = new FImage[slices.length];
		int width = slices[0].width;
		int height = slices[0].height;
		int [][] indices = new int[height][width];
		
		for (int i=0; i<slices.length; i++) {
			if (slices[i] == null) 
				continue;
			
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
	
	protected FImage [] constructSlices(List<List<Pixel>> ltpPixels, int width, int height) {
		FImage[] slices = new FImage[ltpPixels.size()];
		
		for (int i=0; i<slices.length; i++) {
			List<Pixel> pixels = ltpPixels.get(i);
			
			if (pixels == null)
				continue;
			
			slices[i] = new FImage(width, height);
			for (Pixel p : pixels) {
				slices[i].pixels[p.y][p.x] = 1; 
			}
		}
		
		return slices;
	}
	
	@Override
	public void initialise(Q face, boolean isQuery) {
		FImage patch = aligner.align(face);
		
		ltpPixels = extractLTPSlicePixels(normaliseImage(patch));
		
		if (!isQuery)
			distanceMaps = extractDistanceTransforms(constructSlices(ltpPixels, patch.width, patch.height));
	}
	
	@Override
	public double compare(T feature) {
		List<List<Pixel>> slicePixels = feature.ltpPixels;
		float distance = 0;
		
		for (int i=0; i<distanceMaps.length; i++) {
			List<Pixel> pixels = slicePixels.get(i);
			
			if (distanceMaps[i] == null || pixels == null)
				continue;
			
			for (Pixel p : pixels) {
				distance += distanceMaps[i].pixels[p.y][p.x];
			}
		}
		
		return distance;
	}
}
