package org.openimaj.tools.faces.recognition;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.ExperimentRunner;
import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFold;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.IdentityAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.IdentityFaceDetector;
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.recognition.benchmarking.CrossValidationBenchmark;
import org.openimaj.image.processing.face.recognition.benchmarking.FaceRecogniserProvider;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.TextFileDataset;
import org.openimaj.tools.faces.recognition.options.EngineProvider;
import org.openimaj.tools.faces.recognition.options.RecognitionStrategy;

public class FaceRecognitionCrossValidator<FACE extends DetectedFace> {
	@Option(name="-s", aliases="--strategy", usage="Recognition strategy", required=false, handler = ProxyOptionHandler.class)
	RecognitionStrategy strategy = RecognitionStrategy.EigenFaces_KNN;
	EngineProvider strategyOp;
		
	@Option(name="-dataset", aliases="--dataset", usage="File formatted as each line being: IDENTIFIER,img", required=true)
	File datasetFile;
	
	protected void performBenchmark() throws IOException {
		final FaceRecognitionEngine<FACE, ?, String> engine = strategyOp.createRecognitionEngine();
		
		CrossValidationBenchmark<String, FImage, FACE> benchmark = new CrossValidationBenchmark<String, FImage, FACE>();
		
		benchmark.crossValidator = new StratifiedGroupedKFold<String, FACE>(10);
		benchmark.dataset = getDataset();
		benchmark.faceDetector = engine.getDetector();
		benchmark.engine = new FaceRecogniserProvider<FACE, String>() {
			@Override
			public FaceRecogniser<FACE, ?, String> create(GroupedDataset<String, ListDataset<FACE>, FACE> dataset)
			{
				//Note: we need a new instance of a recogniser, hence we don't use the engine object.
				FaceRecogniser<FACE, ?, String> rec = (FaceRecogniser<FACE, ?, String>) strategyOp.createRecognitionEngine().getRecogniser();
				//FaceRecogniser<FACE, ?, String> rec = engine.getRecogniser();
				//FaceRecogniser<FACE, ?, String> rec = EigenFaceRecogniser.create(10, new IdentityAligner<FACE>(), 1, DoubleFVComparison.EUCLIDEAN, 0);
				
				rec.train(dataset);
				
				return rec;
			}
			
			@Override
			public String toString() {
				return engine.getRecogniser().toString();
			}
		};
		
		ExperimentContext ctx = ExperimentRunner.runExperiment(benchmark);
		
		System.out.println(ctx);

	}
	
	private GroupedDataset<String, ListDataset<FImage>, FImage> getDataset() throws IOException {
		return new TextFileDataset(datasetFile);
	}

	public static <FACE extends DetectedFace> void main(String [] args) throws IOException {
		FaceRecognitionCrossValidator<FACE> frcv = new FaceRecognitionCrossValidator<FACE>();
		CmdLineParser parser = new CmdLineParser( frcv );
		
        try
        {
	        parser.parseArgument( args );
        }
        catch( CmdLineException e )
        {
	        System.err.println( e.getMessage() );
	        System.err.println( "java FaceRecognitionCrossValidator options...");
	        parser.printUsage( System.err );
	        
	        System.err.println();
	        System.err.println("Strategy information:");
	        for (RecognitionStrategy s : RecognitionStrategy.values()) {
	        	System.err.println(s);
	        }
	        return;
        }
        
        frcv.performBenchmark();
	}
}
