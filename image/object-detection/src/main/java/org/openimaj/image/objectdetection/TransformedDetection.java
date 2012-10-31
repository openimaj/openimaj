package org.openimaj.image.objectdetection;

import Jama.Matrix;

/**
 * An object detection with an associated transform that maps the detection
 * shape to the image. Typically this is used by {@link ObjectDetector}s that
 * perform some form of pre-processing transform on the image (for example to
 * simulate rotations or affine warps in order to increase invariance). In these
 * cases, the transformation held by the {@link TransformedDetection} would be
 * the <b>inverse</b> of the pre-process transform.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DETECTED_OBJECT>
 *            Type of detected object
 */
public class TransformedDetection<DETECTED_OBJECT> {
	/**
	 * The transform to be applied to the detected object to map it to the image
	 * in which the detection was made.
	 */
	public Matrix transform;

	/**
	 * The object that was detected
	 */
	public DETECTED_OBJECT detected;

	/**
	 * Construct a new {@link TransformedDetection} with the given detected
	 * object and transform.
	 * 
	 * @param detected
	 *            the detected object
	 * @param transform
	 *            the transform
	 */
	public TransformedDetection(DETECTED_OBJECT detected, Matrix transform) {
		this.detected = detected;
		this.transform = transform;
	}
}
