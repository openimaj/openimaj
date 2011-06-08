package org.openimaj.image.processing.face.recognition.benchmarking;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.image.processing.face.features.FacialFeatureFactory;
import org.openimaj.image.processing.face.features.TruncatedDistanceLTPFeature;
import org.openimaj.image.processing.face.parts.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceMatchResult;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.SimpleKNNRecogniser;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.FaceDataset;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.GeorgiaTechFaceDataset;
import org.openimaj.image.processing.face.recognition.benchmarking.split.FaceDatasetSplitter;
import org.openimaj.image.processing.face.recognition.benchmarking.split.PercentageRandomPerClassSplit;

public class Benchmark {
	FaceDataset dataset;
	FaceDatasetSplitter splitter;
	FaceRecogniser recogniser;

	public Benchmark(FaceDataset dataset, FaceDatasetSplitter splitter, FaceRecogniser recogniser) {
		this.dataset = dataset;
		this.splitter = splitter;
		this.recogniser = recogniser;
	}

	public void run(int iterations) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for (int i=0; i<iterations; i++) {
			recogniser.reset();
			splitter.split(dataset);

			train(splitter.getTrainingDataset());
			
			double score = test(splitter.getTestingDataset());
			
			stats.addValue(score);
		}
		
		System.out.println(stats.getMean() + "\t" + stats.getVariance());
	}

	private double test(FaceDataset testingDataset) {
		List<List<DetectedFace>> data = testingDataset.getData();
		int correct = 0;
		int incorrect = 0;
		
		for (int i=0; i<data.size(); i++) {
			String identifier = "" + i;
			
			for (DetectedFace f : data.get(i)) {
				FaceMatchResult match = recogniser.queryBestMatch(f);
				
				if (identifier.equals(match.getIdentifier())) {
					correct++;
				} else {
					incorrect++;
				}
			}
		}
		
		return (double)correct / (double)(correct + incorrect);
	}

	private void train(FaceDataset trainingDataset) {
		List<List<DetectedFace>> data = trainingDataset.getData();

		for (int i=0; i<data.size(); i++) {
			String identifier = "" + i;
			for (DetectedFace f : data.get(i)) {
				recogniser.addInstance(identifier, f);
			}
		}
	}
	
	public static void main(String [] args) throws IOException, ClassNotFoundException {
		FaceDataset dataset = new GeorgiaTechFaceDataset(new File("/Users/jon/Downloads/gt_db"));
		FacialFeatureFactory<TruncatedDistanceLTPFeature> factory = new TruncatedDistanceLTPFeature.Factory();
		
		System.out.println("training split size\tk\tmean accuracy\tvariance");
		for (float i=0.1f; i<=0.9f; i+=0.1f) {
			FaceDatasetSplitter splitter = new PercentageRandomPerClassSplit(i);
			
			for (int k=1; k<=3; k++) {
				System.out.print(i + "\t" + k + "\t");
			
				FaceRecogniser recogniser = new SimpleKNNRecogniser<TruncatedDistanceLTPFeature>(factory, 1);
				Benchmark benchmark = new Benchmark(dataset, splitter, recogniser);
				benchmark.run(20);
			}
		}
	}
}
