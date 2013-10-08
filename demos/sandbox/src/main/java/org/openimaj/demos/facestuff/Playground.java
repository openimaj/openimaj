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
package org.openimaj.demos.facestuff;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.ImageUtilities;

public class Playground {
	public static void main(String[] args) throws IOException {
		ImageUtilities.readMBF(new File("/Users/jon/Desktop/im307026.jpg"));
	}

	// public static void main(String[] args) {
	// final BingAPIToken token =
	// DefaultTokenFactory.getInstance().getToken(BingAPIToken.class);
	//
	// final MapBackedDataset<String, BingImageDataset<FImage>, FImage> dataset
	// = MapBackedDataset.of(
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token,
	// "Tom Cruise", "Face:Face", 10),
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token,
	// "Nicole Kidman", "Face:Face", 10),
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token,
	// "Angelina Jolie", "Face:Face", 10),
	// BingImageDataset.create(ImageUtilities.FIMAGE_READER, token, "Brad Pitt",
	// "Face:Face", 10)
	// );
	//
	// for (final Entry<String, BingImageDataset<FImage>> entry :
	// dataset.entrySet()) {
	// DisplayUtilities.display(entry.getKey(), entry.getValue());
	// }
	//
	// final LocalLBPHistogram.Extractor<CLMDetectedFace> extractor = new
	// LocalLBPHistogram.Extractor<CLMDetectedFace>(new CLMAligner(), 20, 20, 8,
	// 1);
	//
	// final FacialFeatureComparator<LocalLBPHistogram> comparator = new
	// FaceFVComparator<LocalLBPHistogram,
	// FloatFV>(FloatFVComparison.EUCLIDEAN);
	//
	// final CrossValidationBenchmark<String, FImage, CLMDetectedFace> cvd = new
	// CrossValidationBenchmark<String, FImage, CLMDetectedFace>(
	// new StratifiedGroupedKFold<String, CLMDetectedFace>(5),
	// dataset,
	// new CLMFaceDetector(),
	// new FaceRecogniserProvider<CLMDetectedFace, String>() {
	// @Override
	// public FaceRecogniser<CLMDetectedFace, String> create(
	// GroupedDataset<String, ? extends ListDataset<CLMDetectedFace>,
	// CLMDetectedFace> dataset)
	// {
	// final KNNAnnotator<CLMDetectedFace, String, LocalLBPHistogram> knn =
	// KNNAnnotator.create(extractor, comparator, 1, 5f);
	//
	// final AnnotatorFaceRecogniser<CLMDetectedFace, String> recogniser =
	// AnnotatorFaceRecogniser.create(knn);
	//
	// recogniser.train(dataset);
	//
	// return recogniser;
	// }
	// });
	//
	// final ExperimentContext ctx = ExperimentRunner.runExperiment(cvd);
	// System.out.println(ctx);
	//
	// }
}
