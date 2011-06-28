package org.openimaj.image.processing.face.recognition.dataset;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.processing.face.detection.DetectedFace;

public class FaceDataset<T extends DetectedFace> {
	protected List<List<T>> data = new ArrayList<List<T>>();
	
	public int getNumberPeople() {
		return getData().size();
	}
	
	List<T> getInstances(int personId) {
		return getData().get(personId);
	}

	public List<List<T>> getData() {
		return data;
	}
	
	public String getIdentifier(int id) {
		return id + "";
	}
}
