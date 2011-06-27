package org.openimaj.image.processing.face.recognition.dataset;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.processing.face.parts.DetectedFace;

public class FaceDataset {
	protected List<List<DetectedFace>> data = new ArrayList<List<DetectedFace>>();
	
	public int getNumberPeople() {
		return getData().size();
	}
	
	List<DetectedFace> getInstances(int personId) {
		return getData().get(personId);
	}

	public List<List<DetectedFace>> getData() {
		return data;
	}
	
	public String getIdentifier(int id) {
		return id + "";
	}
}
