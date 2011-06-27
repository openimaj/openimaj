package org.openimaj.image.processing.face.recognition.benchmarking.split;

import java.util.Collections;
import java.util.List;

import org.openimaj.image.processing.face.parts.DetectedFace;
import org.openimaj.image.processing.face.recognition.dataset.FaceDataset;

public class PercentageRandomPerClassSplit extends FaceDatasetSplitter {
	private float trainingPercentage;

	public PercentageRandomPerClassSplit(float trainingPercentage) {
		this.trainingPercentage = trainingPercentage;
	}

	@Override
	public void split(FaceDataset dataset) {
		 training = new FaceDataset();
		 testing = new FaceDataset();
		
		for (List<DetectedFace> instances : dataset.getData()) {
			Collections.shuffle(instances);
			
			int trainingSamples = (int)Math.round(trainingPercentage*instances.size());
			
			training.getData().add(instances.subList(0, trainingSamples));
			testing.getData().add(instances.subList(trainingSamples, instances.size()));
		}
	}
}
