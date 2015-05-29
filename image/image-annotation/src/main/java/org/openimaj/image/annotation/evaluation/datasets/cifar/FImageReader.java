package org.openimaj.image.annotation.evaluation.datasets.cifar;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.cifar.BinaryReader;

/**
 * {@link BinaryReader} for CIFAR data that converts the encoded rgb pixel
 * values into an {@link FImage} (by unweighted averaging).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FImageReader implements BinaryReader<FImage> {
	int width;
	int height;

	/**
	 * Construct with the given image height and width. The byte arrays given to
	 * {@link #read(byte[])} must be 3 * height * width in length.
	 *
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 */
	public FImageReader(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public final FImage read(final byte[] record) {
		final FImage image = new FImage(width, height);
		final float[][] p = image.pixels;
		for (int y = 0, j = 0; y < height; y++) {
			for (int x = 0; x < width; x++, j++) {
				final float r = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j] & 0xff];
				final float g = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j] & 0xff];
				final float b = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j] & 0xff];
				p[y][x] = (r + g + b) / 3;
			}
		}
		return image;
	}
}
