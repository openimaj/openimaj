package org.openimaj.image.annotation.evaluation.datasets.cifar;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

/**
 * {@link BinaryReader} for CIFAR data that converts the encoded rgb pixel
 * values into an {@link MBFImage}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MBFImageReader implements BinaryReader<MBFImage> {
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
	public MBFImageReader(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public MBFImage read(byte[] record) {
		final MBFImage image = new MBFImage(width, height, ColourSpace.RGB);
		final float[][] r = image.getBand(0).pixels;
		final float[][] g = image.getBand(1).pixels;
		final float[][] b = image.getBand(2).pixels;
		for (int y = 0, j = 0; y < height; y++) {
			for (int x = 0; x < width; x++, j++) {
				r[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j] & 0xff];
				g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j + height * width] & 0xff];
				b[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j + 2 * height * width] & 0xff];
			}
		}
		return image;
	}
}
