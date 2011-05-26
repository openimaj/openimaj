package org.openimaj.image.feature.local.engine;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.MatrixUtils;
// import org.openimaj.image.feature.local.interest.AffineIPD.MODE;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class InterestPointImageExtractorProperties extends ScaleSpaceImageExtractorProperties<FImage>{

	public InterestPointImageExtractorProperties(FImage image,InterestPointData point) {
		this.image = extractSubImage(image,point);
		this.scale = point.getScale();
		this.x = image.width/2;
		this.y = image.height/2;
	}

	private FImage extractSubImage(FImage image, InterestPointData point) {
		Matrix secondMoments = point.secondMoments.copy();
		double divFactor = 1/Math.sqrt(secondMoments.det());
		double scaleFctor = 4 * point.scale;
		
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
		
		Matrix transformMatrix = usingEllipseTransformMatrix(point.x,point.y,d1,d2,rotation);
		int windowSize = (int) ((scaleFctor*d1/d2)/2);
		int featureWindowSize = (int) scaleFctor;
		
		return extractSubImage(image,transformMatrix,windowSize,featureWindowSize);
	}
	
	private FImage extractSubImage(FImage image, Matrix transformMatrix, int windowSize,int featureWindowSize) {
		ProjectionProcessor<Float,FImage> pp = new ProjectionProcessor<Float,FImage>();
		pp.setMatrix(transformMatrix);
		image.process(pp);
		FImage patch = pp.performProjection((int)-windowSize,(int)windowSize,(int)-windowSize,(int)windowSize,Float.NaN);
		if(patch.getWidth()>0&&patch.getHeight()>0)
		{
			return patch.extractCenter(featureWindowSize, featureWindowSize);
		}
		return null;
	}

	private Matrix usingEllipseTransformMatrix(int x, int y, double major, double minor, double rotation){
		Matrix rotate = TransformUtilities.rotationMatrix(rotation);
		Matrix scale = TransformUtilities.scaleMatrix(major, minor);
		Matrix translation = TransformUtilities.translateMatrix(x, y);
//		Matrix transformMatrix = scale.times(translation).times(rotation);
		Matrix transformMatrix = translation.times(rotate.times(scale));
		return transformMatrix.inverse();
	}
	
}
