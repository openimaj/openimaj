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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.processing.convolution.BasicDerivativeKernels;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.ScaleSpacePoint;
import org.openimaj.math.geometry.shape.EllipseUtilities;

import Jama.Matrix;

/**
 * An interest point detector which uses derivatives in some way
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public abstract class AbstractIPD implements InterestPointDetector {
	protected int borderSkip;
	
	protected FImage originalImage;
	public FImage l, lx, ly, lxmx, lymy, lxmy;
	public FImage lxmxblur, lymyblur, lxmyblur;
	
	protected float detectionScaleVariance;
	protected float integrationScaleVariance;
	protected List<Maxima> maxima;

	public AbstractIPD(float detectionScaleVariance, float integrationScaleVariance) {
		this(detectionScaleVariance, integrationScaleVariance, 2);
	}
	
	public AbstractIPD(float detectionScaleVariance, float integrationScaleVariance, int borderSkip) {
		if (borderSkip < 1) borderSkip = 1;
		
		this.detectionScaleVariance = detectionScaleVariance;
		this.integrationScaleVariance = integrationScaleVariance;
		this.borderSkip = borderSkip;
	}
	
	@Override
	public void findInterestPoints(FImage image) {
		this.originalImage = image;
		
		l = image.clone().processInline(new FGaussianConvolve((float) Math.sqrt(detectionScaleVariance)));
		lx = l.process(BasicDerivativeKernels.DX_KERNEL).multiplyInline((float)Math.sqrt(detectionScaleVariance));
		ly = l.process(BasicDerivativeKernels.DY_KERNEL).multiplyInline((float)Math.sqrt(detectionScaleVariance));
		
		lxmx = lx.multiply(lx);
		lymy = ly.multiply(ly);
		lxmy = lx.multiply(ly);
		
		lxmxblur = lxmx.clone().processInline(new FGaussianConvolve((float) Math.sqrt(integrationScaleVariance)));
		lymyblur = lymy.clone().processInline(new FGaussianConvolve((float) Math.sqrt(integrationScaleVariance)));
		lxmyblur = lxmy.clone().processInline(new FGaussianConvolve((float) Math.sqrt(integrationScaleVariance)));
		
		FImage cornerImage = createInterestPointMap();
		
		detectMaxima(cornerImage);
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
	protected void detectMaxima(FImage image) {
		maxima = new ArrayList<Maxima>();
		
		for (int y=borderSkip; y<image.height-borderSkip; y++) {
			for (int x=borderSkip; x<image.width-borderSkip; x++) {
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
		public double scale;
		public double score;
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
	
	public float getDetectionScaleVariance() {
		return detectionScaleVariance;
	}

	public void setDetectionScaleVariance(float detectionScaleVariance) {
		this.detectionScaleVariance = detectionScaleVariance;
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
	
	public static MBFImage visualise(List<InterestPointData> data, FImage l) {
		return visualise(data, new MBFImage(l,l,l),0,null);
	}
	
	public static MBFImage visualise(List<InterestPointData> data, FImage l, int scale) {
		return visualise(data, new MBFImage(l,l,l),scale,null);
	}
	
	public static MBFImage visualise(List<InterestPointData> data, MBFImage rgbimage, int scale,Float[] colour) {
		if (colour == null) colour = new Float[] {1f, 0f, 0f};
//		for(int i = 0; i < scale; i++){
//			rgbimage = rgbimage.doubleSize();
//		}
		
		for (InterestPointData ipd : data) {
			rgbimage.drawPoint(new Point2dImpl((int)(ipd.x), (int)(ipd.y)), colour, 2);
			rgbimage.drawShape(EllipseUtilities.ellipseFromSecondMoments(ipd.x, ipd.y, ipd.secondMoments, ipd.getScale()),1,colour);
		}
		
		return rgbimage;
	}
	
	public MBFImage visualise(int limit) {
		return AbstractIPD.visualise(this.getInterestPoints(limit), this.l);
	}
	
	public MBFImage visualise(int limit, int scale) {
		return AbstractIPD.visualise(this.getInterestPoints(limit), new MBFImage(this.l,this.l,this.l),scale,new Float[] {1f, 0f, 0f});
	}
	
	public MBFImage visualiseThresh(float thresh) {
		return AbstractIPD.visualise(this.getInterestPointsThresh(thresh), this.l);
	}
	
	public MBFImage visualiseThresh(float thresh, int scale) {
		return AbstractIPD.visualise(this.getInterestPointsThresh(thresh), this.l,scale);
	}
	
	public Matrix getSecondMomentsAt(int x, int y) {
		Matrix secondMoments = new Matrix(2,2);
		secondMoments.set(0, 0, lxmxblur.pixels[y][x]);
		secondMoments.set(0, 1, lxmyblur.pixels[y][x]);
		secondMoments.set(1, 0, lxmyblur.pixels[y][x]);
		secondMoments.set(1, 1, lymyblur.pixels[y][x]);
		return secondMoments;
	}
}
