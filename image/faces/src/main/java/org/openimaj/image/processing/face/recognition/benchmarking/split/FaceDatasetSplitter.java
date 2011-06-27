package org.openimaj.image.processing.face.recognition.benchmarking.split;

import org.openimaj.image.processing.face.recognition.dataset.FaceDataset;

public abstract class FaceDatasetSplitter {
	protected FaceDataset training;
	protected FaceDataset testing;
	
	public FaceDatasetSplitter() {
	}
	
	public abstract void split(FaceDataset dataset);

	public FaceDataset getTrainingDataset() {
		return training;
	}
	
	public FaceDataset getTestingDataset() {
		return testing;
	}
}
