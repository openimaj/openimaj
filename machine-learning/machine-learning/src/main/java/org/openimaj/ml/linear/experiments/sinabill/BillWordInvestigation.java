package org.openimaj.ml.linear.experiments.sinabill;

import gov.sandia.cognition.math.matrix.Matrix;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.media.jai.ColorModelFactory;

import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.BilinearSparseOnlineLearner;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class BillWordInvestigation extends BilinearExperiment{
	
	public static void main(String[] args) throws Exception {	
		BillWordInvestigation bi = new BillWordInvestigation();
		bi.performExperiment();
	}

	@Override
	public void performExperiment() throws Exception {
		String exproot = "/Users/ss/Dropbox/TrendMiner/deliverables/year2-18month/Austrian Data/streamingExperiments/experiment_1392738388042";
		File[] folds = new File(exproot).listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.contains("fold");
			}
		});
		
		int f = (folds.length-1)/2;
		File fold = folds[f];
		
		File learner = fold.listFiles()[0];
		BilinearSparseOnlineLearner l = IOUtils.read(learner,BilinearSparseOnlineLearner.class);
		
		Matrix words  = l.getW();
		Matrix users = l.getU();
		String name = "fold_"+f;
		MatFileWriter writ = new MatFileWriter();
		ArrayList<MLArray> col = new ArrayList<MLArray>();
		col.add(new MLDouble("words_"+name, CFMatrixUtils.asJama(words).getArray()));
		col.add(new MLDouble("users_"+name, CFMatrixUtils.asJama(users).getArray()));
		writ.write(new File("/Users/ss/" + name + ".mat"), col);
	}
}