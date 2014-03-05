package org.openimaj.image.segmentation;

import gnu.trove.map.hash.TFloatObjectHashMap;

import java.util.Arrays;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.processor.Processor;

/**
 * Simple wrapper to make thresholding algorithms into {@link Segmenter}s by
 * applying the thresholding operation and then gathering the pixel sets
 * belonging to each segment. Note that class does not perform connected
 * component analysis, and for example in the case of binary thresholding, there
 * will only be two {@link PixelSet}s produced (i.e. foreground and background).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ThresholdSegmenter implements Segmenter<FImage> {
	Processor<FImage> thresholder;

	/**
	 * Construct with the given thresholding algorithm implementation.
	 * 
	 * @param thresholder
	 *            the thresholding algorithm
	 */
	public ThresholdSegmenter(Processor<FImage> thresholder) {
		this.thresholder = thresholder;
	}

	@Override
	public List<? extends PixelSet> segment(FImage image) {
		final FImage timg = image.process(thresholder);
		final TFloatObjectHashMap<PixelSet> sets = new TFloatObjectHashMap<PixelSet>();

		for (int y = 0; y < timg.height; y++) {
			for (int x = 0; x < timg.width; x++) {
				final float p = image.getPixel(x, y);

				PixelSet ps = sets.get(p);
				if (ps == null)
					sets.put(p, ps = new PixelSet());
				ps.addPixel(x, y);
			}
		}

		return Arrays.asList(sets.values());
	}
}
