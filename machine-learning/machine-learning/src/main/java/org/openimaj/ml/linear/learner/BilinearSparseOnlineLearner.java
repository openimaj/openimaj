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

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.AbstractSparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.ml.linear.learner.init.ContextAwareInitStrategy;
import org.openimaj.ml.linear.learner.init.InitStrategy;
import org.openimaj.ml.linear.learner.init.SparseSingleValueInitStrat;
import org.openimaj.ml.linear.learner.loss.LossFunction;
import org.openimaj.ml.linear.learner.loss.MatLossFunction;
import org.openimaj.ml.linear.learner.regul.Regulariser;


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
public class BilinearSparseOnlineLearner implements OnlineLearner<Matrix,Matrix>, ReadWriteableBinary{

	static Logger logger = Logger.getLogger(BilinearSparseOnlineLearner.class);

	protected BilinearLearnerParameters params;
	protected Matrix w;
	protected Matrix u;
	protected SparseMatrixFactoryMTJ smf = SparseMatrixFactoryMTJ.INSTANCE;
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

	private double eta_gamma;

	private double biasEta0;

	/**
	 * The default parameters. These won't work with your dataset, i promise.
	 */
	public BilinearSparseOnlineLearner() {
		this(new BilinearLearnerParameters());
	}
	/**
	 * @param params the parameters used by this learner
	 */
	public BilinearSparseOnlineLearner(BilinearLearnerParameters params) {
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
		this.biasEta0 = this.params.getTyped(BilinearLearnerParameters.ETA0_BIAS);
		this.eta_gamma = params.getTyped(BilinearLearnerParameters.ETA_GAMMA);
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
		if(this.forceSparcity)
		{
			this.u = CFMatrixUtils.asSparseColumn(this.u);
			this.w = CFMatrixUtils.asSparseColumn(this.w);
		}

		this.bias = smf.createMatrix(ycols,ycols);
		if(this.biasMode){
			final InitStrategy bstrat = getInitStrat(BilinearLearnerParameters.BIASINITSTRAT,x,y);
			this.bias = bstrat.init(ycols, ycols);
			this.diagX = smf.createIdentity(ycols, ycols);
		}
	}
	
	

	private InitStrategy getInitStrat(String initstrat, Matrix x, Matrix y) {
		final InitStrategy strat = this.params.getTyped(initstrat);
		if(strat instanceof ContextAwareInitStrategy){
			final ContextAwareInitStrategy<Matrix, Matrix> cwStrat = this.params.getTyped(initstrat);
			cwStrat.setLearner(this);
			cwStrat.setContext(x, y);
			return cwStrat;
		}
		return strat;
	}
	@Override
	public void process(Matrix X, Matrix Y){
		prepareNextRound(X, Y);
		int iter = 0;
		Matrix xt = X.transpose();
		Matrix xtrows = xt;
		if(xt instanceof AbstractSparseMatrix){
			xtrows = CFMatrixUtils.asSparseRow(xt);
		}
		while(true) {
			// We need to set the bias here because it is used in the loss calculation of U and W
			if(this.biasMode) loss.setBias(this.bias);
			iter += 1;

			// Perform the bilinear operation
			final Matrix neww = updateW(xt,eta0_w, lambda_u);
			final Matrix newu = updateU(xtrows,neww,eta0_u, lambda_w);
			Matrix newbias = null;
			if(this.biasMode){
				newbias = updateBias(xt, newu, neww, biasEta0);
			}
			
			// This part of the code checks if we can stop the bilinear steps by checking how much everything has changed proportionally
			
			double ratioB = 0;
			double totalbias = 0;
			
			final double sumchangew = CFMatrixUtils.absSum(neww.minus(this.w));
			final double totalw = CFMatrixUtils.absSum(this.w);

			final double sumchangeu = CFMatrixUtils.absSum(newu.minus(this.u));
			final double totalu = CFMatrixUtils.absSum(this.u);

			double ratioU = 0;
			if(totalu!=0) ratioU = sumchangeu/totalu;
			final double ratioW = 0;
			if(totalw!=0) ratioU = sumchangew/totalw;
			double ratio = ratioU + ratioW;
			if(this.biasMode){
				final double sumchangebias = CFMatrixUtils.absSum(newbias.minus(this.bias));
				totalbias = CFMatrixUtils.absSum(this.bias);
				if(totalbias!=0) ratioB = (sumchangebias/totalbias) ;
				ratio += ratioB;
				ratio/=3;
			} else {
				ratio/=2;
			}
			
			/**
			 * This is not a matter of simply type
			 * The 0 values of the sparse matrix are also removed. very important.
			 */
			if(this.forceSparcity)
			{
				this.u = CFMatrixUtils.asSparseColumn(newu);
				this.w = CFMatrixUtils.asSparseColumn(neww);
			}
			else{

				this.w = neww;
				this.u = newu;
			}
			
			if(this.biasMode){
				this.bias = newbias;
			}

			final Double biconvextol = this.params.getTyped("biconvex_tol");
			final Integer maxiter = this.params.getTyped("biconvex_maxiter");
			if(iter%3 == 0){
				logger.debug(String.format("Iter: %d. Last Ratio: %2.3f",iter,ratio));
				logger.debug("W row sparcity: " + CFMatrixUtils.rowSparsity(w));
				logger.debug("U row sparcity: " + CFMatrixUtils.rowSparsity(u));
				logger.debug("Total U magnitude: " + totalu);
				logger.debug("Total W magnitude: " + totalw);
				logger.debug("Total Bias: " + totalbias);
			}
			if(biconvextol  < 0 || ratio < biconvextol || iter >= maxiter) {
				logger.debug("tolerance reached after iteration: " + iter);
				logger.debug("W row sparcity: " + CFMatrixUtils.rowSparsity(w));
				logger.debug("U row sparcity: " + CFMatrixUtils.rowSparsity(u));
				logger.debug("Total U magnitude: " + totalu);
				logger.debug("Total W magnitude: " + totalw);
				logger.debug("Total Bias: " + totalbias);
				break;
			}
		}
	}
	private void prepareNextRound(Matrix X, Matrix Y) {
		final int nfeatures = X.getNumRows();
		final int nusers = X.getNumColumns();
		final int ntasks = Y.getNumColumns();
//		int ninstances = Y.getNumRows(); // Assume 1 instance!
		
		// only inits when the current params is null
		if (this.w == null){
			initParams(X,Y,nfeatures, nusers, ntasks); // Number of words, users and tasks
		}

		final Double dampening = this.params.getTyped(BilinearLearnerParameters.DAMPENING);
		final double weighting = 1.0 - dampening ;

		logger.debug("... dampening w, u and bias by: " + weighting);

		// Adjust for weighting
		this.w.scaleEquals(weighting);
		this.u.scaleEquals(weighting);
		if(this.biasMode){
			this.bias.scaleEquals(weighting);
		}
		// First expand Y s.t. blocks of rows contain the task values for each row of Y.
		// This means Yexp has (n * t x t)
		final SparseMatrix Yexp = expandY(Y);
		loss.setY(Yexp);
	}
	
