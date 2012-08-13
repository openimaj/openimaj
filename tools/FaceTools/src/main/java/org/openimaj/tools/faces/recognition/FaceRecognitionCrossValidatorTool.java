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
import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecogniser;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.image.processing.face.recognition.benchmarking.CrossValidationBenchmark;
import org.openimaj.image.processing.face.recognition.benchmarking.FaceRecogniserProvider;
import org.openimaj.image.processing.face.recognition.benchmarking.dataset.TextFileDataset;
import org.openimaj.tools.faces.recognition.options.RecognitionEngineProvider;
import org.openimaj.tools.faces.recognition.options.RecognitionStrategy;

/**
 * A command line tool for performing cross-validation experiments for face
 * recognition or classification. {@link StratifiedGroupedKFold} cross
 * validation is used internally.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FACE>
 *            Type of {@link DetectedFace}
 */
public class FaceRecognitionCrossValidatorTool<FACE extends DetectedFace> {
	@Option(name = "--strategy", usage = "Recognition strategy", required = false, handler = ProxyOptionHandler.class)
	RecognitionStrategy strategy = RecognitionStrategy.EigenFaces_KNN;
	RecognitionEngineProvider strategyOp;

	@Option(name = "--dataset", usage = "File formatted as each line being: IDENTIFIER,img", required = true)
	File datasetFile;

	@Option(name = "--num-folds", usage = "number of cross-validation folds", required = false)
	int numFolds = 10;

	protected void performBenchmark() throws IOException {
		final FaceRecognitionEngine<FACE, ?, String> engine = strategyOp.createRecognitionEngine();

		final CrossValidationBenchmark<String, FImage, FACE> benchmark = new CrossValidationBenchmark<String, FImage, FACE>();

		benchmark.crossValidator = new StratifiedGroupedKFold<String, FACE>(10);
		benchmark.dataset = getDataset();
		benchmark.faceDetector = engine.getDetector();
		benchmark.engine = new FaceRecogniserProvider<FACE, String>() {
			@Override
			public FaceRecogniser<FACE, ?, String> create(GroupedDataset<String, ListDataset<FACE>, FACE> dataset) {
				// Note: we need a new instance of a recogniser, hence we don't
				// use the engine object.
				@SuppressWarnings("unchecked")
				final FaceRecogniser<FACE, ?, String> rec = (FaceRecogniser<FACE, ?, String>) strategyOp
						.createRecognitionEngine().getRecogniser();

				rec.train(dataset);

				return rec;
			}

			@Override
			public String toString() {
				return engine.getRecogniser().toString();
			}
		};

		final ExperimentContext ctx = ExperimentRunner.runExperiment(benchmark);

		System.out.println(ctx);

	}

	private GroupedDataset<String, ListDataset<FImage>, FImage> getDataset() throws IOException {
		return new TextFileDataset(datasetFile);
	}

	/**
	 * The main method for the tool
	 * 
	 * @param <FACE>
	 *            Type of {@link DetectedFace}
	 * @param args
	 *            tool arguments
	 * @throws IOException
	 */
	public static <FACE extends DetectedFace> void main(String[] args) throws IOException {
		final FaceRecognitionCrossValidatorTool<FACE> frcv = new FaceRecognitionCrossValidatorTool<FACE>();
		final CmdLineParser parser = new CmdLineParser(frcv);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java FaceRecognitionCrossValidator options...");
			parser.printUsage(System.err);

			System.err.println();
			System.err.println("Strategy information:");
			for (final RecognitionStrategy s : RecognitionStrategy.values()) {
				System.err.println(s);
			}
			return;
		}

		frcv.performBenchmark();
	}
}
