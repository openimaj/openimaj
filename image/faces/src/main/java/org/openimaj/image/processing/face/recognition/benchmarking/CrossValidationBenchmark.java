package org.openimaj.image.processing.face.recognition.benchmarking;

import org.openimaj.experiment.RunnableExperiment;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.AggregatedCMResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationOperation;
import org.openimaj.experiment.validation.ValidationRunner;
import org.openimaj.experiment.validation.cross.CrossValidator;
import org.openimaj.image.Image;
import org.openimaj.image.processing.face.detection.DatasetFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;

@Experiment(author = "Jonathon Hare", dateCreated = "20120726", description = "Face recognition experiment")
public class CrossValidationBenchmark<KEY, IMAGE extends Image<?, IMAGE>, FACE extends DetectedFace> implements RunnableExperiment {
	@IndependentVariable
	GroupedDataset<KEY, ListDataset<IMAGE>, IMAGE> dataset;
	
	@IndependentVariable
	CrossValidator<GroupedDataset<KEY, ListDataset<FACE>, FACE>> crossValidator;
	
	@IndependentVariable
	FaceDetector<FACE, IMAGE> faceDetector;
	
	@IndependentVariable
	FaceRecogniserProvider<FACE, KEY> engine;

	@DependentVariable
	AggregatedCMResult<KEY> result;
	
	@Override
	public void performExperiment() {
		CMAggregator<KEY> aggregator = new CMAggregator<KEY>();
		
		GroupedDataset<KEY, ListDataset<FACE>, FACE> faceDataset = DatasetFaceDetector.process(dataset, faceDetector);
		
		result = ValidationRunner.run(
				aggregator, 
				faceDataset, 
				crossValidator, 
				new ValidationOperation<GroupedDataset<KEY, ListDataset<FACE>, FACE>, CMResult<KEY>>() 
		{
			@Override
			public CMResult<KEY> evaluate(
					GroupedDataset<KEY, ListDataset<FACE>, FACE> training,
					GroupedDataset<KEY, ListDataset<FACE>, FACE> validation) 
			{
				FaceRecogniser<FACE, ?, KEY> rec = engine.create(training);
				
				ClassificationEvaluator<CMResult<KEY>, KEY, FACE> eval = 
					new ClassificationEvaluator<CMResult<KEY>, KEY, FACE>(
							rec, validation, new CMAnalyser<FACE, KEY>()
					);
				
				return eval.analyse(eval.evaluate());
			}
		});
	}
}
