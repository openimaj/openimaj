package org.openimaj.image.processing.face.recognition.benchmarking.split;

import java.util.Collections;
import java.util.List;

import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.FaceDataset;

public class PercentageRandomPerClassSplit<T extends DetectedFace> extends FaceDatasetSplitter<T> {
	private float trainingPercentage;

	public PercentageRandomPerClassSplit(float trainingPercentage) {
		this.trainingPercentage = trainingPercentage;
	}

	@Override
	public void split(FaceDataset<T> dataset) {
		 training = new FaceDataset<T>();
		 testing = new FaceDataset<T>();
		
		for (List<T> instances : dataset.getData()) {
			Collections.shuffle(instances);
			
			int trainingSamples = (int)Math.round(trainingPercentage*instances.size());
			
			training.getData().add(instances.subList(0, trainingSamples));
			testing.getData().add(instances.subList(trainingSamples, instances.size()));
		}
	}
}
