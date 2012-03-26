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
package org.openimaj.image.processing.face.recognition.benchmarking;

import java.io.IOException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.FacialFeatureFactory;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram;
import org.openimaj.image.processing.face.feature.comparison.FaceFVComparator;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceMatchResult;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.SimpleKNNRecogniser;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.FaceDataset;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.FaceInstance;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.GeorgiaTechFaceDataset;
import org.openimaj.image.processing.face.recognition.benchmarking.split.FaceDatasetSplitter;
import org.openimaj.image.processing.face.recognition.benchmarking.split.PercentageRandomPerClassSplit;

public class Benchmark<K, T extends DetectedFace> {
	FaceDataset<K, T> dataset;
	FaceDatasetSplitter<K, T> splitter;
	FaceRecogniser<T> recogniser;

	public Benchmark(FaceDataset<K, T> dataset, FaceDatasetSplitter<K, T> splitter, FaceRecogniser<T> recogniser) {
		this.dataset = dataset;
		this.splitter = splitter;
		this.recogniser = recogniser;
	}

	public void run(int iterations) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for (int i=0; i<iterations; i++) {
			recogniser.reset();
			splitter.split(dataset);

//			long t1 = System.currentTimeMillis();
			train(splitter.getTrainingDataset());
//			long t2 = System.currentTimeMillis();
			double score = test(splitter.getTestingDataset());
//			long t3 = System.currentTimeMillis();
			
//			System.err.println("training took " + (t2-t1) + "ms");
//			System.err.println("testing took " + (t3-t2) + "ms");
			
			stats.addValue(score);
		}
		
		System.out.println(stats.getMean() + "\t" + stats.getVariance());
	}

	private double test(FaceDataset<K, T> testingDataset) {
		int correct = 0;
		int incorrect = 0;
		
		for (K key : testingDataset.getGroups()) {
			String identifier = key.toString();
			
			for (FaceInstance<T> f : testingDataset.getItems(key)) {
				FaceMatchResult match = recogniser.queryBestMatch(f.face);
				
				if (identifier.equals(match.getIdentifier())) {
					correct++;
				} else {
					incorrect++;
				}
			}
		}
		
		return (double)correct / (double)(correct + incorrect);
	}

	private void train(FaceDataset<K, T> trainingDataset) {
		for (K key : trainingDataset.getGroups()) {
			String identifier = key.toString();
			
			for (FaceInstance<T> f : trainingDataset.getItems(key)) {
				recogniser.addInstance(identifier, f.face);
			}
		}
		
		recogniser.train();
	}
	
	public static void main(String [] args) throws IOException, ClassNotFoundException {
		FaceDataset<Integer, KEDetectedFace> dataset = new GeorgiaTechFaceDataset<KEDetectedFace>(new FKEFaceDetector());
		
//		FacialFeatureFactory<ReversedLtpDtFeature, KEDetectedFace> factory = new ReversedLtpDtFeature.Factory<KEDetectedFace>(new AffineAligner(), new TruncatedWeighting());
//		FacialFeatureComparator<ReversedLtpDtFeature> comparator = new ReversedLtpDtFeatureComparator();
		
//		FacialFeatureFactory<FacePatchFeature, KEDetectedFace> factory = new FacePatchFeature.Factory();
//		FacialFeatureComparator<FacePatchFeature> comparator = new FaceFVComparator<FacePatchFeature>();
		
		FacialFeatureFactory<LocalLBPHistogram, KEDetectedFace> factory = new LocalLBPHistogram.Factory<KEDetectedFace>(new AffineAligner(), 20, 20, 8, 2);
		FacialFeatureComparator<LocalLBPHistogram> comparator = new FaceFVComparator<LocalLBPHistogram>();
		
		System.out.println("training split size\tk\tmean accuracy\tvariance");
		for (float i=0.1f; i<1f; i+=0.1f) {
			FaceDatasetSplitter<Integer, KEDetectedFace> splitter = new PercentageRandomPerClassSplit<Integer, KEDetectedFace>(i);
			
			for (int k=1; k<=1; k+=2) {
				System.out.print(i + "\t" + k + "\t");
			
//				FaceRecogniser<KEDetectedFace> recogniser = new SimpleKNNRecogniser<ReversedLtpDtFeature, KEDetectedFace>(factory, comparator, k);
//				FaceRecogniser<KEDetectedFace> recogniser = new SimpleKNNRecogniser<FacePatchFeature, KEDetectedFace>(factory, comparator, k);
				FaceRecogniser<KEDetectedFace> recogniser = new SimpleKNNRecogniser<LocalLBPHistogram, KEDetectedFace>(factory, comparator, k);
				
				Benchmark<Integer, KEDetectedFace> benchmark = new Benchmark<Integer, KEDetectedFace>(dataset, splitter, recogniser);
				benchmark.run(3);
			}
		}
	}
}
