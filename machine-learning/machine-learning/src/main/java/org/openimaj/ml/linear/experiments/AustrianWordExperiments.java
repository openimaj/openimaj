package org.openimaj.ml.linear.experiments;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.DenseVectorFactoryMTJ;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openimaj.io.IOUtils;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator;
import org.openimaj.ml.linear.data.BillMatlabFileDataGenerator.Mode;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;
import org.openimaj.ml.linear.learner.init.SingleValueInitStrat;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.util.pair.Pair;

public class AustrianWordExperiments extends BilinearExperiment {
	public static void main(String[] args) throws IOException {
		final AustrianWordExperiments exp = new AustrianWordExperiments();
		exp.performExperiment();
	}

	@Override
	public void performExperiment() throws IOException {
		final BilinearLearnerParameters params = new BilinearLearnerParameters();
		params.put(BilinearLearnerParameters.ETA0_U, 0.0002);
		params.put(BilinearLearnerParameters.ETA0_W, 0.002);
		params.put(BilinearLearnerParameters.LAMBDA, 0.001);
		params.put(BilinearLearnerParameters.BICONVEX_TOL, 0.05);
		params.put(BilinearLearnerParameters.BICONVEX_MAXITER, 5);
		params.put(BilinearLearnerParameters.BIAS, true);
		params.put(BilinearLearnerParameters.ETA0_BIAS, 0.05);
		params.put(BilinearLearnerParameters.WINITSTRAT, new SingleValueInitStrat(0.1));
		params.put(BilinearLearnerParameters.UINITSTRAT, new SparseZerosInitStrategy());
		final BillMatlabFileDataGenerator bmfdg = new BillMatlabFileDataGenerator(new File(BILL_DATA()), 98, true);
		prepareExperimentLog(params);
		final int fold = 0;
		// File foldParamFile = new
		// File(prepareExperimentRoot(),String.format("fold_%d_learner", fold));
		final File foldParamFile = new File(
				"/Users/ss/Dropbox/TrendMiner/deliverables/year2-18month/Austrian Data/streamingExperiments/experiment_1365684128359/fold_0_learner");
		logger.debug("Fold: " + fold);
		BilinearSparseOnlineLearner learner = new BilinearSparseOnlineLearner(params);
		learner.reinitParams();
		bmfdg.setFold(fold, Mode.TEST);

		logger.debug("...training");
		bmfdg.setFold(fold, Mode.TRAINING);
		int j = 0;
		if (!foldParamFile.exists()) {

			while (true) {
				final Pair<Matrix> next = bmfdg.generate();
				if (next == null)
					break;
				logger.debug("...trying item " + j++);
				learner.process(next.firstObject(), next.secondObject());
			}
			System.out.println("Writing W and U to: " + foldParamFile);
			IOUtils.writeBinary(foldParamFile, learner);
		} else {
			learner = IOUtils.read(foldParamFile, BilinearSparseOnlineLearner.class);
		}

		final Matrix w = learner.getW();
		final int ncols = w.getNumColumns();
		final int nwords = 20;
		for (int c = 0; c < ncols; c++) {
			System.out.println("Top " + nwords + " words for: " + bmfdg.getTasks()[c]);
			final Vector col = w.getColumn(c);
			final double[] wordWeights = new DenseVectorFactoryMTJ().copyVector(col).getArray();
			final Integer[] integerRange = ArrayIndexComparator.integerRange(wordWeights);
			Arrays.sort(integerRange, new ArrayIndexComparator(wordWeights));
			for (int i = wordWeights.length - 1; i >= wordWeights.length - nwords; i--) {
				System.out
						.printf("%s: %1.5f\n", bmfdg.getVocabulary().get(integerRange[i]), wordWeights[integerRange[i]]);
			}
		}
	}
}
