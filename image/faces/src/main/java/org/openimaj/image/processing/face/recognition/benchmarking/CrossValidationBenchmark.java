package org.openimaj.image.processing.face.recognition.benchmarking;

import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.RunnableExperiment;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;
import org.openimaj.experiment.annotations.Time;
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

@Experiment(author = "Jonathon Hare", dateCreated = "2012-07-26", description = "Face recognition cross validation experiment")
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
	public void perform() {
		CMAggregator<KEY> aggregator = new CMAggregator<KEY>();
		
		GroupedDataset<KEY, ListDataset<FACE>, FACE> faceDataset = DatasetFaceDetector.process(dataset, faceDetector);
		
		result = ValidationRunner.run(
				aggregator, 
				faceDataset, 
				crossValidator, 
				new ValidationOperation<GroupedDataset<KEY, ListDataset<FACE>, FACE>, CMResult<KEY>>() 
		{
			@Time(identifier="Train and Evaluate recogniser")
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

	@Override
	public void setup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish(ExperimentContext context) {
		// TODO Auto-generated method stub
		
	}
}
