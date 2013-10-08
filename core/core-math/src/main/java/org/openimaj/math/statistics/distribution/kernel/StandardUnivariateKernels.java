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
package org.openimaj.math.statistics.distribution.kernel;

import java.util.Random;

/**
 * Standard univariate (1-d) kernel (window) implementations
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public enum StandardUnivariateKernels implements UnivariateKernel {
	/**
	 * Univariate Gaussian kernel
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	Gaussian {
		@Override
		public double sample(Random rng) {
			return rng.nextGaussian();
		}

		@Override
		public double getCutOff() {
			// 99.7% of all the data lies within 3 s.d. of the mean
			return 3;
		}

		@Override
		public double evaluate(double value) {
			return Math.exp(-(value * value) / 2) / Math.sqrt(2 * Math.PI);
		}
	},
	/**
	 * Flat window
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	Flat {
		@Override
		public double sample(Random rng) {
			return rng.nextGaussian() - 0.5;
		}

		@Override
		public double getCutOff() {
			return 0.5;
		}

		@Override
		public double evaluate(double value) {
			if (value > 0.5)
				return 0;
			if (value < -0.5)
				return 0;
			return 1;
		}

	}
}
