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
package org.openimaj.ml.kernel;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.math.util.MathUtils;
import org.openimaj.math.util.MathUtils.ExponentAndMantissa;

/**
 * Implementation of the Homogeneous Kernel Map. The Homogeneous Kernel Map
 * transforms data into a compact linear representation such that applying a
 * linear SVM can approximate to a high degree of accuracy the application of a
 * non-linear SVM to the original data. Additive kernels including Chi2,
 * intersection, and Jensen-Shannon are supported.
 * <p>
 * This implementation is based directly on the VLFeat implementation written by
 * Andrea Verdaldi, although it has been refactored to better fit with Java
 * conventions.
 * 
 * @see "http://www.vlfeat.org/api/homkermap.html"
 * @see "http://www.robots.ox.ac.uk/~vgg/software/homkermap/"
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Based on code originally written by Andrea Verdaldi
 */
@References(
		references = {
				@Reference(
						type = ReferenceType.Article,
						author = { "Vedaldi, A.", "Zisserman, A." },
						title = "Efficient Additive Kernels via Explicit Feature Maps",
						year = "2012",
						journal = "Pattern Analysis and Machine Intelligence, IEEE Transactions on",
						pages = { "480", "492" },
						number = "3",
						volume = "34",
						customData = {
								"keywords", "approximation theory;computer vision;data handling;feature extraction;learning (artificial intelligence);spectral analysis;support vector machines;Nystrom approximation;additive homogeneous kernels;approximate finite-dimensional feature maps;approximation error;computer vision;data dependency;explicit feature maps;exponential decay;large scale nonlinear support vector machines;linear SVM;spectral analysis;Additives;Approximation methods;Histograms;Kernel;Measurement;Support vector machines;Training;Kernel methods;feature map;large scale learning;object detection.;object recognition",
								"doi", "10.1109/TPAMI.2011.153",
								"ISSN", "0162-8828"
						}
				),
				@Reference(
						type = ReferenceType.Inproceedings,
						author = { "A. Vedaldi", "A. Zisserman" },
						title = "Efficient Additive Kernels via Explicit Feature Maps",
						year = "2010",
						booktitle = "Proceedings of the IEEE Conf. on Computer Vision and Pattern Recognition (CVPR)"
				)
		})
public class HomogeneousKernelMap {
	/**
	 * Types of supported kernel for the {@link HomogeneousKernelMap}
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum KernelType {
		/**
		 * Intersection kernel
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		Intersection {
			@Override
			protected double getSpectrum(double omega) {
				return (2.0 / Math.PI) / (1 + 4 * omega * omega);
			}
		},
		/**
		 * Chi^2 kernel
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		Chi2 {
			@Override
			protected double getSpectrum(double omega) {
				return 2.0 / (Math.exp(Math.PI * omega) + Math.exp(-Math.PI * omega));
			}
		},
		/**
		 * Jenson-Shannon Kernel
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		JensonShannon {
			@Override
			protected double getSpectrum(double omega) {
				return (2.0 / Math.log(4.0)) * 2.0 / (Math.exp(Math.PI * omega) + Math.exp(-Math.PI * omega))
						/ (1 + 4 * omega * omega);
			}
		};

		protected abstract double getSpectrum(double omega);
	}

	/**
	 * Types of window supported by the {@link HomogeneousKernelMap}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum WindowType {
		/**
		 * Uniform window
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		Uniform {
			@Override
			double computeKappaHat(KernelType type, double omega, HomogeneousKernelMap map) {
				return type.getSpectrum(omega);
			}
		},
		/**
		 * Rectangular window
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 * 
		 */
		Rectangular {
			@Override
			double computeKappaHat(KernelType type, double omega, HomogeneousKernelMap map) {
				double kappa_hat = 0;
				final double epsilon = 1e-2;
				final double omegaRange = 2.0 / (map.period * epsilon);
				final double domega = 2 * omegaRange / (2 * 1024.0 + 1);

				for (double omegap = -omegaRange; omegap <= omegaRange; omegap += domega) {
					double win = sinc((map.period / 2.0) * omegap);
					win *= (map.period / (2.0 * Math.PI));
					kappa_hat += win * type.getSpectrum(omegap + omega);
				}

				kappa_hat *= domega;

				// project on the positive orthant (see PAMI)
				kappa_hat = Math.max(kappa_hat, 0.0);

				return kappa_hat;
			}

			private double sinc(double x)
			{
				if (x == 0.0)
					return 1.0;
				return Math.sin(x) / x;
			}
		};

		abstract double computeKappaHat(KernelType type, double omega, HomogeneousKernelMap map);

