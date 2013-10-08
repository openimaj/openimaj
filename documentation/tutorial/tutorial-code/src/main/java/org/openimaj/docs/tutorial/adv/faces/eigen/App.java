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
package org.openimaj.docs.tutorial.adv.faces.eigen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * Load the data, and create some training and test data
		 */
		final VFSGroupDataset<FImage> dataset =
				new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip",
						ImageUtilities.FIMAGE_READER);

		final GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(dataset, 5, 0, 5);
		final GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
		final GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

		/*
		 * Now learn the PCA basis
		 */
		final List<FImage> basisImages = DatasetAdaptors.asList(training);
		final int nEigenvectors = 100;
		final EigenImages eigen = new EigenImages(nEigenvectors);
		eigen.train(basisImages);

		/*
		 * Display the top 12 eigenimages
		 */
		final List<FImage> eigenFaces = new ArrayList<FImage>();
		for (int i = 0; i < 12; i++) {
			eigenFaces.add(eigen.visualisePC(i));
		}
		DisplayUtilities.display("EigenFaces", eigenFaces);

		/*
		 * Build a map of person->[features] for all the training data
		 */
		final Map<String, DoubleFV[]> features = new HashMap<String, DoubleFV[]>();
		for (final String person : training.getGroups()) {
			final DoubleFV[] fvs = new DoubleFV[5];

			for (int i = 0; i < 5; i++) {
				final FImage face = training.get(person).get(i);
				fvs[i] = eigen.extractFeature(face);
			}
			features.put(person, fvs);
		}

		/*
		 * Now we can test our performance on the test set
		 */
		double correct = 0, incorrect = 0;
		for (final String truePerson : testing.getGroups()) {
			for (final FImage face : testing.get(truePerson)) {
				final DoubleFV testFeature = eigen.extractFeature(face);

				String bestPerson = null;
				double minDistance = Double.MAX_VALUE;
				for (final String person : features.keySet()) {
					for (final DoubleFV fv : features.get(person)) {
						final double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);

						if (distance < minDistance) {
							minDistance = distance;
							bestPerson = person;
						}
					}
				}

				System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);

				if (truePerson.equals(bestPerson))
					correct++;
				else
					incorrect++;
			}
		}

		System.out.println("Accuracy: " + (correct / (correct + incorrect)));
	}
}
