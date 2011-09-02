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

//public class AffineIPD implements InterestPointDetector {
//	private enum MODE {
//		SCALE, SHAPE, SPATIAL;
//	};
//	
//	static Logger logger = Logger.getLogger(AffineIPD.class);
//	static{
//		BasicConfigurator.configure();
//		logger.setLevel(Level.DEBUG);
//	}
//
//	private class Position extends InterestPointData {
//		public Position(int x, int y, float scale, Matrix secondMoments) {
//			this.x = x;
//			this.y = y;
//			this.scale = scale;
//			this.secondMoments = secondMoments;
//
//			this.secondMoments.set(0, 0, 1);
//			this.secondMoments.set(0, 1, 0);
//			this.secondMoments.set(1, 0, 0);
//			this.secondMoments.set(1, 1, 1);
//		}
//
//		public Position(InterestPointData interestPoint) {
//			this(interestPoint.x, interestPoint.y, (float) interestPoint.scale,interestPoint.secondMoments);
//		}
//	}
//
//	private final List<MODE> modes = new ArrayList<MODE>();
//	private AbstractStructureTensorIPD initialPointsDetector;
//	private AbstractStructureTensorIPD internalPointDetector;
//	private final int MAX_ITER = 20;
//	private final int SCALE_MAX = 256;
//	private final int MASK_SIZE = 9;
//	private final float MIN_QRATIO = 1.05f; // If the ratio of eigenvalues between two iterations is below this number we have converged
//	public boolean FAST_EIGEN = false; 
//	
//	private List<InterestPointData> points;
//	private List<InterestPointData> initialPoints;
//	private IPDSelectionMode internalDetectorSelector;
//	private IPDSelectionMode initialDetectorSelector;
//	
//
//	public AffineIPD(AbstractStructureTensorIPD initialPoints,IPDSelectionMode initialDetectorSelector,IPDSelectionMode internalDetectorSelector) {
//		this.initialPointsDetector = initialPoints.clone();
//		this.internalPointDetector = initialPoints.clone();
//		if(this.internalPointDetector instanceof HarrisIPD)
//			((HarrisIPD)this.internalPointDetector).eigenRatio = 0.01f;
////		modes.addAll(Arrays.asList(MODE.values()));
//		modes.add(MODE.SPATIAL);
//		modes.add(MODE.SCALE);
//		modes.add(MODE.SHAPE);
//		this.internalDetectorSelector = internalDetectorSelector;
//		this.initialDetectorSelector = initialDetectorSelector;
//
//	}
//
//	@Override
//	public void findInterestPoints(FImage image) {
//		detectMaxima(image);
//	}
//
//	@Override
//	public List<InterestPointData> getInterestPoints(int npoints) {
//		if(npoints < 0) npoints = this.points.size();
//		return this.points.subList(0, npoints < this.points.size() ? npoints : this.points.size());
//	}
//	
//	@Override
//	public List<InterestPointData> getInterestPoints(float threshold) {
//		List<InterestPointData> validPoints = new ArrayList<InterestPointData>();
//		for(InterestPointData  point : this.points){
//			if(point.score > threshold){
//				validPoints.add(point);
//			}
//		}
//		return validPoints;
//	}
//	
//	@Override
//	public List<InterestPointData> getInterestPoints() {
//		return this.points;
//	}
//	
//	public List<InterestPointData> getInitialInterestPoints(int npoints) {
//		return this.initialPoints.subList(0, npoints < this.initialPoints.size() ? npoints : this.initialPoints.size());
//	}
//	
//	class AffineIPDLoopState{
//		
//		public AffineIPDLoopState(FImage image, InterestPointData interestPoint) {
//			this.interestPoint = interestPoint;
//			selected = new Position(interestPoint);
//			prev = new Position(interestPoint);
//			this.image = image;
//			
//		}
//		FImage image;
//		int iteration = 0;
//		int laplacianDirection = 0;
//		InterestPointData selected = null;
//		InterestPointData prev = null;
//		boolean loopConvergence = false;
//		boolean loopDivergence = false;
//		boolean scaleConvergence = false;
//		boolean affineConvergence = false;
//		float affineExp = 0.8f;
//		float sx_step = 0.25f;
//		float QratioOld=0;
//		float QratioSgn=0;
//		float normLaplacian = 0;
//		public int cx;
//		public int cy;
//		public FImage subImage;
//		public float xszf;
//		public int windowRadius;
//		public Matrix selcov;
//		public Matrix selcovSqrt;
//		public float doubleScale;
//		public InterestPointData interestPoint;
//	}
//	
//	private void detectMaxima(FImage image) {
//		// for each initial interest point
//		List<InterestPointData> currentInitialPoints = initialPointsFromIPD(image);
//		int i = 0;
//		for (InterestPointData interestPoint : currentInitialPoints) {
//			logger.info(++i + "/" + currentInitialPoints.size());
//			AffineIPDLoopState state = getNewState(image, interestPoint);
//			while (!stoppingCondition(state)) 
//			{	
//				initLoopState(state);
//				if (this.modes.contains(MODE.SCALE)) {
//					convergeScale(state);
//				}
//				if (!(state.scaleConvergence && state.affineConvergence)) {
//					InterestPointData closest = findPointInCurrentRegion(state);
//					if(closest  == null) {
//						logger.debug("... no new points found, divergence!");
//						state.loopDivergence = true;
//					}
//					else{
//						// Point found
//						state.selected = closest;
//						if(!this.modes.contains(MODE.SPATIAL)){
//							resetSelectedPosition(state);
//						}
//						if(this.modes.contains(MODE.SHAPE)){
//							convergeShape(state);
//						}
//					}
//				}
//				state.iteration+=1;
//				checkForDivergence(state);
//				outputState(state);
//			}
//			addNonDivergentPoint(state);
//		}
//	}
//	
//	
//
//	private List<InterestPointData> initialPointsFromIPD(FImage image) {
//		this.initialPointsDetector.findInterestPoints(image);
//		List<InterestPointData> currentInitialPoints = this.initialDetectorSelector.selectPoints(this.initialPointsDetector);
//		logger.debug("Initial detector located: " + currentInitialPoints.size() + " points");
//		this.points = new ArrayList<InterestPointData>();
//		this.initialPoints = new ArrayList<InterestPointData>();
//		
//		return currentInitialPoints;
//	}
//	
//	public AffineIPDLoopState getNewState(FImage image,InterestPointData interestPoint){
//		return new AffineIPDLoopState(image,interestPoint);
//	}
//	
//	private boolean stoppingCondition(AffineIPDLoopState state) {
//		boolean notMovedOnFirstIteration = (state.iteration!=0 || !state.selected.equalPos(state.prev));
//		return (notMovedOnFirstIteration && isScaleDone(state.scaleConvergence) && isAffineDone(state.affineConvergence))
//				|| state.iteration >= MAX_ITER || state.loopConvergence || state.loopDivergence;
//	}
//	
//	/**
//	 * Find a window that corresponds to the affine transform area around the current point. Note that the window size in the origianl code was a SQRT of the scale,
//	 * removing this seemed to remove some unstable features and results in general with bigger windows used to finding future points.
//	 * @param state
//	 */
//	public void initLoopState(AffineIPDLoopState state){
//		logger.debug("Loop: " + state.iteration );
//		logger.debug("Current second moment matrix: " );
//		logger.debug(MatrixUtils.toString(state.selected.secondMoments));
//		state.prev = state.selected.clone();
//		logger.debug("Cloned selected...");
//		state.selcov = state.selected.secondMoments.copy().inverse();
//		state.selcov = state.selcov.times(1.0/Math.sqrt(state.selcov.det()));
//		logger.debug("Got covar...");
//		state.selcovSqrt = MatrixUtils.sqrt(state.selcov);
//		logger.debug("Got sqrt...");
//		
//		
//		state.xszf = 1;
//		if (this.modes.contains(MODE.SHAPE)){
////			EigenvalueDecomposition seleig = state.selcov.eig();
////			Matrix seld = seleig.getD();
//			EigenValueVectorPair seleig = fast2x2EigenDecomposition(state.selcov);
//			Matrix seld = seleig.getV();
//			float Qratio = (float) (MatrixUtils.maxAbsDiag(seld) / MatrixUtils.minAbsDiag(seld));
//			state.xszf = Qratio;
//		}
//		logger.debug("Scaling windows by: " + state.xszf);
//
//		state.windowRadius = (int) Math.max(9,Math.round(state.xszf * 4 * Math.sqrt(state.selected.scale)));
//		state.subImage = state.image.extractCenter(state.selected.x,state.selected.y, state.windowRadius * 2, state.windowRadius * 2);
//		logger.debug(String.format("Extracting center: %d,%d (%dx%d)", state.selected.x,state.selected.y, state.windowRadius * 2, state.windowRadius * 2));
//		
//		state.cx = Math.min(state.windowRadius, state.selected.x);
//		state.cy = Math.min(state.windowRadius, state.selected.y);
//		if (this.modes.contains(MODE.SHAPE)) {
//			state.windowRadius = (int) Math.max(9,Math.round(4 * Math.sqrt(state.selected.scale)));
////			state.subImage = state.subImage.funkyTransform(state.selcovSqrt, state.cx, state.cy);
////			state.subImage = state.subImage.extractCenter(state.cx,state.cy,state.windowRadius * 2, state.windowRadius * 2);
////			// Transform and extract the middle
//			Matrix shape = new Matrix(new double[][]{
//					{state.selcovSqrt.get(0, 0),state.selcovSqrt.get(0, 1),0},
//					{state.selcovSqrt.get(1, 0),state.selcovSqrt.get(1, 1),0},
//					{0,0,1},
//			});
//			Matrix center = TransformUtilities.translateMatrix(state.cx,state.cy);
//			Matrix transform = center.times(shape);
////			System.out.println(MatrixUtils.toString(transform));
////			Ellipse e = EllipseUtilities.ellipseFromCovariance(state.cx, state.cy, state.selcov, 1.0f);
////			Matrix ellipseTransform = e.normTransformMatrix();
////			System.out.println(MatrixUtils.toString(ellipseTransform));
//			ProjectionProcessor<Float,FImage> pp = new ProjectionProcessor<Float,FImage>();
//			pp.setMatrix(transform.inverse());
//			state.subImage.process(pp);
//			state.subImage = pp.performProjection(-state.windowRadius, state.windowRadius+1,-state.windowRadius, state.windowRadius+1,null);
//			
////			logger.debug("Current transform matrix: ");
////			logger.debug(MatrixUtils.toString(state.selcovSqrt));
//			state.cx = Math.min(state.windowRadius, state.cx);
//			state.cy = Math.min(state.windowRadius, state.cy);
//		}
//		state.doubleScale = (float) (2 * state.selected.scale);
//		logger.debug("Transformed!...");
//	}
//	
//	JFrame displayFrame = null;
//	private void displayCurrentPatch(AffineIPDLoopState state) {
//		logger.debug("Displaying patch");
//		float patchWH = 250;
//		float warppedPatchScale = patchWH / state.subImage.width;
//		ResizeProcessor patchSizer = new ResizeProcessor(patchWH ,patchWH );
//		FImage warppedPatchGrey = state.subImage.process(patchSizer);
//		MBFImage warppedPatch = new MBFImage(warppedPatchGrey.clone(),warppedPatchGrey.clone(),warppedPatchGrey.clone());
//		float x = state.cx*warppedPatchScale;
//		float y = state.cy*warppedPatchScale;
//		float r = state.selected.scale*warppedPatchScale*2;
//		warppedPatch.createRenderer().drawShape(new Ellipse(x,y,r,r,0), RGBColour.RED);
//		warppedPatch.createRenderer().drawPoint(new Point2dImpl(x,y), RGBColour.RED,3);
//		
//		int unwarppedWH = (int) Math.ceil(state.selected.scale*2*4);
//		FImage unwarppedPatchGrey = state.image.extractCenter((int)state.selected.x, (int)state.selected.y, unwarppedWH, unwarppedWH);
//		MBFImage unwarppedPatch = new MBFImage(unwarppedPatchGrey.clone(),unwarppedPatchGrey.clone(),unwarppedPatchGrey.clone());
//		unwarppedPatch = unwarppedPatch.process(patchSizer);
//		float unwarppedPatchScale = patchWH / unwarppedWH;
//		
//		x = unwarppedPatch.getWidth()/2;
//		y = unwarppedPatch.getHeight()/2;
////		Matrix sm = state.selected.secondMoments;
////		float scale = state.selected.scale * unwarppedPatchScale;
////		Ellipse e = EllipseUtilities.ellipseFromSecondMoments(x, y, sm, scale*2);
//		Matrix sm = state.selcovSqrt;
//		float scale = state.selected.scale * unwarppedPatchScale * 2;
//		Ellipse e = EllipseUtilities.ellipseFromCovariance(x, y, sm, scale);
//		unwarppedPatch.createRenderer().drawShape(e, RGBColour.BLUE);
//		unwarppedPatch.createRenderer().drawPoint(new Point2dImpl(x,y), RGBColour.RED,3);
//		// give the patch a border (10px, black)
//		warppedPatch = warppedPatch.padding(5, 5, RGBColour.BLACK);
//		unwarppedPatch = unwarppedPatch.padding(5, 5,RGBColour.BLACK);
//		
//		MBFImage displayArea = warppedPatch.newInstance(warppedPatch.getWidth()*2, warppedPatch.getHeight());
//		displayArea.createRenderer().drawImage(warppedPatch, 0, 0);
//		displayArea.createRenderer().drawImage(unwarppedPatch, warppedPatch.getWidth(), 0);
//		if(displayFrame == null)
//			displayFrame = DisplayUtilities.display(displayArea);
//		else
//			DisplayUtilities.display(displayArea,displayFrame);
//		logger.debug("Done");
//	}
//	
//	private void convergeScale(AffineIPDLoopState state) {
//		FImage blurredSubImage = state.subImage.process(new FGaussianConvolve((float)state.doubleScale));
//		blurredSubImage = blurredSubImage.extractCenter( state.cx,state.cy,MASK_SIZE, MASK_SIZE);
//		float xxSum, yySum, xxxxSum, yyyySum, xxyySum;
//		xxSum = (float) elementWiseMask(BasicDerivativeKernels.DXX_KERNEL,blurredSubImage);
//		yySum = (float) elementWiseMask(BasicDerivativeKernels.DYY_KERNEL,blurredSubImage);
//		xxxxSum = (float) elementWiseMask(BasicDerivativeKernels.DXXXX_KERNEL,blurredSubImage);
//		xxyySum = (float) elementWiseMask(BasicDerivativeKernels.DXXYY_KERNEL,blurredSubImage);
//		yyyySum = (float) elementWiseMask(BasicDerivativeKernels.DYYYY_KERNEL,blurredSubImage);
//		
//		logger.debug("xxSum = " + xxSum);
//		logger.debug("yySum = " + yySum);
//		logger.debug("xxxxSum = " + xxxxSum);
//		logger.debug("xxyySum = " + xxyySum);
//		logger.debug("yyyySum = " + yyyySum);
//		logger.debug("state.doubleScale = " + state.doubleScale);
//
//		state.normLaplacian = (float) ((xxSum + yySum) * state.doubleScale);
//		float xvalLaplacian = (xxSum + yySum) + ((state.selected.scale) * (xxxxSum + yyyySum + 2 * xxyySum));
//
//		int newLaplacianDirection = sign(xvalLaplacian) * sign(state.normLaplacian) ;
//		if (newLaplacianDirection * state.laplacianDirection < 0) {
//			state.sx_step = state.sx_step / 2;
//		}
//		state.laplacianDirection = newLaplacianDirection;
//		logger.debug("xvalLaplacian: " + xvalLaplacian + " sign: " + sign(xvalLaplacian));
//		logger.debug("normLaplacian: " + state.normLaplacian + " sign: " + sign(state.normLaplacian));
//		logger.debug("Current lap direction is: " + state.laplacianDirection);
//
//		float newScale =  (float) (state.selected.scale * Math.pow(2, state.sx_step * state.laplacianDirection));
//		state.doubleScale = 2 * newScale;
//
//		blurredSubImage = state.subImage.process(new FGaussianConvolve( (float) state.doubleScale));
//		blurredSubImage = blurredSubImage.extractCenter( MASK_SIZE, MASK_SIZE);
//
//		xxSum = (float) elementWiseMask(BasicDerivativeKernels.DXX_KERNEL,blurredSubImage);
//		yySum = (float) elementWiseMask(BasicDerivativeKernels.DYY_KERNEL, blurredSubImage);
//		float newNormLaplacian = (xxSum + yySum) * state.doubleScale;
//		logger.debug("OLD normLaplacian = " + state.normLaplacian);
//		logger.debug("NEW normLaplacian = " + newNormLaplacian);
//		if (state.normLaplacian * state.normLaplacian > newNormLaplacian * newNormLaplacian ) {
//			logger.debug("SCALE CONVERGED");
//			state.scaleConvergence = true;
//		} else {
//			logger.debug("FAIL SCALE CONVERGED");
//			state.selected.scale = newScale;
//			state.scaleConvergence = false;
//		}
//		if (state.doubleScale > SCALE_MAX) {
//			// If the scal
//			state.loopDivergence = true;
//		}
//	}
//	
//	private InterestPointData findPointInCurrentRegion(AffineIPDLoopState state) {
//		state.doubleScale = 2 * state.selected.scale;
//		logger.debug("Finding new points with internal detector settings(" + state.selected.scale + "," + state.doubleScale + ")");
//		this.internalPointDetector.setDetectionScaleVariance(state.selected.scale);
//		this.internalPointDetector.setIntegrationScaleVariance( state.doubleScale);
//		this.internalPointDetector.findInterestPoints(state.subImage);
//		
//		logger.debug("Found maxima from internal detector: " + this.internalPointDetector.pointsFound());
//		
//		List<InterestPointData> newPoints = this.internalPointDetector.getInterestPoints(100);
//		correctPoints(state.cx, state.cy, newPoints, Math.max(0,state.selected.x - state.windowRadius ), Math.max(0,state.selected.y - state.windowRadius),state.selcovSqrt);
//		InterestPointData closest = findClosestPoint(newPoints,state.interestPoint);
//		return closest;
//	}
//	
//	private void correctPoints(int cx, int cy,List<InterestPointData> newPoints, int x, int y, Matrix selcovSqrt) {
//		for(InterestPointData point : newPoints){
//			if (this.modes.contains(MODE.SHAPE)) {
//				float correctedX = point.x - cx;
//				float correctedY = point.y - cy;
//				point.x = (int) Math.round(correctedX * selcovSqrt.get(0, 0) + correctedY * selcovSqrt.get(0, 1) + cx);
//				point.y = (int) Math.round(correctedX * selcovSqrt.get(1, 0) + correctedY * selcovSqrt.get(1, 1) + cx);
//			}
//			
//			point.x = point.x + x;
//			point.y = point.y + y;
//		}
//	}
//
//	/**
//	 * Closest point in a set of points. Points are relative to a subimage which was originally extracted about the center of current. Find the point
//	 * closest to initial. The function also correctly transforms list of points based on current's second moment matrix.
//	 * @param subImage
//	 * @param newPoints
//	 * @param initial
//	 * @param current
//	 * @return
//	 */
//	private InterestPointData findClosestPoint(List<InterestPointData> newPoints, InterestPointData initial) {
//		InterestPointData best = null;
//		float bestDistance = -1;
//		for(InterestPointData possible : newPoints){
//			int dX = initial.x - possible.x;
//			int dY = initial.y - possible.y;
//			
//			float newDistance = (float) Math.sqrt(dX*dX + dY*dY);
//			if(best == null || newDistance < bestDistance){
//				bestDistance = newDistance;
//				best = possible;
//			}
//		}
//		return best;
//	}
//
//	private void resetSelectedPosition(AffineIPDLoopState state) {
//		state.selected.x = state. prev.x;
//		state.selected.y = state.prev.y;
//	}
//	
//	private void convergeShape(AffineIPDLoopState state) {
//		Matrix newSecondOrder = state.selected.secondMoments.inverse();
//		if(MatrixUtils.anyNaNorInf(newSecondOrder)){
//			logger.debug("... the new second order matrix contains NaN or Inf!");
//			state.loopDivergence = true;
//		}
//		else{
//			newSecondOrder = newSecondOrder.times(1.0 / Math.sqrt(newSecondOrder.det()));
////			EigenvalueDecomposition eig = newSecondOrder.eig();
////			Matrix d = eig.getD();
////			Matrix v = eig.getV();
//			EigenValueVectorPair eig = fast2x2EigenDecomposition(newSecondOrder);
//			Matrix d = eig.getV();
//			Matrix v = eig.getD();
//			newSecondOrder = v.times(MatrixUtils.pow(d.copy(),state.affineExp)).times(v.transpose());
//			newSecondOrder = newSecondOrder.times(1.0 / Math.sqrt(newSecondOrder.det()));
//			
//			Matrix newSecondOrderCorr = newSecondOrder.inverse().times(state.selcov.inverse());
//			state.selected.secondMoments = newSecondOrderCorr;
//			
//			newSecondOrder = newSecondOrder.times(1.0/Math.sqrt(newSecondOrder.det()));
////			eig = newSecondOrder.eig();
////			d = eig.getD();
////			v = eig.getV(); 
//			eig = fast2x2EigenDecomposition(newSecondOrder);
//			d = eig.getV();
//			v = eig.getD(); 
//			float Qratio = (float) (MatrixUtils.maxAbsDiag(d) / MatrixUtils.minAbsDiag(d));
//			if(state.QratioOld > 0){
//				if (state.QratioSgn *(Qratio-state.QratioOld)<0 && state.affineExp>0.05)
//					state.affineExp=state.affineExp/2;
//			    state.QratioSgn = (float) (Qratio - state.QratioOld);
//			}
//			state.QratioOld = Qratio;
//			
//			if(Qratio < MIN_QRATIO)
//			{
//				logger.debug("Shape convergence!");
//				logger.debug(state.selected.secondMoments.toString());
//				
//				state.affineConvergence = true;
//			}
//			else{
//				state.affineConvergence = false;
//			}
//				
//		}
//	}
//	
//	private void checkForDivergence(AffineIPDLoopState state) {
////		EigenvalueDecomposition eig = state.selected.secondMoments.eig();
////		d = eig.getD();
////		v = eig.getV();
////		// If it's imaginary, divergence!
////		Matrix imaginaries = eig.imaginaries;
////		if (MatrixUtils.sum(imaginaries) != 0) {
//		EigenValueVectorPair eig = fast2x2EigenDecomposition(state.selected.secondMoments);
//		// if eig is null, we have imagineries
//		if(eig == null){
//			logger.debug("... New affine transform contains imaginarys, divergence!");
//			state.loopDivergence = true;
//		}
//		if(state.iteration >= MAX_ITER || state.selected.x < 0 || state.selected.x >= state.image.width || state.selected.y < 0 || state.selected.y >= state.image.height){
//			logger.debug("... New point is outside the image, divergence!");
//			state.loopDivergence = true;
//		}
//		List<InterestPointData> current = new ArrayList<InterestPointData>();
//		current.add(state.selected);
//	}
//	
//	private void outputState(AffineIPDLoopState state) {
////		debugFrame = DisplayUtilities.display(AbstractIPD.visualise(current, original.clone().gaussianBlur((float)Math.sqrt(selected.scale)),2),debugFrame);
////		eig = eig(selected);
////	    Q=max(abs(diag(d)))/min(abs(diag(d)));
//		EigenvalueDecomposition eigFinal = new EigenvalueDecomposition(state.selected.secondMoments.copy().inverse());
//		Matrix dFinal = eigFinal.getD();
//		float QratioFinal = (float) (MatrixUtils.maxAbsDiag(dFinal) / MatrixUtils.minAbsDiag(dFinal));
//		logger.info(String.format("iter: %d, x=%d, y=%d, sx=%1.5f, Q=%1.2f, Lapval=%2.1f",state.iteration,state.selected.x,state.selected.y,state.selected.scale,QratioFinal,state.normLaplacian));
//		if(logger.getLevel() == Level.DEBUG){
//			displayCurrentPatch(state);
//		}
//		return;
//	}
//	
//	private void addNonDivergentPoint(AffineIPDLoopState state) {
//		if(!state.loopDivergence){
//			logger.debug("No divergence, Adding it!");
//			EigenvalueDecomposition eigFinal = new EigenvalueDecomposition(state.selected.secondMoments.inverse());
//			Matrix dFinal = eigFinal.getD();
//			float QratioFinal = (float) (MatrixUtils.maxAbsDiag(dFinal) / MatrixUtils.minAbsDiag(dFinal));
//			logger.info("Final point at: ");
//			logger.info(MatrixUtils.toString(state.selected.secondMoments));
//			logger.info(String.format("ScaleConverge(%b),AffineConverge(%b),LoopConvergence(%b)", state.scaleConvergence,state.affineConvergence,state.loopConvergence));
//			logger.info(String.format("FINAL iter: %d, x=%d, y=%d, sx=%1.5f, Q=%1.2f, Lapval=%2.1f\n",state.iteration-1,state.selected.x,state.selected.y,state.selected.scale,QratioFinal,state.normLaplacian));
//			logger.info(String.format("ORIGNAL x=%d, y=%d, sx=%1.5f\n",state.interestPoint.x,state.interestPoint.y,state.interestPoint.scale));
//			this.points.add(state.selected);
//			this.initialPoints.add(state.interestPoint);
//		}
//		else{
//			logger.debug("There was a divergence!");
//		}
//	}
//
//	
//
//	private int sign(float xvalLaplacian) {
//		return (xvalLaplacian <= 0 ? (xvalLaplacian == 0 ? 0 : -1) : 1);
//	}
//
//	private double elementWiseMask(KernelProcessor<Float, FImage> kernel, FImage image) {
//		int roiWidth = kernel.getKernelWidth();
//		int roiHeight = kernel.getKernelHeight();
//		return kernel.processKernel(image.extractCenter(roiWidth, roiHeight));
//	}
//	
//	public EigenValueVectorPair fast2x2EigenDecomposition(Matrix m){
//		if(!FAST_EIGEN){
////			EigenvalueDecomposition eig = m.eig();
////			EigenValueVectorPair ret = new EigenValueVectorPair(eig.getD(),eig.getV());
//		}
//		/**
//		 * A = 1
//		 * B = a + d
//		 * C = ad - bc
//		 * 
//		 * x = ( - B (+/-) sqrt(B^2 - 4AC) )/ (2A) 
//		 */
//		double a = m.get(0, 0);
//		double b = m.get(0, 1);
//		double c = m.get(1, 0);
//		double d = m.get(1, 1);
//		double A = 1;
//		double B = a + d;
//		double C = a*d - b*c;
//		
//		Matrix val = new Matrix(2,2);
//		double sqrtInner = B*B - 4 * A * C;
//		if(sqrtInner < 0){
//			return null;
//		}
//		
//		double firstEig = ( B + Math.sqrt(sqrtInner) ) / ( 2 * A );
//		double secondEig = ( B - Math.sqrt(sqrtInner) ) / ( 2 * A );
//		
//		val.set(0, 0, firstEig);
//		val.set(1, 1, secondEig);
//		
//		Matrix vec = new Matrix(2,2);
//		if(b == 0 && c == 0){
//			vec.set(0, 0, 1);
//			vec.set(1, 1, 1);
//		}
//		else
//		{	
//			if(c != 0){
//				double v1 = firstEig - d;
//				double v2 = secondEig - d;
//				double norm1 = Math.sqrt(v1*v1 + c*c);
//				double norm2 = Math.sqrt(c*c + v2*v2);
//				vec.set(0, 0, v1/norm1);
//				vec.set(0, 1, v2/norm2);
//				vec.set(1, 0, c/norm1);
//				vec.set(1, 1, c/norm2);
//			}
//			else if(b != 0){
//				double v1 = firstEig - a;
//				double v2 = secondEig - a;
//				double norm1 = Math.sqrt(v1*v1 + b*b);
//				double norm2 = Math.sqrt(b*b + v2*v2);
//				vec.set(0, 0, b/norm1);
//				vec.set(0, 1, b/norm2);
//				vec.set(1, 0, v1/norm1);
//				vec.set(1, 1, v2/norm2);
//			}
//		}
//		
//		EigenValueVectorPair ret = new EigenValueVectorPair(val,vec);
//		return ret;
//	}
//
////	public FImage affineWarpImage(FImage subImage, Matrix selcov) {
////		return subImage.funkyTransform(selcov, (int)(subImage.getWidth()/2.0), (int)(subImage.getHeight()/2.0));
////	}
//
//	private boolean isAffineDone(boolean affineConvergence) {
//		return !(this.modes.contains(MODE.SHAPE) ^ affineConvergence);
//	}
//
//	private boolean isScaleDone(boolean scaleConvergence) {
//		return !(this.modes.contains(MODE.SCALE) ^ scaleConvergence);
//	}
//
//	public void setIPDSelectionMode(IPDSelectionMode internalDetectorSelector) {
//		this.internalDetectorSelector = internalDetectorSelector;
//	}
//
//	public List<InterestPointData> getInitialInterestPoints(){
//		return this.initialPoints;
//	}
//	
//	public static void main(String args[]) throws IOException{
////		testAffineIPDEigenvectorSpeedtest();
//		testAffineIPDOnce();
//	}
//
//	private static void testAffineIPDOnce() throws IOException {
//		MBFImage img1 = ImageUtilities.readMBF(
//			IPDSIFTEngine.class.getResourceAsStream("/org/openimaj/image/feature/validator/graf/img1.ppm")
//		);
//		List<InterestPointData> extracted = extractAffineIPD(new HarrisIPD(16,32f),img1.clone(),false);
//		InterestPointVisualiser<Float[], MBFImage> visExt = InterestPointVisualiser.visualiseInterestPoints(img1, extracted,1);
//		DisplayUtilities.display(visExt.drawPatches(RGBColour.GREEN, RGBColour.RED));
//	}
//
////	private static void testAffineIPDEigenvectorSpeedtest() throws IOException {
////		
////		MBFImage img1 = ImageUtilities.readMBF(IPDSIFTEngine.class.getResourceAsStream("/org/openimaj/image/feature/validator/graf/img1.ppm"));
////		
////		long avgTime = 0;
////		for(int i = 0 ; i < 10; i++)
////			avgTime += timeAffineIPD(new HarrisIPD(4,8),img1.clone(),true);
////		long time = avgTime / 10;
////		System.out.println("Affine correction Time taken (slow eig): " + time + "ms" );
////		
////		avgTime = 0;
////		for(int i = 0 ; i < 10; i++)
////			avgTime += timeAffineIPD(new HarrisIPD(4,8),img1.clone(),false);
////		time = avgTime / 10;
////		System.out.println("Affine correction Time taken (fast eig): " + time + "ms" );
////		
////	}
//	
//	public static long timeAffineIPD(AbstractStructureTensorIPD harris, MBFImage img1, boolean fastEigen){
//		long begin = System.currentTimeMillis();
//		AffineIPD affine = new AffineIPD(harris,new IPDSelectionMode.Count(25),new IPDSelectionMode.Count(1));
//		affine.FAST_EIGEN = fastEigen;
//		affine.findInterestPoints(Transforms.calculateIntensity(img1));
//		affine.getInterestPoints(25);
//		long end = System.currentTimeMillis();
//		return end - begin;
//	}
//	
//	public static List<InterestPointData> extractAffineIPD(AbstractStructureTensorIPD harris, MBFImage img1, boolean fastEigen){
//		AffineIPD affine = new AffineIPD(harris,new IPDSelectionMode.Count(25),new IPDSelectionMode.Count(100));
//		affine.FAST_EIGEN = fastEigen;
//		affine.findInterestPoints(Transforms.calculateIntensity(img1));
//		List<InterestPointData> extracted = affine.getInterestPoints(25);
//		return extracted;
//	}
//
//	@Override
//	public void setDetectionScaleVariance(float detectionScaleVariance) {
//		this.internalPointDetector.setDetectionScaleVariance(detectionScaleVariance);
//		this.initialPointsDetector.setDetectionScaleVariance(detectionScaleVariance);
//	}
//
//	@Override
//	public void findInterestPoints(FImage image, Rectangle window) {
//		// TODO Auto-generated method stub
//		
//	}
//}
