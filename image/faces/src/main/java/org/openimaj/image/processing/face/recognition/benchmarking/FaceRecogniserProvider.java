package org.openimaj.image.processing.face.recognition.benchmarking;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;

/**
 * Interface for objects that can create new {@link FaceRecogniser} instances to
 * use in a {@link CrossValidationBenchmark}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FACE>
 *            type of {@link DetectedFace}
 * @param <PERSON>
 *            type representing a person or class
 */
public interface FaceRecogniserProvider<FACE extends DetectedFace, PERSON> {
	/**
	 * Create and train a new recogniser instance based on the given dataset
	 * 
	 * @param dataset
	 *            the dataset
	 * @return newly created and trained recogniser instance
	 */
	public abstract FaceRecogniser<FACE, ?, PERSON> create(GroupedDataset<PERSON, ListDataset<FACE>, FACE> dataset);
}
