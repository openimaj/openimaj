package org.openimaj.image.feature.dense.binarypattern;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.algorithm.DifferenceOfGaussian;
import org.openimaj.image.processing.algorithm.GammaCorrection;
import org.openimaj.image.processing.algorithm.MaskedRobustContrastEqualisation;

/**
 * Class for determining whether specific binary patterns are "uniform".
 * Uniform patterns have less than one 01 transition and one 01 
 * transition when viewed as a circular buffer.
 * 
 * The class caches lookup tables of uniform patterns on demand, with the
 * exception of the commonly used 8-bit patterns which are cached on 
 * initialization.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class UniformBinaryPattern {
	protected static TIntObjectHashMap<TIntArrayList> lut = new TIntObjectHashMap<TIntArrayList>();
	
	static {
		//pre-cache the table for 8-bit patterns as it is common
		lut.put(8, calculateUniformPatterns(8));
		//other patterns will be cached on demand
	}
	
	protected static TIntArrayList calculateUniformPatterns(int nbits) {
		TIntArrayList result = new TIntArrayList();
		
		boolean [] bits = new boolean[nbits];
		
		for (int i=0; i<Math.pow(2, nbits); i++) {
			Arrays.fill(bits, false);
			
			for (int temp=i, j=1; j<=nbits; j++) {
				int pow = (int)Math.pow(2, (nbits-j));
				
				if(temp / pow > 0) {
					bits[j-1] = true;
				}
				temp = temp % pow;
			}
			
			if (isUniform(bits)) {
				result.add(i);
			}
		}
		
		return result;
	}

	protected static boolean isUniform(boolean [] pattern) {
		int count = 0;

		for (int i=0; i<pattern.length-1; i++) {
			if (pattern[i] != pattern[i+1]) {
				count++;
			}
		}

		return count <= 2;
	}

	/**
	 * Get a list of all the binary patterns of a given length 
	 * that are "uniform". Uniform patterns have less than one
	 * 01 transition and one 01 transition when viewed as a circular
	 * buffer.
	 * 
	 * The length must be between 1 and 32 bits.
	 * 
	 * @param nbits pattern length
	 * @return set of patterns encoded as integers
	 */
	public static TIntArrayList getUniformPatterns(int nbits) {
		if (nbits < 1 || nbits > 32)
			throw new IllegalArgumentException("Only patterns with lengths between 1 and 32 bits are supported");
		
		TIntArrayList patterns = lut.get(nbits);
		
		if (patterns  == null) {
			patterns = calculateUniformPatterns(nbits);
			lut.put(nbits, patterns);
		}
		
		return patterns;
	}
	
	/**
	 * Check whether the given nbits pattern is uniform. 
	 * @param pattern the pattern
	 * @param nbits the pattern length
	 * @return true if uniform; false otherwise.
	 */
	public static boolean isPatternUniform(int pattern, int nbits) {
		return getUniformPatterns(nbits).contains(pattern);
	}
	
	public static FImage extractPatternImage(int [][] patternImage, int code) {
		FImage image = new FImage(patternImage[0].length, patternImage.length);
		//image.fill(Float.POSITIVE_INFINITY);
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (patternImage[y][x] == code) {
					image.pixels[y][x] = 1;
				}
			}
		}
		
		return image;
	}
	
	public static FImage[] extractPatternImages(int [][] patternImage, int nbits) {
		TIntArrayList uniformPatterns = getUniformPatterns(nbits);
		
		FImage [] images = new FImage[uniformPatterns.size() + 1];
		for (int i=0; i<images.length; i++) {
			images[i] = new FImage(patternImage[0].length, patternImage.length);
			//image.fill(Float.POSITIVE_INFINITY);
		}
		
		for (int y=0; y<images[0].height; y++) {
			for (int x=0; x<images[0].width; x++) {
				int idx = uniformPatterns.indexOf(patternImage[y][x]);
				images[idx+1].pixels[y][x] = 1;
			}
		}
		
		return images;
	}
}
