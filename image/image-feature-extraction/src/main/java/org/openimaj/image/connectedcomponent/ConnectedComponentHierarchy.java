package org.openimaj.image.connectedcomponent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.Pair;

public class ConnectedComponentHierarchy {
	public static void main(String[] args) throws IOException {
		String markerFile = "/Users/ss/Desktop/marker-profile.jpeg";
		FImage markerImage = ImageUtilities.readF(new File(markerFile));
		markerImage.threshold(0.5f);
		
		ConnectedComponentLabeler labler = new ConnectedComponentLabeler(ConnectMode.CONNECT_8);
		
		List<ConnectedComponent> components = labler.findComponents(markerImage);
		MBFImage output = new MBFImage(markerImage.clone(),markerImage.clone(),markerImage.clone());
		SegmentationUtilities.renderSegments(output, components);
		DisplayUtilities.displayName(output, "labeled");
		List<Pair<ConnectedComponent>> containsList = new ArrayList<Pair<ConnectedComponent>>();
		for(ConnectedComponent outer: components){
			Rectangle outerBox = outer.calculateRegularBoundingBox();
			for(ConnectedComponent inner : components){
				if(inner == outer) continue;
				
				Rectangle innerBox = inner.calculateRegularBoundingBox();
				if(innerBox.isInside(outerBox)){
					containsList.add(new Pair<ConnectedComponent>(outer, inner));
				}
			}
		}
		for (Pair<ConnectedComponent> pair : containsList) {
			System.out.println(pair);
		}
		
	}
}
