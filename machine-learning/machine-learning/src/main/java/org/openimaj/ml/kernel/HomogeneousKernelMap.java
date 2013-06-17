package org.openimaj.ml.kernel;

import org.openimaj.feature.DoubleFV;
import org.openimaj.math.util.MathUtils;
import org.openimaj.math.util.MathUtils.ExponentAndMantissa;

public class HomogeneousKernelMap {
	public enum KernelType {
		Intersection {
			@Override
			protected double getSpectrum(double omega) {
				return (2.0 / Math.PI) / (1 + 4 * omega * omega);
			}
		},
		Chi2 {
			@Override
			protected double getSpectrum(double omega) {
				return 2.0 / (Math.exp(Math.PI * omega) + Math.exp(-Math.PI * omega));
			}
		},
		JensonShannon {
			@Override
			protected double getSpectrum(double omega) {
				return (2.0 / Math.log(4.0)) * 2.0 / (Math.exp(Math.PI * omega) + Math.exp(-Math.PI * omega))
						/ (1 + 4 * omega * omega);
			}
		};

		protected abstract double getSpectrum(double omega);
	}

	public enum WindowType {
		Uniform {
			@Override
			double computeKappaHat(KernelType type, double omega, HomogeneousKernelMap map) {
				return type.getSpectrum(omega);
			}
		},
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

	private KernelType kernelType;
	private WindowType windowType;
	private double period;
	private double gamma;
	private int order;
	private int numSubdivisions;
	private double subdivision;
	private int minExponent;
	private int maxExponent;
	private double[] table;

	public HomogeneousKernelMap(KernelType kernelType, WindowType windowType) {
		this(kernelType, 1, 1, -1, windowType);
	}

	public HomogeneousKernelMap(KernelType kernelType,
			double gamma,
			int order,
			WindowType windowType)
	{
		this(kernelType, gamma, order, -1, windowType);
	}

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
		this.windowType = windowType;
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

	public DoubleFV evaluate(DoubleFV in) {
		final int step = (2 * order + 1);
		final DoubleFV out = new DoubleFV(step * in.length());

		for (int i = 0; i < in.length(); i++) {
			evaluate(out.values, 1, i * step, in.values[i]);
		}

		return out;
	}
}
