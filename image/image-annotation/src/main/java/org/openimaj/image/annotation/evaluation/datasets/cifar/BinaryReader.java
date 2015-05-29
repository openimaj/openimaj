package org.openimaj.image.annotation.evaluation.datasets.cifar;

/**
 * Interface for reader objects that convert a raw CIFAR style binary record
 * into an image (or some other format)
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 */
public interface BinaryReader<IMAGE> {
	/**
	 * Read the binary data into an object
	 * 
	 * @param bytes
	 *            the data
	 * @return the object
	 */
	IMAGE read(byte[] bytes);
}
