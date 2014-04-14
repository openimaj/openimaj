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

public class FVFWExperiment {
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
			return IOUtils.read(firstFV, FloatFV.class);
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

		if (nsets != 10 || nhpairs != 300)
			throw new RuntimeException();

		for (int s = 0; s < 10; s++) {
			for (int i = 0; i < 300; i++) {
				final String name = sc.next();
				final int firstIdx = sc.nextInt();
				final int secondIdx = sc.nextInt();

				final File first = new File(file.getParentFile(), "lfw-centre-affine-pdsift-pca64-augm-fv512/" + name
						+ "/" + name + String.format("_%04d.bin", firstIdx));
				final File second = new File(file.getParentFile(), "lfw-centre-affine-pdsift-pca64-augm-fv512/" + name
						+ "/" + name + String.format("_%04d.bin", secondIdx));

				subsets.get(s).testPairs.add(new FacePair(first, second, true));
			}

			for (int i = 0; i < 300; i++) {
				final String firstName = sc.next();
				final int firstIdx = sc.nextInt();
				final String secondName = sc.next();
				final int secondIdx = sc.nextInt();

				final File first = new File(file.getParentFile(), "lfw-centre-affine-pdsift-pca64-augm-fv512/"
						+ firstName
						+ "/" + firstName + String.format("_%04d.bin", firstIdx));
				final File second = new File(file.getParentFile(), "lfw-centre-affine-pdsift-pca64-augm-fv512/"
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

		if (nsets != 10)
			throw new RuntimeException();

		for (int s = 0; s < 10; s++) {
			final int nnames = sc.nextInt();
			final List<File> files = new ArrayList<File>(nnames);
			for (int i = 0; i < nnames; i++) {
				final String name = sc.next();
				final int numPeople = sc.nextInt();
				for (int j = 1; j <= numPeople; j++) {
					final File f = new File(file.getParentFile(), "lfw-centre-affine-pdsift-pca64-augm-fv512/" + name
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

		/*
		 * final LargeMarginDimensionalityReduction lmdr = new
		 * LargeMarginDimensionalityReduction(128);
		 * 
		 * final double[][] fInit = new double[1000][]; final double[][] sInit =
		 * new double[1000][]; final boolean[] same = new boolean[1000]; for
		 * (int i = 0; i < 1000; i++) { final FacePair p =
		 * fold.trainingPairs.get(i); fInit[i] = p.loadFirst().asDoubleVector();
		 * sInit[i] = p.loadSecond().asDoubleVector(); same[i] = p.same; }
		 * 
		 * System.out.println("LMDR Init"); lmdr.initialise(fInit, sInit, same);
		 * IOUtils.writeToFile(lmdr, new
		 * File("/Users/jon/Data/lfw/lmdr-init.bin"));
		 */
		final LargeMarginDimensionalityReduction lmdr = IOUtils
				.readFromFile(new File("/Users/jon/Data/lfw/lmdr-init.bin"));

		for (int i = 0; i < 1e6; i++) {
			if (i % 10 == 0)
				System.out.println("Iter " + i);
			final FacePair p = fold.trainingPairs.get(i);
			lmdr.step(p.loadFirst().asDoubleVector(), p.loadSecond().asDoubleVector(), p.same);
		}
		IOUtils.writeToFile(lmdr, new File("/Users/jon/Data/lfw/lmdr.bin"));
	}
}
