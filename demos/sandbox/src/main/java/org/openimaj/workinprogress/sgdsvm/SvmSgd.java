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

import java.util.List;

import org.apache.commons.math.random.MersenneTwister;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.array.SparseFloatArray;
import org.openimaj.util.array.SparseFloatArray.Entry;
import org.openimaj.util.array.SparseHashedFloatArray;

import gnu.trove.list.array.TDoubleArrayList;

public class SvmSgd implements Cloneable {
	Loss LOSS = LossFunctions.HingeLoss;
	boolean BIAS = true;
	boolean REGULARIZED_BIAS = false;

	public double lambda;
	public double eta0;
	FloatFV w;
	double wDivisor;
	double wBias;
	double t;

	public SvmSgd(int dim, double lambda) {
		this(dim, lambda, 0);
	}

	public SvmSgd(int dim, double lambda, double eta0) {
		this.lambda = lambda;
		this.eta0 = eta0;
		this.w = new FloatFV(dim);
		this.wDivisor = 1;
		this.wBias = 0;
		this.t = 0;
	}

	private double dot(FloatFV v1, SparseFloatArray v2) {
		double d = 0;
		for (final Entry e : v2.entries()) {
			d += e.value * v1.values[e.index];
		}

		return d;
	}

	private double dot(FloatFV v1, FloatFV v2) {
		return FloatFVComparison.INNER_PRODUCT.compare(v1, v2);
	}

	private void add(FloatFV y, SparseFloatArray x, double d) {
		// w2 = w2 + x*w1

		for (final Entry e : x.entries()) {
			y.values[e.index] += e.value * d;
		}
	}

	/// Renormalize the weights
	public void renorm() {
		if (wDivisor != 1.0) {
			ArrayUtils.multiply(w.values, (float) (1.0 / wDivisor));
			// w.scale(1.0 / wDivisor);
			wDivisor = 1.0;
		}
	}

	/// Compute the norm of the weights
	public double wnorm() {
		double norm = dot(w, w) / wDivisor / wDivisor;

		if (REGULARIZED_BIAS)
			norm += wBias * wBias;
		return norm;
	}

	/// Compute the output for one example.
	public double testOne(final SparseFloatArray x, double y, double[] ploss, double[] pnerr) {
		final double s = dot(w, x) / wDivisor + wBias;
		if (ploss != null)
			ploss[0] += LOSS.loss(s, y);
		if (pnerr != null)
			pnerr[0] += (s * y <= 0) ? 1 : 0;
		return s;
	}

	/// Perform one iteration of the SGD algorithm with specified gains
	public void trainOne(final SparseFloatArray x, double y, double eta) {
		final double s = dot(w, x) / wDivisor + wBias;
		// update for regularization term
		wDivisor = wDivisor / (1 - eta * lambda);
		if (wDivisor > 1e5)
			renorm();
		// update for loss term
		final double d = LOSS.dloss(s, y);
		if (d != 0)
			add(w, x, eta * d * wDivisor);

		// same for the bias
		if (BIAS) {
			final double etab = eta * 0.01;
			if (REGULARIZED_BIAS) {
				wBias *= (1 - etab * lambda);
			}
			wBias += etab * d;
		}
	}

