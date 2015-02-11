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
package org.openimaj.demos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.openimaj.feature.FloatFV;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.linear.projection.LargeMarginDimensionalityReduction;

import Jama.Matrix;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLSingle;

public class FVFWExperiment {
	// private static final String FOLDER =
	// "lfw-centre-affine-pdsift-pca64-augm-fv512/";
	// private static final String FOLDER = "lfw-centre-affine-matlab-fisher/";
	private static final String FOLDER = "matlab-fvs/";

	static class FacePair {
		boolean same;
		File firstFV;
		File secondFV;

		public FacePair(File first, File second, boolean same) {
			this.firstFV = first;
			this.secondFV = second;
			this.same = same;
		}

		FloatFV loadFirst() throws IOException {
			return IOUtils.read(firstFV, FloatFV.class);
		}

		FloatFV loadSecond() throws IOException {
			return IOUtils.read(secondFV, FloatFV.class);
		}
	}

	static class Subset {
		List<FacePair> testPairs = new ArrayList<FacePair>();
		List<FacePair> trainingPairs = new ArrayList<FacePair>();
	}

	static List<Subset> loadSubsets() throws IOException {
		final List<Subset> subsets = new ArrayList<Subset>();

		for (int i = 0; i < 10; i++)
			subsets.add(new Subset());

		loadPairs(new File("/Users/jon/Data/lfw/pairs.txt"), subsets);
		loadPeople(new File("/Users/jon/Data/lfw/people.txt"), subsets);

		return subsets;
	}

	private static void loadPairs(File file, List<Subset> subsets) throws FileNotFoundException {
		final Scanner sc = new Scanner(file);

		final int nsets = sc.nextInt();
		final int nhpairs = sc.nextInt();

		if (nsets != 10 || nhpairs != 300) {
			sc.close();
			throw new RuntimeException();
		}

		for (int s = 0; s < 10; s++) {
			for (int i = 0; i < 300; i++) {
				final String name = sc.next();
				final int firstIdx = sc.nextInt();
				final int secondIdx = sc.nextInt();

				final File first = new File(file.getParentFile(), FOLDER + name
						+ "/" + name + String.format("_%04d.bin", firstIdx));
				final File second = new File(file.getParentFile(), FOLDER + name
						+ "/" + name + String.format("_%04d.bin", secondIdx));

				subsets.get(s).testPairs.add(new FacePair(first, second, true));
			}

			for (int i = 0; i < 300; i++) {
				final String firstName = sc.next();
				final int firstIdx = sc.nextInt();
				final String secondName = sc.next();
				final int secondIdx = sc.nextInt();

				final File first = new File(file.getParentFile(), FOLDER
						+ firstName
						+ "/" + firstName + String.format("_%04d.bin", firstIdx));
				final File second = new File(file.getParentFile(), FOLDER
						+ secondName
						+ "/" + secondName + String.format("_%04d.bin", secondIdx));

				subsets.get(s).testPairs.add(new FacePair(first, second, false));
			}
		}

		sc.close();
	}

	private static void loadPeople(File file, List<Subset> subsets) throws FileNotFoundException {
		final Scanner sc = new Scanner(file);

		final int nsets = sc.nextInt();

		if (nsets != 10) {
			sc.close();
			throw new RuntimeException();
		}

		for (int s = 0; s < 10; s++) {
			final int nnames = sc.nextInt();
			final List<File> files = new ArrayList<File>(nnames);
			for (int i = 0; i < nnames; i++) {
				final String name = sc.next();
				final int numPeople = sc.nextInt();
				for (int j = 1; j <= numPeople; j++) {
					final File f = new File(file.getParentFile(), FOLDER + name
							+ "/" + name + String.format("_%04d.bin", j));

					files.add(f);
				}
			}

			for (int i = 0; i < files.size(); i++) {
				final File first = files.get(i);
				for (int j = i + 1; j < files.size(); j++) {
					final File second = files.get(j);

					final boolean same = first.getName().substring(0, first.getName().lastIndexOf("_"))
							.equals(second.getName().substring(0, second.getName().lastIndexOf("_")));

					subsets.get(s).trainingPairs.add(new FacePair(first, second, same));
					subsets.get(s).trainingPairs.add(new FacePair(second, first, same));
				}
			}
		}

		sc.close();
	}

	static Subset createExperimentalFold(List<Subset> subsets, int foldIdx) {
		final Subset subset = new Subset();
		// testing data is from the indexed fold
		subset.testPairs = subsets.get(foldIdx).testPairs;

		// training data is from the other folds
		final List<FacePair> training = new ArrayList<FacePair>();
		for (int i = 0; i < foldIdx; i++)
			training.addAll(subsets.get(i).trainingPairs);
		for (int i = foldIdx + 1; i < subsets.size(); i++)
			training.addAll(subsets.get(i).trainingPairs);

		subset.trainingPairs = reorder(training);

		return subset;
	}

	private static List<FacePair> reorder(List<FacePair> training) {
		final List<FacePair> trainingTrue = new ArrayList<FacePair>();
		final List<FacePair> trainingFalse = new ArrayList<FacePair>();

		for (final FacePair fp : training) {
			if (fp.same)
				trainingTrue.add(fp);
			else
				trainingFalse.add(fp);
		}

		resample(trainingTrue, 4000000);
		resample(trainingFalse, 4000000);

		final List<FacePair> trainingResorted = new ArrayList<FacePair>();
		for (int i = 0; i < trainingTrue.size(); i++) {
			trainingResorted.add(trainingTrue.get(i));
			trainingResorted.add(trainingFalse.get(i));
		}

		return trainingResorted;
	}

