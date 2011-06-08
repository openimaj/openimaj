package org.openimaj.demos;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.detector.mser.MSERFeatureGenerator;
import org.openimaj.image.processing.watershed.Component;
import org.openimaj.image.processing.watershed.feature.MomentFeature;
import org.openimaj.math.geometry.shape.Ellipse;

public class MSEREllipseFinder {
	public MSEREllipseFinder(){
		MBFImage image = new MBFImage(400,400,ColourSpace.RGB);
		image.fill(RGBColour.WHITE);
		List<Ellipse> ellipses = new ArrayList<Ellipse>();
		ellipses.add(new Ellipse(200,100,100,80,Math.PI/4));
		ellipses.add(new Ellipse(200,300,50,30,-Math.PI/4));
		ellipses.add(new Ellipse(100,300,30,50,-Math.PI/3));
		
		for(Ellipse ellipse : ellipses){
			image.drawShapeFilled(ellipse, RGBColour.BLACK);
		}
		
		@SuppressWarnings("unchecked")
		MSERFeatureGenerator mser = new MSERFeatureGenerator(MomentFeature.class);
		List<Component> features = mser.generateMSERs(Transforms.calculateIntensityNTSC(image));
		for(Component c : features){
			MomentFeature feature = c.getFeature(MomentFeature.class);
			image.drawShape(feature.getEllipse(),RGBColour.RED);
			image.drawShape(feature.getEllipse().calculateOrientedBoundingBox(),RGBColour.GREEN);
		}
		DisplayUtilities.display(image);
	}
	public static void main(String args[]){
		new MSEREllipseFinder();
	}
}
