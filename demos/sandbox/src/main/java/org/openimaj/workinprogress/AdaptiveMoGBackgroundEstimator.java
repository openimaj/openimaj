package org.openimaj.workinprogress;

import java.io.File;
import java.io.IOException;

import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.video.Video;
import org.openimaj.video.processor.VideoProcessor;
import org.openimaj.video.xuggle.XuggleVideo;

public class AdaptiveMoGBackgroundEstimator<IMAGE extends Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
extends
VideoProcessor<IMAGE>
{
	/**
	 * The mixture means [y][x][gaussian][band]
	 */
	float[][][][] mu;

	/**
	 * The mixture standard deviations (Gaussians are spherical)
	 * [y][x][gaussian]
	 */
	float[][][] sigma;

	/**
	 * The mixture weights [y][x][gaussian]
	 */
	float[][][] weights;

	/**
	 * Number of dimensions
	 */
	int n;

	/**
	 * Number of Gaussians per mixture
	 */
	int K = 3;

	/**
	 * Learning rate
	 */
	float alpha = 0.005f;

	/**
	 * Initial (low) weight for new Gaussians
	 */
	float initialWeight = 0.05f;

	/**
	 * Initial (high) standard deviation for new Gaussians
	 */
	float initialSigma = 30f / 255f;

	/**
	 * Number of standard deviations for a pixel to be considered a match
	 */
	float matchThreshold = 2.5f;

	private float T = 0.7f;

	/**
	 * The segmentation mask
	 */
	private float[][] mask;

	public AdaptiveMoGBackgroundEstimator(Video<IMAGE> xv) {
		super(xv);
	}

	private float density(float[] pixel, float[] pkmu, float pksigma) {
		if (pksigma == 0)
			return 0;

		final double norm = 1 / Math.sqrt(Math.pow(2 * Math.PI, n) * pksigma * pksigma);
		double exp = 0;
		for (int i = 0; i < n; i++) {
			final float diff = pixel[i] - pkmu[i];
			exp += diff * diff / (pksigma * pksigma);
		}

		return (float) (Math.exp(-0.5 * exp) * norm);
	}

	void updateModel(float[][][] tImg) {
		final int height = tImg[0].length;
		final int width = tImg[0][0].length;

		if (n != tImg.length || height != mu.length || width != mu[0].length)
			initialiseModel(width, height, tImg.length);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// final float[] pixel = tImg[y][x];
				final float[] pixel = new float[n];
				for (int i = 0; i < n; i++)
					pixel[i] = tImg[i][y][x];

				final float[][] means = mu[y][x];
				final float[] stddev = sigma[y][x];
				final float[] weight = weights[y][x];

				int match = -1;
				for (int i = 0; i < K; i++) {
					final double distance = FloatFVComparison.EUCLIDEAN.compare(means[i], pixel);

					if (distance < stddev[i] * matchThreshold) {
						match = i;
						break;
					}
				}

				if (match >= 0) {
					// update matched distribution
					final float p = alpha * density(pixel, means[match], stddev[match]);

					float dot = 0;
					for (int i = 0; i < n; i++) {
						means[match][i] = (1 - p) * means[match][i] + p * pixel[i];

						dot += (pixel[i] - means[match][i]) * (pixel[i] - means[match][i]);
					}

					stddev[match] = (float) Math.sqrt((1 - p) * stddev[match] * stddev[match] + p * dot);
				} else {
					// find least-probable gaussian
					float minProb = density(pixel, means[0], stddev[0]);
					int minProbIdx = 0;
					for (int i = 1; i < K; i++) {
						final float prob = density(pixel, means[i], stddev[i]);
						if (prob < minProb) {
							minProb = prob;
							minProbIdx = i;
						}
					}

					// init new gaussian:
					means[minProbIdx] = pixel.clone();
					stddev[minProbIdx] = initialSigma;
					weight[minProbIdx] = initialWeight;
					match = minProbIdx;
				}

				// update weights
				float weightsum = 0;
				for (int i = 0; i < K; i++) {
					weight[i] = (1 - alpha) * weight[i];
					if (i == match) {
						weight[i] += alpha;
					}
					weightsum += weight[i];
				}

				// renormalise weights
				ArrayUtils.divide(weight, weightsum);

				// compute scores
				final float[] scores = new float[K];
				for (int i = 0; i < K; i++) {
					if (stddev[i] == 0)
						scores[i] = 0;
					else
						scores[i] = weight[i] / stddev[i];
				}

				final int[] indices = ArrayUtils.indexSort(scores);

				float c = 0;
				boolean found = false;
				for (int i = indices.length - 1; i >= 0; i--) {
					c += weight[indices[i]];
					if (match == indices[i]) {
						found = true;
						break;
					}
					if (c > T) {
						break;
					}
				}
				mask[y][x] = found ? 1 : 0;
			}
		}
	}

	/**
	 * Initialise the internal state of the model. This does not need to be
	 * called manually (the first call to {@link #updateModel(Image)} will call
	 * it automatically), however, it is public to allow the model to be reset.
	 *
	 * @param width
	 *            the frame width
	 * @param height
	 *            the frame height
	 * @param numBands
	 *            the number of image bands
	 */
	public void initialiseModel(int width, int height, int numBands) {
		this.n = numBands;
		this.mu = new float[height][width][K][numBands];
		this.sigma = new float[height][width][K];
		this.weights = new float[height][width][K];
		this.mask = new float[height][width];

		// for (int y=0; y<height; y++)
		// for (int x=0; x<width; x++)
		// for (int k=0; k<K; k++)
		// sigma[y][x][k] = 0f;
	}

	public void updateModel(IMAGE frame) {
		if (frame instanceof MBFImage) {
			final float[][][] data = new float[((MBFImage) frame).numBands()][][];

			for (int i = 0; i < data.length; i++)
				data[i] = ((MBFImage) frame).getBand(i).pixels;

			updateModel(data);
		} else if (frame instanceof FImage) {
			final float[][][] data = new float[1][][];
			data[0] = ((FImage) frame).pixels;

			updateModel(data);
		} else {
			throw new UnsupportedOperationException("Only FImage and MBFImage are supported");
		}
	}

	@Override
	public IMAGE processFrame(IMAGE frame) {
		if (frame instanceof MBFImage) {
			final float[][][] data = new float[((MBFImage) frame).numBands()][][];

			for (int i = 0; i < data.length; i++)
				data[i] = ((MBFImage) frame).getBand(i).pixels;

			updateModel(data);
		} else if (frame instanceof FImage) {
			final float[][][] data = new float[1][][];
			data[0] = ((FImage) frame).pixels;

			updateModel(data);
		} else {
			throw new UnsupportedOperationException("Only FImage and MBFImage are supported");
		}

		final FImage tmp = new FImage(mask);
		if (frame instanceof FImage) {
			((FImage) frame).internalAssign(tmp);
		} else {
			// ((MBFImage) frame).drawImage(tmp.toRGB(), 0, 0);
			((MBFImage) frame).multiplyInplace(tmp.inverse().toRGB());
		}

		return frame;
	}

	public static void main(String[] args) throws IOException {
		// final XuggleVideo xv = new XuggleVideo(new
		// File("/Users/jon/Desktop/merlin/tunnel480.mov"));
		final XuggleVideo xv = new XuggleVideo(new File("/Users/jon/Downloads/ewap_dataset/seq_hotel/seq_hotel.avi"));

		final AdaptiveMoGBackgroundEstimator<MBFImage> proc = new AdaptiveMoGBackgroundEstimator<MBFImage>(xv);
		for (final MBFImage img : proc) {
			DisplayUtilities.displayName(img, "video");
		}
	}
}