	private static void resample(List<FacePair> pairs, int sz) {
		final List<FacePair> oldPairs = new ArrayList<FVFWExperiment.FacePair>(sz);
		oldPairs.addAll(pairs);
		pairs.clear();

		final Random r = new Random();

		for (int i = 0; i < sz; i++) {
			pairs.add(oldPairs.get(r.nextInt(oldPairs.size())));
		}
	}

	public static void main(String[] args) throws IOException {
		final List<Subset> subsets = loadSubsets();
		final Subset fold = createExperimentalFold(subsets, 1);

		// // final LargeMarginDimensionalityReduction lmdr = new
		// // LargeMarginDimensionalityReduction(128);
		// final LargeMarginDimensionalityReduction lmdr = loadMatlabPCAW();
		//
		// final double[][] fInit = new double[1000][];
		// final double[][] sInit = new double[1000][];
		// final boolean[] same = new boolean[1000];
		// for (int i = 0; i < 1000; i++) {
		// final FacePair p =
		// fold.trainingPairs.get(i);
		// fInit[i] = p.loadFirst().asDoubleVector();
		// sInit[i] = p.loadSecond().asDoubleVector();
		// same[i] = p.same;
		//
		// for (int j = 0; j < fInit[i].length; j++) {
		// if (Double.isInfinite(fInit[i][j]) || Double.isNaN(fInit[i][j]))
		// throw new RuntimeException("" + fold.trainingPairs.get(i).firstFV);
		// if (Double.isInfinite(sInit[i][j]) || Double.isNaN(sInit[i][j]))
		// throw new RuntimeException("" + fold.trainingPairs.get(i).secondFV);
		// }
		// }
		//
		// System.out.println("LMDR Init");
		// lmdr.recomputeBias(fInit, sInit, same);
		// // lmdr.initialise(fInit, sInit, same);
		// IOUtils.writeToFile(lmdr, new
		// File("/Users/jon/Data/lfw/lmdr-matlabfvs-pcaw-init.bin"));
		// // final LargeMarginDimensionalityReduction lmdr = IOUtils
		// // .readFromFile(new File("/Users/jon/Data/lfw/lmdr-init.bin"));
		//
		// for (int i = 0; i < 1e6; i++) {
		// if (i % 100 == 0)
		// System.out.println("Iter " + i);
		// final FacePair p = fold.trainingPairs.get(i);
		// lmdr.step(p.loadFirst().asDoubleVector(),
		// p.loadSecond().asDoubleVector(), p.same);
		// }
		// IOUtils.writeToFile(lmdr, new
		// File("/Users/jon/Data/lfw/lmdr-matlabfvs-pcaw.bin"));

		final LargeMarginDimensionalityReduction lmdr =
				IOUtils.readFromFile(new
						File("/Users/jon/Data/lfw/lmdr-matlabfvs-pcaw.bin"));
		// final LargeMarginDimensionalityReduction lmdr = loadMatlabLMDR();
		// final LargeMarginDimensionalityReduction lmdr = loadMatlabPCAW();

		final double[][] first = new double[fold.testPairs.size()][];
		final double[][] second = new double[fold.testPairs.size()][];
		final boolean[] same = new boolean[fold.testPairs.size()];
		for (int j = 0; j < same.length; j++) {
			final FacePair p = fold.testPairs.get(j);
			first[j] = p.loadFirst().asDoubleVector();
			second[j] = p.loadSecond().asDoubleVector();
			same[j] = p.same;
		}
		// System.out.println("Current bias: " + lmdr.getBias());
		// lmdr.recomputeBias(first, second, same);
		// System.out.println("Best bias: " + lmdr.getBias());

		double correct = 0;
		double count = 0;
		for (int j = 0; j < same.length; j++) {
			final boolean pred = lmdr.classify(first[j],
					second[j]);

			if (pred == same[j])
				correct++;
			count++;
		}
		System.out.println(lmdr.getBias() + " " + (correct / count));
	}

	// private static double[] reorder(double[] in) {
	// final double[] out = new double[in.length];
	// final int D = 64;
	// final int K = 512;
	// for (int k = 0; k < K; k++) {
	// for (int j = 0; j < D; j++) {
	// out[k * D + j] = in[k * 2 * D + j];
	// out[k * D + j + D * K] = in[k * 2 * D + j + D];
	// }
	// }
	// return out;
	// }

	private static LargeMarginDimensionalityReduction loadMatlabLMDR() throws IOException {
		final LargeMarginDimensionalityReduction lmdr = new LargeMarginDimensionalityReduction(128);

		final MatFileReader reader = new MatFileReader(new File("/Users/jon/lmdr.mat"));
		final MLSingle W = (MLSingle) reader.getContent().get("W");
		final MLSingle b = (MLSingle) reader.getContent().get("b");

		lmdr.setBias(b.get(0, 0));

		final Matrix proj = new Matrix(W.getM(), W.getN());
		for (int j = 0; j < W.getN(); j++) {
			for (int i = 0; i < W.getM(); i++) {
				proj.set(i, j, W.get(i, j));
			}
		}

		lmdr.setTransform(proj);

		return lmdr;
	}

	private static LargeMarginDimensionalityReduction loadMatlabPCAW() throws IOException {
		final LargeMarginDimensionalityReduction lmdr = new LargeMarginDimensionalityReduction(128);

		final MatFileReader reader = new MatFileReader(new File("/Users/jon/pcaw.mat"));
		final MLSingle W = (MLSingle) reader.getContent().get("proj");

		lmdr.setBias(169.6264190673828);

		final Matrix proj = new Matrix(W.getM(), W.getN());
		for (int j = 0; j < W.getN(); j++) {
			for (int i = 0; i < W.getM(); i++) {
				proj.set(i, j, W.get(i, j));
			}
		}

		lmdr.setTransform(proj);

		return lmdr;
	}
}
