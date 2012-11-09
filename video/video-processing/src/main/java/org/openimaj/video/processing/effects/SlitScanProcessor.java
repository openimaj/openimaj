package org.openimaj.video.processing.effects;

import java.util.LinkedList;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.video.Video;
import org.openimaj.video.processor.VideoProcessor;

/**
 * {@link VideoProcessor} that produces a slit-scan effect.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SlitScanProcessor extends VideoProcessor<MBFImage> {
	LinkedList<float[][][]> cache = new LinkedList<float[][][]>();

	final float[] blurKern = FGaussianConvolve.makeKernel(0.5f);
	int cacheSize = 240;

	/**
	 * Default constructor for using the video processor in an ad-hoc manner.
	 * 
	 * @param cacheSize
	 *            The number of frames to retain for creating the slitscan
	 *            effect
	 */
	public SlitScanProcessor(int cacheSize)
	{
		this.cacheSize = cacheSize;
	}

	/**
	 * Constructor for creating a video processor which is chainable.
	 * 
	 * @param video
	 *            The video to process
	 * @param cacheSize
	 *            The number of frames to retain for creating the slitscan
	 *            effect
	 */
	public SlitScanProcessor(Video<MBFImage> video, int cacheSize) {
		super(video);
		this.cacheSize = cacheSize;
	}

	@Override
	public MBFImage processFrame(MBFImage frame) {
		addToCache(frame);

		final int height = frame.getHeight();
		final float prop = (float) (cacheSize) / height;
		frame.fill(RGBColour.BLACK);
		final float[][] framer = frame.getBand(0).pixels;
		final float[][] frameg = frame.getBand(1).pixels;
		final float[][] frameb = frame.getBand(2).pixels;
		for (int y = 0; y < height; y++) {
			final int index = (int) (y * prop);
			if (index >= cache.size()) {
				break;
			}
			// System.out.println("y = " + y);
			// System.out.println("index = " + index);
			final float[][][] cacheImage = cache.get(index);
			System.arraycopy(cacheImage[0][y], 0, framer[y], 0, cacheImage[0][y].length);
			System.arraycopy(cacheImage[1][y], 0, frameg[y], 0, cacheImage[1][y].length);
			System.arraycopy(cacheImage[2][y], 0, frameb[y], 0, cacheImage[2][y].length);
		}

		for (final FImage f : frame.bands) {
			FImageConvolveSeparable.convolveVertical(f, blurKern);
		}

		if (cache.size() >= cacheSize)
			cache.removeLast();

		return frame;
	}

	private void addToCache(MBFImage frame) {
		final MBFImage f = frame.clone();

		final float[][][] entry = new float[3][][];

		entry[0] = f.getBand(0).pixels;
		entry[1] = f.getBand(1).pixels;
		entry[2] = f.getBand(2).pixels;

		cache.addFirst(entry);
	}
}
