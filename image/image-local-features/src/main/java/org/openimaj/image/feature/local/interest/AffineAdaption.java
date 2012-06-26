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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.FProjectionProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.EigenValueVectorPair;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 * Using an underlying feature detector, adapt the ellipse detected to result in a more
 * stable region according to work by http://www.robots.ox.ac.uk/~vgg/research/affine/
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class AffineAdaption implements InterestPointDetector<EllipticInterestPointData>{
	private static final FImage LAPLACIAN_KERNEL = new FImage(new float[][] {{2, 0, 2}, {0, -8, 0}, {2, 0, 2}});
	private static final FConvolution LAPLACIAN_KERNEL_CONV = new FConvolution(LAPLACIAN_KERNEL);
//	private static final FImage DX_KERNEL = new FImage(new float[][] {{-1, 0, 1}});
//	private static final FImage DY_KERNEL = new FImage(new float[][] {{-1}, {0}, {1}});
	
	static Logger logger = Logger.getLogger(AffineAdaption.class);
	static{
		
		BasicConfigurator.configure();
		logger.setLevel(Level.INFO);
	}

	private AbstractStructureTensorIPD  internal;
	private AbstractStructureTensorIPD  initial;
	private List<EllipticInterestPointData> points;

	private IPDSelectionMode initialMode;

	private boolean fastDifferentiationScale = false;
	
	/**
	 * instatiate with a {@link HarrisIPD} detector with scales 2.0f and 2.0f * 1.4f. Selection 100 points
	 */
	public AffineAdaption(){
		this(new HarrisIPD(2.0f,2.0f*1.4f),new IPDSelectionMode.Count(100));
	}
	
	/**
	 * specify the internal detector and the selection mode
	 * @param internal
	 * @param initialSelectionMode
	 */
	public AffineAdaption(AbstractStructureTensorIPD internal, IPDSelectionMode initialSelectionMode){
		this(internal,internal.clone(),initialSelectionMode);
	}
	
	/**
	 * set both the internal and intitial feature detectors (possibly different settings of the same detector) and a selection mode
	 * @param internal
	 * @param initial
	 * @param initialSelectionMode
	 */
	public AffineAdaption(AbstractStructureTensorIPD internal, AbstractStructureTensorIPD initial,IPDSelectionMode initialSelectionMode){
		this.internal = internal;
		this.initial = initial;
		
		this.initialMode = initialSelectionMode;
		this.points = new ArrayList<EllipticInterestPointData>();
	}
	
	@Override
	public void findInterestPoints(FImage image){
		findInterestPoints(image, image.getBounds());
	}
	
	@Override
	public void findInterestPoints(FImage image, Rectangle window) {
		this.points  = new ArrayList<EllipticInterestPointData>();
		initial.findInterestPoints(image,window);
//		if(logger.getLevel() == Level.INFO)
//			initial.printStructureTensorStats();
		List<InterestPointData> a = initialMode.selectPoints(initial);
		
		logger.info("Found " + a.size() + " features at sd/si: " + initial.detectionScale + "/" + initial.integrationScale);
		
		for (InterestPointData d : a) {
			EllipticInterestPointData kpt = new EllipticInterestPointData();
//			InterestPointData d = new InterestPointData();
//			d.x = 102;
//			d.y = 396;
			kpt.scale = initial.getIntegrationScale();
			kpt.x = d.x;
			kpt.y = d.y;
			
			boolean converge = calcAffineAdaptation(image, kpt, internal.clone());
			if(converge)
			{
				logger.debug("Keypoint at: " + d.x + ", " + d.y);
				logger.debug("... converged: "+ converge);
				this.points.add(kpt);
			}
			
		}
	}
	
	@Override
	public List<EllipticInterestPointData> getInterestPoints(int npoints) {
		if(points == null) return null;
		if(npoints < 0) npoints = this.points.size();
		return this.points.subList(0, npoints < this.points.size() ? npoints : this.points.size());
	}

	@Override
	public List<EllipticInterestPointData> getInterestPoints(float threshold) {
		List<EllipticInterestPointData> validPoints = new ArrayList<EllipticInterestPointData>();
		for(EllipticInterestPointData  point : this.points){
			if(point.score > threshold){
				validPoints.add(point);
			}
		}
		return validPoints;
	}

	@Override
	public List<EllipticInterestPointData> getInterestPoints() {
		return this.points;
	}

	@Override
	public void setDetectionScale(float detectionScaleVariance) {
		this.initial.setDetectionScale(detectionScaleVariance);
//		this.internal.setDetectionScaleVariance(detectionScaleVariance);
	}
	
	@Override
	public void setIntegrationScale(float integrationScaleVariance) {
		this.initial.setIntegrationScale(integrationScaleVariance);
	}
	
	/*
	 * Calculates second moments matrix in point p
	 */
//	Matrix calcSecondMomentMatrix(final FImage dx2, final FImage dxy, final FImage dy2, Pixel p) {
//		int x = p.x;
//		int y = p.y;
//
//		Matrix M = new Matrix(2, 2);
//		M.set(0, 0, dx2.pixels[y][x]);
//		M.set(0, 1, dxy.pixels[y][x]);
//		M.set(1, 0, dxy.pixels[y][x]);
//		M.set(1, 1, dy2.pixels[y][x]);
//		
//		return M;
//	}
	Matrix calcSecondMomentMatrix(AbstractStructureTensorIPD ipd, int x, int y) {
		
		return ipd.getSecondMomentsAt(x, y);
	}

	/*
	 * Performs affine adaptation
	 */
	boolean calcAffineAdaptation(final FImage fimage, EllipticInterestPointData kpt, AbstractStructureTensorIPD ipd) {
//		DisplayUtilities.createNamedWindow("warp", "Warped Image ROI",true);
		Matrix transf = new Matrix(2, 3); 	// Transformation matrix
		Point2dImpl c = new Point2dImpl(); 	// Transformed point
		Point2dImpl p = new Point2dImpl(); 	// Image point

		Matrix U = Matrix.identity(2, 2); 	// Normalization matrix

		Matrix Mk = U.copy(); 
		FImage img_roi, warpedImg = new FImage(1,1);
		float Qinv = 1, q, si = kpt.scale; //sd = 0.75f * si;
		float kptSize = 2 * 3 * kpt.scale;
		boolean divergence = false, convergence = false;
		int i = 0;

		//Coordinates in image
		int py = (int) kpt.y;
		int px = (int) kpt.x;

		//Roi coordinates
		int roix, roiy;

		//Coordinates in U-trasformation
		int cx = px;
		int cy = py;
		int cxPr = cx;
		int cyPr = cy;

		float radius = kptSize / 2 * 1.4f;
		float half_width, half_height;

		Rectangle roi;

		//Affine adaptation
		while (i <= 10 && !divergence && !convergence)
		{
			//Transformation matrix 
			MatrixUtils.zero(transf);
			transf.setMatrix(0, 1, 0, 1, U);
			
			kpt.setTransform(U);

			Rectangle boundingBox = new Rectangle();

			double ac_b2 = U.det();
			boundingBox.width = (float) Math.ceil(U.get(1, 1)/ac_b2  * 3 * si*1.4 );
			boundingBox.height = (float) Math.ceil(U.get(0, 0)/ac_b2 * 3 * si*1.4 );

			//Create window around interest point
			half_width = Math.min((float) Math.min(fimage.width - px-1, px), boundingBox.width);
			half_height = Math.min((float) Math.min(fimage.height - py-1, py), boundingBox.height);
			
			if (half_width <= 0 || half_height <= 0) return divergence;
			
			roix = Math.max(px - (int) boundingBox.width, 0);
			roiy = Math.max(py - (int) boundingBox.height, 0);
			roi = new Rectangle(roix, roiy, px - roix + half_width+1, py - roiy + half_height+1);

			//create ROI
			img_roi = fimage.extractROI(roi);

			//Point within the ROI
			p.x = px - roix;
			p.y = py - roiy;

			//Find coordinates of square's angles to find size of warped ellipse's bounding box
			float u00 = (float) U.get(0, 0);
			float u01 = (float) U.get(0, 1);
			float u10 = (float) U.get(1, 0);
			float u11 = (float) U.get(1, 1);

			float minx = u01 * img_roi.height < 0 ? u01 * img_roi.height : 0;
			float miny = u10 * img_roi.width < 0 ? u10 * img_roi.width : 0;
			float maxx = (u00 * img_roi.width > u00 * img_roi.width + u01 * img_roi.height ? u00
					* img_roi.width : u00 * img_roi.width + u01 * img_roi.height) - minx;
			float maxy = (u11 * img_roi.width > u10 * img_roi.width + u11 * img_roi.height ? u11
					* img_roi.height : u10 * img_roi.width + u11 * img_roi.height) - miny;

			//Shift
			transf.set(0, 2, -minx);
			transf.set(1, 2, -miny);

			if (maxx >=  2*radius+1 && maxy >=  2*radius+1)
			{
				//Size of normalized window must be 2*radius
				//Transformation
				FImage warpedImgRoi;
				FProjectionProcessor proc = new FProjectionProcessor();
				proc.setMatrix(transf);
				img_roi.accumulateWith(proc);
				warpedImgRoi = proc.performProjection(0, (int)maxx, 0, (int)maxy, null);

//				DisplayUtilities.displayName(warpedImgRoi.clone().normalise(), "warp");
				
				//Point in U-Normalized coordinates
				c = p.transform(U);
				cx = (int) (c.x - minx);
				cy = (int) (c.y - miny);
				
				


				if (warpedImgRoi.height > 2 * radius+1 && warpedImgRoi.width > 2 * radius+1)
				{
					//Cut around normalized patch
					roix = (int) Math.max(cx - Math.ceil(radius), 0.0);
					roiy = (int) Math.max(cy - Math.ceil(radius), 0.0);
					roi = new Rectangle(roix, roiy,
							cx - roix + (float)Math.min(Math.ceil(radius), warpedImgRoi.width - cx-1)+1,
							cy - roiy + (float)Math.min(Math.ceil(radius), warpedImgRoi.height - cy-1)+1);
					warpedImg = warpedImgRoi.extractROI(roi);

					//Coordinates in cutted ROI
					cx = cx - roix;
					cy = cy - roiy;
				} else {
					warpedImg.internalAssign(warpedImgRoi);
				}
				
				if(logger.getLevel() == Level.DEBUG){
					displayCurrentPatch(img_roi.clone().normalise(),p.x,p.y,warpedImg.clone().normalise(),cx,cy,U,si*3);
				}
				
				//Integration Scale selection
				si = selIntegrationScale(warpedImg, si, new Pixel(cx, cy));

				//Differentation scale selection
				if(fastDifferentiationScale ){
					ipd = selDifferentiationScaleFast(warpedImg, ipd, si, new Pixel(cx, cy));
				}
				else{
					ipd = selDifferentiationScale(warpedImg, ipd, si, new Pixel(cx, cy));
				}
				
				if(ipd.maxima.size() == 0){
					divergence = true;
					continue;
				}
				//Spatial Localization
				cxPr = cx; //Previous iteration point in normalized window
				cyPr = cy;
//
//				float cornMax = 0;
//				for (int j = 0; j < 3; j++)
//				{
//					for (int t = 0; t < 3; t++)
//					{
//						float dx2 = Lxm2smooth.pixels[cyPr - 1 + j][cxPr - 1 + t];
//						float dy2 = Lym2smooth.pixels[cyPr - 1 + j][cxPr - 1 + t];
//						float dxy = Lxmysmooth.pixels[cyPr - 1 + j][cxPr - 1 + t];
//						float det = dx2 * dy2 - dxy * dxy;
//						float tr = dx2 + dy2;
//						float cornerness = (float) (det - (0.04 * Math.pow(tr, 2)));
//						
//						if (cornerness > cornMax) {
//							cornMax = cornerness;
//							cx = cxPr - 1 + t;
//							cy = cyPr - 1 + j;
//						}
//					}
//				}
				
				FValuePixel max = ipd.findMaximum(new Rectangle(cxPr - 1, cyPr -1, 3, 3));
				cx = max.x;
				cy = max.y;

				//Transform point in image coordinates
				p.x = px;
				p.y = py;
				
				//Displacement vector
				c.x = cx - cxPr;
				c.y = cy - cyPr;
				
				//New interest point location in image
				p.translate(c.transform(U.inverse()));
				px = (int) p.x;
				py = (int) p.y;

				q = calcSecondMomentSqrt(ipd, new Pixel(cx, cy), Mk);

				float ratio = 1 - q;

				//if ratio == 1 means q == 0 and one axes equals to 0
				if (!Float.isNaN(ratio) && ratio != 1)
				{
					//Update U matrix
					U = U.times(Mk);

					Matrix uVal, uV;
//					EigenvalueDecomposition ueig = U.eig(); 
					EigenValueVectorPair ueig = MatrixUtils.symmetricEig2x2(U);
					uVal = ueig.getValues();
					uV = ueig.getVectors();

					Qinv = normMaxEval(U, uVal, uV);

					//Keypoint doesn't converge
					if (Qinv >= 6) {
						logger.debug("QInverse too large, feature too edge like, affine divergence!");
						divergence = true;
					} else if (ratio <= 0.05) { //Keypoint converges
						convergence = true;

						//Set transformation matrix
						MatrixUtils.zero(transf);
						transf.setMatrix(0, 1, 0, 1, U);
						// The order here matters, setTransform uses the x and y to calculate a new ellipse
						kpt.x = px;
						kpt.y = py;
						kpt.scale = si;
						kpt.setTransform(U);
						kpt.score = max.value;

//						ax1 = (float) (1 / Math.abs(uVal.get(1, 1)) * 3 * si);
//						ax2 = (float) (1 / Math.abs(uVal.get(0, 0)) * 3 * si);
//						phi = Math.atan(uV.get(1, 1) / uV.get(0, 1));
//						kpt.axes = new Point2dImpl(ax1, ax2);
//						kpt.phi = phi;
//						kpt.centre = new Pixel(px, py);
//						kpt.si = si;
//						kpt.size = 2 * 3 * si;

					} else {
						radius = (float) (3 * si * 1.4);
					}
				} else {
					logger.debug("QRatio was close to 0, affine divergence!");
					divergence = true;
				}
			} else {
				logger.debug("Window size has grown too fast, scale divergence!");
				divergence = true;
			}

			++i;
		}
		if(!divergence && !convergence){
			logger.debug("Reached max iterations!");
		}
		return convergence;
	}

	private void displayCurrentPatch(FImage unwarped, float unwarpedx, float unwarpedy, FImage warped, int warpedx, int warpedy, Matrix transform, float scale) {
		DisplayUtilities.createNamedWindow("warpunwarp", "Warped and Unwarped Image",true);
		logger.debug("Displaying patch");
		float resizeScale = 5f;
		float warppedPatchScale = resizeScale ;
		ResizeProcessor patchSizer = new ResizeProcessor(resizeScale);
		FImage warppedPatchGrey = warped.process(patchSizer);
		MBFImage warppedPatch = new MBFImage(warppedPatchGrey.clone(),warppedPatchGrey.clone(),warppedPatchGrey.clone());
		float x = warpedx*warppedPatchScale;
		float y = warpedy*warppedPatchScale;
		float r = scale * warppedPatchScale;
		
		warppedPatch.createRenderer().drawShape(new Ellipse(x,y,r,r,0), RGBColour.RED);
		warppedPatch.createRenderer().drawPoint(new Point2dImpl(x,y), RGBColour.RED,3);
		
		FImage unwarppedPatchGrey = unwarped.clone();
		MBFImage unwarppedPatch = new MBFImage(unwarppedPatchGrey.clone(),unwarppedPatchGrey.clone(),unwarppedPatchGrey.clone());
		unwarppedPatch = unwarppedPatch.process(patchSizer);
		float unwarppedPatchScale = resizeScale;
		
		x = unwarpedx * unwarppedPatchScale ;
		y = unwarpedy * unwarppedPatchScale ;
//		Matrix sm = state.selected.secondMoments;
//		float scale = state.selected.scale * unwarppedPatchScale;
//		Ellipse e = EllipseUtilities.ellipseFromSecondMoments(x, y, sm, scale*2);
		Ellipse e = EllipseUtilities.fromTransformMatrix2x2(transform,x,y,scale*unwarppedPatchScale);
		
		unwarppedPatch.createRenderer().drawShape(e, RGBColour.BLUE);
		unwarppedPatch.createRenderer().drawPoint(new Point2dImpl(x,y), RGBColour.RED,3);
		// give the patch a border (10px, black)
		warppedPatch = warppedPatch.padding(5, 5, RGBColour.BLACK);
		unwarppedPatch = unwarppedPatch.padding(5, 5,RGBColour.BLACK);
		
		MBFImage displayArea = warppedPatch.newInstance(warppedPatch.getWidth()*2, warppedPatch.getHeight());
		displayArea.createRenderer().drawImage(warppedPatch, 0, 0);
		displayArea.createRenderer().drawImage(unwarppedPatch, warppedPatch.getWidth(), 0);
		DisplayUtilities.displayName(displayArea, "warpunwarp");
		logger.debug("Done");	
	}

	/*
	 * Selects the integration scale that maximize LoG in point c
	 */
	float selIntegrationScale(final FImage image, float si, Pixel c) {
		FImage L;
		int cx = c.x;
		int cy = c.y;
		float maxLap = -Float.MAX_VALUE;
		float maxsx = si;
		float sigma, sigma_prev = 0;

		L = image.clone();
		/* 
		 * Search best integration scale between previous and successive layer
		 */
		for (float u = 0.7f; u <= 1.41; u += 0.1)
		{
			float sik = u * si;
			sigma = (float) Math.sqrt(Math.pow(sik, 2) - Math.pow(sigma_prev, 2));

			L.processInplace(new FGaussianConvolve(sigma, 3));
			
			sigma_prev = sik;
//			Lap = L.process(LAPLACIAN_KERNEL_CONV);

			float lapVal = sik * sik * Math.abs(LAPLACIAN_KERNEL_CONV.responseAt(cx,cy,L));
//			float lapVal = sik * sik * Math.abs(Lap.pixels[cy][cx]);

			if (lapVal >= maxLap)
			{
				maxLap = lapVal;
				maxsx = sik;
			}
		}
		return maxsx;
	}

	/*
	 * Calculates second moments matrix square root
	 */
	float calcSecondMomentSqrt(AbstractStructureTensorIPD ipd, Pixel p, Matrix Mk)
	{
		Matrix M, V, eigVal, Vinv;

		M = calcSecondMomentMatrix(ipd, p.x, p.y);

		/* *
		 * M = V * D * V.inv()
		 * V has eigenvectors as columns
		 * D is a diagonal Matrix with eigenvalues as elements
		 * V.inv() is the inverse of V
		 * */
//		EigenvalueDecomposition meig = M.eig();
		EigenValueVectorPair meig = MatrixUtils.symmetricEig2x2(M);
		eigVal = meig.getValues();
		V = meig.getVectors();
		
//		V = V.transpose();
		Vinv = V.inverse();

		double eval1 = Math.sqrt(eigVal.get(0, 0));
		eigVal.set(0, 0, eval1);
		double eval2 = Math.sqrt(eigVal.get(1, 1));
		eigVal.set(1, 1, eval2);

		//square root of M
		Mk.setMatrix(0, 1, 0, 1, V.times(eigVal).times(Vinv));
		
		//return q isotropic measure
		return (float) (Math.min(eval1, eval2) / Math.max(eval1, eval2));
	}

	float normMaxEval(Matrix U, Matrix uVal, Matrix uVec) {
		/* *
		 * Decomposition:
		 * U = V * D * V.inv()
		 * V has eigenvectors as columns
		 * D is a diagonal Matrix with eigenvalues as elements
		 * V.inv() is the inverse of V
		 * */
//		uVec = uVec.transpose();
		Matrix uVinv = uVec.inverse();

		//Normalize min eigenvalue to 1 to expand patch in the direction of min eigenvalue of U.inv()
		double uval1 = uVal.get(0, 0);
		double uval2 = uVal.get(1, 1);

		if (Math.abs(uval1) < Math.abs(uval2))
		{
			uVal.set(0, 0, 1);
			uVal.set(1, 1, uval2 / uval1);

		} else
		{
			uVal.set(1, 1, 1);
			uVal.set(0, 0, uval1 / uval2);
		}

		//U normalized
		U.setMatrix(0,1,0,1,uVec.times(uVal).times(uVinv));

		return (float) (Math.max(Math.abs(uVal.get(0, 0)), Math.abs(uVal.get(1, 1))) / 
				Math.min(Math.abs(uVal.get(0, 0)), Math.abs(uVal.get(1, 1)))); //define the direction of warping
	}

	/*
	 * Selects diffrentiation scale
	 */
	AbstractStructureTensorIPD selDifferentiationScale(FImage img, AbstractStructureTensorIPD ipdToUse, float si, Pixel c) {
		AbstractStructureTensorIPD best = null;
		float s = 0.5f;
		float sigma_prev = 0, sigma;

		FImage L;

		double qMax = 0;

		L = img.clone();
		
		AbstractStructureTensorIPD ipd = ipdToUse.clone();

		while (s <= 0.751)
		{
			Matrix M;
			float sd = s * si;

			//Smooth previous smoothed image L
			sigma = (float) Math.sqrt(Math.pow(sd, 2) - Math.pow(sigma_prev, 2));

			L.processInplace(new FGaussianConvolve(sigma, 3));
			sigma_prev = sd;


			//X and Y derivatives
			ipd.setDetectionScale(sd);
			ipd.setIntegrationScale(si);
			ipd.setImageBlurred(true);
			
			ipd.findInterestPoints(L);
//			FImage Lx = L.process(new FConvolution(DX_KERNEL.multiply(sd)));
//			FImage Ly = L.process(new FConvolution(DY_KERNEL.multiply(sd)));		
//
//			FGaussianConvolve gauss = new FGaussianConvolve(si, 3);
//			
//			FImage Lxm2 = Lx.multiply(Lx);
//			dx2 = Lxm2.process(gauss);
//			
//			FImage Lym2 = Ly.multiply(Ly);
//			dy2 = Lym2.process(gauss);
//
//			FImage Lxmy = Lx.multiply(Ly);
//			dxy = Lxmy.process(gauss);
			
			M = calcSecondMomentMatrix(ipd, c.x, c.y);

			//calc eigenvalues
//			EigenvalueDecomposition meig = M.eig();
			EigenValueVectorPair meig = MatrixUtils.symmetricEig2x2(M);
			Matrix eval = meig.getValues();
			double eval1 = Math.abs(eval.get(0, 0));
			double eval2 = Math.abs(eval.get(1, 1));
			double q = Math.min(eval1, eval2) / Math.max(eval1, eval2);

			if (q >= qMax) {
				qMax = q;
				best = ipd.clone();
			}

			s += 0.05;
		}
		return best;
	}
	
	AbstractStructureTensorIPD selDifferentiationScaleFast(FImage img, AbstractStructureTensorIPD ipd, float si, Pixel c) {
		AbstractStructureTensorIPD best = ipd.clone();
		float s = 0.75f;
		float sigma;
		FImage L;
		L = img.clone();
		float sd = s * si;

		//Smooth previous smoothed image L
		sigma = sd;

		L.processInplace(new FGaussianConvolve(sigma, 3));
		
		//X and Y derivatives
		best.setDetectionScale(sd);
		best.setIntegrationScale(si);
		best.setImageBlurred(true);
		
		best.findInterestPoints(L);
		
//		M = calcSecondMomentMatrix(best, c.x, c.y);

//		EigenValueVectorPair meig = MatrixUtils.symmetricEig2x2(M);
//		Matrix eval = meig.getD();
//		double eval1 = Math.abs(eval.get(0, 0));
//		double eval2 = Math.abs(eval.get(1, 1));

		return best;
	}

//	void calcAffineCovariantRegions(final Matrix image, final vector<KeyPoint> & keypoints,
//			vector<Elliptic_KeyPoint> & affRegions, string detector_type)
//	{
//
//		for (size_t i = 0; i < keypoints.size(); ++i)
//		{
//			KeyPoint kp = keypoints[i];
//			Elliptic_KeyPoint ex(kp.pt, 0, Size_<float> (kp.size / 2, kp.size / 2), kp.size,
//					kp.size / 6);
//
//			if (calcAffineAdaptation(image, ex))        
//				affRegions.push_back(ex);
//
//		}
//		//Erase similar keypoint
//		float maxDiff = 4;
//		Matrix colorimg;
//		for (size_t i = 0; i < affRegions.size(); i++)
//		{
//			Elliptic_KeyPoint kp1 = affRegions[i];
//			for (size_t j = i+1; j < affRegions.size(); j++){
//
//				Elliptic_KeyPoint kp2 = affRegions[j];
//
//				if(norm(kp1.centre-kp2.centre)<=maxDiff){
//					double phi1, phi2;
//					Size axes1, axes2;
//					double si1, si2;
//					phi1 = kp1.phi;
//					phi2 = kp2.phi;
//					axes1 = kp1.axes;
//					axes2 = kp2.axes;
//					si1 = kp1.si;
//					si2 = kp2.si;
//					if(Math.abs(phi1-phi2)<15 && Math.max(si1,si2)/Math.min(si1,si2)<1.4 && axes1.width-axes2.width<5 && axes1.height-axes2.height<5){
//						affRegions.erase(affRegions.begin()+j);
//						j--;                        
//					}
//				}
//			}
//		}
//	}

//	void calcAffineCovariantDescriptors(final Ptr<DescriptorExtractor>& dextractor, final Mat& img,
//			vector<Elliptic_KeyPoint>& affRegions, Mat& descriptors)
//	{
//
//		assert(!affRegions.empty());
//		int size = dextractor->descriptorSize();
//		int type = dextractor->descriptorType();
//		descriptors = Mat(Size(size, affRegions.size()), type);
//		descriptors.setTo(0);
//
//		int i = 0;
//
//		for (vector<Elliptic_KeyPoint>::iterator it = affRegions.begin(); it < affRegions.end(); ++it)
//		{
//			Point p = it->centre;
//
//			Mat_<float> size(2, 1);
//			size(0, 0) = size(1, 0) = it->size;
//
//			//U matrix
//			Matrix transf = it->transf;
//			Mat_<float> U(2, 2);
//			U.setTo(0);
//			Matrix col0 = U.col(0);
//			transf.col(0).copyTo(col0);
//			Matrix col1 = U.col(1);
//			transf.col(1).copyTo(col1);
//
//			float radius = it->size / 2;
//			float si = it->si;
//
//			Size_<float> boundingBox;
//
//			double ac_b2 = determinant(U);
//			boundingBox.width = ceil(U.get(1, 1)/ac_b2  * 3 * si );
//			boundingBox.height = ceil(U.get(0, 0)/ac_b2 * 3 * si );
//
//			//Create window around interest point
//			float half_width = Math.min((float) Math.min(img.width - p.x-1, p.x), boundingBox.width);
//			float half_height = Math.min((float) Math.min(img.height - p.y-1, p.y), boundingBox.height);
//			float roix = max(p.x - (int) boundingBox.width, 0);
//			float roiy = max(p.y - (int) boundingBox.height, 0);
//			Rect roi = Rect(roix, roiy, p.x - roix + half_width+1, p.y - roiy + half_height+1);
//
//			Matrix img_roi = img(roi);
//
//			size(0, 0) = img_roi.width;
//			size(1, 0) = img_roi.height;
//
//			size = U * size;
//
//			Matrix transfImgRoi, transfImg;
//			warpAffine(img_roi, transfImgRoi, transf, Size(ceil(size(0, 0)), ceil(size(1, 0))),
//					INTER_AREA, BORDER_DEFAULT);
//
//
//			Mat_<float> c(2, 1); //Transformed point
//			Mat_<float> pt(2, 1); //Image point
//			//Point within the Roi
//			pt(0, 0) = p.x - roix;
//			pt(1, 0) = p.y - roiy;
//
//			//Point in U-Normalized coordinates
//			c = U * pt;
//			float cx = c(0, 0);
//			float cy = c(1, 0);
//
//
//			//Cut around point to have patch of 2*keypoint->size
//
//			roix = Math.max(cx - radius, 0.f);
//			roiy = Math.max(cy - radius, 0.f);
//
//			roi = Rect(roix, roiy, Math.min(cx - roix + radius, size(0, 0)),
//					Math.min(cy - roiy + radius, size(1, 0)));
//			transfImg = transfImgRoi(roi);
//
//			cx = c(0, 0) - roix;
//			cy = c(1, 0) - roiy;
//
//			Matrix tmpDesc;
//			KeyPoint kp(Point(cx, cy), it->size);
//
//			vector<KeyPoint> k(1, kp);
//
//			transfImg.convertTo(transfImg, CV_8U);
//			dextractor->compute(transfImg, k, tmpDesc);
//
//			for (int j = 0; j < tmpDesc.width; j++)
//			{
//				descriptors.get(i, j) = tmpDesc.get(0, j);
//			}
//
//			i++;
//
//		}
//
//	}
	
	/**
	 * an example run
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		float sd = 5;
		float si = 1.4f * sd;
		HessianIPD ipd = new HessianIPD(sd, si);
		FImage img = ImageUtilities.readF(AffineAdaption.class.getResourceAsStream("/org/openimaj/image/data/sinaface.jpg"));
		
//		img = img.multiply(255f);
		
//		ipd.findInterestPoints(img);
//		List<InterestPointData> a = ipd.getInterestPoints(1F/(256*256));
//		
//		System.out.println("Found " + a.size() + " features");
//		
//		AffineAdaption adapt = new AffineAdaption();
//		EllipticKeyPoint kpt = new EllipticKeyPoint();
		MBFImage outImg = new MBFImage(img.clone(),img.clone(),img.clone());
//		for (InterestPointData d : a) {
//			
////			InterestPointData d = new InterestPointData();
////			d.x = 102;
////			d.y = 396;
//			logger.info("Keypoint at: " + d.x + ", " + d.y);
//			kpt.si = si;
//			kpt.centre = new Pixel(d.x, d.y);
//			kpt.size = 2 * 3 * kpt.si;
//			
//			boolean converge = adapt.calcAffineAdaptation(img, kpt);
//			if(converge)
//			{
//				outImg.drawShape(new Ellipse(kpt.centre.x,kpt.centre.y,kpt.axes.getX(),kpt.axes.getY(),kpt.phi), RGBColour.BLUE);
//				outImg.drawPoint(kpt.centre, RGBColour.RED,3);
//			}
//			
//			
//			
//			logger.info("... converged: "+ converge);
//		}
		AffineAdaption adapt = new AffineAdaption(ipd,new IPDSelectionMode.Count(100));
		adapt.findInterestPoints(img);
		InterestPointVisualiser<Float[],MBFImage> ipv = InterestPointVisualiser.visualiseInterestPoints(outImg, adapt.points);
		DisplayUtilities.display(ipv .drawPatches(RGBColour.BLUE, RGBColour.RED));
		
	}

	/**
	 * @param b whether the differencial scaling should be done iteratively (slow) or not (fast)
	 */
	public void setFastDifferentiationScale(boolean b) {
		this.fastDifferentiationScale = b;
	}
}

