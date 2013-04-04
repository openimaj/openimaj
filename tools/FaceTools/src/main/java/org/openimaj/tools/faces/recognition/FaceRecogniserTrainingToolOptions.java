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
package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.tools.faces.recognition.options.RecognitionEngineProvider;
import org.openimaj.tools.faces.recognition.options.RecognitionStrategy;

class FaceRecogniserTrainingToolOptions {
	// public enum RecognitionStrategy {
	// LTP_DT_TRUNCATED_REVERSED_AFFINE_1NN {
	// @Override
	// public FaceRecognitionEngine<?, ?, String> newRecognitionEngine() {
	// LtpDtFeature.Extractor<KEDetectedFace> extractor = new
	// LtpDtFeature.Extractor<KEDetectedFace>(new AffineAligner(), new
	// TruncatedWeighting());
	// FacialFeatureComparator<LtpDtFeature> comparator = new
	// ReversedLtpDtFeatureComparator();
	//
	// AnnotatorFaceRecogniser<KEDetectedFace,
	// LtpDtFeature.Extractor<KEDetectedFace>, String> recogniser =
	// AnnotatorFaceRecogniser.create(
	// new KNNAnnotator<KEDetectedFace, String,
	// LtpDtFeature.Extractor<KEDetectedFace>, LtpDtFeature>(extractor,
	// comparator, 1)
	// );
	//
	// FKEFaceDetector detector = new FKEFaceDetector();
	//
	// return FaceRecognitionEngine.create(detector, recogniser);
	// }
	//
	// @Override
	// public String description() {
	// return
	// "Local Ternary Pattern feature using truncated distance-maps for comparison in a 1NN classifier. Faces aligned using affine transform.";
	// }
	// },
	// FACEPATCH_EUCLIDEAN_1NN {
	// @Override
	// public FaceRecognitionEngine<?, ?, String> newRecognitionEngine() {
	// FacePatchFeature.Extractor extractor = new FacePatchFeature.Extractor();
	// FacialFeatureComparator<FacePatchFeature> comparator = new
	// FaceFVComparator<FacePatchFeature>(FloatFVComparison.EUCLIDEAN);
	//
	// AnnotatorFaceRecogniser<KEDetectedFace, FacePatchFeature.Extractor,
	// String> recogniser = AnnotatorFaceRecogniser.create(
	// new KNNAnnotator<KEDetectedFace, String, FacePatchFeature.Extractor,
	// FacePatchFeature>(extractor, comparator, 1)
	// );
	//
	// FKEFaceDetector detector = new FKEFaceDetector();
	//
	// return FaceRecognitionEngine.create(detector, recogniser);
	// }
	//
	// @Override
	// public String description() {
	// return
	// "Patched facial features, compared as a big vector using Euclidean distance in a 1NN classifier.";
	// }
	// },
	// LBP_LOCAL_HISTOGRAM_AFFINE_1NN {
	//
	// @Override
	// public FaceRecognitionEngine<?, ?, String> newRecognitionEngine() {
	// LocalLBPHistogram.Extractor<KEDetectedFace> extractor = new
	// LocalLBPHistogram.Extractor<KEDetectedFace>(new AffineAligner(), 20, 20,
	// 8, 1);
	// FacialFeatureComparator<LocalLBPHistogram> comparator = new
	// FaceFVComparator<LocalLBPHistogram>(FloatFVComparison.CHI_SQUARE);
	//
	// //SimpleKNNRecogniser<LocalLBPHistogram, KEDetectedFace> recogniser = new
	// SimpleKNNRecogniser<LocalLBPHistogram, KEDetectedFace>(factory,
	// comparator, 1);
	// AnnotatorFaceRecogniser<KEDetectedFace,
	// LocalLBPHistogram.Extractor<KEDetectedFace>, String> recogniser =
	// AnnotatorFaceRecogniser.create(
	// new KNNAnnotator<KEDetectedFace, String,
	// LocalLBPHistogram.Extractor<KEDetectedFace>,
	// LocalLBPHistogram>(extractor, comparator, 1)
	// );
	//
	//
	// FKEFaceDetector detector = new FKEFaceDetector();
	//
	// return FaceRecognitionEngine.create(detector, recogniser);
	// }
	//
	// @Override
	// public String description() {
	// return
	// "Local LBP histograms compared using Chi squared distance in a 1NN classifier. Faces aligned using affine transform.";
	// }
	//
	// },
	// GRANULAR_LBP_LOCAL_HISTOGRAM_AFFINE_1NN {
	//
	// @Override
	// public FaceRecognitionEngine<?, ?, String> newRecognitionEngine() {
	// LocalLBPHistogram.Extractor<KEDetectedFace> extractor = new
	// LocalLBPHistogram.Extractor<KEDetectedFace>(new AffineAligner(), 20, 20,
	// 8, 1);
	// FacialFeatureComparator<LocalLBPHistogram> comparator = new
	// FaceFVComparator<LocalLBPHistogram>(FloatFVComparison.CHI_SQUARE);
	//
	// AnnotatorFaceRecogniser<KEDetectedFace,
	// LocalLBPHistogram.Extractor<KEDetectedFace>, String> recogniser =
	// AnnotatorFaceRecogniser.create(
	// new KNNAnnotator<KEDetectedFace, String,
	// LocalLBPHistogram.Extractor<KEDetectedFace>,
	// LocalLBPHistogram>(extractor, comparator, 1)
	// );
	//
	//
	// FKEFaceDetector detector = new FKEFaceDetector(40);
	//
	// return FaceRecognitionEngine.create(detector, recogniser);
	// }
	//
	// @Override
	// public String description() {
	// return
	// "Local LBP histograms compared using Chi squared distance in a 1NN classifier. Faces aligned using affine transform.";
	// }
	//
	// },
	// LBP_LOCAL_HISTOGRAM_AFFINE_NAIVE_BAYES {
	// @Override
	// public FaceRecognitionEngine<?, ?, String> newRecognitionEngine() {
	// LocalLBPHistogram.Extractor<KEDetectedFace> extractor = new
	// LocalLBPHistogram.Extractor<KEDetectedFace>(new AffineAligner(), 20, 20,
	// 8, 1);
	//
	// FVProviderExtractor<FloatFV, KEDetectedFace, Extractor<KEDetectedFace>>
	// extractorWrapper = FVProviderExtractor.create(extractor);
	//
	// AnnotatorFaceRecogniser<KEDetectedFace, FVProviderExtractor<FloatFV,
	// KEDetectedFace, Extractor<KEDetectedFace>>, String> recogniser =
	// AnnotatorFaceRecogniser.create(
	// new NaiveBayesAnnotator<KEDetectedFace, String,
	// FVProviderExtractor<FloatFV, KEDetectedFace,
	// Extractor<KEDetectedFace>>>(extractorWrapper)
	// );
	//
	// FKEFaceDetector detector = new FKEFaceDetector(40);
	//
	// return FaceRecognitionEngine.create(detector, recogniser);
	// }
	//
	// @Override
	// public String description() {
	// return "";
	// }
	// },
	// ;
	//
	// public abstract <FACE extends DetectedFace, EXTRACTOR extends
	// FeatureExtractor<?, FACE>> FaceRecognitionEngine<FACE, EXTRACTOR, String>
	// newRecognitionEngine();
	// public abstract String description();
	// }

	@Option(name = "-f", aliases = "--file", usage = "Recogniser file", required = true)
	File recogniserFile;

	@Option(
			name = "-s",
			aliases = "--strategy",
			usage = "Recognition strategy",
			required = false,
			handler = ProxyOptionHandler.class)
	RecognitionStrategy strategy = RecognitionStrategy.EigenFaces_KNN;
	RecognitionEngineProvider<?> strategyOp;

	@Option(name = "-id", aliases = "--identifier", usage = "Identifier of person", required = false)
	String identifier;

	@Option(
			name = "-dataset",
			aliases = "--dataset",
			usage = "File formatted as each line being: IDENTIFIER,img",
			required = false)
	File datasetFile;

	@Argument()
	List<File> files;

	@SuppressWarnings("unchecked")
	public <FACE extends DetectedFace>
			FaceRecognitionEngine<FACE, String>
			getEngine() throws IOException
	{
		if (recogniserFile.exists()) {
			return FaceRecognitionEngine.load(recogniserFile);
		}

		return (FaceRecognitionEngine<FACE, String>) strategyOp.createRecognitionEngine();
	}
}
