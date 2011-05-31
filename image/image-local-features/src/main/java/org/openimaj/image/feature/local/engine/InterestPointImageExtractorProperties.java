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
package org.openimaj.image.feature.local.engine;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class InterestPointImageExtractorProperties<
		P,
		I extends Image<P,I> & SinglebandImageProcessor.Processable<Float,FImage,I>
	> extends ScaleSpaceImageExtractorProperties<I>{

	private boolean affineInvariant;
	public int halfWindowSize;
	public int featureWindowSize;
	public InterestPointData interestPointData;

	public InterestPointImageExtractorProperties(I image,InterestPointData point) {
		this(image, point,true);
	}
	public InterestPointImageExtractorProperties(I image,InterestPointData point,boolean affineInvariant) {
		this.image = extractSubImage(image,point);
		this.scale = point.getScale();
		this.x = this.image.getWidth()/2;
		this.y = this.image.getHeight()/2;
		this.affineInvariant = affineInvariant;
		this.interestPointData = point;
	}

	
	private I extractSubImage(I image, InterestPointData point) {
		// First extract the window around the point at the window size
		double scaleFctor = 5 * point.scale;
		this.featureWindowSize = (int)scaleFctor;
		this.halfWindowSize = (int) Math.ceil(scaleFctor/2.0);
		I subImage = null;
		Matrix transformMatrix = null;
		if(this.affineInvariant){
			transformMatrix = calculateTransformMatrix(point);
		}
		else{
			transformMatrix = TransformUtilities.translateMatrix(-point.x, -point.y);
		}
		
		subImage = extractSubImage(image,transformMatrix,halfWindowSize,featureWindowSize);
		return  subImage;
	}
	
	private I extractSubImage(I image, Matrix transformMatrix, int windowSize,int featureWindowSize) {
		ProjectionProcessor<P,I> pp = new ProjectionProcessor<P,I>();
		pp.setMatrix(transformMatrix);
		image.process(pp);
		I patch = pp.performProjection((int)-windowSize,(int)windowSize,(int)-windowSize,(int)windowSize);
		if(patch.getWidth()>0&&patch.getHeight()>0)
		{
			I patchToReturn = patch.extractCenter(featureWindowSize, featureWindowSize);
			return patchToReturn;
		}
		return null;
	}

	private Matrix usingEllipseTransformMatrix(int x, int y, double major, double minor, double rotation){
		Matrix rotate = TransformUtilities.rotationMatrix(rotation);
		Matrix scale = TransformUtilities.scaleMatrix(major, minor);
		Matrix translation = TransformUtilities.translateMatrix(x, y);
		Matrix transformMatrix = translation.times(rotate.times(scale));
		return transformMatrix.inverse();
	}
	
	private Matrix calculateTransformMatrix(InterestPointData point){
		Matrix secondMoments = point.secondMoments.copy();
		double divFactor = 1/Math.sqrt(secondMoments.det());
		
		
		EigenvalueDecomposition rdr = secondMoments.times(divFactor).eig();
		double rotation = Math.atan2(rdr.getV().get(1,0),rdr.getV().get(1,1));
		double d1,d2;
		if(rdr.getD().get(0,0) == 0)
			d1 = 0;
		else
			d1 = 1.0/Math.sqrt(rdr.getD().get(0,0));
		if(rdr.getD().get(1,1) == 0)
			d2 = 0;
		else
			d2 = 1.0/Math.sqrt(rdr.getD().get(1,1));
		
		this.halfWindowSize = (int) (this.halfWindowSize*d1/d2);
		Matrix transformMatrix = usingEllipseTransformMatrix(point.x,point.y,d1,d2,rotation);
		
		return transformMatrix;
	}
	
}