		protected double getSmoothSpectrum(double omega, HomogeneousKernelMap map) {
			return computeKappaHat(map.kernelType, omega, map);
		}
	}

	/**
	 * Helper implementation of a {@link FeatureExtractor} that wraps another
	 * {@link FeatureExtractor} and then applies the
	 * {@link HomogeneousKernelMap} to the output before returning the vector.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <T>
	 *            Type of object that features can be extracted from
	 */
	public static class ExtractorWrapper<T> implements FeatureExtractor<DoubleFV, T> {
		private FeatureExtractor<? extends FeatureVector, T> inner;
		private HomogeneousKernelMap map;

		/**
		 * Construct with the given internal extractor and homogeneous kernel
		 * map.
		 * 
		 * @param inner
		 *            the internal extractor
		 * @param map
		 *            the homogeneous kernel map
		 */
		public ExtractorWrapper(FeatureExtractor<? extends FeatureVector, T> inner, HomogeneousKernelMap map) {
			this.inner = inner;
			this.map = map;
		}

		@Override
		public DoubleFV extractFeature(T object) {
			return map.evaluate(inner.extractFeature(object).asDoubleFV());
		}
	}

	private KernelType kernelType;
	private double period;
	private double gamma;
	private int order;
	private int numSubdivisions;
	private double subdivision;
	private int minExponent;
	private int maxExponent;
	private double[] table;

	/**
	 * Construct with the given kernel and window. The Gamma and order values
	 * are set at their defaults of 1. The period is computed automatically.
	 * 
	 * @param kernelType
	 *            the type of kernel
	 * @param windowType
	 *            the type of window (use {@link WindowType#Rectangular} if
	 *            unsure)
	 */
	public HomogeneousKernelMap(KernelType kernelType, WindowType windowType) {
		this(kernelType, 1, 1, -1, windowType);
	}

	/**
	 * Construct with the given kernel, gamma and window. The period is computed
	 * automatically and the approximation order is set to 1.
	 * 
	 * @param kernelType
	 *            the type of kernel
	 * @param gamma
	 *            the gamma value. the standard kernels are 1-homogeneous, but
	 *            smaller values can work better in practice.
	 * @param windowType
	 *            the type of window (use {@link WindowType#Rectangular} if
	 *            unsure)
	 */
	public HomogeneousKernelMap(KernelType kernelType,
			double gamma,
			WindowType windowType)
	{
		this(kernelType, gamma, 1, -1, windowType);
	}

	/**
	 * Construct with the given kernel, gamma, order and window. The period is
	 * computed automatically.
	 * 
	 * @param kernelType
	 *            the type of kernel
	 * @param gamma
	 *            the gamma value. the standard kernels are 1-homogeneous, but
	 *            smaller values can work better in practice.
	 * @param order
	 *            the approximation order (usually 1 is enough)
	 * @param windowType
	 *            the type of window (use {@link WindowType#Rectangular} if
	 *            unsure)
	 */
	public HomogeneousKernelMap(KernelType kernelType,
			double gamma,
			int order,
			WindowType windowType)
	{
		this(kernelType, gamma, order, -1, windowType);
	}

	/**
	 * Construct with the given kernel, gamma, order, period and window. If the
	 * period is negative, it will be replaced by the default.
	 * 
	 * @param kernelType
	 *            the type of kernel
	 * @param gamma
	 *            the gamma value. the standard kernels are 1-homogeneous, but
	 *            smaller values can work better in practice.
	 * @param order
	 *            the approximation order (usually 1 is enough)
	 * @param period
	 *            the periodicity of the kernel spectrum
	 * @param windowType
	 *            the type of window (use {@link WindowType#Rectangular} if
	 *            unsure)
	 */
	public HomogeneousKernelMap(KernelType kernelType,
			double gamma,
			int order,
			double period,
			WindowType windowType)
	{
		if (gamma <= 0)
			throw new IllegalArgumentException("Gamma must be > 0");
		final int tableWidth, tableHeight;

		if (period < 0) {
			period = computeDefaultPeriod(windowType, kernelType);
		}

		this.kernelType = kernelType;
		this.gamma = gamma;
		this.order = order;
		this.period = period;
		this.numSubdivisions = 8 + 8 * order;
		this.subdivision = 1.0 / this.numSubdivisions;
		this.minExponent = -20;
		this.maxExponent = 8;

		tableHeight = 2 * this.order + 1;
		tableWidth = this.numSubdivisions * (this.maxExponent - this.minExponent + 1);
		this.table = new double[tableHeight * tableWidth + 2 * (1 + this.order)];

		int tableOffset = 0;
		final int kappaOffset = tableHeight * tableWidth;
		final int freqOffset = kappaOffset + 1 + order;
		final double L = 2.0 * Math.PI / this.period;

		/* precompute the sampled periodicized spectrum */
		int j = 0;
		int i = 0;
		while (i <= this.order) {
			table[freqOffset + i] = j;
			table[kappaOffset + i] = windowType.getSmoothSpectrum(j * L, this);
			j++;
			if (table[kappaOffset + i] > 0 || j >= 3 * i)
				i++;
		}

		/* fill table */
		for (int exponent = minExponent; exponent <= maxExponent; exponent++) {
			double x, Lxgamma, Llogx, xgamma;
			double sqrt2kappaLxgamma;
			double mantissa = 1.0;

			for (i = 0; i < numSubdivisions; i++, mantissa += subdivision) {
				x = mantissa * Math.pow(2, exponent);
				xgamma = Math.pow(x, this.gamma);
				Lxgamma = L * xgamma;
				Llogx = L * Math.log(x);

				table[tableOffset++] = Math.sqrt(Lxgamma * table[kappaOffset]);
				for (j = 1; j <= this.order; ++j) {
					sqrt2kappaLxgamma = Math.sqrt(2.0 * Lxgamma * table[kappaOffset + j]);
					table[tableOffset++] = sqrt2kappaLxgamma * Math.cos(table[freqOffset + j] * Llogx);
					table[tableOffset++] = sqrt2kappaLxgamma * Math.sin(table[freqOffset + j] * Llogx);
				}
			}
		}
	}

	private double computeDefaultPeriod(WindowType windowType, KernelType kernelType) {
		double period = 0;

		// compute default period
		switch (windowType) {
		case Uniform:
			switch (kernelType) {
			case Chi2:
				period = 5.86 * Math.sqrt(order + 0) + 3.65;
				break;
			case JensonShannon:
				period = 6.64 * Math.sqrt(order + 0) + 7.24;
				break;
			case Intersection:
				period = 2.38 * Math.log(order + 0.8) + 5.6;
				break;
			}
			break;
		case Rectangular:
			switch (kernelType) {
			case Chi2:
				period = 8.80 * Math.sqrt(order + 4.44) - 12.6;
				break;
			case JensonShannon:
				period = 9.63 * Math.sqrt(order + 1.00) - 2.93;
				break;
			case Intersection:
				period = 2.00 * Math.log(order + 0.99) + 3.52;
				break;
			}
			break;
		}
		return Math.max(period, 1.0);
	}

	/**
	 * Evaluate the kernel for the given <code>x</code> value. The output values
	 * will be written into the destination array at
	 * <code>offset + j*stride</code> intervals where <code>j</code> is between
	 * 0 and <code>2 * order + 1</code>.
	 * 
	 * @param destination
	 *            the destination array
	 * @param stride
	 *            the stride
	 * @param offset
	 *            the offset
	 * @param x
	 *            the value to compute the kernel approximation for
	 */
	public void evaluate(double[] destination, int stride, int offset, double x) {
		final ExponentAndMantissa em = MathUtils.frexp(x);

		double mantissa = em.mantissa;
		int exponent = em.exponent;
		final double sign = (mantissa >= 0.0) ? +1.0 : -1.0;
		mantissa *= 2 * sign;
		exponent--;

		if (mantissa == 0 || exponent <= minExponent || exponent >= maxExponent) {
			for (int j = 0; j <= order; j++) {
				destination[offset + j * stride] = 0.0;
			}
			return;
		}

		final int featureDimension = 2 * order + 1;
		int v1offset = (exponent - minExponent) * numSubdivisions * featureDimension;

		mantissa -= 1.0;
		while (mantissa >= subdivision) {
			mantissa -= subdivision;
			v1offset += featureDimension;
		}

		int v2offset = v1offset + featureDimension;
		for (int j = 0; j < featureDimension; j++) {
			final double f1 = table[v1offset++];
			final double f2 = table[v2offset++];

			destination[offset + j * stride] = sign * ((f2 - f1) * (numSubdivisions * mantissa) + f1);
		}
	}

	/**
	 * Compute the Homogeneous Kernel Map approximation of the given feature
	 * vector
	 * 
	 * @param in
	 *            the feature vector
	 * @return the expanded feature vector
	 */
	public DoubleFV evaluate(DoubleFV in) {
		final int step = (2 * order + 1);
		final DoubleFV out = new DoubleFV(step * in.length());

		for (int i = 0; i < in.length(); i++) {
			evaluate(out.values, 1, i * step, in.values[i]);
		}

		return out;
	}

	/**
	 * Construct a new {@link ExtractorWrapper} that applies the map to features
	 * extracted by an internal extractor.
	 * 
	 * @param inner
	 *            the internal extractor
	 * @return the wrapped {@link FeatureExtractor}
	 * @param <T>
	 *            Type of object that features can be extracted from
	 */
	public <T> FeatureExtractor<DoubleFV, T> createWrappedExtractor(FeatureExtractor<? extends FeatureVector, T> inner) {
		return new ExtractorWrapper<T>(inner, this);
	}
}