	protected Matrix updateBias(Matrix xt, Matrix nu, Matrix nw, double biasLossWeight) {
		Matrix newut = nu.transpose();
		Matrix utxt = CFMatrixUtils.fastdot(newut,xt);
		Matrix utxtw = CFMatrixUtils.fastdot(utxt,nw);
		final Matrix mult = utxtw.plus(this.bias);
		// We must set bias to null!
		loss.setBias(null);
		loss.setX(diagX);
		// Calculate gradient of bias (don't regularise)
		final Matrix biasGrad = loss.gradient(mult);
		Matrix newbias = null;
		for (int i = 0; i < 1000; i++) {
			logger.debug("... Line searching etab = " + biasLossWeight);
			newbias = this.bias.clone();
			Matrix scaledGradW = biasGrad.scale(1./biasLossWeight);
			newbias = CFMatrixUtils.fastminus(newbias,scaledGradW);
			
			if(loss.test_backtrack(this.bias, biasGrad, newbias, biasLossWeight)) 
				break;
			biasLossWeight *= eta_gamma;
		}
//		final Matrix newbias = this.bias.minus(
//				CFMatrixUtils.timesInplace(
//						biasGrad,
//						biasLossWeight
//				)
//		);
		return newbias;
	}
	protected Matrix updateW(Matrix xt, double wLossWeighted, double weightedLambda) {
		// Dprime is tasks x nwords
		
		Matrix Dprime = null;
		Matrix ut = this.u.transpose();				
		if(this.nodataseen){
			this.nodataseen = false;
			Matrix fakeu = new SparseSingleValueInitStrat(1).init(this.u.getNumColumns(), this.u.getNumRows());
			Dprime = CFMatrixUtils.fastdot(fakeu,xt);
		} else {
			Dprime = CFMatrixUtils.fastdot(ut, xt);
		}
		
		// ... as is the cost function's X
		if(zStandardise){
			Vector rowMean = CFMatrixUtils.rowMean(Dprime);
			CFMatrixUtils.minusEqualsCol(Dprime,rowMean);
		}
		loss.setX(Dprime);
		final Matrix gradW = loss.gradient(this.w);
		logger.debug("Abs w_grad: " + CFMatrixUtils.absSum(gradW));
		Matrix neww = null;
		for (int i = 0; i < 1000; i++) {
			logger.debug("... Line searching etaw = " + wLossWeighted);
			neww = this.w.clone();
			Matrix scaledGradW = gradW.scale(1./wLossWeighted);
			neww = CFMatrixUtils.fastminus(neww,scaledGradW);
			neww = regul.prox(neww, weightedLambda/wLossWeighted);
			if(loss.test_backtrack(this.w, gradW, neww, wLossWeighted)) 
				break;
			wLossWeighted *= eta_gamma;
		}
		
		return neww;
	}
	protected Matrix updateU(Matrix xtrows, Matrix neww, double uLossWeight, double uWeightedLambda) {
		// Vprime is nusers x tasks
		final Matrix Vprime = CFMatrixUtils.fastdot(xtrows,neww);
		// ... so the loss function's X is (tasks x nusers)
		Matrix Vt = CFMatrixUtils.asSparseRow(Vprime.transpose());
		if(zStandardise){
			Vector rowMean = CFMatrixUtils.rowMean(Vt);
			CFMatrixUtils.minusEqualsCol(Vt,rowMean);
		}
		loss.setX(Vt);
		final Matrix gradU = loss.gradient(this.u);
		logger.debug("Abs u_grad: " + CFMatrixUtils.absSum(gradU));
//		CFMatrixUtils.timesInplace(gradU,uLossWeight);
//		newu = regul.prox(newu, uWeightedLambda);
		Matrix newu = null;
		for (int i = 0; i < 1000; i++) {
			logger.debug("... Line searching etau = " + uLossWeight);
			newu = this.u.clone();
			Matrix scaledGradW = gradU.scale(1./uLossWeight);
			newu = CFMatrixUtils.fastminus(newu,scaledGradW);
			newu = regul.prox(newu, uWeightedLambda/uLossWeight);
			if(loss.test_backtrack(this.u, gradU, newu, uLossWeight)) 
				break;
			uLossWeight *= eta_gamma;
		}
		
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
		final int ntasks = Y.getNumColumns();
		final SparseMatrix Yexp = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(ntasks, ntasks);
		for (int touter = 0; touter < ntasks; touter++) {
			for (int tinner = 0; tinner < ntasks; tinner++) {
				if(tinner == touter){
					Yexp.setElement(touter, tinner, Y.getElement(0, tinner));
				}
				else{
					Yexp.setElement(touter, tinner, Double.NaN);
				}
			}
		}
		return Yexp;
	}


