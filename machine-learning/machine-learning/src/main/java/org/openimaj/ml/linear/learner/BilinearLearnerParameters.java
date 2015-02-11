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
package org.openimaj.ml.linear.learner;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.openimaj.io.WriteableASCII;
import org.openimaj.ml.linear.learner.init.CurrentUMean;
import org.openimaj.ml.linear.learner.init.CurrentWMean;
import org.openimaj.ml.linear.learner.init.SparseRandomInitStrategy;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.ml.linear.learner.loss.SquareMissingLossFunction;
import org.openimaj.ml.linear.learner.regul.L1L2Regulariser;

/**
 * Parameters used to control a {@link BilinearSparseOnlineLearner}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class BilinearLearnerParameters extends LearningParameters implements WriteableASCII {

	/**
	 * whether a bias component is added to w and u. Default is false.
	 */
	public static final String BIAS = "bias";
	/**
	 * The random seed of any randomised components of this learner (usually
	 * initialisation). Defaults to -1 (i.e. no seed)
	 */
	public static final String SEED = "seed";
	/**
	 * The initialisation strategy for W. Defaults to a
	 * {@link SparseRandomInitStrategy} with sparsity set to 0.5
	 */
	public static final String WINITSTRAT = "winitstrat";
	/**
	 * The initialisation strategy for U. Defaults to a
	 * {@link SparseRandomInitStrategy} with sparsity set to 0.5
	 */
	public static final String UINITSTRAT = "uinitstrat";
	/**
	 * The initialisation strategy for W when it is expanded. Defaults to the
	 * context aware {@link CurrentWMean}
	 */
	public static final String EXPANDEDWINITSTRAT = "expandedwinitstrat";
	/**
	 * The initialisation strategy for U when it is expanded. Defaults to the
	 * context aware {@link CurrentUMean}
	 */
	public static final String EXPANDEDUINITSTRAT = "expandeduinitstrat";
	/**
	 * The initialisation strategy for BIAS. Defaults to a
	 * {@link SparseZerosInitStrategy}
	 */
	public static final String BIASINITSTRAT = "biasinitstrat";
	/**
	 * The maximum number of iterations in the biconvex iterative stage.
	 * defaults to 3
	 */
	public static final String BICONVEX_MAXITER = "biconvex_maxiter";
	/**
	 * The threshold of the ratio between the (sum(new_w - old_w) + sum(new_u -
	 * old_u)) / (sum(old_u) + sum(old_w)) i.e. some notion of normalised
	 * changed of the paramters. Defaults to 0.01
	 */
	public static final String BICONVEX_TOL = "biconvex_tol";
	/**
	 * The parameter of the regulariser for W, defaults to LAMBDA
	 */
	public static final String LAMBDA_W = "lambda_w";
	/**
	 * The parameter of the regulariser for U, defaults to LAMBDA
	 */
	public static final String LAMBDA_U = "lambda_u";
	/**
	 * The parameter of the regulariser for both W and U
	 */
	public static final String LAMBDA = "lambda";
	/**
	 * The weighting of the subgradient of U, weighted down each ETASTEPS number
	 * of iterations of the biconvex scheme, defaults to 0.05
	 */
	public static final String ETA0_U = "eta0u";
	/**
	 * The weighting of the subgradient of W, weighted down each ETASTEPS number
	 * of iterations of the biconvex scheme, defaults to 0.05
	 */
	public static final String ETA0_W = "eta0w";
	/**
	 * The weighting of the subgradient of BIAS, weighted down each ETASTEPS
	 * number of iterations of the biconvex scheme, defaults to eta0 (0.05)
	 */
	public static final String ETA0_BIAS = "biaseta0";
	/**
	 * The loss function, defaults to {@link SquareMissingLossFunction}
	 */
	public static final String LOSS = "loss";
	/**
	 * The regularisation function, defaults to {@link L1L2Regulariser}
	 */
	public static final String REGUL = "regul";

	/**
	 * The steps at which point the eta parameter is reduced, defaults to 3
	 */
	public static final String ETASTEPS = "etasteps";
	/**
	 * Should all parameter matricies be held {@link SparseMatrix} instances and
	 * therefore remain sparse. Forces a copy but could save a lot.
	 */
	public static final String FORCE_SPARCITY = "forcesparcity";
	/**
	 * The value of w, u and beta are updated each time data is added s.t. w = w
	 * * (1.0 - DAM
	 * PENING). The default value is 0
	 */
	public static final String DAMPENING = "dampening";
	
	
	/**
	 * Whether the Vprime and Dprime matrices should be zscore standardised
	 */
	public static final String Z_STANDARDISE ="z_standardise";
	/**
	 *
	 */
	private static final long serialVersionUID = -2059819246888686435L;
	
	
	/**
	 * The ammount by which ETA is made to increase each iteration 
	 */
	public static final String ETA_GAMMA = "gamma";

	/**
	 * sets up the defaults
	 */
	public BilinearLearnerParameters() {
		this.defaults.put(REGUL, new L1L2Regulariser());
		this.defaults.put(LOSS, new SquareMissingLossFunction());
		this.defaults.put(ETA0_U, 0.05);
		this.defaults.put(ETA0_W, 0.05);
		this.defaults.put(LAMBDA, 0.001);
		this.defaults.put(LAMBDA_W, new Placeholder(LAMBDA));
		this.defaults.put(LAMBDA_U, new Placeholder(LAMBDA));
		this.defaults.put(BICONVEX_TOL, 0.01);
		this.defaults.put(BICONVEX_MAXITER, 3);
		this.defaults.put(SEED, -1);
		this.defaults.put(WINITSTRAT, new SparseRandomInitStrategy(0, 1, 0.5, new Random()));
		this.defaults.put(UINITSTRAT, new SparseRandomInitStrategy(0, 1, 0.5, new Random()));
		this.defaults.put(EXPANDEDWINITSTRAT, new CurrentWMean());
		this.defaults.put(EXPANDEDUINITSTRAT, new CurrentUMean());
		this.defaults.put(BIAS, false);
		this.defaults.put(BIASINITSTRAT, new SparseZerosInitStrategy());
		this.defaults.put(ETA0_BIAS, 0.05);
		this.defaults.put(ETASTEPS, 3);
		this.defaults.put(FORCE_SPARCITY, true);
		this.defaults.put(DAMPENING, 0d);
		this.defaults.put(Z_STANDARDISE, false);
		this.defaults.put(ETA_GAMMA,1.5);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		final Set<String> a = new HashSet<String>(this.keySet());
		a.addAll(this.defaults.keySet());
		for (final String key : a) {
			out.printf("%s: %s\n", key, this.getTyped(key));
		}
	}
	
	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer, true);
		try {
			writeASCII(pw);
		} catch (IOException e) {
		}
		pw.flush();
		return writer.toString();
	}

	@Override
	public String asciiHeader() {
		return "Bilinear Learner Params";
	}
	
	@Override
	public BilinearLearnerParameters clone() {
		BilinearLearnerParameters ret = new BilinearLearnerParameters();
		for (java.util.Map.Entry<String, Object> ent : this.entrySet()) {
			ret.put(ent.getKey(), ent.getValue());
		}
		return ret;
	}

}
