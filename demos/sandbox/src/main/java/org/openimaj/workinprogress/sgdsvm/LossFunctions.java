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
