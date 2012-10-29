package org.openimaj.image.objectdetection;

import Jama.Matrix;

/**
 * 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DETECTED_OBJECT>
 */
public class TransformedDetection<DETECTED_OBJECT> {
	public Matrix transform;
	public DETECTED_OBJECT detection;

	TransformedDetection(DETECTED_OBJECT detection, Matrix transform) {
		this.detection = detection;
		this.transform = transform;
	}

}
