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
package org.openimaj.image.processing.face.detection.benchmarking;

import gnu.trove.set.hash.TDoubleHashSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.data.dataset.ListDataset;
import org.openimaj.image.objectdetection.filtering.OpenCVGrouping;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.benchmarking.Matcher.Match;

public class FDDBEvaluation {
	public interface EvaluationDetector {
		List<? extends DetectedFace> getDetections(FDDBRecord record);
	}

	public List<Results> performEvaluation(ListDataset<FDDBRecord> dataset, EvaluationDetector detector) {
		// cumRes stores the cumulative results for all the images
		List<Results> cumRes = new ArrayList<Results>();
		final Matcher matcher = new Matcher();

		// Process each image
		final int numImages = dataset.size();
		for (int i = 0; i < numImages; i++) {
			final FDDBRecord data = dataset.getInstance(i);
			final String imName = data.getImageName();

			final List<? extends DetectedFace> annot = data.getGroundTruth();
			final List<? extends DetectedFace> det = detector.getDetections(data);

			// imageResults holds the results for different thresholds
			// applied to a single image
			final List<Results> imageResults = new ArrayList<Results>();

			if (det.size() == 0) {
				// create the image results for zero detections
				final Results r = new Results(imName, Double.MAX_VALUE, null, annot, det);
				imageResults.add(r);
			} else {
				// find the unique values for detection scores
				final double[] uniqueScores = getUniqueConfidences(det);

				// For each unique score value st,
				// (a) filter the detections with score >= st
				// (b) compute the matching annot-det pairs
				// (c) compute the result statistics
				for (final double scoreThreshold : uniqueScores) {
					final ArrayList<DetectedFace> filteredDet = new ArrayList<DetectedFace>();
					// (a) filter the detections with score >= st
					for (int di = 0; di < det.size(); di++) {
						final DetectedFace rd = det.get(di);
						if (rd.getConfidence() >= scoreThreshold)
							filteredDet.add(rd);
					}

					// (b) match annotations to detections
					final List<Match> mps = matcher.match(annot, filteredDet);

					// (c) compute the result statistics and append to the list
					final Results r = new Results(imName, scoreThreshold, mps, annot, filteredDet);
					imageResults.add(r);
				}
			}

			// merge the list of results for this image (imageResults) with the
			// global list (cumRes)
			cumRes = Results.merge(cumRes, imageResults);
		}

		return cumRes;
	}

	/**
	 * Get a list of unique confidences associated with the given list of faces.
	 * 
	 * @param faces
	 *            the faces
	 * @return the unique confidences, sorted in ascending order.
	 */
	private double[] getUniqueConfidences(List<? extends DetectedFace> faces) {
		final TDoubleHashSet set = new TDoubleHashSet();

		for (final DetectedFace f : faces) {
			set.add(f.getConfidence());
		}

		final double[] ret = set.toArray();

		Arrays.sort(ret);

		return ret;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		final File fddbGroundTruth = new File("/Users/jsh2/Downloads/FDDB-folds/FDDB-fold-01-ellipseList.txt");
		final File imageBase = new File("/Users/jsh2/Downloads/originalPics/");
		final FDDBDataset dataset = new FDDBDataset(fddbGroundTruth, imageBase, true);

		final HaarCascadeDetector det = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
		det.setGroupingFilter(new OpenCVGrouping(0));
		det.setMinSize(80);
		final EvaluationDetector evDet = new EvaluationDetector() {

			@Override
			public synchronized List<? extends DetectedFace> getDetections(FDDBRecord record) {
				final List<DetectedFace> faces = det.detectFaces(record.getFImage());

				// for (final DetectedFace f : faces)
				// f.setConfidence(1);

				return faces;
			}
		};

		final FDDBEvaluation eval = new FDDBEvaluation();
		final List<Results> result = eval.performEvaluation(dataset, evDet);

		System.out.println(Results.getROCData(result));
	}
}
