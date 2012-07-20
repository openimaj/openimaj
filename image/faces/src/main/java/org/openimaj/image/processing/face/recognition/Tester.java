package org.openimaj.image.processing.face.recognition;

import java.io.File;
import java.io.IOException;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationOperation;
import org.openimaj.experiment.validation.ValidationRunner;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFoldIterable;
import org.openimaj.feature.FVProviderExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram.Extractor;
import org.openimaj.ml.annotation.basic.KNNAnnotator;

public class Tester {
	public static void main(String[] args) throws IOException {
		MapBackedDataset<Integer, ListDataset<DetectedFace>, DetectedFace> dataset = 
			new MapBackedDataset<Integer, ListDataset<DetectedFace>, DetectedFace>();

		for (int s=1; s<=40; s++) {
			ListBackedDataset<DetectedFace> list = new ListBackedDataset<DetectedFace>();
			dataset.getMap().put(s, list);

			for (int i=1; i<=10; i++) {
				File file = new File("/Users/jsh2/Downloads/att_faces/s" + s + "/" + i + ".pgm");

				FImage image = ImageUtilities.readF(file);

				list.add(new DetectedFace(null, image));
			}
		}

		LocalLBPHistogram.Extractor<DetectedFace> extractor = new LocalLBPHistogram.Extractor<DetectedFace>();
		final FVProviderExtractor<FloatFV, DetectedFace, Extractor<DetectedFace>> extractor2 = FVProviderExtractor.create(extractor);
		
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
				FaceRecogniser<DetectedFace, FVProviderExtractor<FloatFV, DetectedFace, Extractor<DetectedFace>>, Integer> rec = 
					new AnnotatorFaceRecogniser<DetectedFace, 
							FVProviderExtractor<FloatFV, DetectedFace, 
							Extractor<DetectedFace>>, Integer>(
						new KNNAnnotator<
							DetectedFace, 
							Integer, 
							FVProviderExtractor<FloatFV, DetectedFace, Extractor<DetectedFace>>, 
							FloatFV>(extractor2, FloatFVComparison.EUCLIDEAN)
					);
				
				rec.train(training);
				
				ClassificationEvaluator<CMResult<Integer>, Integer, DetectedFace> eval = 
					new ClassificationEvaluator<CMResult<Integer>, Integer, DetectedFace>(rec, validation, new CMAnalyser<DetectedFace, Integer>());
				
				return eval.analyse(eval.evaluate());
			}
		});

		System.out.println(score);
	}
}
