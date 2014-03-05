/**
 * 
 */
package org.openimaj.image.segmentation;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler.Algorithm;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processor.Processor;

/**
 * Simple wrapper to make thresholding algorithms into {@link Segmenter}s by
 * applying the thresholding operation and then applying connected component
 * labeling. This class will produce components for both the foreground and
 * background elements of thresholded input image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ConnectedThresholdSegmenter extends ThresholdSegmenter {
	private ConnectMode mode;
	private Algorithm algorithm;

	/**
	 * Construct with the given thresholding algorithm implementation and
	 * connection mode.
	 * 
	 * @param thresholder
	 *            the thresholding algorithm
	 * @param mode
	 *            the connection mode
	 */
	public ConnectedThresholdSegmenter(Processor<FImage> thresholder, ConnectMode mode) {
		this(thresholder, ConnectedComponentLabeler.Algorithm.TWO_PASS, mode);
	}

	/**
	 * Construct with the given thresholding algorithm implementation.
	 * 
	 * @param thresholder
	 *            the thresholding algorithm
	 * @param algorithm
	 *            the connected component labeling algorithm to use
	 * @param mode
	 *            the connection mode
	 */
	public ConnectedThresholdSegmenter(Processor<FImage> thresholder, Algorithm algorithm, ConnectMode mode) {
		super(thresholder);
		this.mode = mode;
		this.algorithm = algorithm;
	}

	@Override
	public List<ConnectedComponent> segment(FImage image) {
		final FImage timg = image.process(thresholder);

		return algorithm.findComponents(timg, 0, mode);
	}
}
