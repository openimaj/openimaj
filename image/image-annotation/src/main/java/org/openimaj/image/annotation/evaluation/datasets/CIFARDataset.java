package org.openimaj.image.annotation.evaluation.datasets;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.cifar.BinaryReader;
import org.openimaj.image.annotation.evaluation.datasets.cifar.FImageReader;
import org.openimaj.image.annotation.evaluation.datasets.cifar.MBFImageReader;

/**
 * Base class for CIFAR-10 and CIFAR-100, which both contain 32x32 pixel images.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public abstract class CIFARDataset {
	/**
	 * Image height
	 */
	public static final int HEIGHT = 32;

	/**
	 * Image width
	 */
	public static final int WIDTH = 32;

	/**
	 * Reader for getting the data as {@link MBFImage}s
	 */
	public static BinaryReader<MBFImage> MBFIMAGE_READER = new MBFImageReader(HEIGHT, WIDTH);

	/**
	 * Reader for getting the data as {@link FImage}s
	 */
	public static BinaryReader<FImage> FIMAGE_READER = new FImageReader(HEIGHT, WIDTH);
}
