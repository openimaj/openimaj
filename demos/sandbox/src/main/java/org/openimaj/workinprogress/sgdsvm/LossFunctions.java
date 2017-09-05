package org.openimaj.workinprogress.sgdsvm;

import static java.lang.Math.exp;
import static java.lang.Math.log;

public enum LossFunctions implements Loss {
	LogLoss
	{
		// logloss(a,y) = log(1+exp(-a*y))
		@Override
		public double loss(double a, double y) {
			final double z = a * y;
			if (z > 18)
				return exp(-z);
			if (z < -18)
				return -z;
			return log(1 + exp(-z));
		}

		// -dloss(a,y)/da
		@Override
		public double dloss(double a, double y) {
			final double z = a * y;
			if (z > 18)
				return y * exp(-z);
			if (z < -18)
				return y;
			return y / (1 + exp(z));
		}
	},
	HingeLoss
	{
		// hingeloss(a,y) = max(0, 1-a*y)
		@Override
		public double loss(double a, double y) {
			final double z = a * y;
			if (z > 1)
				return 0;
			return 1 - z;
		}

		// -dloss(a,y)/da
		@Override
		public double dloss(double a, double y) {
			final double z = a * y;
			if (z > 1)
				return 0;
			return y;
		}
	},
	SquaredHingeLoss
	{
		// squaredhingeloss(a,y) = 1/2 * max(0, 1-a*y)^2
		@Override
		public double loss(double a, double y) {
			final double z = a * y;
			if (z > 1)
				return 0;
			final double d = 1 - z;
			return 0.5 * d * d;

		}

		// -dloss(a,y)/da
		@Override
		public double dloss(double a, double y) {
			final double z = a * y;
			if (z > 1)
				return 0;
			return y * (1 - z);
		}
	},
	SmoothHingeLoss
	{
		// smoothhingeloss(a,y) = ...
		@Override
		public double loss(double a, double y) {
			final double z = a * y;
			if (z > 1)
				return 0;
			if (z < 0)
				return 0.5 - z;
			final double d = 1 - z;
			return 0.5 * d * d;
		}

		// -dloss(a,y)/da
		@Override
		public double dloss(double a, double y) {
			final double z = a * y;
			if (z > 1)
				return 0;
			if (z < 0)
				return y;
			return y * (1 - z);
		}
	};
}
