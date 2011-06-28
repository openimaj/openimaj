package org.openimaj.image.processing.face.recognition.benchmarking;

import java.io.IOException;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.FacialFeatureFactory;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.image.processing.face.feature.comparison.ReversedLtpDtFeatureComparator;
import org.openimaj.image.processing.face.feature.ltp.ReversedLtpDtFeature;
import org.openimaj.image.processing.face.feature.ltp.TruncatedWeighting;
import org.openimaj.image.processing.face.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceMatchResult;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.SimpleKNNRecogniser;
import org.openimaj.image.processing.face.recognition.benchmarking.split.FaceDatasetSplitter;
import org.openimaj.image.processing.face.recognition.benchmarking.split.PercentageRandomPerClassSplit;
import org.openimaj.image.processing.face.recognition.dataset.FaceDataset;
import org.openimaj.image.processing.face.recognition.dataset.GeorgiaTechFaceDataset;

public class Benchmark<T extends DetectedFace> {
	FaceDataset<T> dataset;
	FaceDatasetSplitter<T> splitter;
	FaceRecogniser<T> recogniser;

	public Benchmark(FaceDataset<T> dataset, FaceDatasetSplitter<T> splitter, FaceRecogniser<T> recogniser) {
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

	private double test(FaceDataset<T> testingDataset) {
		List<List<T>> data = testingDataset.getData();
		int correct = 0;
		int incorrect = 0;
		
		for (int i=0; i<data.size(); i++) {
			String identifier = "" + i;
			
			for (T f : data.get(i)) {
//				long t1 = System.currentTimeMillis();
				FaceMatchResult match = recogniser.queryBestMatch(f);
//				long t2 = System.currentTimeMillis();
				
//				System.err.println("took " + (t2-t1) + "ms");
				
				if (identifier.equals(match.getIdentifier())) {
					correct++;
				} else {
					incorrect++;
				}
			}
		}
		
		return (double)correct / (double)(correct + incorrect);
	}

	private void train(FaceDataset<T> trainingDataset) {
		List<List<T>> data = trainingDataset.getData();

		for (int i=0; i<data.size(); i++) {
			String identifier = "" + i;
			for (T f : data.get(i)) {
				recogniser.addInstance(identifier, f);
			}
		}
		
		recogniser.train();
	}
	
	public static void main(String [] args) throws IOException, ClassNotFoundException {
		FaceDataset<KEDetectedFace> dataset = new GeorgiaTechFaceDataset<KEDetectedFace>(new FKEFaceDetector());
		
		FacialFeatureFactory<ReversedLtpDtFeature, KEDetectedFace> factory = new ReversedLtpDtFeature.Factory<KEDetectedFace>(new AffineAligner(), new TruncatedWeighting());
		FacialFeatureComparator<ReversedLtpDtFeature> comparator = new ReversedLtpDtFeatureComparator();
		
//		FacialFeatureFactory<FacePatchFeature> factory = new FacePatchFeature.Factory();
		
		System.out.println("training split size\tk\tmean accuracy\tvariance");
		for (float i=0.1f; i<1f; i+=0.1f) {
			FaceDatasetSplitter<KEDetectedFace> splitter = new PercentageRandomPerClassSplit<KEDetectedFace>(i);
			
			for (int k=1; k<=1; k+=2) {
				System.out.print(i + "\t" + k + "\t");
			
				FaceRecogniser<KEDetectedFace> recogniser = new SimpleKNNRecogniser<ReversedLtpDtFeature, KEDetectedFace>(factory, comparator, k);
//				FaceRecogniser recogniser = new SimpleKNNRecogniser<FacePatchFeature>(factory, k);
				Benchmark<KEDetectedFace> benchmark = new Benchmark<KEDetectedFace>(dataset, splitter, recogniser);
				benchmark.run(3);
			}
		}
	}
}
