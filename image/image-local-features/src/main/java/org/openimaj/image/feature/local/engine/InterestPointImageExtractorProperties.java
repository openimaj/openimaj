package org.openimaj.image.feature.local.engine;

import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.AffineIPD.MODE;

import Jama.Matrix;

public class InterestPointImageExtractorProperties extends ScaleSpaceImageExtractorProperties<FImage>{

	public FImage subImage;
	private int actualX;

	public InterestPointImageExtractorProperties(FImage image,InterestPointData point) {
		this.image = extractSubImage(image,point);
		this.scale = point.getScale();
		this.x = image.width/2;
		this.y = image.height/2;
	}

	private FImage extractSubImage(FImage image, InterestPointData point) {
		// The second<oments matrix represents the transform of a unit circle which 
		// form the ellipse which best describes the feature detected at this point. When we scale
		// this ellipse by point.getScale() we get the pixel area which needs describing.
		//
		// We want to extract a bounding box around an ellipse that is <elipseMultiple> times bigger than 
		// the ellipse defined by the equation because we expect interesting things to be happening just outside
		// the area of interest, especially if the interestpoint is the center of a blob which is itself an elipse.
		//
		// The second moments describe the transform FROM a circle TO an ellipse. If we want to transform 
		Matrix a = point.secondMoments;
		return subImage;
	}
	
}
