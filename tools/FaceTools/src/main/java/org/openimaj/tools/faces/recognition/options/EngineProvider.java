package org.openimaj.tools.faces.recognition.options;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;

public interface EngineProvider {
	public <FACE extends DetectedFace> FaceRecognitionEngine<FACE, ?, String> createRecognitionEngine();
}
