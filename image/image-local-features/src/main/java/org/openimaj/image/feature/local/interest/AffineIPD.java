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
package org.openimaj.image.feature.local.interest;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.processing.convolution.BasicDerivativeKernels;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class AffineIPD implements InterestPointDetector {
	private enum MODE {
		SCALE, SHAPE, SPATIAL;
	};
	
	static Logger logger = Logger.getLogger(AffineIPD.class);

	private class Position extends InterestPointData {
		public Position(int x, int y, double scale, Matrix secondMoments) {
			this.x = x;
			this.y = y;
			this.scale = scale;
			this.secondMoments = secondMoments;

			this.secondMoments.set(0, 0, 1);
			this.secondMoments.set(0, 1, 0);
			this.secondMoments.set(1, 0, 0);
			this.secondMoments.set(1, 1, 1);
		}

		public Position(InterestPointData interestPoint) {
			this(interestPoint.x, interestPoint.y, (float) interestPoint.scale,interestPoint.secondMoments);
		}
	}

	private final List<MODE> modes = new ArrayList<MODE>();
	private AbstractIPD initialPointsDetector;
	private final int MAX_ITER = 40;
	private final int SCALE_MAX = 256;
	private final int MASK_SIZE = 9;
	
	private List<InterestPointData> points;
	private List<InterestPointData> initialPoints;
	private int ipdThreshold;

	public AffineIPD(AbstractIPD initialPoints) {
		this.initialPointsDetector = initialPoints;
//		modes.addAll(Arrays.asList(MODE.values()));
		modes.add(MODE.SPATIAL);
		modes.add(MODE.SCALE);
		modes.add(MODE.SHAPE);
		this.ipdThreshold = 10;

	}

	@Override
	public void findInterestPoints(FImage image) {
		detectMaxima(image);
	}

	@Override
	public List<InterestPointData> getInterestPoints(int npoints) {
		return this.points.subList(0, npoints < this.points.size() ? npoints : this.points.size());
	}
	
	@Override
	public List<InterestPointData> getInterestPoints() {
		return this.points;
	}
	
	public List<InterestPointData> getInitialInterestPoints(int npoints) {
		return this.initialPoints.subList(0, npoints < this.initialPoints.size() ? npoints : this.initialPoints.size());
	}
	
	class AffineIPDLoopState{
		
		public AffineIPDLoopState(FImage image, InterestPointData interestPoint) {
			this.interestPoint = interestPoint;
			selected = new Position(interestPoint);
			prev = new Position(interestPoint);
			this.image = image;
			
		}
		FImage image;
		int iteration = 0;
		int laplacianDirection = 0;
		InterestPointData selected = null;
		InterestPointData prev = null;
		boolean loopConvergence = false;
		boolean loopDivergence = false;
		boolean scaleConvergence = false;
		boolean affineConvergence = false;
		double affineExp = 0.8f;
		double sx_step = 0.25f;
		double QratioOld=0;
		double QratioSgn=0;
		double normLaplacian = 0;
		public int cx;
		public int cy;
		public FImage subImage;
		public double xszf;
		public int windowRadius;
		public Matrix selcov;
		public Matrix selcovSqrt;
		public double doubleScale;
		public InterestPointData interestPoint;
	}
	
	public AffineIPDLoopState getNewState(FImage image,InterestPointData interestPoint){
		return new AffineIPDLoopState(image,interestPoint);
	}
	
	private void detectMaxima(FImage image) {
		// for each initial interest point
		List<InterestPointData> currentInitialPoints = initialPointsFromIPD(image);
		int i = 0;
		for (InterestPointData interestPoint : currentInitialPoints) {
			logger.info(i++ + "/" + currentInitialPoints.size());
			AffineIPDLoopState state = getNewState(image, interestPoint);
			while (!stoppingCondition(state)) 
			{	
				initLoopState(state);
				if (this.modes.contains(MODE.SCALE)) {
					convergeScale(state);
				}

				if (!(state.scaleConvergence && state.affineConvergence)) {
					InterestPointData closest = findPointInCurrentRegion(state);
					if(closest  == null) {
						logger.debug("... no new points found, divergence!");
						state.loopDivergence = true;
					}
					else{
						// Point found
						state.selected = closest;
						if(!this.modes.contains(MODE.SPATIAL)){
							resetSelectedPosition(state);
						}
						if(this.modes.contains(MODE.SHAPE)){
							convergeShape(state);
						}
					}
				}
				checkForDivergence(state);
				state.iteration+=1;
				outputState(state);
			}
			addNonDivergentPoint(state);
		}
	}
	
	private List<InterestPointData> initialPointsFromIPD(FImage image) {
		List<InterestPointData> currentInitialPoints = this.initialPointsDetector.getInterestPoints(ipdThreshold);
		image = image.multiply(255f);
		this.points = new ArrayList<InterestPointData>();
		this.initialPoints = new ArrayList<InterestPointData>();
		
		return currentInitialPoints;
	}

	private void addNonDivergentPoint(AffineIPDLoopState state) {
		if(!state.loopDivergence){
			logger.debug("No divergence, Adding it!");
			EigenvalueDecomposition eigFinal = new EigenvalueDecomposition(state.selected.secondMoments.inverse());
			Matrix dFinal = eigFinal.getD();
			double QratioFinal = MatrixUtils.maxAbsDiag(dFinal) / MatrixUtils.minAbsDiag(dFinal);
			logger.info("Final point at: ");
			logger.info(state.selected.secondMoments.toString());
			logger.info(String.format("FINAL iter: %d, x=%d, y=%d, sx=%1.5f, Q=%1.2f, Lapval=%2.1f\n",state.iteration-1,state.selected.x,state.selected.y,state.selected.scale,QratioFinal,state.normLaplacian));
			logger.info(String.format("ORIGNAL x=%d, y=%d, sx=%1.5f\n",state.interestPoint.x,state.interestPoint.y,state.interestPoint.scale));
			this.points.add(state.selected);
			this.initialPoints.add(state.interestPoint);
		}
		else{
			logger.debug("There was a divergence!");
		}
	}

	private void outputState(AffineIPDLoopState state) {
//		debugFrame = DisplayUtilities.display(AbstractIPD.visualise(current, original.clone().gaussianBlur((float)Math.sqrt(selected.scale)),2),debugFrame);
//		eig = eig(selected);
//	    Q=max(abs(diag(d)))/min(abs(diag(d)));
		EigenvalueDecomposition eigFinal = new EigenvalueDecomposition(state.selected.secondMoments.inverse());
		Matrix dFinal = eigFinal.getD();
		double QratioFinal = MatrixUtils.maxAbsDiag(dFinal) / MatrixUtils.minAbsDiag(dFinal);
		
		logger.debug(String.format("iter: %d, x=%d, y=%d, sx=%1.5f, Q=%1.2f, Lapval=%2.1f\n",state.iteration,state.selected.x,state.selected.y,state.selected.scale,QratioFinal,state.normLaplacian));
	}

	private void checkForDivergence(AffineIPDLoopState state) {
		EigenvalueDecomposition eig = state.selected.secondMoments.eig();
		// If it's imaginary, divergence!
		double[] imaginaries = eig.getImagEigenvalues();
		if (imaginaries[0] + imaginaries[1] != 0) {
			logger.debug("... New affine transform contains imaginarys, divergence!");
			state.loopDivergence = true;
		}
		if(!(state.iteration<MAX_ITER && state.selected.x >= 0 && state.selected.x < state.image.width && state.selected.y >= 0 && state.selected.y < state.image.height)){
			logger.debug("... New point is outside the image, divergence!");
			state.loopDivergence = true;
		}
		List<InterestPointData> current = new ArrayList<InterestPointData>();
		current.add(state.selected);
	}

	private void convergeShape(AffineIPDLoopState state) {
		Matrix newSecondOrder = state.selected.secondMoments.inverse();
		if(MatrixUtils.anyNaNorInf(newSecondOrder)){
			logger.debug("... the new second order matrix contains NaN or Inf!");
			state.loopDivergence = true;
		}
		else{
			newSecondOrder = newSecondOrder.times(1.0 / Math.sqrt(newSecondOrder.det()));
			EigenvalueDecomposition eig = newSecondOrder.eig();
			Matrix d = eig.getD();
//			newSecondOrder = v.transpose().times(diagonalPow(d.copy(),affineExp)).times(v);
//			newSecondOrder = v.times(diagonalPow(d.copy(),affineExp)).times(v.transpose());
//			newSecondOrder = newSecondOrder.times(1.0 / Math.sqrt(newSecondOrder.det()));
			
			Matrix newSecondOrderCorr = newSecondOrder.inverse().times(state.selcov.inverse());
			state.selected.secondMoments = newSecondOrderCorr;
			
			double QratioMin = 1.05f;
			newSecondOrder = newSecondOrder.times(1.0/Math.sqrt(newSecondOrder.det()));
//			eig = newSecondOrder.eig();
			d = eig.getD();
			
			double Qratio = MatrixUtils.maxAbsDiag(d) / MatrixUtils.minAbsDiag(d);
			if(state.QratioOld > 0){
				
				state.QratioSgn = Qratio - state.QratioOld;
			}
			state.QratioOld = Qratio;
			
			if(Qratio < QratioMin)
			{
				logger.debug("Shape convergence!");
				logger.debug(state.selected.secondMoments.toString());
				
				state.affineConvergence = true;
			}
			else{
				state.affineConvergence = false;
			}
				
		}
	}

	private void resetSelectedPosition(AffineIPDLoopState state) {
		state.selected.x = state. prev.x;
		state.selected.y = state.prev.y;
	}

	private InterestPointData findPointInCurrentRegion(AffineIPDLoopState state) {
		state.doubleScale = 2 * state.selected.scale;
		this.initialPointsDetector.setDetectionScaleVariance((float) state.selected.scale);
		this.initialPointsDetector.setIntegrationScaleVariance((float) state.doubleScale);
		if(this.initialPointsDetector instanceof HarrisIPD)
			((HarrisIPD)this.initialPointsDetector).eigenRatio = 0.01f;
		this.initialPointsDetector.findInterestPoints(state.subImage);
		
		List<InterestPointData> newPoints = this.initialPointsDetector.getInterestPoints(100);
		correctPoints(state.cx, state.cy, newPoints, Math.max(0,state.selected.x - state.windowRadius ), Math.max(0,state.selected.y - state.windowRadius),state.selcovSqrt);
		InterestPointData closest = findClosestPoint(newPoints,state.interestPoint);
		return closest;
	}

	private void convergeScale(AffineIPDLoopState state) {
		FImage blurredSubImage = state.subImage.clone().processInline(new FGaussianConvolve((float)Math.sqrt(state.doubleScale)));
		blurredSubImage = blurredSubImage.extractCenter( state.cx,state.cy,MASK_SIZE, MASK_SIZE);
		double xxSum, yySum, xxxxSum, yyyySum, xxyySum;
		xxSum = elementWiseMask(BasicDerivativeKernels.DXX_KERNEL,blurredSubImage);
		yySum = elementWiseMask(BasicDerivativeKernels.DYY_KERNEL,blurredSubImage);
		xxxxSum = elementWiseMask(BasicDerivativeKernels.DXXXX_KERNEL,blurredSubImage);
		xxyySum = elementWiseMask(BasicDerivativeKernels.DXXYY_KERNEL,blurredSubImage);
		yyyySum = elementWiseMask(BasicDerivativeKernels.DYYYY_KERNEL,blurredSubImage);
		
		logger.debug("xxSum = " + xxSum);
		logger.debug("yySum = " + yySum);
		logger.debug("xxxxSum = " + xxxxSum);
		logger.debug("xxyySum = " + xxyySum);
		logger.debug("yyyySum = " + yyyySum);
		logger.debug("state.doubleScale = " + state.doubleScale);

		state.normLaplacian = (xxSum + yySum) * state.doubleScale;
		double xvalLaplacian = (xxSum + yySum) + ((state.selected.scale) * (xxxxSum + yyyySum + 2 * xxyySum));

		int newLaplacianDirection = sign(xvalLaplacian) * sign(state.normLaplacian) ;
		if (newLaplacianDirection * state.laplacianDirection < 0) {
			state.sx_step = state.sx_step / 2;
		}
		state.laplacianDirection = newLaplacianDirection;
		logger.debug("xvalLaplacian: " + xvalLaplacian + " sign: " + sign(xvalLaplacian));
		logger.debug("normLaplacian: " + state.normLaplacian + " sign: " + sign(state.normLaplacian));
		logger.debug("Current lap direction is: " + state.laplacianDirection);

		double newScale =  (state.selected.scale * Math.pow(2, state.sx_step * state.laplacianDirection));
		state.doubleScale = 2 * newScale;

		blurredSubImage = state.subImage.clone().process(new FGaussianConvolve( (float) Math.sqrt(state.doubleScale)));
		blurredSubImage = blurredSubImage.extractCenter( MASK_SIZE, MASK_SIZE);

		xxSum = elementWiseMask(BasicDerivativeKernels.DXX_KERNEL,blurredSubImage);
		yySum = elementWiseMask(BasicDerivativeKernels.DYY_KERNEL, blurredSubImage);
		double newNormLaplacian = (xxSum + yySum) * state.doubleScale;
		logger.debug("OLD normLaplacian = " + state.normLaplacian);
		logger.debug("NEW normLaplacian = " + newNormLaplacian);
		if (state.normLaplacian * state.normLaplacian > newNormLaplacian * newNormLaplacian ) {
			logger.debug("SCALE CONVERGED");
			state.scaleConvergence = true;
		} else {
			logger.debug("FAIL SCALE CONVERGED");
			state.selected.scale = state.selected.scale * Math.pow(2, state.sx_step * state.laplacianDirection);
			state.scaleConvergence = false;
		}
		if (state.doubleScale > SCALE_MAX) {
			// If the scal
			state.loopDivergence = true;
		}
	}

	/**
	 * Find a window that corresponds to the affine transform area around the current point. Note that the window size in the origianl code was a SQRT of the scale,
	 * removing this seemed to remove some unstable features and results in general with bigger windows used to finding future points.
	 * @param state
	 */
	public void initLoopState(AffineIPDLoopState state){
		logger.debug("Loop: " + state.iteration );
		logger.debug("Current second moment matrix: " );
		logger.debug(state.selected.secondMoments.toString());
		state.prev = state.selected.clone();
		logger.debug("Cloned selected...");
		state.selcov = state.selected.getCovarianceMatrix();
		logger.debug("Got covar...");
		state.selcovSqrt = MatrixUtils.sqrt(state.selcov);
		logger.debug("Got sqrt...");
		
		
		state.xszf = 1;
		if (this.modes.contains(MODE.SHAPE)){
			EigenvalueDecomposition seleig = state.selcov.eig();
			Matrix seld = seleig.getD();
			double Qratio = MatrixUtils.maxAbsDiag(seld) / MatrixUtils.minAbsDiag(seld);
			state.xszf = Qratio;
		}
		logger.debug("Got eig...");

		state.windowRadius = (int) Math.max(9,Math.round(state.xszf * 4 * state.selected.scale));
		state.subImage = state.image.extractCenter(state.selected.x,state.selected.y, state.windowRadius * 2, state.windowRadius * 2);
		logger.debug("Extracted center...");
		
		state.cx = Math.min(state.windowRadius, state.selected.x);
		state.cy = Math.min(state.windowRadius, state.selected.y);
		if (this.modes.contains(MODE.SHAPE)) {
			state.subImage = state.subImage.funkyTransform(state.selcovSqrt, state.cx, state.cy);
			state.windowRadius = (int) Math.max(9,Math.round(4 * state.selected.scale));
			state.subImage = state.subImage.extractCenter(state.cx,state.cy,state.windowRadius * 2, state.windowRadius * 2);
			state.cx = Math.min(state.windowRadius, state.cx);
			state.cy = Math.min(state.windowRadius, state.cy);
		}
		state.doubleScale = 2 * state.selected.scale;
		logger.debug("Transformed!...");
	}
	
	private void correctPoints(int cx, int cy,List<InterestPointData> newPoints, int x, int y, Matrix selcovSqrt) {
		for(InterestPointData point : newPoints){
			if (this.modes.contains(MODE.SHAPE)) {
				double correctedX = point.x - cx;
				double correctedY = point.y - cy;
				point.x = (int) Math.round(correctedX * selcovSqrt.get(0, 0) + correctedY * selcovSqrt.get(0, 1) + cx);
				point.y = (int) Math.round(correctedX * selcovSqrt.get(1, 0) + correctedY * selcovSqrt.get(1, 1) + cx);
			}
			
			point.x = point.x + x;
			point.y = point.y + y;
		}
	}

	/**
	 * Closest point in a set of points. Points are relative to a subimage which was originally extracted about the center of current. Find the point
	 * closest to initial. The function also correctly transforms list of points based on current's second moment matrix.
	 * @param subImage
	 * @param newPoints
	 * @param initial
	 * @param current
	 * @return
	 */
	private InterestPointData findClosestPoint(List<InterestPointData> newPoints, InterestPointData initial) {
		InterestPointData best = null;
		double bestDistance = -1;
		for(InterestPointData possible : newPoints){
			int dX = initial.x - possible.x;
			int dY = initial.y - possible.y;
			
			double newDistance = Math.sqrt(dX*dX + dY*dY);
			if(best == null || newDistance < bestDistance){
				bestDistance = newDistance;
				best = possible;
			}
		}
		return best;
	}

	private int sign(double xvalLaplacian) {
		return (xvalLaplacian <= 0 ? (xvalLaplacian == 0 ? 0 : -1) : 1);
	}

	private double elementWiseMask(KernelProcessor<Float, FImage> kernel, FImage image) {
		int roiWidth = kernel.getKernelWidth();
		int roiHeight = kernel.getKernelHeight();
		return kernel.processKernel(image.extractCenter(roiWidth, roiHeight));
	}
	
	public EigenValueVectorPair fast2x2EigenDecomposition(Matrix m){
		/**
		 * A = 1
		 * B = a + d
		 * C = ad - bc
		 * 
		 * x = ( - B (+/-) sqrt(B^2 - 4AC) )/ (2A) 
		 */
		double a = m.get(0, 0);
		double b = m.get(0, 1);
		double c = m.get(1, 0);
		double d = m.get(1, 1);
		double A = 1;
		double B = a + d;
		double C = a*d - b*c;
		
		Matrix val = new Matrix(2,1);
		double sqrtInner = B*B - 4 * A * C;
		if(sqrtInner < 0){
			return null;
		}
		double firstEig = ( B + Math.sqrt(sqrtInner) ) / ( 2 * A );
		double secondEig = ( B - Math.sqrt(sqrtInner) ) / ( 2 * A );
		
		val.set(0, 0, firstEig);
		val.set(1, 0, secondEig);
		
		Matrix vec = new Matrix(2,2);
		if(b == 0 && c == 0){
			vec.set(0, 0, 1);
			vec.set(1, 1, 1);
		}
		else
		{	
			if(c != 0){
				double v1 = firstEig - d;
				double v2 = secondEig - d;
				double norm1 = Math.sqrt(v1*v1 + c*c);
				double norm2 = Math.sqrt(c*c + v2*v2);
				vec.set(0, 0, v1/norm1);
				vec.set(0, 1, v2/norm2);
				vec.set(1, 0, c/norm1);
				vec.set(1, 1, c/norm2);
			}
			else if(b != 0){
				double v1 = firstEig - a;
				double v2 = secondEig - a;
				double norm1 = Math.sqrt(v1*v1 + b*b);
				double norm2 = Math.sqrt(b*b + v2*v2);
				vec.set(0, 0, b/norm1);
				vec.set(0, 1, b/norm2);
				vec.set(1, 0, v1/norm1);
				vec.set(1, 1, v2/norm2);
			}
		}
		
		EigenValueVectorPair ret = new EigenValueVectorPair(val,vec);
		return ret;
	}

	public FImage affineWarpImage(FImage subImage, Matrix selcov) {
		return subImage.funkyTransform(selcov, (int)(subImage.getWidth()/2.0), (int)(subImage.getHeight()/2.0));
	}

	private boolean stoppingCondition(AffineIPDLoopState state) {
		return (state.iteration >= MAX_ITER
				|| (state.selected.equalPos(state.prev)
						&& isScaleDone(state.scaleConvergence) && isAffineDone(state.affineConvergence))
				|| state.loopConvergence || state.loopDivergence);
	}

	private boolean isAffineDone(boolean affineConvergence) {
		return !(this.modes.contains(MODE.SHAPE) ^ affineConvergence);
	}

	private boolean isScaleDone(boolean scaleConvergence) {
		return !(this.modes.contains(MODE.SCALE) ^ scaleConvergence);
	}

	public void setIPDThreshold(int i) {
		this.ipdThreshold = i;
	}

	public List<InterestPointData> getInitialInterestPoints(){
		return this.initialPoints;
	}
}
