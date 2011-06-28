package org.openimaj.image.processing.face.recognition.benchmarking.split;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.dataset.FaceDataset;

public abstract class FaceDatasetSplitter<T extends DetectedFace> {
	protected FaceDataset<T> training;
	protected FaceDataset<T> testing;
	
	public FaceDatasetSplitter() {
	}
	
	public abstract void split(FaceDataset<T> dataset);

	public FaceDataset<T> getTrainingDataset() {
		return training;
	}
	
	public FaceDataset<T> getTestingDataset() {
		return testing;
	}
}
