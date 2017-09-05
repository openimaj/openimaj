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
package org.openimaj.workinprogress.sgdsvm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.time.Timer;
import org.openimaj.util.array.SparseFloatArray;

import gnu.trove.list.array.TDoubleArrayList;

public class SvmSgdMain {
	Loss LOSS = LossFunctions.LogLoss;
	boolean BIAS = true;
	boolean REGULARIZED_BIAS = false;

	String trainfile = null;
	String testfile = null;
	boolean normalize = true;
	double lambda = 1e-5;
	int epochs = 5;
	int maxtrain = -1;

	String NAM(String s) {
		return String.format("%16s ", s);
	}

	String DEF(Object v) {
		return " (default: " + v + ".)";
	}

	void usage(String progname) {
		System.err.println("Usage: " + progname + " [options] trainfile [testfile]");
		System.err.println("Options:");

		System.err.println(NAM("-lambda x") + "Regularization parameter" + DEF(lambda));
		System.err.println(NAM("-epochs n") + "Number of training epochs" + DEF(epochs));
		System.err.println(NAM("-dontnormalize") + "Do not normalize the L2 norm of patterns.");
		System.err.println(NAM("-maxtrain n") + "Restrict training set to n examples.");
		System.exit(10);
	}

	void
			parse(String[] args)
	{
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (arg.charAt(0) != '-') {
				if (trainfile == null)
					trainfile = arg;
				else if (testfile == null)
					testfile = arg;
				else
					usage(this.getClass().getName());
			} else {
				// while (arg.charAt(0) == '-')
				// arg += 1;
				final String opt = arg;
				if (opt == "lambda" && i + 1 < args.length) {
					lambda = Double.parseDouble(args[++i]);
					assert (lambda > 0 && lambda < 1e4);
				} else if (opt == "epochs" && i + 1 < args.length) {
					epochs = Integer.parseInt(args[++i]);
					assert (epochs > 0 && epochs < 1e6);
				} else if (opt == "dontnormalize") {
					normalize = false;
				} else if (opt == "maxtrain" && i + 1 < args.length) {
					maxtrain = Integer.parseInt(args[++i]);
					assert (maxtrain > 0);
				} else {
					System.err.println("Option " + args[i] + " not recognized.");
					usage(this.getClass().getName());
				}
			}
		}
		if (trainfile == null)
			usage(this.getClass().getName());
	}

	void
			config(String progname)
	{
		System.out.print("# Running: " + progname);
		System.out.print(" -lambda " + lambda);
		System.out.print(" -epochs " + epochs);
		if (!normalize)
			System.out.print(" -dontnormalize");
		if (maxtrain > 0)
			System.out.print(" -maxtrain " + maxtrain);
		System.out.println();

		System.out.print(
				"# Compiled with: " + "-DLOSS=" + LOSS + " -DBIAS=" + BIAS + "-DREGULARIZED_BIAS=" + REGULARIZED_BIAS);
	}

	// --- main function
	int[] dims = { 0 };
	List<SparseFloatArray> xtrain = new ArrayList<>();
	TDoubleArrayList ytrain = new TDoubleArrayList();
	List<SparseFloatArray> xtest = new ArrayList<>();
	TDoubleArrayList ytest = new TDoubleArrayList();

	public static void main(String[] args) throws IOException {
		final SvmSgdMain main = new SvmSgdMain();
		main.run(args);
	}

	void run(String[] args) throws IOException {
		parse(args);
		config(this.getClass().getName());
		if (trainfile != null)
			load_datafile(trainfile, xtrain, ytrain, dims, normalize, maxtrain);
		if (testfile != null)
			load_datafile(testfile, xtest, ytest, dims, normalize);
		System.out.println("# Number of features " + dims + ".");

		// prepare svm
		final int imin = 0;
		final int imax = xtrain.size() - 1;
		final int tmin = 0;
		final int tmax = xtest.size() - 1;

		final SvmSgd svm = new SvmSgd(dims[0], lambda);
		svm.BIAS = BIAS;
		svm.LOSS = LOSS;
		svm.REGULARIZED_BIAS = REGULARIZED_BIAS;

		final Timer timer = new Timer();
		// determine eta0 using sample
		final int smin = 0;
		final int smax = imin + Math.min(1000, imax);
		timer.start();
		svm.determineEta0(smin, smax, xtrain, ytrain);
		timer.stop();
		// train
		for (int i = 0; i < epochs; i++) {
			System.out.println("--------- Epoch " + i + 1 + ".");
			timer.start();
			svm.train(imin, imax, xtrain, ytrain);
			timer.stop();
			System.out.println("Total training time " + timer.duration() / 1000 + "secs.");
			svm.test(imin, imax, xtrain, ytrain, "train:");
			if (tmax >= tmin)
				svm.test(tmin, tmax, xtest, ytest, "test: ");
		}
	}

	private static int load_datafile(String file, List<SparseFloatArray> x, TDoubleArrayList y, int[] dims,
			boolean normalize) throws IOException
	{
		return load_datafile(file, x, y, dims, normalize, -1);
	}

	private static int load_datafile(String file, List<SparseFloatArray> x, TDoubleArrayList y, int[] dims,
			boolean normalize, int maxn) throws IOException
	{
		final Loader loader = new Loader(file);

		final int[] maxdim = { 0 };
		final int[] pcount = { 0 }, ncount = { 0 };
		loader.load(x, y, normalize, maxn, maxdim, pcount, ncount);
		if (pcount[0] + ncount[0] > 0)
			System.out.println("# Read " + pcount + "+" + ncount
					+ "=" + pcount + ncount + " examples "
					+ "from \"" + file + "\".");
		if (dims[0] < maxdim[0])
			dims[0] = maxdim[0];
		return pcount[0] + ncount[0];
	}

}
