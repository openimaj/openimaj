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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.BasicDerivativeKernels;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.ScaleSpacePoint;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 * An interest point detector which uses derivatives in some way
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class AbstractStructureTensorIPD implements InterestPointDetector {
	
	
	protected int borderSkip;
	FImage originalImage;
	FImage l, lx, ly, lxmx, lymy, lxmy;
	public FImage lxmxblur, lymyblur, lxmyblur;
	
	protected float detectionScaleVariance;
	protected float integrationScaleVariance;
	protected float detIntScaleFactor = 1.4f;
	

	protected List<Maxima> maxima;

	private boolean blurred;
	
	/**
	 * Set the scale factor between the integration scale and the detection scale. 
	 * When detection scale is set, integration scale = detIntScaleFactor * detectionScale
	 * @param detIntScaleFactor
	 */
	public AbstractStructureTensorIPD(float detIntScaleFactor) {
		this.detIntScaleFactor = detIntScaleFactor;
		this.borderSkip = 2;
	}
	
	/**
	 * Abstract structure tensor detected at a given scale, the first derivatives found and a structure tensor combined from
	 * these first derivatives with a gaussian window of sigma = integrationScaleVariance
	 * @param detectionScaleVariance
	 * @param integrationScaleVariance
	 */
	public AbstractStructureTensorIPD(float detectionScaleVariance, float integrationScaleVariance) {
		this(detectionScaleVariance, integrationScaleVariance, 2,false);
	}
	
	/**
	 * Abstract structure tensor detected at a given scale, the first derivatives found and a structure tensor combined from
	 * these first derivatives with a gaussian window of sigma = integrationScaleVariance. Also state whether the image
	 * in from which features are extracted is already blurred to the detection scale, if not it will be blurred to the correct
	 * level
	 * 
	 * @param detectionScaleVariance
	 * @param integrationScaleVariance
	 * @param blurred
	 */
	public AbstractStructureTensorIPD(float detectionScaleVariance, float integrationScaleVariance, boolean blurred) {
		this(detectionScaleVariance, integrationScaleVariance, 2,blurred);
	}
	
	/**
	 * Abstract structure tensor detected at a given scale, the first derivatives found and a structure tensor combined from
	 * these first derivatives with a gaussian window of sigma = integrationScaleVariance. Also specify how many pixels to skip 
	 * around the edge of the image. The kernel used to extract edges results in a black border so some pixels are better ignored 
	 * in terms of corner detection.
	 * 
	 * @param detectionScaleVariance
	 * @param integrationScaleVariance
	 * @param borderSkip
	 */
	public AbstractStructureTensorIPD(float detectionScaleVariance, float integrationScaleVariance, int borderSkip) {
		this(detectionScaleVariance,integrationScaleVariance,borderSkip,false);
	}
	
	/**
	 * Abstract structure tensor detected at a given scale, the first derivatives found and a structure tensor combined from
	 * these first derivatives with a gaussian window of sigma = integrationScaleVariance. Also specify how many pixels to skip 
	 * around the edge of the image. The kernel used to extract edges results in a black border so some pixels are better ignored 
	 * in terms of corner detection. Also state whether the image
	 * in from which features are extracted is already blurred to the detection scale, if not it will be blurred to the correct
	 * level
	 * 
	 * @param detectionScaleVariance
	 * @param integrationScaleVariance
	 * @param borderSkip
	 * @param blurred
	 */
	public AbstractStructureTensorIPD(float detectionScaleVariance, float integrationScaleVariance, int borderSkip, boolean blurred) {
		this.blurred = blurred;
		if (borderSkip < 1) borderSkip = 1;
		
		this.detectionScaleVariance = detectionScaleVariance;
		this.integrationScaleVariance = integrationScaleVariance;
		this.borderSkip = borderSkip;
	}
	
	public void prepareInterestPoints(FImage image) {
		originalImage = image;
//		// Add padding around the edges of the image (4 pixels all the way around)
//		image = image.padding(4,4);
//		l = image.clone().processInline(new FDiscGausConvolve(detectionScaleVariance));
//		lx = l.process(BasicDerivativeKernels.DX_KERNEL).extractROI(4,4,this.originalImage.getWidth(), this.originalImage.getHeight()).multiplyInline((float)Math.sqrt(detectionScaleVariance));
//		ly = l.process(BasicDerivativeKernels.DY_KERNEL).extractROI(4,4,this.originalImage.getWidth(), this.originalImage.getHeight()).multiplyInline((float)Math.sqrt(detectionScaleVariance));
		
		l = image.clone();
		if(!this.blurred) l = l.processInline(new FGaussianConvolve(detectionScaleVariance)).multiply(detectionScaleVariance * detectionScaleVariance);
		lx = l.process(BasicDerivativeKernels.DX_KERNEL);
		ly = l.process(BasicDerivativeKernels.DY_KERNEL);
		
		
		lxmx = lx.multiply(lx);
		lymy = ly.multiply(ly);
		lxmy = lx.multiply(ly);
		
		lxmxblur = lxmx.clone().processInline(new FGaussianConvolve(integrationScaleVariance)).multiply(integrationScaleVariance * integrationScaleVariance );
		lymyblur = lymy.clone().processInline(new FGaussianConvolve(integrationScaleVariance)).multiply(integrationScaleVariance * integrationScaleVariance );
		lxmyblur = lxmy.clone().processInline(new FGaussianConvolve(integrationScaleVariance)).multiply(integrationScaleVariance * integrationScaleVariance );
	}
	
	@Override
	public void findInterestPoints(FImage image) {
		
		this.prepareInterestPoints(image);
		FImage cornerImage = createInterestPointMap();
		
		detectMaxima(cornerImage,image.getBounds());
	}
	
	@Override
	public void findInterestPoints(FImage image, Rectangle window) {
		
		this.prepareInterestPoints(image);
		FImage cornerImage = createInterestPointMap();
		
		detectMaxima(cornerImage,window);
	}
	
	public FValuePixel findMaximum(Rectangle window){
		FImage cornerImage = createInterestPointMap();
		FValuePixel c = cornerImage.extractROI(window).maxPixel();
		c.translate(window.x,window.y);
		return c;
	}
	
	public class Maxima {
		public int x, y;
		public float val;
		
		public Maxima(int x, int y, float v) {
			this.x = x;
			this.y = y;
			this.val = v;
		}
	}
	protected void detectMaxima(FImage image, Rectangle window) {
		maxima = new ArrayList<Maxima>();
		
		for (int y=borderSkip; y<image.height-borderSkip; y++) {
			for (int x=borderSkip; x<image.width-borderSkip; x++) {
				if(!window.isInside(new Pixel(x,y))) continue;
				float curr = image.pixels[y][x];
				if ( curr > image.pixels[y-1][x-1] &&
						curr > image.pixels[y-1][x] &&
						curr > image.pixels[y-1][x+1] &&
						curr > image.pixels[y][x-1] &&
						curr > image.pixels[y][x+1] &&
						curr > image.pixels[y+1][x-1] &&
						curr > image.pixels[y+1][x] &&
						curr > image.pixels[y+1][x+1] ) {
					maxima.add(new Maxima(x,y,curr));
				}
			}
		}
		
		Collections.sort(maxima, new Comparator<Maxima>() {
			@Override
			public int compare(Maxima o1, Maxima o2) {
				if (o1.val == o2.val) return 0;
				return o1.val<o2.val ? 1 : -1;
			}});
	}
	
	public abstract FImage createInterestPointMap();
	
	public static class InterestPointData implements ScaleSpacePoint, Cloneable {
		public int x, y;
		public float scale;
		public float score;
		public Matrix secondMoments = new Matrix(2,2);
		
		
		public InterestPointData(){
			
		}
//		public InterestPointData(Keypoint k) {
//			this.x = (int) k.col;
//			this.y = (int) k.row;
//			this.secondMoments = new Matrix(2,2);
//			this.secondMoments.set(0, 0, 1);
//			this.secondMoments.set(0, 1, 0);
//			this.secondMoments.set(1, 0, 0);
//			this.secondMoments.set(1, 1, 1);
//			this.scale = Math.pow(k.scale,2)/2.0;
//		}

		public static Matrix getCovarianceMatrix(Matrix secondMoments) {
			Matrix covariance = secondMoments.copy().inverse();
			covariance = covariance.times(1.0/Math.sqrt(covariance.det()));
			// covar = sec' / det(sec')
			return covariance;
		}
		
		public Matrix getCovarianceMatrix() {
			return InterestPointData.getCovarianceMatrix(secondMoments);
		}
		
		@Override
		public InterestPointData clone() {
			InterestPointData copy = new InterestPointData();
			copy.x = x;
			copy.y = y;
			copy.scale = scale;
			copy.score = score;
			copy.secondMoments = this.secondMoments.copy();
			return copy;
		}
		
		boolean equalPos(InterestPointData otherPos){
			return otherPos.x == this.x && otherPos.y == this.y && otherPos.scale == this.scale;
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof InterestPointData))
				return false;
			InterestPointData otherPos = (InterestPointData) other;
			return otherPos.x == this.x && otherPos.y == this.y
					&& otherPos.scale == this.scale
					&& this.secondMoments.equals(otherPos.secondMoments);
		}

		@Override
		public float getX() {
			return x;
		}

		@Override
		public void setX(float x) {
			this.x = (int)x;
		}

		@Override
		public float getY() {
			return y;
		}

		@Override
		public void setY(float y) {
			this.y = (int)y;
		}

		@Override
		public void copyFrom(Point2d p) {
			this.x = (int)p.getX();
			this.y = (int)p.getY();
		}

		@Override
		public void translate(float x, float y) {
			this.x += x;
			this.y += y;
		}

		@Override
		public Float getOrdinate(int dimension) {
			float [] pos = {x, y, (float) scale};
			return pos[dimension];
		}
		
		@Override
		public int getDimensions() {
			return 3;
		}
		
		@Override
		public float getScale() {
			return (float) scale;
		}

		@Override
		public void setScale(float scale) {
			this.scale = scale;
		}

		@Override
		public Point2dImpl transform(Matrix transform) {
			float xt = (float)transform.get(0, 0) * getX() + (float)transform.get(0, 1) * getY() + (float)transform.get(0, 2);
			float yt = (float)transform.get(1, 0) * getX() + (float)transform.get(1, 1) * getY() + (float)transform.get(1, 2);
			float zt = (float)transform.get(2, 0) * getX() + (float)transform.get(2, 1) * getY() + (float)transform.get(2, 2);
			
			xt /= zt;
			yt /= zt;
			
			return new Point2dImpl(xt,yt);
		}
		@Override
		public Point2d minus(Point2d a) {
			InterestPointData ipd = this.clone();
			ipd.x = this.x - (int)a.getX();
			ipd.y = this.y - (int)a.getY();
			return ipd;
		}

		public Ellipse getEllipse() {
			return EllipseUtilities.ellipseFromSecondMoments(x, y, secondMoments,this.scale);
		}

		@Override
		public void readASCII(Scanner in) throws IOException {
			x = in.nextInt();
			y = in.nextInt();
			scale = in.nextFloat();
			score = in.nextFloat();
			secondMoments.set(0, 0, in.nextDouble());
			secondMoments.set(0, 1, in.nextDouble());
			secondMoments.set(1, 0, in.nextDouble());
			secondMoments.set(1, 1, in.nextDouble());
		}

		@Override
		public String asciiHeader() {
			return this.getClass().getName();
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			x = in.readInt();
			y = in.readInt();
			scale = in.readFloat();
			score = in.readFloat();
			secondMoments.set(0, 0, in.readDouble());
			secondMoments.set(0, 1, in.readDouble());
			secondMoments.set(1, 0, in.readDouble());
			secondMoments.set(1, 1, in.readDouble());
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeASCII(PrintWriter out) throws IOException {
			out.format("%d %d %f %f %f %f %f %f", x, y, scale, score, secondMoments.get(0, 0), secondMoments.get(0, 1), secondMoments.get(1, 0), secondMoments.get(1, 1));
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeInt(x);
			out.writeInt(y);
			out.writeFloat(scale);
			out.writeFloat(score);
			out.writeDouble(secondMoments.get(0, 0));
			out.writeDouble(secondMoments.get(0, 1));
			out.writeDouble(secondMoments.get(1, 0));
			out.writeDouble(secondMoments.get(1, 1));
		}

		@Override
		public void translate(Point2d v) {
			// TODO Auto-generated method stub
			
		}
	}
	
	@Override
	public List<InterestPointData> getInterestPoints(int npoints) {
		if (npoints<0 || npoints>maxima.size()) npoints = maxima.size();
		List<InterestPointData> ipdata = new ArrayList<InterestPointData>();
		
		for (int i=0; i<npoints; i++) {
			InterestPointData ipd = new InterestPointData();
			
			ipd.x = maxima.get(i).x;
			ipd.y = maxima.get(i).y;
			ipd.scale = detectionScaleVariance;
			ipd.score = maxima.get(i).val;
			ipd.secondMoments.set(0, 0, lxmxblur.pixels[ipd.y][ipd.x]);
			ipd.secondMoments.set(0, 1, lxmyblur.pixels[ipd.y][ipd.x]);
			ipd.secondMoments.set(1, 0, lxmyblur.pixels[ipd.y][ipd.x]);
			ipd.secondMoments.set(1, 1, lymyblur.pixels[ipd.y][ipd.x]);
			
			ipdata.add(ipd);
		}
		
		return ipdata;
	}
	
	
	public float getDetIntScaleFactor() {
		return detIntScaleFactor;
	}

	public void setDetIntScaleFactor(float detIntScaleFactor) {
		this.detIntScaleFactor = detIntScaleFactor;
	}
	
	public float getDetectionScaleVariance() {
		return detectionScaleVariance;
	}
	
	public void setImageBlurred(boolean blurred) {
		this.blurred = blurred;
	}
	
	@Override
	public void setDetectionScaleVariance(float detectionScaleVariance) {
		this.detectionScaleVariance = detectionScaleVariance;
		this.integrationScaleVariance = this.detectionScaleVariance * this.detIntScaleFactor;
	}

	public float getIntegrationScaleVariance() {
		return integrationScaleVariance;
	}

	public void setIntegrationScaleVariance(float integrationScaleVariance) {
		this.integrationScaleVariance = integrationScaleVariance;
	}

	@Override
	public List<InterestPointData> getInterestPoints() {
		return getInterestPoints(-1);
	}
	
	@Override
	public List<InterestPointData> getInterestPoints(float threshold) {
		return getInterestPointsThresh(threshold);
	}
	
	public List<InterestPointData> getInterestPointsThresh(float thresh) {
		List<InterestPointData> ipdata = new ArrayList<InterestPointData>();
		
		for (Maxima m : maxima) {
			if (m.val < thresh)
				continue;
			
			InterestPointData ipd = new InterestPointData();
			
			ipd.x = m.x;
			ipd.y = m.y;
			ipd.scale = detectionScaleVariance;
			ipd.score = m.val;
			ipd.secondMoments.set(0, 0, lxmxblur.pixels[ipd.y][ipd.x]);
			ipd.secondMoments.set(0, 1, lxmyblur.pixels[ipd.y][ipd.x]);
			ipd.secondMoments.set(1, 0, lxmyblur.pixels[ipd.y][ipd.x]);
			ipd.secondMoments.set(1, 1, lymyblur.pixels[ipd.y][ipd.x]);
			
			ipdata.add(ipd);
		}
		
		return ipdata;
	}
	
	
	public Matrix getSecondMomentsAt(int x, int y) {
		Matrix secondMoments = new Matrix(2,2);
		secondMoments.set(0, 0, lxmxblur.pixels[y][x]);
		secondMoments.set(0, 1, lxmyblur.pixels[y][x]);
		secondMoments.set(1, 0, lxmyblur.pixels[y][x]);
		secondMoments.set(1, 1, lymyblur.pixels[y][x]);
		return secondMoments;
	}
	
	@Override
	public AbstractStructureTensorIPD clone(){
		AbstractStructureTensorIPD a = null;
		try {
			a = (AbstractStructureTensorIPD) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		return a;
	}

	public int pointsFound() {
		return this.maxima.size();
	}
}
