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
package org.openimaj.math.geometry.shape;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.shape.algorithm.GeneralisedProcrustesAnalysis;
import org.openimaj.math.geometry.shape.algorithm.ProcrustesAnalysis;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis.ComponentSelector;
import org.openimaj.math.matrix.algorithm.pca.SvdPrincipalComponentAnalysis;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * A 2d point distribution model learnt from a set of {@link PointList}s with
 * corresponding points (the ith point in each {@link PointList} is the same
 * landmark).
 *
 * The pdm models the mean shape and the variance from the mean of the top N
 * principal components. The model is generative and can generate new shapes
 * from a scaling vector. To ensure that newly generated shapes are plausible,
 * scaling vectors have {@link Constraint}s applied to them.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@References(references = {
		@Reference(
				author = { "Cootes, T. F.", "Taylor, C. J." },
				title = "Statistical Models of Appearance for Computer Vision",
				type = ReferenceType.Unpublished,
				month = "October",
				year = "2001",
				url = "http://isbe.man.ac.uk/~bim/Models/app_model.ps.gz"
		),
		@Reference(
				type = ReferenceType.Inproceedings,
				author = { "C. J. Taylor", "D. H. Cooper", "J. Graham" },
				title = "Training models of shape from sets of examples",
				year = "1992",
				booktitle = "Proc. BMVC92, Springer-Verlag",
				pages = { "9", "", "18" }
		)
})
public class PointDistributionModel {
	/**
	 * Interface for modelling constraints applied to the scaling vector of
	 * {@link PointDistributionModel}s so that generated models are plausible.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public interface Constraint {
		/**
		 * Apply constraints to a scaling vector so that it will generated a
		 * plausible model and return the new constrained vector.
		 *
		 * @param scaling
		 *            the scaling vector to constrain
		 * @param lamda
		 *            the eigenvalues of the {@link PointDistributionModel}
		 * @return the constrained scaling vector
		 */
		public double[] apply(double[] scaling, double[] lamda);
	}

	/**
	 * A constraint that does nothing.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class NullConstraint implements Constraint {
		@Override
		public double[] apply(double[] in, double[] lamda) {
			return in;
		}
	}

	/**
	 * A constraint that ensures that each individual element of the scaling
	 * vector is within +/- x standard deviations of the model.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class BoxConstraint implements Constraint {
		double multiplier;

		/**
		 * Construct with the given multiplier of the standard deviation.
		 *
		 * @param multiplier
		 */
		public BoxConstraint(double multiplier) {
			this.multiplier = multiplier;
		}

		@Override
		public double[] apply(double[] in, double[] lamda) {
			final double[] out = new double[in.length];

			for (int i = 0; i < in.length; i++) {
				final double w = multiplier * Math.sqrt(lamda[i]);
				out[i] = in[i] > w ? w : in[i] < -w ? -w : in[i];
			}

			return out;
		}
	}

	/**
	 * Constrain the scaling vector to a hyper-ellipsoid.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class EllipsoidConstraint implements Constraint {
		double dmax;

		/**
		 * Construct with the given maximum normalised ellipsoid radius.
		 *
		 * @param dmax
		 */
		public EllipsoidConstraint(double dmax) {
			this.dmax = dmax;
		}

		@Override
		public double[] apply(double[] in, double[] lamda) {
			double dmsq = 0;
			for (int i = 0; i < in.length; i++) {
				dmsq += in[i] * in[i] / lamda[i];
			}

			if (dmsq < dmax * dmax) {
				return in;
			}

			final double sc = dmax / Math.sqrt(dmsq);
			final double[] out = new double[in.length];
			for (int i = 0; i < in.length; i++) {
				out[i] = in[i] * sc;
			}

			return out;
		}
	}

	protected Constraint constraint;
	protected PrincipalComponentAnalysis pc;
	protected PointList mean;
	protected int numComponents;
	protected int maxIter = 100;

	/**
	 * Construct a {@link PointDistributionModel} from the given data with a
	 * {@link NullConstraint}.
	 *
	 * @param data
	 */
	public PointDistributionModel(List<PointList> data) {
		this(new NullConstraint(), data);
	}

	/**
	 * Construct a {@link PointDistributionModel} from the given data and
	 * {@link Constraint}.
	 *
	 * @param constraint
	 * @param data
	 */
	public PointDistributionModel(Constraint constraint, List<PointList> data) {
		this.constraint = constraint;

		// align
		mean = GeneralisedProcrustesAnalysis.alignPoints(data, 5, 10);

		// build data matrix
		final Matrix m = buildDataMatrix(data);

		// perform pca
		this.pc = new SvdPrincipalComponentAnalysis();
		pc.learnBasis(m);

		numComponents = this.pc.getEigenValues().length;
	}

	private Matrix buildDataMatrix(PointList data) {
		final List<PointList> pls = new ArrayList<PointList>(1);
		pls.add(data);
		return buildDataMatrix(pls);
	}

	private Matrix buildDataMatrix(List<PointList> data) {
		final int nData = data.size();
		final int nPoints = data.get(0).size();

		final Matrix m = new Matrix(nData, nPoints * 2);
		final double[][] mData = m.getArray();

		for (int i = 0; i < nData; i++) {
			final PointList pts = data.get(i);
			for (int j = 0, k = 0; k < nPoints; j += 2, k++) {
				final Point2d pt = pts.points.get(k);

				mData[i][j] = pt.getX();
				mData[i][j + 1] = pt.getY();
			}
		}

		return m;
	}

	/**
	 * @return the mean shape
	 */
	public PointList getMean() {
		return mean;
	}

	/**
	 * Set the number of components of the PDM
	 *
	 * @param n
	 *            number of components
	 */
	public void setNumComponents(int n) {
		pc.selectSubset(n);
		numComponents = this.pc.getEigenValues().length;
	}

	/**
	 * Set the number of components of the PDM using a {@link ComponentSelector}
	 * .
	 *
	 * @param selector
	 *            the {@link ComponentSelector} to apply.
	 */
	public void setNumComponents(ComponentSelector selector) {
		pc.selectSubset(selector);
		numComponents = this.pc.getEigenValues().length;
	}

	/**
	 * Generate a plausible new shape from the scaling vector. The scaling
	 * vector is constrained by the underlying {@link Constraint} before being
	 * used to generate the model.
	 *
	 * @param scaling
	 *            scaling vector.
	 * @return a new shape
	 */
	public PointList generateNewShape(double[] scaling) {
		final PointList newShape = new PointList();

		final double[] pts = pc.generate(constraint.apply(scaling, pc.getEigenValues()));

		for (int i = 0; i < pts.length; i += 2) {
			final float x = (float) pts[i];
			final float y = (float) pts[i + 1];

			newShape.points.add(new Point2dImpl(x, y));
		}

		return newShape;
	}

	/**
	 * Compute the standard deviations of the shape components, multiplied by
	 * the given value.
	 *
	 * @param multiplier
	 *            the multiplier
	 * @return the multiplied standard deviations
	 */
	public double[] getStandardDeviations(double multiplier) {
		final double[] rngs = pc.getStandardDeviations();

		for (int i = 0; i < rngs.length; i++) {
			rngs[i] = rngs[i] * multiplier;
		}

		return rngs;
	}

	/**
	 * Determine the best parameters of the PDM for the given model.
	 *
	 * @param observed
	 *            the observed model.
	 * @return the parameters that best fit the model.
	 */
	public IndependentPair<Matrix, double[]> fitModel(PointList observed) {
		double[] model = new double[numComponents];
		double delta = 1.0;
		Matrix pose = null;

		final ProcrustesAnalysis pa = new ProcrustesAnalysis(observed);
		int count = 0;
		while (delta > 1e-6 && count++ < maxIter) {
			final PointList instance = this.generateNewShape(model);

			pose = pa.align(instance);

			final PointList projected = observed.transform(pose.inverse());

			// TODO: tangent space projection???

			final Matrix y = buildDataMatrix(projected);
			final Matrix xbar = new Matrix(new double[][] { pc.getMean() });
			double[] newModel = (y.minus(xbar)).times(pc.getBasis()).getArray()[0];

			newModel = constraint.apply(newModel, pc.getEigenValues());

			delta = 0;
			for (int i = 0; i < newModel.length; i++)
				delta += (newModel[i] - model[i]) * (newModel[i] - model[i]);
			delta = Math.sqrt(delta);

			model = newModel;
		}

		return new IndependentPair<Matrix, double[]>(pose, model);
	}
}
