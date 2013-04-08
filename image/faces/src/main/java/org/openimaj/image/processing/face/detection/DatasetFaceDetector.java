/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.processing.face.detection;

import java.util.List;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.image.Image;

/**
 * Convenience methods for dealing with face detections in datasets and lists of
 * images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class DatasetFaceDetector {
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
			process(GroupedDataset<PERSON, ? extends ListDataset<IMAGE>, IMAGE> input, FaceDetector<FACE, IMAGE> detector)
	{
		final MapBackedDataset<PERSON, ListDataset<FACE>, FACE> output = new MapBackedDataset<PERSON, ListDataset<FACE>, FACE>();

		for (final PERSON group : input.getGroups()) {
			final ListBackedDataset<FACE> detected = new ListBackedDataset<FACE>();
			final ListDataset<IMAGE> instances = input.getInstances(group);

			for (int i = 0; i < instances.size(); i++) {
				final IMAGE img = instances.getInstance(i);
				final List<FACE> faces = detector.detectFaces(img);

				if (faces == null || faces.size() == 0) {
					System.err.println("There was no face detected in " + group + " instance " + i);
					// detected.add(null);
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
	 * Apply a face detector to all the images in the given dataset, choosing
	 * only the biggest face if multiple are found.
	 * 
	 * @param <IMAGE>
	 *            Type of image
	 * @param <FACE>
	 *            Type of {@link DetectedFace} extracted
	 * @param instances
	 *            The input faces
	 * @param detector
	 *            The face detector
	 * @return a dataset of detected faces.
	 */
	public static <IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace>
			ListDataset<FACE>
			process(List<IMAGE> instances, FaceDetector<FACE, IMAGE> detector)
	{
		final ListBackedDataset<FACE> detected = new ListBackedDataset<FACE>();

		for (int i = 0; i < instances.size(); i++) {
			final IMAGE img = instances.get(i);
			final List<FACE> faces = detector.detectFaces(img);

			if (faces == null || faces.size() == 0) {
				System.err.println("There was no face detected in instance " + i);
				// detected.add(null);
				continue;
			}

			if (faces.size() == 1) {
				detected.add(faces.get(0));
				continue;
			}

			detected.add(getBiggest(faces));
		}

		return detected;
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
