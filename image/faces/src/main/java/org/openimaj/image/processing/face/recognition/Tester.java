package org.openimaj.image.processing.face.recognition;

import java.io.File;
import java.io.IOException;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationOperation;
import org.openimaj.experiment.validation.ValidationRunner;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFoldIterable;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FVProviderExtractor;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.alignment.NullAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.EigenFaceFeature;
import org.openimaj.image.processing.face.feature.FisherFaceFeature.Extractor;
import org.openimaj.image.processing.face.feature.FisherFaceFeature;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.ATandTDataset;
import org.openimaj.ml.annotation.basic.KNNAnnotator;

public class Tester {
	public static void main(String[] args) throws IOException {
		MapBackedDataset<Integer, ListDataset<DetectedFace>, DetectedFace> dataset = new ATandTDataset();
//		LocalLBPHistogram.Extractor<DetectedFace> extractor = new LocalLBPHistogram.Extractor<DetectedFace>();
//		final FeatureExtractor<FloatFV, DetectedFace> extractor2 = FVProviderExtractor.create(extractor);

		//Extractor<DetectedFace> extractor = new EigenFaceFeature.Extractor<DetectedFace>(15, new NullAligner<DetectedFace>());
		Extractor<DetectedFace> extractor = new FisherFaceFeature.Extractor<DetectedFace>(15, new NullAligner<DetectedFace>());
		extractor.train(dataset);
		
		final FeatureExtractor<DoubleFV, DetectedFace> extractor2 = FVProviderExtractor.create(extractor);
		
		CMAggregator<Integer> aggregator = new CMAggregator<Integer>();
		AnalysisResult score = new ValidationRunner().run(aggregator, 
				new StratifiedGroupedKFoldIterable<Integer, DetectedFace>(dataset, 10), 
				new ValidationOperation<GroupedDataset<Integer, ListDataset<DetectedFace>, DetectedFace>, CMResult<Integer>>() 
		{
			@Override
			public CMResult<Integer> evaluate(
					GroupedDataset<Integer, ListDataset<DetectedFace>, DetectedFace> training,
					GroupedDataset<Integer, ListDataset<DetectedFace>, DetectedFace> validation) 
			{
				KNNAnnotator<DetectedFace, Integer, FeatureExtractor<DoubleFV, DetectedFace>, DoubleFV> knn = 
					KNNAnnotator.create(extractor2, DoubleFVComparison.EUCLIDEAN);
				
				FaceRecogniser<DetectedFace, FeatureExtractor<DoubleFV, DetectedFace>, Integer> rec = 
					AnnotatorFaceRecogniser.create(
						knn
					);
				
				rec.train(training);
				
				ClassificationEvaluator<CMResult<Integer>, Integer, DetectedFace> eval = 
					new ClassificationEvaluator<CMResult<Integer>, Integer, DetectedFace>(
							rec, validation, new CMAnalyser<DetectedFace, Integer>()
					);
				
				return eval.analyse(eval.evaluate());
			}
		});

		System.out.println(score);
		System.out.println(score.getDetailReport());
	}
}
