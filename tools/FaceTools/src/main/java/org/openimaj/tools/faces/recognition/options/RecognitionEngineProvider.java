package org.openimaj.tools.faces.recognition.options;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

/**
 * Interface for objects capable of providing configured recognition engines
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FACE>
 *            type of {@link DetectedFace}
 */
public interface RecognitionEngineProvider<FACE extends DetectedFace> {
	/**
	 * @return the configured recognition engine
	 */
	public FaceRecognitionEngine<FACE, ?, String> createRecognitionEngine();
}
