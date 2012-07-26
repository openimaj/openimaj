package org.openimaj.image.processing.face.recognition.benchmarking;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;

public interface FaceRecogniserProvider<FACE extends DetectedFace, KEY> {
	public abstract FaceRecogniser<FACE, ?, KEY> create(GroupedDataset<KEY, ListDataset<FACE>, FACE> dataset);
}
