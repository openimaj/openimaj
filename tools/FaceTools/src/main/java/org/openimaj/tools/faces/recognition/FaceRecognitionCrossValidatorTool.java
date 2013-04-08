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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.ExperimentRunner;
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
	RecognitionEngineProvider<FACE> strategyOp;

	@Option(name = "--dataset", usage = "File formatted as each line being: IDENTIFIER,img", required = true)
	File datasetFile;

	@Option(name = "--num-folds", usage = "number of cross-validation folds", required = false)
	int numFolds = 10;

	@Option(
			name = "--save-recogniser",
			usage = "After cross-validation, create a recogniser using all folds and save it to the given file",
			required = false)
	File savedRecogniser;

	protected void performBenchmark() throws IOException {
		final FaceRecognitionEngine<FACE, String> engine = strategyOp.createRecognitionEngine();

		final CrossValidationBenchmark<String, FImage, FACE> benchmark = new CrossValidationBenchmark<String, FImage, FACE>(
				new StratifiedGroupedKFold<String, FACE>(10),
				getDataset(),
				engine.getDetector(),
				new FaceRecogniserProvider<FACE, String>() {
					@Override
					public FaceRecogniser<FACE, String>
							create(GroupedDataset<String, ? extends ListDataset<FACE>, FACE> dataset)
					{
						// Note: we need a new instance of a recogniser, hence
						// we don't
						// use the engine object.
						final FaceRecogniser<FACE, String> rec = strategyOp.createRecognitionEngine().getRecogniser();

						rec.train(dataset);

						return rec;
					}

					@Override
					public String toString() {
						return engine.getRecogniser().toString();
					}
				}
				);

		final ExperimentContext ctx = ExperimentRunner.runExperiment(benchmark);

		System.out.println(ctx);
	}

	protected void saveRecogniser() throws IOException {
		final FaceRecognitionEngine<FACE, String> engine = strategyOp.createRecognitionEngine();
		engine.train(getDataset());
		engine.save(savedRecogniser);
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

		if (frcv.savedRecogniser != null) {
			frcv.saveRecogniser();
		}
	}
}