	@Override
	protected SvmSgd clone() {
		SvmSgd clone;
		try {
			clone = (SvmSgd) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		clone.w = clone.w.clone();
		return clone;
	}

	/// Perform a training epoch
	public void train(int imin, int imax, SparseFloatArray[] xp, double[] yp) {
		System.out.println("Training on [" + imin + ", " + imax + "].");
		assert (imin <= imax);
		assert (eta0 > 0);
		for (int i = imin; i <= imax; i++) {
			final double eta = eta0 / (1 + lambda * eta0 * t);
			trainOne(xp[i], yp[i], eta);
			t += 1;
		}
		// cout << prefix << setprecision(6) << "wNorm=" << wnorm();
		System.out.format("wNorm=%.6f", wnorm());
		if (BIAS) {
			// cout << " wBias=" << wBias;
			System.out.format(" wBias=%.6f", wBias);
		}
		System.out.println();
		// cout << endl;
	}

	/// Perform a training epoch
	public void train(int imin, int imax, List<SparseFloatArray> xp, TDoubleArrayList yp) {
		System.out.println("Training on [" + imin + ", " + imax + "].");
		assert (imin <= imax);
		assert (eta0 > 0);
		for (int i = imin; i <= imax; i++) {
			final double eta = eta0 / (1 + lambda * eta0 * t);
			trainOne(xp.get(i), yp.get(i), eta);
			t += 1;
		}
		// cout << prefix << setprecision(6) << "wNorm=" << wnorm();
		System.out.format("wNorm=%.6f", wnorm());
		if (BIAS) {
			// cout << " wBias=" << wBias;
			System.out.format(" wBias=%.6f", wBias);
		}
		System.out.println();
		// cout << endl;
	}

	/// Perform a test pass
	public void test(int imin, int imax, SparseFloatArray[] xp, double[] yp, String prefix) {
		// cout << prefix << "Testing on [" << imin << ", " << imax << "]." <<
		// endl;
		System.out.println(prefix + "Testing on [" + imin + ", " + imax + "].");
		assert (imin <= imax);
		final double nerr[] = { 0 };
		final double loss[] = { 0 };
		for (int i = imin; i <= imax; i++)
			testOne(xp[i], yp[i], loss, nerr);
		nerr[0] = nerr[0] / (imax - imin + 1);
		loss[0] = loss[0] / (imax - imin + 1);
		final double cost = loss[0] + 0.5 * lambda * wnorm();
		// cout << prefix
		// << "Loss=" << setprecision(12) << loss
		// << " Cost=" << setprecision(12) << cost
		// << " Misclassification=" << setprecision(4) << 100 * nerr << "%."
		// << endl;
		System.out.println(prefix + "Loss=" + loss[0] + " Cost=" + cost + " Misclassification="
				+ String.format("%2.4f", 100 * nerr[0]) + "%");
	}

	/// Perform a test pass
	public void test(int imin, int imax, List<SparseFloatArray> xp, TDoubleArrayList yp, String prefix) {
		// cout << prefix << "Testing on [" << imin << ", " << imax << "]." <<
		// endl;
		System.out.println(prefix + "Testing on [" + imin + ", " + imax + "].");
		assert (imin <= imax);
		final double nerr[] = { 0 };
		final double loss[] = { 0 };
		for (int i = imin; i <= imax; i++)
			testOne(xp.get(i), yp.get(i), loss, nerr);
		nerr[0] = nerr[0] / (imax - imin + 1);
		loss[0] = loss[0] / (imax - imin + 1);
		final double cost = loss[0] + 0.5 * lambda * wnorm();
		// cout << prefix
		// << "Loss=" << setprecision(12) << loss
		// << " Cost=" << setprecision(12) << cost
		// << " Misclassification=" << setprecision(4) << 100 * nerr << "%."
		// << endl;
		System.out.println(prefix + "Loss=" + loss[0] + " Cost=" + cost + " Misclassification="
				+ String.format("%2.4f", 100 * nerr[0]) + "%");
	}

	/// Perform one epoch with fixed eta and return cost
	public double evaluateEta(int imin, int imax, SparseFloatArray[] xp, double[] yp, double eta) {
		final SvmSgd clone = this.clone(); // take a copy of the current state
		assert (imin <= imax);
		for (int i = imin; i <= imax; i++)
			clone.trainOne(xp[i], yp[i], eta);
		final double loss[] = { 0 };
		double cost = 0;
		for (int i = imin; i <= imax; i++)
			clone.testOne(xp[i], yp[i], loss, null);
		loss[0] = loss[0] / (imax - imin + 1);
		cost = loss[0] + 0.5 * lambda * clone.wnorm();
		// cout << "Trying eta=" << eta << " yields cost " << cost << endl;
		System.out.println("Trying eta=" + eta + " yields cost " + cost);
		return cost;
	}

	/// Perform one epoch with fixed eta and return cost
	public double evaluateEta(int imin, int imax, List<SparseFloatArray> xp, TDoubleArrayList yp, double eta) {
		final SvmSgd clone = this.clone(); // take a copy of the current state
		assert (imin <= imax);
		for (int i = imin; i <= imax; i++)
			clone.trainOne(xp.get(i), yp.get(i), eta);
		final double loss[] = { 0 };
		double cost = 0;
		for (int i = imin; i <= imax; i++)
			clone.testOne(xp.get(i), yp.get(i), loss, null);
		loss[0] = loss[0] / (imax - imin + 1);
		cost = loss[0] + 0.5 * lambda * clone.wnorm();
		// cout << "Trying eta=" << eta << " yields cost " << cost << endl;
		System.out.println("Trying eta=" + eta + " yields cost " + cost);
		return cost;
	}

	public void determineEta0(int imin, int imax, SparseFloatArray[] xp, double[] yp) {
		final double factor = 2.0;
		double loEta = 1;
		double loCost = evaluateEta(imin, imax, xp, yp, loEta);
		double hiEta = loEta * factor;
		double hiCost = evaluateEta(imin, imax, xp, yp, hiEta);
		if (loCost < hiCost)
			while (loCost < hiCost) {
				hiEta = loEta;
				hiCost = loCost;
				loEta = hiEta / factor;
				loCost = evaluateEta(imin, imax, xp, yp, loEta);
			}
		else if (hiCost < loCost)
			while (hiCost < loCost) {
				loEta = hiEta;
				loCost = hiCost;
				hiEta = loEta * factor;
				hiCost = evaluateEta(imin, imax, xp, yp, hiEta);
			}
		eta0 = loEta;
		// cout << "# Using eta0=" << eta0 << endl;
		System.out.println("# Using eta0=" + eta0 + "\n");
	}

	public void determineEta0(int imin, int imax, List<SparseFloatArray> xp, TDoubleArrayList yp) {
		final double factor = 2.0;
		double loEta = 1;
		double loCost = evaluateEta(imin, imax, xp, yp, loEta);
		double hiEta = loEta * factor;
		double hiCost = evaluateEta(imin, imax, xp, yp, hiEta);
		if (loCost < hiCost)
			while (loCost < hiCost) {
				hiEta = loEta;
				hiCost = loCost;
				loEta = hiEta / factor;
				loCost = evaluateEta(imin, imax, xp, yp, loEta);
			}
		else if (hiCost < loCost)
			while (hiCost < loCost) {
				loEta = hiEta;
				loCost = hiCost;
				hiEta = loEta * factor;
				hiCost = evaluateEta(imin, imax, xp, yp, hiEta);
			}
		eta0 = loEta;
		// cout << "# Using eta0=" << eta0 << endl;
		System.out.println("# Using eta0=" + eta0 + "\n");
	}

	public static void main(String[] args) {
		final MersenneTwister mt = new MersenneTwister();
		final SparseFloatArray[] tr = new SparseFloatArray[10000];
		final double[] clz = new double[tr.length];
		for (int i = 0; i < tr.length; i++) {
			tr[i] = new SparseHashedFloatArray(2);

			if (i < tr.length / 2) {
				tr[i].set(0, (float) (mt.nextGaussian() - 2));
				tr[i].set(1, (float) (mt.nextGaussian() - 2));
				clz[i] = -1;
			} else {
				tr[i].set(0, (float) (mt.nextGaussian() + 2));
				tr[i].set(1, (float) (mt.nextGaussian() + 2));
				clz[i] = 1;
			}
			System.out.println(tr[i].values()[0] + " " + clz[i]);
		}

		final SvmSgd svm = new SvmSgd(2, 1e-5);
		svm.BIAS = true;
		svm.REGULARIZED_BIAS = false;
		svm.determineEta0(0, tr.length - 1, tr, clz);
		for (int i = 0; i < 10; i++) {
			System.out.println();
			svm.train(0, tr.length - 1, tr, clz);
			svm.test(0, tr.length - 1, tr, clz, "training ");
			System.out.println(svm.w);
			System.out.println(svm.wBias);
			System.out.println(svm.wDivisor);
		}

		// svm.w.values[0] = 1f;
		// svm.w.values[1] = 1f;
		// svm.wDivisor = 1;
		// svm.wBias = 0;
		// svm.test(0, 999, tr, clz, "training ");
	}
}
