package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.image.processing.face.detection.DetectedFace;

public class FaceInstance<V extends DetectedFace> implements Identifiable {
	public V face;
	private String id;
	
	public FaceInstance(V face, String id) {
		this.face = face;
		this.id = id;
	}
	
	@Override
	public String getID() {
		return id;
	}
}