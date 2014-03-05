package org.openimaj.ml.linear.experiments.sinabill;

import gov.sandia.cognition.math.matrix.Matrix;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class BillWordInvestigation extends BilinearExperiment {

	public static void main(String[] args) throws Exception {
		final BillWordInvestigation bi = new BillWordInvestigation();
		bi.performExperiment();
	}

	@Override
	public void performExperiment() throws Exception {
		final String exproot = "/Users/ss/Dropbox/TrendMiner/deliverables/year2-18month/Austrian Data/streamingExperiments/experiment_1392738388042";
		final File[] folds = new File(exproot).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.contains("fold");
			}
		});

		final int f = (folds.length - 1) / 2;
		final File fold = folds[f];

		final File learner = fold.listFiles()[0];
		final BilinearSparseOnlineLearner l = IOUtils.read(learner, BilinearSparseOnlineLearner.class);

		final Matrix words = l.getW();
		final Matrix users = l.getU();
		final String name = "fold_" + f;
		final MatFileWriter writ = new MatFileWriter();
		final ArrayList<MLArray> col = new ArrayList<MLArray>();
		col.add(new MLDouble("words_" + name, CFMatrixUtils.asJama(words).getArray()));
		col.add(new MLDouble("users_" + name, CFMatrixUtils.asJama(users).getArray()));
		writ.write(new File("/Users/ss/" + name + ".mat"), col);
	}
}
