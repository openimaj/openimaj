package org.openimaj.image.processing.face.feature.ltp;

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
import org.openimaj.image.processing.face.feature.FacialFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureFactory;

/**
 * LTP based feature using a truncated Euclidean distance transform
 * to estimate the distances within each slice.
 * 
 * Based on: 
 * "Enhanced Local Texture Feature Sets for Face Recognition 
 * Under Difficult Lighting Conditions" by Xiaoyang Tan and 
 * Bill Triggs.
 *
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ReversedLtpDtFeature implements FacialFeature {
	public static class Factory<Q extends DetectedFace> implements FacialFeatureFactory<ReversedLtpDtFeature, Q> {
		private static final long serialVersionUID = 1L;
		
		LTPWeighting weighting;
		FaceAligner<Q> aligner;
		
		public Factory(FaceAligner<Q> aligner, LTPWeighting weighting) {
			this.aligner = aligner;
			this.weighting = weighting;
		}
		
		@Override
		public ReversedLtpDtFeature createFeature(Q detectedFace, boolean isquery) {
			ReversedLtpDtFeature f = new ReversedLtpDtFeature();
			
			FImage face = aligner.align(detectedFace);
			FImage mask = aligner.getMask();
			
			f.initialise(face, mask, weighting, isquery);
			
			return f;
		}
	}
	
	public List<List<Pixel>> ltpPixels;
	public FImage[] distanceMaps;
	
	protected FImage normaliseImage(FImage image, FImage mask) {
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
	
	protected FImage[] extractDistanceTransforms(FImage [] slices, LTPWeighting weighting) {
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
					dist[i].pixels[y][x] = weighting.weightDistance((float)Math.sqrt(dist[i].pixels[y][x])); 
				}
			}
		}
		
		return dist;
	}
	
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
	
	protected void initialise(FImage face, FImage mask, LTPWeighting weighting, boolean isQuery) {
		FImage npatch = normaliseImage(face, mask);
		
		ltpPixels = extractLTPSlicePixels(npatch);
		
		if (isQuery)
			distanceMaps = extractDistanceTransforms(constructSlices(ltpPixels, face.width, face.height), weighting);
	}
}
