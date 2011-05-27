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
		double scaleFctor = 4 * point.scale;
		this.featureWindowSize = (int)scaleFctor;
		this.halfWindowSize = (int) scaleFctor/2;
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
		double rotation = Math.atan2(rdr.getV().get(0,0),rdr.getV().get(0,1));
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
