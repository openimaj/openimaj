package org.openimaj.video.processing.pixels;

import org.openimaj.image.FImage;
import org.openimaj.video.analyser.VideoAnalyser;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;

/**
 * Compute the mean and variance fields from a video of {@link FImage} frames.
 * The generated fields could be used to analyse which parts of a video are
 * stationary or change a lot. If your video consists of multiple shots, between
 * which there are large changes in the content, then it probably makes sense to
 * segment the video using a {@link VideoShotDetector} and apply a new analyser
 * to each shot independently.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FMeanVarianceField
		extends
			VideoAnalyser<FImage>
{
	private FImage mean;
	private FImage m2;
	private int n;

	@Override
	public void analyseFrame(FImage frame) {
		final int width = frame.width;
		final int height = frame.height;

		if (mean == null || mean.width != width || mean.height != height) {
			n = 0;
			mean = new FImage(width, height);
			m2 = new FImage(width, height);
		}

		final float[][] mp = mean.pixels;
		final float[][] m2p = m2.pixels;
		final float[][] fp = frame.pixels;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float v = fp[y][x];
				final float delta = v - mp[y][x];

				n++;
				mp[y][x] = mp[y][x] + delta / n;
				m2p[y][x] = m2p[y][x] + delta * (v - mp[y][x]);
			}
		}
	}

	/**
	 * Reset the accumulated field data.
	 * 
	 * @see org.openimaj.video.processor.VideoProcessor#reset()
	 */
	@Override
	public void reset() {
		this.mean = null;
		this.m2 = null;
	}

	/**
	 * Get the mean field of all the frames that have been analysed so far.
	 * 
	 * @return the mean field.
	 */
	public FImage getMean() {
		return mean;
	}

	/**
	 * Get the variance field of all the frames that have been analysed so far.
	 * 
	 * @return the variance field.
	 */
	public FImage getVariance() {
		if (m2 == null)
			return null;

		return m2.divide((float) (n - 1));
	}
}
