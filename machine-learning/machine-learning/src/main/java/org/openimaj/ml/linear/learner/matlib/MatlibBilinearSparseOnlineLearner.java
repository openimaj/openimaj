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
package org.openimaj.ml.linear.learner.matlib;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.DiagonalMatrix;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.linear.learner.BilinearLearnerParameters;
import org.openimaj.ml.linear.learner.OnlineLearner;
import org.openimaj.ml.linear.learner.matlib.init.InitStrategy;
import org.openimaj.ml.linear.learner.matlib.init.SparseSingleValueInitStrat;
import org.openimaj.ml.linear.learner.matlib.loss.LossFunction;
import org.openimaj.ml.linear.learner.matlib.loss.MatLossFunction;
import org.openimaj.ml.linear.learner.matlib.regul.Regulariser;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;


/**
 * An implementation of a stochastic gradient decent with proximal perameter adjustment
 * (for regularised parameters).
 *
 * Data is dealt with sequentially using a one pass implementation of the
 * online proximal algorithm described in chapter 9 and 10 of:
 * The Geometry of Constrained Structured Prediction: Applications to Inference and
 * Learning of Natural Language Syntax, PhD, Andre T. Martins
 *
 * The implementation does the following:
 * 	- When an X,Y is recieved:
 * 		- Update currently held batch
 * 		- If the batch is full:
 * 			- While There is a great deal of change in U and W:
 * 				- Calculate the gradient of W holding U fixed
 * 				- Proximal update of W
 * 				- Calculate the gradient of U holding W fixed
 * 				- Proximal update of U
 * 				- Calculate the gradient of Bias holding U and W fixed
 * 			- flush the batch
 * 		- return current U and W (same as last time is batch isn't filled yet)
 *
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MatlibBilinearSparseOnlineLearner implements OnlineLearner<Matrix,Matrix>, ReadWriteableBinary{

	static Logger logger = Logger.getLogger(MatlibBilinearSparseOnlineLearner.class);

	protected BilinearLearnerParameters params;
	protected Matrix w;
	protected Matrix u;
	protected LossFunction loss;
	protected Regulariser regul;
	protected Double lambda_w,lambda_u;
	protected Boolean biasMode;
	protected Matrix bias;
	protected Matrix diagX;
	protected Double eta0_u;
	protected Double eta0_w;

	private Boolean forceSparcity;

	private Boolean zStandardise;

	private boolean nodataseen;

	/**
	 * The default parameters. These won't work with your dataset, i promise.
	 */
	public MatlibBilinearSparseOnlineLearner() {
		this(new BilinearLearnerParameters());
	}
	/**
	 * @param params the parameters used by this learner
	 */
	public MatlibBilinearSparseOnlineLearner(BilinearLearnerParameters params) {
		this.params = params;
		reinitParams();
	}

	/**
	 * must be called if any parameters are changed
	 */
	public void reinitParams() {
		this.loss = this.params.getTyped(BilinearLearnerParameters.LOSS);
		this.regul = this.params.getTyped(BilinearLearnerParameters.REGUL);
		this.lambda_w = this.params.getTyped(BilinearLearnerParameters.LAMBDA_W);
		this.lambda_u = this.params.getTyped(BilinearLearnerParameters.LAMBDA_U);
		this.biasMode = this.params.getTyped(BilinearLearnerParameters.BIAS);
		this.eta0_u = this.params.getTyped(BilinearLearnerParameters.ETA0_U);
		this.eta0_w = this.params.getTyped(BilinearLearnerParameters.ETA0_W);
		this.forceSparcity = this.params.getTyped(BilinearLearnerParameters.FORCE_SPARCITY);
		this.zStandardise = this.params.getTyped(BilinearLearnerParameters.Z_STANDARDISE);
		if(!this.loss.isMatrixLoss())
			this.loss = new MatLossFunction(this.loss);
		this.nodataseen = true;
	}
	private void initParams(Matrix x, Matrix y, int xrows, int xcols, int ycols) {
		final InitStrategy wstrat = getInitStrat(BilinearLearnerParameters.WINITSTRAT,x,y);
		final InitStrategy ustrat = getInitStrat(BilinearLearnerParameters.UINITSTRAT,x,y);
		this.w = wstrat.init(xrows, ycols);
		this.u = ustrat.init(xcols, ycols);

		this.bias = SparseMatrix.sparse(ycols,ycols);
		if(this.biasMode){
			final InitStrategy bstrat = getInitStrat(BilinearLearnerParameters.BIASINITSTRAT,x,y);
			this.bias = bstrat.init(ycols, ycols);
			this.diagX = new DiagonalMatrix(ycols,1);
		}
	}

	private InitStrategy getInitStrat(String initstrat, Matrix x, Matrix y) {
		final InitStrategy strat = this.params.getTyped(initstrat);
		return strat;
	}
	@Override
	public void process(Matrix X, Matrix Y){
		final int nfeatures = X.rowCount();
		final int nusers = X.columnCount();
		final int ntasks = Y.columnCount();
//		int ninstances = Y.rowCount(); // Assume 1 instance!
		
		// only inits when the current params is null
		if (this.w == null){
			initParams(X,Y,nfeatures, nusers, ntasks); // Number of words, users and tasks
		}

		final Double dampening = this.params.getTyped(BilinearLearnerParameters.DAMPENING);
		final double weighting = 1.0 - dampening ;

		logger.debug("... dampening w, u and bias by: " + weighting);

		// Adjust for weighting
		MatlibMatrixUtils.scaleInplace(this.w,weighting);
		MatlibMatrixUtils.scaleInplace(this.u,weighting);
		if(this.biasMode){
			MatlibMatrixUtils.scaleInplace(this.bias,weighting);
		}
		// First expand Y s.t. blocks of rows contain the task values for each row of Y.
		// This means Yexp has (n * t x t)
		final SparseMatrix Yexp = expandY(Y);
		loss.setY(Yexp);
		int iter = 0;
		while(true) {
			// We need to set the bias here because it is used in the loss calculation of U and W
			if(this.biasMode) loss.setBias(this.bias);
			iter += 1;

			final double uLossWeight = etat(iter,eta0_u);
			final double wLossWeighted = etat(iter,eta0_w);
			final double weightedLambda_u = lambdat(iter,lambda_u);
			final double weightedLambda_w = lambdat(iter,lambda_w);
			// Dprime is tasks x nwords
			Matrix Dprime = null;
			if(this.nodataseen){
				this.nodataseen = false;
				Matrix fakeut = new SparseSingleValueInitStrat(1).init(this.u.columnCount(),this.u.rowCount());
				Dprime = MatlibMatrixUtils.dotProductTranspose(fakeut, X); // i.e. fakeut . X^T
			} else {
				Dprime = MatlibMatrixUtils.dotProductTransposeTranspose(u, X); // i.e. u^T . X^T
			}
			
			// ... as is the cost function's X
			if(zStandardise){
//				Vector rowMean = CFMatrixUtils.rowMean(Dprime);
//				CFMatrixUtils.minusEqualsCol(Dprime,rowMean);
			}
			loss.setX(Dprime);
			final Matrix neww = updateW(this.w,wLossWeighted, weightedLambda_w);

			// Vprime is nusers x tasks
			final Matrix Vt = MatlibMatrixUtils.transposeDotProduct(neww,X); // i.e. (X^T.neww)^T X.transpose().times(neww);
			// ... so the loss function's X is (tasks x nusers)
			loss.setX(Vt);
			final Matrix newu = updateU(this.u,uLossWeight, weightedLambda_u);
			
			final double sumchangew = MatlibMatrixUtils.normF(MatlibMatrixUtils.minus(neww, this.w));
			final double totalw = MatlibMatrixUtils.normF(this.w);

			final double sumchangeu = MatlibMatrixUtils.normF(MatlibMatrixUtils.minus(newu, this.u));
			final double totalu = MatlibMatrixUtils.normF(this.u);

			double ratioU = 0;
			if(totalu!=0) ratioU = sumchangeu/totalu;
			final double ratioW = 0;
			if(totalw!=0) ratioU = sumchangew/totalw;
			double ratioB = 0;
			double ratio = ratioU + ratioW;
			double totalbias = 0;
			if(this.biasMode){
				Matrix mult = MatlibMatrixUtils.dotProductTransposeTranspose(newu, X);
				mult = MatlibMatrixUtils.dotProduct(mult, neww);				
				MatlibMatrixUtils.plusInplace(mult, bias);
				// We must set bias to null!
				loss.setBias(null);
				loss.setX(diagX);
				// Calculate gradient of bias (don't regularise)
				final Matrix biasGrad = loss.gradient(mult);
				final double biasLossWeight = biasEtat(iter);
				final Matrix newbias = updateBias(biasGrad, biasLossWeight);

				final double sumchangebias = MatlibMatrixUtils.normF(MatlibMatrixUtils.minus(newbias, bias));
				totalbias = MatlibMatrixUtils.normF(this.bias);
				if(totalbias!=0) ratioB = (sumchangebias/totalbias) ;
				this.bias = newbias;
				ratio += ratioB;
				ratio/=3;
			}
			else{
				ratio/=2;
			}

			final Double biconvextol = this.params.getTyped("biconvex_tol");
			final Integer maxiter = this.params.getTyped("biconvex_maxiter");
			if(iter%3 == 0){
				logger.debug(String.format("Iter: %d. Last Ratio: %2.3f",iter,ratio));
				logger.debug("W row sparcity: " + MatlibMatrixUtils.sparsity(w));
				logger.debug("U row sparcity: " + MatlibMatrixUtils.sparsity(u));
				logger.debug("Total U magnitude: " + totalu);
				logger.debug("Total W magnitude: " + totalw);
				logger.debug("Total Bias: " + totalbias);
			}
			if(biconvextol  < 0 || ratio < biconvextol || iter >= maxiter) {
				logger.debug("tolerance reached after iteration: " + iter);
				logger.debug("W row sparcity: " + MatlibMatrixUtils.sparsity(w));
				logger.debug("U row sparcity: " + MatlibMatrixUtils.sparsity(u));
				logger.debug("Total U magnitude: " + totalu);
				logger.debug("Total W magnitude: " + totalw);
				logger.debug("Total Bias: " + totalbias);
				break;
			}
		}
	}
	
	protected Matrix updateBias(Matrix biasGrad, double biasLossWeight) {
		final Matrix newbias = MatlibMatrixUtils.minus(
				this.bias,
				MatlibMatrixUtils.scaleInplace(
						biasGrad,
						biasLossWeight
				)
		);
		return newbias;
	}
	protected Matrix updateW(Matrix currentW, double wLossWeighted, double weightedLambda) {
		final Matrix gradW = loss.gradient(currentW);
		MatlibMatrixUtils.scaleInplace(gradW,wLossWeighted);

		Matrix neww = MatlibMatrixUtils.minus(currentW,gradW);
		neww = regul.prox(neww, weightedLambda);
		return neww;
	}
	protected Matrix updateU(Matrix currentU, double uLossWeight, double uWeightedLambda) {
		final Matrix gradU = loss.gradient(currentU);
		MatlibMatrixUtils.scaleInplace(gradU,uLossWeight);
		Matrix newu = MatlibMatrixUtils.minus(currentU,gradU);
		newu = regul.prox(newu, uWeightedLambda);
		return newu;
	}
	private double lambdat(int iter, double lambda) {
		return lambda/iter;
	}
	/**
	 * Given a flat value matrix, makes a diagonal sparse matrix containing the values as the diagonal
	 * @param Y
	 * @return the diagonalised Y
	 */
	public static SparseMatrix expandY(Matrix Y) {
		final int ntasks = Y.columnCount();
		final SparseMatrix Yexp = SparseMatrix.sparse(ntasks, ntasks);
		for (int touter = 0; touter < ntasks; touter++) {
			for (int tinner = 0; tinner < ntasks; tinner++) {
				if(tinner == touter){
					Yexp.put(touter, tinner, Y.get(0, tinner));
				}
				else{
					Yexp.put(touter, tinner, Double.NaN);
				}
			}
		}
		return Yexp;
	}
	private double biasEtat(int iter){
		final Double biasEta0 = this.params.getTyped(BilinearLearnerParameters.ETA0_BIAS);
		return biasEta0 / Math.sqrt(iter);
	}


	private double etat(int iter,double eta0) {
		final Integer etaSteps = this.params.getTyped(BilinearLearnerParameters.ETASTEPS);
		final double sqrtCeil = Math.sqrt(Math.ceil(iter/(double)etaSteps));
		return eta(eta0) / sqrtCeil;
	}
	private double eta(double eta0) {
		return eta0 ;
	}



	/**
	 * @return the current apramters
	 */
	public BilinearLearnerParameters getParams() {
		return this.params;
	}

	/**
	 * @return the current user matrix
	 */
	public Matrix getU(){
		return this.u;
	}

	/**
	 * @return the current word matrix
	 */
	public Matrix getW(){
		return this.w;
	}
	/**
	 * @return the current bias (null if {@link BilinearLearnerParameters#BIAS} is false
	 */
	public Matrix getBias() {
		if(this.biasMode)
			return this.bias;
		else
			return null;
	}

	/**
	 * Expand the U parameters matrix by added a set of rows.
	 * If currently unset, this function does nothing (assuming U will be initialised in the first round)
	 * The new U parameters are initialised used {@link BilinearLearnerParameters#EXPANDEDUINITSTRAT}
	 * @param newUsers the number of new users to add
	 */
	public void addU(int newUsers) {
		if(this.u == null) return; // If u has not be inited, then it will be on first process
		final InitStrategy ustrat = this.getInitStrat(BilinearLearnerParameters.EXPANDEDUINITSTRAT,null,null);
		final Matrix newU = ustrat.init(newUsers, this.u.columnCount());
		this.u = MatlibMatrixUtils.vstack(this.u,newU);
	}

	/**
	 * Expand the W parameters matrix by added a set of rows.
	 * If currently unset, this function does nothing (assuming W will be initialised in the first round)
	 * The new W parameters are initialised used {@link BilinearLearnerParameters#EXPANDEDWINITSTRAT}
	 * @param newWords the number of new words to add
	 */
	public void addW(int newWords) {
		if(this.w == null) return; // If w has not be inited, then it will be on first process
		final InitStrategy wstrat = this.getInitStrat(BilinearLearnerParameters.EXPANDEDWINITSTRAT,null,null);
		final Matrix newW = wstrat.init(newWords, this.w.columnCount());
		this.w = MatlibMatrixUtils.vstack(this.w,newW);
	}

	@Override
	public MatlibBilinearSparseOnlineLearner clone(){
		final MatlibBilinearSparseOnlineLearner ret = new MatlibBilinearSparseOnlineLearner(this.getParams());
		ret.u = MatlibMatrixUtils.copy(this.u);
		ret.w = MatlibMatrixUtils.copy(this.w);
		if(this.biasMode){
			ret.bias = MatlibMatrixUtils.copy(this.bias);
		}
		return ret;
	}
	/**
	 * @param newu set the model's U
	 */
	public void setU(Matrix newu) {
		this.u = newu;
	}

	/**
	 * @param neww set the model's W
	 */
	public void setW(Matrix neww) {
		this.w = neww;
	}
	@Override
	public void readBinary(DataInput in) throws IOException {
		final int nwords = in.readInt();
		final int nusers = in.readInt();
		final int ntasks = in.readInt();


		this.w = SparseMatrix.sparse(nwords, ntasks);
		for (int t = 0; t < ntasks; t++) {
			for (int r = 0; r < nwords; r++) {
				final double readDouble = in.readDouble();
				if(readDouble != 0){
					this.w.put(r, t, readDouble);
				}
			}
		}

		this.u = SparseMatrix.sparse(nusers, ntasks);
		for (int t = 0; t < ntasks; t++) {
			for (int r = 0; r < nusers; r++) {
				final double readDouble = in.readDouble();
				if(readDouble != 0){
					this.u.put(r, t, readDouble);
				}
			}
		}

		this.bias = SparseMatrix.sparse(ntasks, ntasks);
		for (int t1 = 0; t1 < ntasks; t1++) {
			for (int t2 = 0; t2 < ntasks; t2++) {
				final double readDouble = in.readDouble();
				if(readDouble != 0){
					this.bias.put(t1, t2, readDouble);
				}
			}
		}
	}
	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(w.rowCount());
		out.writeInt(u.rowCount());
		out.writeInt(u.columnCount());
		final double[] wdata = w.asColumnMajorArray();
		for (int i = 0; i < wdata.length; i++) {
			out.writeDouble(wdata[i]);
		}
		final double[] udata = u.asColumnMajorArray();
		for (int i = 0; i < udata.length; i++) {
			out.writeDouble(udata[i]);
		}
		final double[] biasdata = bias.asColumnMajorArray();
		for (int i = 0; i < biasdata.length; i++) {
			out.writeDouble(biasdata[i]);
		}
	}


	@Override
	public Matrix predict(Matrix x) {
		Matrix xt = MatlibMatrixUtils.transpose(x);
		final Matrix mult = MatlibMatrixUtils.dotProduct(MatlibMatrixUtils.dotProduct(MatlibMatrixUtils.transpose(u), xt),this.w);
		if(this.biasMode) MatlibMatrixUtils.plusInplace(mult,this.bias);
		Matrix ydiag = new DiagonalMatrix(mult);
		return ydiag;
	}
}
