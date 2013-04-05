package org.openimaj.ml.linear.learner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.openimaj.io.WriteableASCII;
import org.openimaj.ml.linear.learner.init.SparseRandomInitStrategy;
import org.openimaj.ml.linear.learner.init.SparseZerosInitStrategy;
import org.openimaj.ml.linear.learner.loss.SquareMissingLossFunction;
import org.openimaj.ml.linear.learner.regul.L1L2Regulariser;

public class BilinearLearnerParameters extends LearningParameters implements WriteableASCII{
	
	/**
	 * whether a bias component is added to w and u. Default is false.
	 */
	public static final String BIAS = "bias";
	/**
	 * Defaults to -1 (i.e. no seed)
	 */
	public static final String SEED = "seed";
	/**
	 * Defaults to a {@link SparseRandomInitStrategy} with sparcity set to 0.5
	 */
	public static final String WINITSTRAT = "winitstrat";
	/**
	 * Defaults to a {@link SparseRandomInitStrategy} with sparcity set to 0.5
	 */
	public static final String UINITSTRAT = "uinitstrat";
	/**
	 * Defaults to a {@link SparseZerosInitStrategy}
	 */
	public static final String BIASINITSTRAT = "biasinitstrat";
	/**
	 * Defaults to 1 (currently this is ignored)
	 */
	public static final String BATCHSIZE = "batchsize";
	/**
	 * Whether different tasks have independant u values, defualts to true
	 */
	public static final String INDU = "indu";
	/**
	 * Whether different tasks have independant w values, defualts to true
	 */
	public static final String INDW = "indw";
	/**
	 * The maximum number of iterations per batch, defaults to 3
	 */
	public static final String BICONVEX_MAXITER = "biconvex_maxiter";
	/**
	 * The threshold of the ratio between the (sum(new_w - old_w) + sum(new_u - old_u)) / (sum(old_u) + sum(old_w))
	 * i.e. some notion of normalised changed of the paramters. Defaults to 0.01
	 */
	public static final String BICONVEX_TOL = "biconvex_tol";
	/**
	 * The parameter of the regulariser, defaults to 0.001
	 */
	public static final String LAMBDA = "lambda";
	/**
	 * The weighting of the subgradient, weighted down each iteration of the biconvex scheme, defaults to 0.05
	 */
	public static final String ETA0_U = "eta0u";
	/**
	 * The weighting of the subgradient, weighted down each iteration of the biconvex scheme, defaults to 0.05
	 */
	public static final String ETA0_W = "eta0w";
	/**
	 * The weighting of the subgradient, weighted down each iteration of the biconvex scheme, defaults to eta0 (0.05)
	 */
	public static final String BIASETA0 = "biaseta0";
	/**
	 * The loss function, defaults to {@link SquareMissingLossFunction}
	 */
	public static final String LOSS = "loss";
	/**
	 * The regularisation function, defaults to {@link L1L2Regulariser}
	 */
	public static final String REGUL = "regul";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2059819246888686435L;
	public static final String ETASTEPS = "etasteps";
	
	public BilinearLearnerParameters() {
		this.defaults.put(REGUL, new L1L2Regulariser());
		this.defaults.put(LOSS, new SquareMissingLossFunction());
		this.defaults.put(ETA0_U, 0.05);
		this.defaults.put(ETA0_W, 0.05);
		this.defaults.put(LAMBDA, 0.001);
		this.defaults.put(BICONVEX_TOL, 0.01);
		this.defaults.put(BICONVEX_MAXITER, 3);
		this.defaults.put(INDW, true);
		this.defaults.put(INDU, true);
		this.defaults.put(SEED, -1);
		this.defaults.put(WINITSTRAT, new SparseRandomInitStrategy(0,1,0.5,new Random()));
		this.defaults.put(UINITSTRAT, new SparseRandomInitStrategy(0,1,0.5,new Random()));
		this.defaults.put(BATCHSIZE, 1); // Currently ignored
		this.defaults.put(BIAS, false);
		this.defaults.put(BIASINITSTRAT, new SparseZerosInitStrategy());
		this.defaults.put(BIASETA0, 0.05);
		this.defaults.put(ETASTEPS, 3);
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		Set<String> a = new HashSet<String>(this.keySet());
		a.addAll(this.defaults.keySet());
		for (String key : a) {
			out.printf("%s: %s\n", key, this.getTyped(key));
		}
	}

	@Override
	public String asciiHeader() {
		return "Bilinear Learner Params";
	}
	
}