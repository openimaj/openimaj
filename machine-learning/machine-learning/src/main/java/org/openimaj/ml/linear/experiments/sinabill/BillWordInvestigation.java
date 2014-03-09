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