	protected double etat(int iter,double eta0) {
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
		final Matrix newU = ustrat.init(newUsers, this.u.getNumColumns());
		this.u = CFMatrixUtils.vstack(this.u,newU);
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
		final Matrix newW = wstrat.init(newWords, this.w.getNumColumns());
		this.w = CFMatrixUtils.vstack(this.w,newW);
	}

	@Override
	public BilinearSparseOnlineLearner clone(){
		final BilinearSparseOnlineLearner ret = new BilinearSparseOnlineLearner(this.getParams());
		ret.u = this.u.clone();
		ret.w = this.w.clone();
		if(this.biasMode){
			ret.bias = this.bias.clone();
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


		this.w = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nwords, ntasks);
		for (int t = 0; t < ntasks; t++) {
			for (int r = 0; r < nwords; r++) {
				final double readDouble = in.readDouble();
				if(readDouble != 0){
					this.w.setElement(r, t, readDouble);
				}
			}
		}

		this.u = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nusers, ntasks);
		for (int t = 0; t < ntasks; t++) {
			for (int r = 0; r < nusers; r++) {
				final double readDouble = in.readDouble();
				if(readDouble != 0){
					this.u.setElement(r, t, readDouble);
				}
			}
		}

		this.bias = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(ntasks, ntasks);
		for (int t1 = 0; t1 < ntasks; t1++) {
			for (int t2 = 0; t2 < ntasks; t2++) {
				final double readDouble = in.readDouble();
				if(readDouble != 0){
					this.bias.setElement(t1, t2, readDouble);
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
		out.writeInt(w.getNumRows());
		out.writeInt(u.getNumRows());
		out.writeInt(u.getNumColumns());
		final double[] wdata = CFMatrixUtils.getData(w);
		for (int i = 0; i < wdata.length; i++) {
			out.writeDouble(wdata[i]);
		}
		final double[] udata = CFMatrixUtils.getData(u);
		for (int i = 0; i < udata.length; i++) {
			out.writeDouble(udata[i]);
		}
		final double[] biasdata = CFMatrixUtils.getData(bias);
		for (int i = 0; i < biasdata.length; i++) {
			out.writeDouble(biasdata[i]);
		}
	}


	@Override
	public Matrix predict(Matrix x) {
		final Matrix mult = this.u.transpose().times(x.transpose()).times(this.w);
		if(this.biasMode)mult.plusEquals(this.bias);
		final Vector ydiag = CFMatrixUtils.diag(mult);
		final Matrix createIdentity = SparseMatrixFactoryMTJ.INSTANCE.createIdentity(1, ydiag.getDimensionality());
		createIdentity.setRow(0, ydiag);
		return createIdentity;
	}
}
