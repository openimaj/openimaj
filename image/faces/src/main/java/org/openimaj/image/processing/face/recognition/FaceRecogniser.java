package org.openimaj.image.processing.face.recognition;

import java.io.Serializable;
import java.util.List;

import org.openimaj.image.processing.face.detection.DetectedFace;

public interface FaceRecogniser<T extends DetectedFace> extends Serializable {
	public void addInstance(String identifier, T face);
	
	public void train();
	
	public List<FaceMatchResult> query(T face);
	public FaceMatchResult queryBestMatch(T face);

	public void reset();
}
