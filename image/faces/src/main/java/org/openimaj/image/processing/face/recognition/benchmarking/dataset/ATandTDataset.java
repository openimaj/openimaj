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
package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

import java.io.File;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

/**
 * A Dataset for Our Database of Faces/The ORL Face Database/The AT&T Face
 * database.
 * <p>
 * Note that the faces are already cropped and (fairly well) aligned.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@DatasetDescription(
		name = "Our Database of Faces/The ORL Face Database/The AT&T Face database",
		description = "Our Database of Faces, (formerly 'The ORL Database of Faces'), "
				+ "contains a set of face images taken between April 1992 and April 1994 "
				+ "at the lab. The database was used in the context of a face recognition "
				+ "project carried out in collaboration with the Speech, Vision and "
				+ "Robotics Group of the Cambridge University Engineering Department. "
				+ "There are ten different images of each of 40 distinct subjects. "
				+ "For some subjects, the images were taken at different times, varying "
				+ "the lighting, facial expressions (open / closed eyes, smiling / not smiling) "
				+ "and facial details (glasses / no glasses). "
				+ "All the images were taken against a dark homogeneous background with the "
				+ "subjects in an upright, frontal position (with tolerance for some side "
				+ "movement). A preview image of the Database of Faces is available.",
		url = "http://www.cl.cam.ac.uk/research/dtg/attarchive/facedatabase.html")
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Samaria, F.S.", "Harter, A.C." },
		title = "Parameterisation of a stochastic model for human face identification",
		year = "1994",
		booktitle = "Applications of Computer Vision, 1994., Proceedings of the Second IEEE Workshop on",
		pages = { "138 ", "142" },
		month = "dec")
public class ATandTDataset extends MapBackedDataset<Integer, ListDataset<FImage>, FImage> {
	/**
	 * Construct the dataset. The dataset must be in a directory called
	 * "att_faces" within a directory called "Data" within your home directory
	 * (the "user.home" system property).
	 * 
	 * @throws IOException
	 *             if an error occurs.
	 */
	public ATandTDataset() throws IOException {
		this(new File(System.getProperty("user.home"), "Data/att_faces"));
	}

	/**
	 * Construct with the given path to the dataset
	 * 
	 * @param baseDir
	 *            the dataset path
	 * 
	 * @throws IOException
	 *             if an error occurs.
	 */
	public ATandTDataset(File baseDir) throws IOException {
		super();

		for (int s = 1; s <= 40; s++) {
			final ListBackedDataset<FImage> list = new ListBackedDataset<FImage>();
			map.put(s, list);

			for (int i = 1; i <= 10; i++) {
				final File file = new File(baseDir, "s" + s + "/" + i + ".pgm");

				final FImage image = ImageUtilities.readF(file);

				list.add(image);
			}
		}
	}
}
