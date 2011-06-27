package org.openimaj.image.processing.face.recognition;

import java.io.Serializable;
import java.util.List;

import org.openimaj.image.processing.face.parts.DetectedFace;

public interface FaceRecogniser extends Serializable {
	public void addInstance(String identifier, DetectedFace face);
	
	public void train();
	
	public List<FaceMatchResult> query(DetectedFace face);
	public FaceMatchResult queryBestMatch(DetectedFace face);

	public void reset();
}
