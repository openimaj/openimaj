package org.openimaj.image.processing.face.detection;

import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.image.Image;

/**
 * Convenience methods for dealing with face detections in datasets of images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class DatasetFaceDetector {
	private static Logger logger = Logger.getLogger(DatasetFaceDetector.class);

	private DatasetFaceDetector() {
	}

	/**
	 * Apply a face detector to all the images in the given dataset, choosing
	 * only the biggest face if multiple are found.
	 * 
	 * @param <PERSON>
	 *            Type representing a person
	 * @param <IMAGE>
	 *            Type of image
	 * @param <FACE>
	 *            Type of {@link DetectedFace} extracted
	 * @param input
	 *            The input dataset
	 * @param detector
	 *            The face detector
	 * @return a dataset of detected faces.
	 */
	public static <PERSON, IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace>
			GroupedDataset<PERSON, ListDataset<FACE>, FACE>
			process(GroupedDataset<PERSON, ListDataset<IMAGE>, IMAGE> input, FaceDetector<FACE, IMAGE> detector)
	{
		final MapBackedDataset<PERSON, ListDataset<FACE>, FACE> output = new MapBackedDataset<PERSON, ListDataset<FACE>, FACE>();

		for (final PERSON group : input.getGroups()) {
			final ListBackedDataset<FACE> detected = new ListBackedDataset<FACE>();
			final ListDataset<IMAGE> instances = input.getInstances(group);

			for (int i = 0; i < instances.size(); i++) {
				final IMAGE img = instances.getInstance(i);
				final List<FACE> faces = detector.detectFaces(img);

				if (faces == null) {
					logger.warn("There was no face detected in " + group + " instance " + i);
					continue;
				}

				if (faces.size() == 1) {
					detected.add(faces.get(0));
					continue;
				}

				detected.add(getBiggest(faces));
			}

			output.getMap().put(group, detected);
		}

		return output;
	}

	/**
	 * Get the biggest face (by area) from the list
	 * 
	 * @param <FACE>
	 *            Type of {@link DetectedFace}
	 * @param faces
	 *            the list of faces
	 * @return the biggest face or null if the list is null or empty
	 */
	public static <FACE extends DetectedFace> FACE getBiggest(List<FACE> faces) {
		if (faces == null || faces.size() == 0)
			return null;

		int biggestIndex = 0;
		double biggestSize = faces.get(0).bounds.calculateArea();

		for (int i = 1; i < faces.size(); i++) {
			final double sz = faces.get(i).bounds.calculateArea();
			if (sz > biggestSize) {
				biggestSize = sz;
				biggestIndex = i;
			}
		}

		return faces.get(biggestIndex);
	}
}
