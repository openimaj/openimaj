package org.openimaj.image.connectedcomponent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.Pair;
import org.openimaj.util.tree.TreeNode;

public class ConnectedComponentHierarchy {
//	private static final int MINIMUM_AREA = 5;
//	private ConnectedComponentLabeler labler;
//	
//	public ConnectedComponentHierarchy(ConnectedComponentLabeler labeler, float threshold){
//		this.labler = labeler;
//	}
//	
//	public TreeNode<ConnectedComponent> hierarchy(FImage markerImage){
//		markerImage.threshold(0.5f);
//		List<ConnectedComponent> components = labler.findComponents(markerImage);
//		List<ConnectedComponent> invComponents = labler.findComponents(markerImage.inverse());
//		components.addAll(invComponents);
//		List<Pair<ConnectedComponent>> containsList = new ArrayList<Pair<ConnectedComponent>>();
//		long start = System.currentTimeMillis();
//		for(ConnectedComponent outer: components){
//			Rectangle outerBox = outer.calculateRegularBoundingBox();
//			if(outer.calculateArea() < MINIMUM_AREA) continue;
////			output.drawShape(outerBox, RGBColour.RED);
//			TreeNode<ConnectedComponent> outerNode = null;
//			for(ConnectedComponent inner : components){
//				if(inner == outer) continue;
//				if(inner.calculateArea() < MINIMUM_AREA) continue;
//				Rectangle innerBox = inner.calculateRegularBoundingBox();
//				if(innerBox.isInside(outerBox)){
//					TreeNode<ConnectedComponent> innerNode = null;
//				}
//			}
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("Total time: " + (end - start));
//		for (Pair<ConnectedComponent> pair : containsList) {
//			System.out.println(pair);
//		}
////		DisplayUtilities.displayName(output, "labeled");
//	}
//	
//	public static void main(String[] args) throws IOException {
//		String markerFile = "/Users/ss/Desktop/marker-profile.jpeg";
//		FImage markerImage = ImageUtilities.readF(new File(markerFile));
//		
//		ConnectedComponentLabeler.DEFAULT_ALGORITHM = ConnectedComponentLabeler.Algorithm.FLOOD_FILL;
//		ConnectedComponentLabeler labler = new ConnectedComponentLabeler(ConnectMode.CONNECT_8);
//	}
}
